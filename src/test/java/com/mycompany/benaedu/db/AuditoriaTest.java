package com.mycompany.benaedu.db;

import com.mycompany.benaedu.SesionUsuario;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;

/** Prueba real de auditoría en un esquema desechable, sin modificar tablas de la escuela. */
public final class AuditoriaTest {
    private AuditoriaTest() { }

    /** Verifica permisos, cambios, secretos, fechas, lotes y motores transaccionales.
     * @param args argumentos no utilizados
     * @throws Exception si falla una comprobación
     */
    public static void main(String[] args) throws Exception {
        String esquema = "codex_auditoria_" + UUID.randomUUID().toString().replace("-", "");
        String urlPruebas = System.getProperty("benaedu.auditoria.test.url");
        try (Connection con = urlPruebas == null ? ConDB.getConnection()
                : java.sql.DriverManager.getConnection(urlPruebas, "root", ""); var schemaStmt = con.createStatement()) {
            String original = con.getCatalog();
            schemaStmt.executeUpdate("CREATE DATABASE `" + esquema + "` CHARACTER SET utf8mb4");
            con.setCatalog(esquema);
            try (var st = con.createStatement()) {
                st.executeUpdate("CREATE TABLE users (nombre_usuario VARCHAR(40) PRIMARY KEY, rol VARCHAR(40), nombre VARCHAR(80), contrasena VARCHAR(255)) ENGINE=InnoDB");
                st.executeUpdate("INSERT INTO users VALUES ('admin','Administrador','Administrador','hash-anterior'), ('cajero','Cajero','Cajero','hash-cajero')");
                st.executeUpdate("CREATE TABLE tespalu (CIA VARCHAR(4), MAT VARCHAR(20), NREC INT PRIMARY KEY, NCAJ INT, IMPMN DECIMAL(12,2)) ENGINE=MyISAM");
                exigir(Auditoria.bloqueos(con).isEmpty(), "Se agregaron bloqueos antes de instalar");
                Auditoria.instalar(con);
                Auditoria.instalar(con); // La instalación debe ser idempotente.
                SesionUsuario.iniciar("cajero", "Cajero");
                Auditoria.asignarUsuario(con);
                st.executeUpdate("INSERT INTO tespalu VALUES ('12','ALUMNO-1',1,7,125.50)");
                denegado(con);
                SesionUsuario.iniciar("admin", "Administrador");
                Auditoria.asignarUsuario(con);
                var cambios = consultar(con, "cajero");
                exigir(cambios.size() == 1, "Falta el pago del cajero");
                var pago = cambios.getFirst();
                exigir(pago.accion().equals("Pago registrado") && pago.usuario().equals("cajero"), "Acción o usuario incorrectos");
                exigir(pago.referencia().contains("MAT=ALUMNO-1") && pago.referencia().contains("NREC=1")
                        && pago.despues().contains("125.50"), "Faltan matrícula, recibo o importe");
                exigir(pago.fecha() != null && pago.antes() == null, "Fecha o datos anteriores incorrectos");
                st.executeUpdate("UPDATE tespalu SET IMPMN=125.50 WHERE NREC=1");
                exigir(consultar(con, "").size() == 1, "Se registró una actualización sin cambios");
                con.setAutoCommit(false);
                st.executeUpdate("UPDATE users SET nombre='Sin confirmar' WHERE nombre_usuario='cajero'");
                con.rollback();
                exigir(consultar(con, "").size() == 1, "Se registró un cambio InnoDB revertido");
                st.executeUpdate("INSERT INTO tespalu VALUES ('12','ALUMNO-2',2,7,50.00)");
                con.rollback();
                exigir(consultar(con, "").size() == 2, "Se perdió un cambio MyISAM que no se revirtió");
                con.setAutoCommit(true);
                st.executeUpdate("UPDATE users SET contrasena='SECRETO-NUEVO' WHERE nombre_usuario='cajero'");
                var secreto = consultar(con, "users").getFirst();
                exigir(secreto.campos().contains("contrasena"), "Falta el cambio de contraseña");
                exigir(!secreto.antes().contains("hash-cajero") && !secreto.despues().contains("SECRETO-NUEVO")
                        && !secreto.despues().contains("contrasena"), "Se filtró un secreto");
                try (var ps = con.prepareStatement("INSERT INTO tespalu VALUES ('12',?, ?,7,10.00)")) {
                    for (int i = 3; i < 5; i++) { ps.setString(1, "ALUMNO-" + i); ps.setInt(2, i); ps.addBatch(); }
                    ps.executeBatch();
                }
                exigir(consultar(con, "tespalu").size() == 4, "No se registró el lote completo");
                st.execute("LOCK TABLES tespalu WRITE" + Auditoria.bloqueos(con));
                try { st.executeUpdate("UPDATE tespalu SET IMPMN=200.00 WHERE NREC=1"); }
                finally { st.execute("UNLOCK TABLES"); }
                exigir(consultar(con, "tespalu").size() == 5, "No se registró el cambio con tablas bloqueadas");
                st.executeUpdate("DELETE FROM tespalu WHERE NREC=4");
                var baja = consultar(con, "tespalu").getFirst();
                exigir(baja.accion().equals("Baja") && baja.antes() != null && baja.despues() == null, "Baja incorrecta");
                exigir(consultar(con, "%").isEmpty(), "La búsqueda interpreta comodines del usuario");
                try (var ps = con.prepareStatement("INSERT INTO tespalu VALUES ('12',?, ?,7,10.00)")) {
                    for (int i = 10; i < 65; i++) { ps.setString(1, "PAGINA-" + i); ps.setInt(2, i); ps.addBatch(); }
                    ps.executeBatch();
                }
                exigir(consultar(con, "PAGINA-").size() == 50, "La primera página no tiene 50 cambios");
                exigir(Auditoria.consultar(con, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1),
                        "PAGINA-", 1).size() == 5, "La segunda página no contiene los restantes");
                SesionUsuario.iniciar("cajero", "Administrador"); // El rol de memoria no basta para autorizar.
                denegado(con);
                SesionUsuario.iniciar("admin", "Administrador");
                st.executeUpdate("UPDATE users SET rol='Cajero' WHERE nombre_usuario='admin'");
                denegado(con);
                SesionUsuario.cerrar();
                denegado(con);
                try {
                    new com.mycompany.benaedu.views.Historial_Cambios();
                    throw new AssertionError("Un usuario sin sesión abrió el módulo");
                } catch (SecurityException esperada) { }
            } finally {
                con.setAutoCommit(true);
                con.setCatalog(original);
                schemaStmt.executeUpdate("DROP DATABASE `" + esquema + "`");
                SesionUsuario.cerrar();
            }
        }
        System.out.println("OK: pagos, altas/modificaciones/bajas, permisos reales, secretos excluidos, lotes, bloqueos y rollback por motor.");
    }

    private static java.util.List<Auditoria.Cambio> consultar(Connection con, String texto) throws SQLException {
        return Auditoria.consultar(con, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1), texto, 0);
    }

    private static void denegado(Connection con) {
        try {
            consultar(con, "");
            throw new AssertionError("Se permitió consultar sin un administrador vigente");
        } catch (SQLException esperada) {
            exigir(esperada.getMessage().contains("administradores"), "La consulta falló por una razón distinta del permiso");
        }
    }

    private static void exigir(boolean condicion, String mensaje) { if (!condicion) throw new AssertionError(mensaje); }
}
