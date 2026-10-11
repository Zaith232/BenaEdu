package com.mycompany.benaedu.db;

import com.mycompany.benaedu.SesionUsuario;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Historial persistente de cambios por fila, atribuido a la sesión de BenaEdu. */
public final class Auditoria {
    private static final String TABLA = "benaedu_historial";
    private static final Set<String> INSTALADAS = new java.util.HashSet<>();
    private Auditoria() { }

    /** Datos de un cambio; nunca contienen contraseñas ni hashes de autenticación.
     * @param id identificador del evento
     * @param fecha momento del cambio en el servidor
     * @param usuario alias autenticado
     * @param tabla tabla modificada
     * @param accion tipo de operación
     * @param referencia identificadores del registro
     * @param campos columnas modificadas
     * @param antes datos anteriores sin secretos
     * @param despues datos nuevos sin secretos
     */
    public record Cambio(long id, Timestamp fecha, String usuario, String tabla, String accion,
            String referencia, String campos, String antes, String despues) { }

    /** Asigna la identidad a esta conexión, incluidas las abiertas por trabajos en segundo plano.
     * @param con conexión del sistema
     * @throws SQLException si no se puede asignar la identidad
     */
    public static void asignarUsuario(Connection con) throws SQLException {
        try (var ps = con.prepareStatement("SET @benaedu_usuario = ?")) {
            ps.setString(1, SesionUsuario.usuario());
            ps.executeUpdate();
        }
    }

    /** Instala los registros automáticos y actualiza la identidad tras autenticar el login.
     * @param con conexión autenticada
     * @throws SQLException si faltan permisos o no se pueden instalar los disparadores
     */
    public static synchronized void prepararSesion(Connection con) throws SQLException {
        String clave = con.getMetaData().getURL() + "|" + con.getCatalog();
        if (!INSTALADAS.contains(clave)) {
            instalar(con);
            INSTALADAS.add(clave);
        }
        asignarUsuario(con);
    }

    private record Columna(String nombre, String tipo, boolean clave) { }

    /** Actualiza los disparadores propios cuando cambia la estructura de una tabla.
     * @param con conexión al esquema activo
     * @param tabla tabla cuyo detalle debe actualizarse
     * @throws SQLException si no se pueden reconstruir los disparadores
     */
    public static synchronized void refrescarTabla(Connection con, String tabla) throws SQLException {
        try (var st = con.createStatement()) {
            for (String operacion : List.of("INSERT", "UPDATE", "DELETE")) {
                String trigger = "benaedu_audit_" + java.util.UUID.nameUUIDFromBytes(
                        (tabla + operacion).getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString().replace("-", "");
                st.executeUpdate("DROP TRIGGER IF EXISTS " + identificador(trigger));
            }
        }
        INSTALADAS.remove(con.getMetaData().getURL() + "|" + con.getCatalog());
        prepararSesion(con);
    }

    /** Incluye el historial en bloqueos explícitos; evita conflictos de disparadores en MariaDB.
     * @param con conexión al esquema activo
     * @return sufijo de LOCK TABLES o vacío si todavía no hay historial instalado
     * @throws SQLException si no se puede comprobar el esquema
     */
    public static String bloqueos(Connection con) throws SQLException {
        try (var ps = con.prepareStatement("SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() "
                + "AND TABLE_NAME IN ('benaedu_historial','benaedu_historial_tx')"); var rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1) == 2 ? ",benaedu_historial WRITE,benaedu_historial_tx WRITE" : "";
        }
    }

