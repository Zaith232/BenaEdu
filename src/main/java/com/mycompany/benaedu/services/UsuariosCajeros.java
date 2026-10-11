package com.mycompany.benaedu.services;

import com.mycompany.benaedu.SesionUsuario;
import com.mycompany.benaedu.db.Auditoria;
import com.mycompany.benaedu.db.PermisosUsuario;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import org.mindrot.jbcrypt.BCrypt;

/** Alta de cuentas y perfiles internos de cajero, y autorización vigente de cobros. */
public final class UsuariosCajeros {
    private UsuariosCajeros() { }

    /** Resultado del alta.
     * @param idUsuario identificador asignado a la cuenta
     * @param numeroCajero número para recibos, o null para administradores
     */
    public record Alta(int idUsuario, Integer numeroCajero) { }

    /** Crea una cuenta y, para cajeros, el perfil activo en la compañía seleccionada.
     * @param con conexión nueva en autocommit
     * @param usuario alias de acceso
     * @param nombre nombre de la persona
     * @param apellidos apellidos de la persona
     * @param clave contraseña original que se guardará como hash
     * @param correo correo de la cuenta
     * @param rol Cajero o Administrador
     * @param cia compañía del cajero; no se usa para administradores
     * @param permisos apartados marcados para el cajero
     * @return identificadores creados
     * @throws SQLException si no hay autorización, existe la cuenta o falla el guardado
     */
    public static Alta crear(Connection con, String usuario, String nombre, String apellidos, String clave,
            String correo, String rol, String cia, Set<String> permisos) throws SQLException {
        Auditoria.exigirAdministrador(con);
        if (!con.getAutoCommit()) throw new SQLException("El alta requiere una conexión nueva.");
        if (List.of(usuario, nombre, apellidos, clave, correo).stream().anyMatch(String::isBlank)
                || !Set.of("Administrador", "Cajero").contains(rol)) throw new IllegalArgumentException("Completa los datos del usuario.");
        Set<String> validos = new java.util.HashSet<>();
        PermisosUsuario.APARTADOS.stream().filter(a -> !a.soloAdministrador()).forEach(a -> validos.add(a.id()));
        if ("Cajero".equals(rol) && !validos.containsAll(permisos)) throw new IllegalArgumentException("Hay apartados no permitidos para cajeros.");
        if ("Cajero".equals(rol)) {
            try { Integer.parseInt(cia); } catch (Exception ex) { throw new IllegalArgumentException("Selecciona una compañía válida."); }
            try (var ps = con.prepareStatement("SELECT 1 FROM tmcias WHERE CIA=?")) {
                ps.setString(1, cia);
                try (var rs = ps.executeQuery()) { if (!rs.next()) throw new SQLException("La compañía seleccionada ya no existe."); }
            }
        }
        String hash = BCrypt.hashpw(clave, BCrypt.gensalt());
        String candado = "benaedu_alta_usuarios_" + con.getCatalog();
        if (candado.length() > 64) candado = candado.substring(0, 64);
        try (var ps = con.prepareStatement("SELECT GET_LOCK(?,10)")) {
            ps.setString(1, candado);
            try (var rs = ps.executeQuery()) {
                if (!rs.next() || rs.getInt(1) != 1) throw new SQLException("No se pudo reservar el número de usuario. Intenta de nuevo.");
            }
        }
        int id = 0;
        Integer numero = null;
        boolean empleadoIntentado = false, cajeroIntentado = false, usuarioIntentado = false;
        try {
            try (var ps = con.prepareStatement("SELECT 1 FROM users WHERE nombre_usuario=? OR correo=?")) {
                ps.setString(1, usuario); ps.setString(2, correo);
                try (var rs = ps.executeQuery()) { if (rs.next()) throw new SQLException("Ya existe un usuario con ese alias o correo."); }
            }
            con.setAutoCommit(false);
            try (var st = con.createStatement(); var rs = st.executeQuery("SELECT COALESCE(MAX(id_user),0)+1 FROM users")) {
                rs.next(); id = Math.toIntExact(rs.getLong(1));
            }
            if ("Cajero".equals(rol)) {
                try (var st = con.createStatement(); var rs = st.executeQuery("SELECT GREATEST("
                        + "(SELECT COALESCE(MAX(NEMP),0) FROM tgemp), (SELECT COALESCE(MAX(NEMP),0) FROM tescaj))+1")) {
                    rs.next(); numero = Math.toIntExact(rs.getLong(1));
                }
                empleadoIntentado = true;
                try (var ps = con.prepareStatement("INSERT INTO tgemp (CIA,NEMP,NOME,MAIL,CAJ,USER,FEAC,HOAC) "
                        + "VALUES (?,?,?,?,'S',?,CURDATE(),DATE_FORMAT(NOW(),'%H:%i:%s'))")) {
                    ps.setString(1, cia); ps.setInt(2, numero); ps.setString(3, nombre + " " + apellidos);
                    ps.setString(4, correo); ps.setString(5, SesionUsuario.usuario()); ps.executeUpdate();
                }
                cajeroIntentado = true;
                try (var ps = con.prepareStatement("INSERT INTO tescaj (CIA,NEMP,ECAJ,USER,FEAC,HOAC) "
                        + "VALUES (?,?,'A',?,CURDATE(),DATE_FORMAT(NOW(),'%H:%i:%s'))")) {
                    ps.setString(1, cia); ps.setInt(2, numero); ps.setString(3, usuario); ps.executeUpdate();
                }
            }
            usuarioIntentado = true;
            try (var ps = con.prepareStatement("INSERT INTO users (id_user,nombre_usuario,nombre,apellidos,contrasena,rol,correo,temporal,benaedu_apartados) "
                    + "VALUES (?,?,?,?,?,?,?,0,?)")) {
                ps.setInt(1, id); ps.setString(2, usuario); ps.setString(3, nombre); ps.setString(4, apellidos);
                ps.setString(5, hash); ps.setString(6, rol); ps.setString(7, correo);
                ps.setString(8, "Administrador".equals(rol) ? null : String.join("\n", new java.util.TreeSet<>(permisos)));
                ps.executeUpdate();
            }
            con.commit();
            return new Alta(id, numero);
        } catch (SQLException | RuntimeException ex) {
            try {
                if (!con.getAutoCommit()) con.rollback();
                con.setAutoCommit(true);
                // MyISAM no revierte: retirar solo filas identificables como parte de esta alta.
                if (usuarioIntentado) borrar(con, "DELETE FROM users WHERE id_user=? AND nombre_usuario=? AND contrasena=?", id, usuario, hash);
                if (cajeroIntentado) borrar(con, "DELETE FROM tescaj WHERE CIA=? AND NEMP=? AND USER=?", cia, numero, usuario);
                if (empleadoIntentado) borrar(con, "DELETE FROM tgemp WHERE CIA=? AND NEMP=? AND USER=? AND NOME=?",
                        cia, numero, SesionUsuario.usuario(), nombre + " " + apellidos);
            } catch (SQLException limpieza) {
                ex.addSuppressed(limpieza);
                throw new SQLException("El alta quedó incompleta y requiere revisión; no vuelvas a registrarla antes de comprobarla.", ex);
            }
            throw ex;
        } finally {
            try {
                con.setAutoCommit(true);
            } finally {
                try (var ps = con.prepareStatement("SELECT RELEASE_LOCK(?)")) { ps.setString(1, candado); ps.executeQuery().close(); }
            }
        }
    }

