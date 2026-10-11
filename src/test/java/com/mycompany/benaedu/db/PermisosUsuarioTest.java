package com.mycompany.benaedu.db;

import com.mycompany.benaedu.SesionUsuario;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/** Prueba los permisos guardados y su auditoría en un esquema desechable. */
public final class PermisosUsuarioTest {
    private PermisosUsuarioTest() { }

    /** Ejecuta las comprobaciones sin modificar usuarios reales.
     * @param args argumentos no utilizados
     * @throws Exception si falla una comprobación
     */
    public static void main(String[] args) throws Exception {
        String url = System.getProperty("benaedu.auditoria.test.url");
        String esquema = "codex_permisos_" + UUID.randomUUID().toString().replace("-", "");
        try (Connection con = url == null ? ConDB.getConnection() : DriverManager.getConnection(url, "root", "");
                var esquemaStmt = con.createStatement()) {
            String original = con.getCatalog();
            esquemaStmt.executeUpdate("CREATE DATABASE `" + esquema + "` CHARACTER SET utf8mb4");
            con.setCatalog(esquema);
            try (var st = con.createStatement()) {
                st.executeUpdate("CREATE TABLE users (nombre_usuario VARCHAR(40) PRIMARY KEY, correo VARCHAR(100), "
                        + "rol ENUM('Administrador','Empleado') NOT NULL DEFAULT 'Empleado', contrasena VARCHAR(100)) ENGINE=MyISAM");
                st.executeUpdate("INSERT INTO users VALUES ('admin','admin@example.com','Administrador','hash'), "
                        + "('cajero1','cajero@example.com','Empleado','hash')");
                Auditoria.instalar(con); // Simula una instalación del historial anterior a los permisos.
                exigir(PermisosUsuario.instalar(con), "No se agregó la columna de permisos");
                Auditoria.refrescarTabla(con, "users");
                SesionUsuario.iniciar("admin", "Administrador");
                Auditoria.asignarUsuario(con);
                PermisosUsuario.actualizarRolCajero(con);
                PermisosUsuario.actualizarRolCajero(con);
                try (var rs = st.executeQuery("SELECT rol FROM users WHERE nombre_usuario='cajero1'")) {
                    exigir(rs.next() && "Cajero".equals(rs.getString(1)), "No se migró la cuenta anterior de empleado");
                }
                exigir("Cajero".equals(SesionUsuario.normalizarRol("Empleado")), "No se reconoció el rol anterior");
                exigir(!PermisosUsuario.instalar(con), "La instalación no es idempotente");
                SesionUsuario.iniciar("cajero1", "Cajero");
                exigir(PermisosUsuario.cargarSesion(con).size() == PermisosUsuario.APARTADOS.size() - 1,
                        "Se alteraron los accesos anteriores de un usuario sin configurar");
                String becados = PermisosUsuario.identificador("Imprime Alumnos Becados");
                SesionUsuario.iniciar("admin", "Administrador");
                Auditoria.asignarUsuario(con);
                PermisosUsuario.guardar(con, "cajero1", "cajero1", "cajero@example.com", "Cajero", Set.of(becados));
                exigir(PermisosUsuario.cargarParaEditar(con, "cajero1").equals(Set.of(becados)), "La casilla no se guardó");
                var auditoria = Auditoria.consultar(con, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), "users", 0);
                exigir(auditoria.size() == 2 && auditoria.getFirst().campos().contains("benaedu_apartados")
                        && auditoria.getFirst().despues().contains(becados), "Falta el cambio de permisos en el historial");
                SesionUsuario.cerrar();
                SesionUsuario.iniciar("cajero1", "Cajero");
                try (Connection otra = url == null ? ConDB.getConnection() : DriverManager.getConnection(url, "root", "")) {
                    otra.setCatalog(esquema);
                    exigir(PermisosUsuario.cargarSesion(otra).equals(Set.of(becados)), "Se perdieron permisos en otra conexión");
                    PermisosUsuario.exigirAcceso(otra, "Imprime Alumnos Becados");
                    denegado(() -> PermisosUsuario.exigirAcceso(otra, "Cobranza Escolar"));
                    denegado(() -> PermisosUsuario.exigirAcceso(otra, "Historial de cambios"));
                    denegado(() -> PermisosUsuario.guardar(otra, "cajero1", "cajero1", "cajero@example.com", "Cajero", Set.of()));
                    SesionUsuario.iniciar("admin", "Administrador");
                    PermisosUsuario.guardar(con, "cajero1", "cajero1", "cajero@example.com", "Cajero", Set.of());
                    SesionUsuario.iniciar("cajero1", "Cajero");
                    exigir(PermisosUsuario.cargarSesion(otra).isEmpty(), "Cero casillas se confundió con accesos heredados");
                    denegado(() -> PermisosUsuario.exigirAcceso(otra, "Imprime Alumnos Becados"));
                }
                SesionUsuario.iniciar("admin", "Administrador");
                PermisosUsuario.guardar(con, "cajero1", "cajero2", "nuevo@example.com", "Cajero", Set.of(becados));
                exigir(PermisosUsuario.cargarParaEditar(con, "cajero2").equals(Set.of(becados)), "El cambio de alias perdió permisos");
                try {
                    PermisosUsuario.guardar(con, "cajero2", "admin", "nuevo@example.com", "Cajero", Set.of());
                    throw new AssertionError("Se aceptó un alias duplicado");
                } catch (SQLException esperada) { }
                exigir(PermisosUsuario.cargarParaEditar(con, "cajero2").equals(Set.of(becados)), "Un guardado rechazado cambió los permisos");
                try {
                    PermisosUsuario.guardar(con, "cajero2", "cajero2", "nuevo@example.com", "Cajero",
                            Set.of(PermisosUsuario.identificador("Historial de cambios")));
                    throw new AssertionError("Se otorgó historial a un cajero");
                } catch (IllegalArgumentException esperada) { }
                exigir(PermisosUsuario.cargarSesion(con).size() == PermisosUsuario.APARTADOS.size(), "El administrador perdió acceso completo");
                SesionUsuario.iniciar("eliminado", "Cajero");
                exigir(PermisosUsuario.cargarSesion(con).isEmpty(), "Se permitieron apartados de un usuario inexistente");
            } finally {
                con.setCatalog(original);
                esquemaStmt.executeUpdate("DROP DATABASE `" + esquema + "`");
                SesionUsuario.cerrar();
            }
        }
        System.out.println("OK: casillas persistentes, solo becados, revocación inmediata, cambio de alias y auditoría incluso en MyISAM.");
    }

    private interface Operacion { void ejecutar() throws SQLException; }

    private static void denegado(Operacion operacion) throws SQLException {
        try { operacion.ejecutar(); throw new AssertionError("Se permitió una operación sin autorización"); }
        catch (SQLException esperada) {
            exigir(esperada.getMessage().contains("permiso") || esperada.getMessage().contains("administradores"), "Error ajeno a permisos");
        }
    }

    private static void exigir(boolean condicion, String mensaje) { if (!condicion) throw new AssertionError(mensaje); }
}
