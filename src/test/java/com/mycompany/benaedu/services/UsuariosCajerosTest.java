package com.mycompany.benaedu.services;

import com.mycompany.benaedu.SesionUsuario;
import com.mycompany.benaedu.db.*;
import java.sql.*;
import java.util.Set;
import java.util.UUID;

/** Prueba altas y autorización de cobros con las definiciones del respaldo y datos ficticios. */
public final class UsuariosCajerosTest {
    private UsuariosCajerosTest() { }

    /** Ejecuta el flujo real JDBC en un esquema desechable.
     * @param args argumentos no utilizados
     * @throws Exception si falla la prueba
     */
    public static void main(String[] args) throws Exception {
        String url = System.getProperty("benaedu.auditoria.test.url");
        String esquema = "codex_cajeros_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection con = url == null ? ConDB.getConnection() : DriverManager.getConnection(url, "root", "");
                var schema = con.createStatement()) {
            String original = con.getCatalog();
            schema.executeUpdate("CREATE DATABASE `" + esquema + "` CHARACTER SET utf8mb4");
            con.setCatalog(esquema);
            try (var st = con.createStatement()) {
                try (var recurso = UsuariosCajerosTest.class.getResourceAsStream("/cajeros-esquema.sql")) {
                    String ddl = new String(java.util.Objects.requireNonNull(recurso).readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                    for (String sentencia : ddl.split(";")) if (!sentencia.isBlank()) st.executeUpdate(sentencia);
                }
                st.executeUpdate("INSERT INTO users (id_user,nombre_usuario,contrasena,rol,correo) VALUES (9,'admin','hash','Administrador','admin@example.com')");
                st.executeUpdate("INSERT INTO tmcias (CIA,NCIA) VALUES ('12','Compañía de prueba'),('13','Otra compañía')");
                SesionUsuario.iniciar("admin", "Administrador");
                PermisosUsuario.instalar(con); Auditoria.prepararSesion(con); PermisosUsuario.actualizarRolCajero(con);
                var permisoCobro = Set.of(PermisosUsuario.identificador("Cobranza Escolar"));
                var alta = crear(con, "cajero1", permisoCobro);
                exigir(alta.idUsuario() == 10 && alta.numeroCajero() != null, "No se asignaron identificadores");
                try (var rs = st.executeQuery("SELECT u.id_user,u.contrasena,c.NEMP,c.USER,c.ECAJ,e.NOME,e.CAJ "
                        + "FROM users u JOIN tescaj c ON c.USER=u.nombre_usuario JOIN tgemp e ON e.NEMP=c.NEMP AND e.CIA=c.CIA "
                        + "WHERE u.nombre_usuario='cajero1'")) {
                    exigir(rs.next() && rs.getInt("id_user") == 10 && rs.getInt("NEMP") == alta.numeroCajero()
                            && "A".equals(rs.getString("ECAJ")) && "S".equals(rs.getString("CAJ"))
                            && "Nombre Apellidos".equals(rs.getString("NOME")), "No se creó el perfil completo");
                    exigir(org.mindrot.jbcrypt.BCrypt.checkpw("Clave-Prueba-123", rs.getString("contrasena")), "La clave no está protegida correctamente");
                }
                denegado(() -> crear(con, "cajero1", permisoCobro));
                exigir(contar(con, "users") == 2 && contar(con, "tgemp") == 1 && contar(con, "tescaj") == 1, "El alta repetida duplicó registros");
                UsuariosCajeros.crear(con, "otroAdmin", "Nombre", "Apellidos", "Clave-Prueba-123", "otroAdmin@example.com", "Administrador", null, Set.of());
                exigir(contar(con, "tescaj") == 1 && contar(con, "tgemp") == 1, "Se creó un cajero para un administrador");
                var soloBecados = crear(con, "becados", Set.of(PermisosUsuario.identificador("Imprime Alumnos Becados")));
                exigir(soloBecados.numeroCajero() != null, "El cajero de reportes necesita su perfil");
                SesionUsuario.iniciar("becados", "Cajero");
                denegado(() -> UsuariosCajeros.exigirParaCobrar(con, "12", "Cobranza Escolar"));
                SesionUsuario.iniciar("cajero1", "Cajero");
                exigir(UsuariosCajeros.exigirParaCobrar(con, "12", "Cobranza Escolar") == alta.numeroCajero(), "No se autorizó al cajero correcto");
                denegado(() -> UsuariosCajeros.exigirParaCobrar(con, "13", "Cobranza Escolar"));
                st.executeUpdate("UPDATE tescaj SET ECAJ='I' WHERE USER='cajero1'");
                denegado(() -> UsuariosCajeros.exigirParaCobrar(con, "12", "Cobranza Escolar"));
                st.executeUpdate("UPDATE tescaj SET ECAJ='A' WHERE USER='cajero1'");
                SesionUsuario.iniciar("admin", "Administrador");
                st.executeUpdate("INSERT INTO tescaj (CIA,NEMP,ECAJ,USER) VALUES (12,999,'A','admin')");
                denegado(() -> UsuariosCajeros.exigirParaCobrar(con, "12", "Cobranza Escolar"));
                PermisosUsuario.guardar(con, "cajero1", "cajeroRenombrado", "cajero1@example.com", "Cajero", permisoCobro);
                SesionUsuario.iniciar("cajeroRenombrado", "Cajero");
                exigir(UsuariosCajeros.exigirParaCobrar(con, "12", "Cobranza Escolar") == alta.numeroCajero(), "El alias nuevo perdió la vinculación al cajero");
                SesionUsuario.iniciar("admin", "Administrador");
                st.executeUpdate("CREATE TRIGGER rechazar_alta BEFORE INSERT ON users FOR EACH ROW BEGIN "
                        + "IF NEW.nombre_usuario='falla' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Fallo de prueba'; END IF; END");
                long empleadosAntes = contar(con, "tgemp"), cajerosAntes = contar(con, "tescaj"), usuariosAntes = contar(con, "users");
                denegado(() -> crear(con, "falla", permisoCobro));
                exigir(contar(con, "users") == usuariosAntes && contar(con, "tgemp") == empleadosAntes
                        && contar(con, "tescaj") == cajerosAntes, "El fallo dejó cuentas o perfiles huérfanos en MyISAM");
                if (url != null) {
                    var ejecutor = java.util.concurrent.Executors.newFixedThreadPool(2);
                    try {
                        java.util.List<java.util.concurrent.Future<UsuariosCajeros.Alta>> resultados = new java.util.ArrayList<>();
                        for (String alias : new String[]{"concurrente1", "concurrente2"}) {
                            resultados.add(ejecutor.submit(() -> {
                                try (var otra = DriverManager.getConnection(url, "root", "")) {
                                    otra.setCatalog(esquema); Auditoria.asignarUsuario(otra);
                                    return crear(otra, alias, permisoCobro);
                                }
                            }));
                        }
                        var primera = resultados.get(0).get(30, java.util.concurrent.TimeUnit.SECONDS);
                        var segunda = resultados.get(1).get(30, java.util.concurrent.TimeUnit.SECONDS);
                        exigir(primera.idUsuario() != segunda.idUsuario() && !primera.numeroCajero().equals(segunda.numeroCajero()),
                                "Las altas simultáneas reutilizaron un identificador");
                    } finally { ejecutor.shutdownNow(); }
                }
            } finally {
                con.setAutoCommit(true); con.setCatalog(original);
                schema.executeUpdate("DROP DATABASE `" + esquema + "`"); SesionUsuario.cerrar();
            }
        }
        System.out.println("OK: alta completa sin AUTO_INCREMENT, perfiles y permisos, rol/compañía/estatus, cambio de alias, limpieza MyISAM y concurrencia.");
    }

    private static UsuariosCajeros.Alta crear(Connection con, String alias, Set<String> permisos) throws SQLException {
        return UsuariosCajeros.crear(con, alias, "Nombre", "Apellidos", "Clave-Prueba-123", alias + "@example.com", "Cajero", "12", permisos);
    }

    private static long contar(Connection con, String tabla) throws SQLException {
        try (var st = con.createStatement(); var rs = st.executeQuery("SELECT COUNT(*) FROM " + tabla)) { rs.next(); return rs.getLong(1); }
    }

    private interface Operacion { void ejecutar() throws SQLException; }
    private static void denegado(Operacion operacion) throws SQLException {
        try { operacion.ejecutar(); throw new AssertionError("La operación debía rechazarse"); } catch (SQLException esperada) { }
    }
    private static void exigir(boolean condicion, String mensaje) { if (!condicion) throw new AssertionError(mensaje); }
}