    /** Instala disparadores sin cambiar ni reconstruir las tablas del sistema.
     * @param con conexión al esquema que se auditará
     * @throws SQLException si falla la instalación
     */
    public static void instalar(Connection con) throws SQLException {
        try (var st = con.createStatement()) {
            for (String sufijo : List.of("", "_tx")) {
                // Las fuentes MyISAM necesitan un historial que sobreviva a su rollback inefectivo.
                st.executeUpdate("CREATE TABLE IF NOT EXISTS " + TABLA + sufijo + " ("
                        + "id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY, fecha DATETIME(6) NOT NULL, "
                        + "usuario VARCHAR(255) NOT NULL, tabla VARCHAR(64) NOT NULL, accion VARCHAR(30) NOT NULL, "
                        + "referencia TEXT, campos TEXT, antes LONGTEXT, despues LONGTEXT, "
                        + "INDEX ix_fecha(fecha), INDEX ix_usuario(usuario)) ENGINE="
                        + (sufijo.isEmpty() ? "MyISAM" : "InnoDB") + " DEFAULT CHARSET=utf8mb4");
            }
        }
        List<String[]> tablas = new ArrayList<>();
        try (var ps = con.prepareStatement("SELECT TABLE_NAME, ENGINE FROM information_schema.TABLES "
                + "WHERE TABLE_SCHEMA=DATABASE() AND TABLE_TYPE='BASE TABLE' AND TABLE_NAME NOT IN ('benaedu_historial','benaedu_historial_tx')")) {
            try (var rs = ps.executeQuery()) {
                while (rs.next()) tablas.add(new String[]{rs.getString(1), rs.getString(2)});
            }
        }
        for (String[] tabla : tablas) {
            List<Columna> columnas = new ArrayList<>();
            try (var ps = con.prepareStatement("SELECT COLUMN_NAME, DATA_TYPE, COLUMN_KEY FROM information_schema.COLUMNS "
                    + "WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME=? ORDER BY ORDINAL_POSITION")) {
                ps.setString(1, tabla[0]);
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) columnas.add(new Columna(rs.getString(1), rs.getString(2), "PRI".equals(rs.getString(3))));
                }
            }
            for (String operacion : List.of("INSERT", "UPDATE", "DELETE")) {
                String trigger = "benaedu_audit_" + java.util.UUID.nameUUIDFromBytes(
                        (tabla[0] + operacion).getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString().replace("-", "");
                try (var ps = con.prepareStatement("SELECT 1 FROM information_schema.TRIGGERS "
                        + "WHERE TRIGGER_SCHEMA=DATABASE() AND TRIGGER_NAME=?")) {
                    ps.setString(1, trigger);
                    try (var rs = ps.executeQuery()) { if (rs.next()) continue; }
                }
                String fila = "DELETE".equals(operacion) ? "OLD" : "NEW";
                String antes = "INSERT".equals(operacion) ? "NULL" : json(columnas, "OLD");
                String despues = "DELETE".equals(operacion) ? "NULL" : json(columnas, "NEW");
                String iguales = String.join(" AND ", columnas.stream().map(c -> "(OLD." + identificador(c.nombre())
                        + " <=> NEW." + identificador(c.nombre()) + ")").toList());
                String campos = "UPDATE".equals(operacion)
                        ? "CONCAT_WS(', ', " + String.join(",", columnas.stream().map(c -> "IF(OLD." + identificador(c.nombre())
                                + " <=> NEW." + identificador(c.nombre()) + ",NULL," + literal(c.nombre()) + ")").toList()) + ")"
                        : literal(String.join(", ", columnas.stream().map(Columna::nombre).toList()));
                String accion = switch (operacion) {
                    case "INSERT" -> "tespalu".equalsIgnoreCase(tabla[0]) ? "Pago registrado" : "Alta";
                    case "DELETE" -> "Baja";
                    default -> "Modificación";
                };
                String destino = TABLA + ("InnoDB".equalsIgnoreCase(tabla[1]) ? "_tx" : "");
                String insert = "INSERT INTO " + destino
                        + " (fecha,usuario,tabla,accion,referencia,campos,antes,despues) VALUES "
                        + "(NOW(6),COALESCE(NULLIF(@benaedu_usuario,''),'Sin sesión autenticada'),"
                        + literal(tabla[0]) + "," + literal(accion) + "," + referencia(columnas, fila)
                        + "," + campos + "," + antes + "," + despues + ");";
                try (var st = con.createStatement()) {
                    st.executeUpdate("CREATE TRIGGER " + identificador(trigger) + " AFTER " + operacion + " ON "
                            + identificador(tabla[0]) + " FOR EACH ROW BEGIN "
                            + ("UPDATE".equals(operacion) ? "IF NOT (" + iguales + ") THEN " + insert + " END IF;" : insert)
                            + " END");
                }
            }
        }
    }

    private static boolean secreto(Columna columna) {
        String nombre = java.text.Normalizer.normalize(columna.nombre().toLowerCase(Locale.ROOT), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return nombre.matches(".*(contrasena|password|passwd|pwd|secret|token|hash|clave).*" )
                || Set.of("blob", "longblob", "mediumblob", "tinyblob", "binary", "varbinary", "geometry").contains(columna.tipo());
    }

    private static String json(List<Columna> columnas, String fila) {
        return "JSON_OBJECT(" + String.join(",", columnas.stream().filter(c -> !secreto(c))
                .map(c -> literal(c.nombre()) + "," + fila + "." + identificador(c.nombre())).toList()) + ")";
    }

    private static String referencia(List<Columna> columnas, String fila) {
        Set<String> claves = Set.of("CIA", "CC", "MAT", "CESC", "NREC", "IDCPT", "NCAJ", "NEMP", "NOMBRE_USUARIO", "ID");
        var partes = columnas.stream().filter(c -> !secreto(c) && (c.clave() || claves.contains(c.nombre().toUpperCase(Locale.ROOT))))
                .limit(8).map(c -> "CONCAT(" + literal(c.nombre() + "=") + ",COALESCE(CAST(" + fila + "."
                        + identificador(c.nombre()) + " AS CHAR),''))").toList();
        return partes.isEmpty() ? "''" : "CONCAT_WS(' · '," + String.join(",", partes) + ")";
    }

    private static String identificador(String valor) { return "`" + valor.replace("`", "``") + "`"; }
    private static String literal(String valor) { return "'" + valor.replace("\\", "\\\\").replace("'", "''") + "'"; }

    /** Comprueba el rol vigente en la base; ocultar el menú no constituye autorización.
     * @param con conexión al sistema
     * @throws SQLException si no existe una sesión administradora vigente
     */
    public static void exigirAdministrador(Connection con) throws SQLException {
        if (SesionUsuario.esAdministrador()) {
            try (var ps = con.prepareStatement("SELECT 1 FROM users WHERE nombre_usuario=? AND rol='Administrador'")) {
                ps.setString(1, SesionUsuario.usuario());
                try (var rs = ps.executeQuery()) { if (rs.next()) return; }
            }
        }
        throw new SQLException("Acceso permitido únicamente a administradores.");
    }

    /** Consulta una página del historial con fecha final inclusiva.
     * @param con conexión al sistema
     * @param desde primer día
     * @param hasta último día
     * @param texto texto en usuario, tabla, acción o referencia
     * @param pagina página empezando por cero
     * @return hasta cincuenta eventos ordenados del más reciente al más antiguo
     * @throws SQLException si falla la consulta o la autorización
     */
    public static List<Cambio> consultar(Connection con, LocalDate desde, LocalDate hasta, String texto, int pagina) throws SQLException {
        exigirAdministrador(con);
        if (desde.isAfter(hasta) || pagina < 0) throw new IllegalArgumentException("El rango de fechas o la página no es válido.");
        List<Cambio> cambios = new ArrayList<>();
        String sql = "SELECT * FROM (SELECT * FROM " + TABLA + " UNION ALL SELECT * FROM " + TABLA + "_tx) h "
                + "WHERE fecha>=? AND fecha<? AND (usuario LIKE ? ESCAPE '!' OR tabla LIKE ? ESCAPE '!' "
                + "OR accion LIKE ? ESCAPE '!' OR referencia LIKE ? ESCAPE '!') "
                + "ORDER BY fecha DESC, tabla, id DESC LIMIT 50 OFFSET ?";
        try (var ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(desde.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
            String termino = "%" + texto.trim().replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
            // ESCAPE explícito para que las búsquedas no interpreten los caracteres del usuario como comodines.
            for (int i = 3; i <= 6; i++) ps.setString(i, termino);
            ps.setInt(7, Math.multiplyExact(pagina, 50));
            try (var rs = ps.executeQuery()) {
                while (rs.next()) cambios.add(new Cambio(rs.getLong("id"), rs.getTimestamp("fecha"), rs.getString("usuario"),
                        rs.getString("tabla"), rs.getString("accion"), rs.getString("referencia"), rs.getString("campos"),
                        rs.getString("antes"), rs.getString("despues")));
            }
        }
        return cambios;
    }
}