    private static void borrar(Connection con, String sql, Object... valores) throws SQLException {
        try (var ps = con.prepareStatement(sql)) {
            for (int i = 0; i < valores.length; i++) ps.setObject(i + 1, valores[i]);
            ps.executeUpdate();
        }
    }

    /** Verifica rol, permisos vigentes y un único perfil activo antes de registrar un pago.
     * @param con conexión de la operación
     * @param cia compañía del pago
     * @param apartado apartado que registra el pago
     * @return número de cajero que se guardará en recibos y pagos
     * @throws SQLException si no existe autorización o asignación activa
     */
    public static int exigirParaCobrar(Connection con, String cia, String apartado) throws SQLException {
        PermisosUsuario.exigirAcceso(con, apartado);
        try (var ps = con.prepareStatement("SELECT rol FROM users WHERE nombre_usuario=?")) {
            ps.setString(1, SesionUsuario.usuario());
            try (var rs = ps.executeQuery()) {
                if (!rs.next() || !"Cajero".equals(SesionUsuario.normalizarRol(rs.getString(1)))) {
                    throw new SQLException("Solo una cuenta con rol Cajero puede registrar pagos.");
                }
            }
        }
        try (var ps = con.prepareStatement("SELECT NEMP FROM tescaj WHERE USER=? AND CIA=? AND ECAJ='A'")) {
            ps.setString(1, SesionUsuario.usuario()); ps.setString(2, cia);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("El usuario no tiene un cajero activo asignado en esta compañía.");
                int numero = rs.getInt(1);
                if (rs.wasNull() || numero <= 0 || rs.next()) throw new SQLException("La asignación de cajero no es válida o está duplicada.");
                return numero;
            }
        }
    }
}
