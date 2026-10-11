package com.mycompany.benaedu.db;

import com.mycompany.benaedu.SesionUsuario;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Permisos por apartado guardados junto al usuario, incluso si cambia su alias. */
public final class PermisosUsuario {
    private PermisosUsuario() { }

    /** Apartado configurable del menú.
     * @param grupo módulo y categoría
     * @param nombre texto de la opción
     * @param soloAdministrador indica que el permiso no puede asignarse a cajeros
     */
    public record Apartado(String grupo, String nombre, boolean soloAdministrador) {
        /** Devuelve el identificador estable sin acentos.
         * @return identificador del apartado
         */
        public String id() { return identificador(nombre); }
    }

    /** Catálogo de las mismas categorías y opciones del dashboard. */
    public static final List<Apartado> APARTADOS = List.of(
            new Apartado("General / 20 Información Maestra", "Catálogo de Compañías", false),
            new Apartado("General / 20 Información Maestra", "Catálogo de Centros de Costo", false),
            new Apartado("General / 20 Información Maestra", "Parámetros por Compañía", false),
            new Apartado("General / 20 Información Maestra", "Ejercicio Fiscal", false),
            new Apartado("General / 20 Información Maestra", "Catálogo de Empleados", false),
            new Apartado("General / 20 Información Maestra", "Direcciones de Entrega", false),
            new Apartado("General / 20 Información Maestra", "Tabla de Clasificaciones", false),
            new Apartado("General / 20 Información Maestra", "Catálogo de Contactos", false),
            new Apartado("General / 30 Transacciones", "Tipos de Cambio Diario", false),
            new Apartado("General / 30 Transacciones", "Condiciones de Pago", false),
            new Apartado("General / 30 Transacciones", "Clave de Impuestos", false),
            new Apartado("General / 30 Transacciones", "Tipo de Cambio Mensual", false),
            new Apartado("General / 30 Transacciones", "Convertir Unidad de Medida", false),
            new Apartado("General / 20 Información Maestra", "Historial de cambios", true),
            new Apartado("Contabilidad / 20 Información Maestra", "Catálogo de Cuentas", false),
            new Apartado("Contabilidad / 30 Transacciones", "Captura de Pólizas", false),
            new Apartado("Contabilidad / 40 Consultas", "Auxiliar de Movimientos", false),
            new Apartado("Contabilidad / 40 Consultas", "Póliza de Diario", false),
            new Apartado("Contabilidad / 50 Reportes", "Balanza de Comprobación", false),
            new Apartado("Contabilidad / 50 Reportes", "Balanza de Comprobación Ssc", false),
            new Apartado("Contabilidad / 50 Reportes", "Balance por Compañía", false),
            new Apartado("Contabilidad / 50 Reportes", "Estado de Resultados x Grupo", false),
            new Apartado("Contabilidad / 50 Reportes", "Estado de Resultados", false),
            new Apartado("Contabilidad / 50 Reportes", "Libro de Diario", false),
            new Apartado("Contabilidad / 50 Reportes", "Libro Mayor", false),
            new Apartado("Contabilidad / 50 Reportes", "Impresión de Póliza", false),
            new Apartado("Contabilidad / 60 Cierre Mensual", "Cierre de Mes", false),
            new Apartado("Contabilidad / 70 Cierre Anual", "Cierre Anual", false),
            new Apartado("Contabilidad / 80 Utilerías", "Actualización de Saldos", false),
            new Apartado("Contabilidad / 80 Utilerías", "FEL", false),
            new Apartado("Escolar / 10 Configuración", "Ciclos Escolares", false),
            new Apartado("Escolar / 10 Configuración", "Grados Escolares", false),
            new Apartado("Escolar / 10 Configuración", "Registro de Becas y Convenios", false),
            new Apartado("Escolar / 10 Configuración", "Conceptos Escolares", false),
            new Apartado("Escolar / 10 Configuración", "Planes de Pago", false),
            new Apartado("Escolar / 20 Información Maestra", "Cuentas Bancarias", false),
            new Apartado("Escolar / 20 Información Maestra", "Catalogo Tipos de Alumno", false),
            new Apartado("Escolar / 20 Información Maestra", "Catalogo de Cajeros", false),
            new Apartado("Escolar / 20 Información Maestra", "Catalogo de Alumnos", false),
            new Apartado("Escolar / 30 Transacciones", "Cobranza Escolar", false),
            new Apartado("Escolar / 30 Transacciones", "Corte de Caja", false),
            new Apartado("Escolar / 30 Transacciones", "Impresión de Facturas", false),
            new Apartado("Escolar / 30 Transacciones", "Registro de Depositos", false),
            new Apartado("Escolar / 30 Transacciones", "Cancelación de Conceptos", false),
            new Apartado("Escolar / 30 Transacciones", "Cancelación de Recibos", false),
            new Apartado("Escolar / 30 Transacciones", "Cancela Contabilizacion de Corte de Caja", false),
            new Apartado("Escolar / 30 Transacciones", "Cancela Contabilizacion de Reg Deposito", false),
            new Apartado("Escolar / 40 Consultas", "Estado de Cuenta Alumnos", false),
            new Apartado("Escolar / 40 Consultas", "Consulta de Pagos", false),
            new Apartado("Escolar / 40 Consultas", "Estado de Cuenta Detallado", false),
            new Apartado("Escolar / 50 Reportes", "Resumen de Inscripciones", false),
            new Apartado("Escolar / 50 Reportes", "Resumen de Alumnos No Inscritos", false),
            new Apartado("Escolar / 50 Reportes", "Reimpresión de Recibos", false),
            new Apartado("Escolar / 50 Reportes", "Reimpresión de Facturas", false),
            new Apartado("Escolar / 50 Reportes", "Resumen de Conceptos Pagados", false),
            new Apartado("Escolar / 50 Reportes", "Detalle de Conceptos Pagados", false),
            new Apartado("Escolar / 50 Reportes", "Imprime Alumnos Becados", false),
            new Apartado("Escolar / 50 Reportes", "Ingresos por Periodo", false),
            new Apartado("Escolar / 80 Utilerías", "Procesa Referencias Bancarias BCM", false),
            new Apartado("Escolar / 80 Utilerías", "Generar Referencias Bancarias", false),
            new Apartado("Escolar / 80 Utilerías", "Interfase Bancaria BCM", false),
            new Apartado("Escolar / 80 Utilerías", "Interfase Factura FEL", false),
            new Apartado("Escolar / 80 Utilerías", "Interfase Factura Global FEL", false),
            new Apartado("Escolar / 80 Utilerías", "Subclasificaciones", false)
    );

    /** Normaliza etiquetas para que menú y buscador compartan permisos.
     * @param nombre etiqueta del apartado
     * @return identificador sin acentos
     */
    public static String identificador(String nombre) {
        return Normalizer.normalize(nombre, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("^_|_$", "");
    }

    private static Set<String> todos(boolean administrador) {
        Set<String> ids = new LinkedHashSet<>();
        APARTADOS.stream().filter(a -> administrador || !a.soloAdministrador()).forEach(a -> ids.add(a.id()));
        return Set.copyOf(ids);
    }

    /** Agrega una columna nullable; null conserva los accesos anteriores a esta función.
     * @param con conexión al sistema
     * @return verdadero si se agregó la columna
     * @throws SQLException si faltan permisos ALTER
     */
    public static boolean instalar(Connection con) throws SQLException {
        try (var ps = con.prepareStatement("SELECT 1 FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() "
                + "AND TABLE_NAME='users' AND COLUMN_NAME='benaedu_apartados'"); var rs = ps.executeQuery()) {
            if (rs.next()) return false;
        }
        try (var st = con.createStatement()) {
            st.executeUpdate("ALTER TABLE users ADD COLUMN benaedu_apartados TEXT NULL");
        }
        return true;
    }

    /** Convierte las cuentas anteriores conservando sus permisos y admite el nuevo rol en columnas ENUM.
     * @param con conexión del sistema con auditoría activa
     * @throws SQLException si no se puede actualizar el rol
     */
    public static void actualizarRolCajero(Connection con) throws SQLException {
        String tipo = null;
        try (var ps = con.prepareStatement("SELECT COLUMN_TYPE FROM information_schema.COLUMNS "
                + "WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='users' AND COLUMN_NAME='rol'"); var rs = ps.executeQuery()) {
            if (rs.next()) tipo = rs.getString(1);
        }
        if (tipo != null && tipo.toLowerCase(Locale.ROOT).startsWith("enum(")
                && !tipo.toLowerCase(Locale.ROOT).contains("'cajero'")) {
            String definicion;
            try (var st = con.createStatement(); var rs = st.executeQuery("SHOW CREATE TABLE users")) {
                rs.next();
                definicion = rs.getString(2).lines().map(String::trim)
                        .filter(linea -> linea.startsWith("`rol` ")).findFirst()
                        .orElseThrow(() -> new SQLException("No se encontró la definición del rol."));
            }
            if (definicion.endsWith(",")) definicion = definicion.substring(0, definicion.length() - 1);
            definicion = definicion.replace(tipo, tipo.substring(0, tipo.length() - 1) + ",'Cajero')")
                    .replaceAll("(?i)DEFAULT\\s+'Empleado'", "DEFAULT 'Cajero'");
            try (var st = con.createStatement()) { st.executeUpdate("ALTER TABLE users MODIFY COLUMN " + definicion); }
        }
        try (var st = con.createStatement()) {
            st.executeUpdate("UPDATE users SET rol='Cajero' WHERE rol='Empleado'");
        }
    }

    private static Set<String> interpretar(String guardados, boolean administrador) {
        if (administrador) return todos(true);
        if (guardados == null) return todos(false);
        Set<String> ids = new LinkedHashSet<>(List.of(guardados.split("\\n")));
        ids.retainAll(todos(false));
        return Set.copyOf(ids);
    }

    /** Carga los permisos y el rol vigentes de la identidad autenticada.
     * @param con conexión al sistema
     * @return apartados permitidos; vacío si no existe una sesión o el usuario fue eliminado
     * @throws SQLException si falla la consulta
     */
    public static Set<String> cargarSesion(Connection con) throws SQLException {
        if (SesionUsuario.usuario().isBlank()) return Set.of();
        try (var ps = con.prepareStatement("SELECT rol,benaedu_apartados FROM users WHERE nombre_usuario=?")) {
            ps.setString(1, SesionUsuario.usuario());
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return Set.of();
                boolean administrador = SesionUsuario.esAdministrador() && "Administrador".equalsIgnoreCase(rs.getString(1));
                return interpretar(rs.getString(2), administrador);
            }
        }
    }

    /** Carga las casillas de un usuario solamente para un administrador vigente.
     * @param con conexión al sistema
     * @param usuario alias que se editará
     * @return selección actual
     * @throws SQLException si no hay autorización o el usuario no existe
     */
    public static Set<String> cargarParaEditar(Connection con, String usuario) throws SQLException {
        Auditoria.exigirAdministrador(con);
        try (var ps = con.prepareStatement("SELECT rol,benaedu_apartados FROM users WHERE nombre_usuario=?")) {
            ps.setString(1, usuario);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("El usuario ya no existe.");
                return interpretar(rs.getString(2), "Administrador".equalsIgnoreCase(rs.getString(1)));
            }
        }
    }

    /** Comprueba el permiso antes de ejecutar o construir el módulo solicitado.
     * @param con conexión al sistema
     * @param apartado etiqueta o identificador del apartado
     * @throws SQLException si no hay permiso vigente
     */
    public static void exigirAcceso(Connection con, String apartado) throws SQLException {
        String id = identificador(apartado);
        if (!cargarSesion(con).contains(id)) throw new SQLException("No tienes permiso para acceder a este apartado.");
    }

    /** Guarda los datos y todas las casillas en una sola actualización del mismo registro.
     * @param con conexión al sistema
     * @param original alias anterior
     * @param usuario nuevo alias
     * @param correo nuevo correo
     * @param rol nuevo rol
     * @param seleccion identificadores de las casillas marcadas
     * @throws SQLException si no hay autorización o no se puede guardar
     */
    public static void guardar(Connection con, String original, String usuario, String correo, String rol, Set<String> seleccion) throws SQLException {
        Auditoria.exigirAdministrador(con);
        if (usuario == null || usuario.isBlank() || correo == null || correo.isBlank()
                || !Set.of("Administrador", "Cajero").contains(rol)) throw new IllegalArgumentException("Completa los datos del usuario.");
        if (!todos(true).containsAll(seleccion)) throw new IllegalArgumentException("Hay apartados desconocidos.");
        if (!"Administrador".equals(rol) && seleccion.stream().anyMatch(id -> APARTADOS.stream()
                .anyMatch(a -> a.soloAdministrador() && a.id().equals(id)))) {
            throw new IllegalArgumentException("El historial de cambios es exclusivo de administradores.");
        }
        boolean propiaCuenta = SesionUsuario.usuario().equalsIgnoreCase(original);
        if (propiaCuenta && !"Administrador".equals(rol)) throw new IllegalArgumentException("No puedes quitarte tu propio rol de administrador.");
        cargarParaEditar(con, original);
        try (var ps = con.prepareStatement("SELECT 1 FROM users WHERE nombre_usuario=? AND nombre_usuario<>?")) {
            ps.setString(1, usuario.trim()); ps.setString(2, original);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) throw new SQLException("Ya existe otro usuario con ese alias.");
            }
        }
        String permisos = "Administrador".equals(rol) ? null : String.join("\n", new java.util.TreeSet<>(seleccion));
        boolean vincularCajero = false;
        if (!usuario.trim().equals(original)) {
            try (var ps = con.prepareStatement("SELECT 1 FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='tescaj'");
                    var rs = ps.executeQuery()) { vincularCajero = rs.next(); }
        }
        String sql = vincularCajero
                ? "UPDATE users u LEFT JOIN tescaj c ON c.USER=u.nombre_usuario SET u.nombre_usuario=?,u.correo=?,u.rol=?,u.benaedu_apartados=?,c.USER=? WHERE u.nombre_usuario=?"
                : "UPDATE users SET nombre_usuario=?,correo=?,rol=?,benaedu_apartados=? WHERE nombre_usuario=?";
        try (var ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario.trim()); ps.setString(2, correo.trim()); ps.setString(3, rol);
            ps.setString(4, permisos);
            if (vincularCajero) { ps.setString(5, usuario.trim()); ps.setString(6, original); }
            else ps.setString(5, original);
            ps.executeUpdate();
        }
        if (propiaCuenta) {
            SesionUsuario.iniciar(usuario.trim(), rol);
            Auditoria.asignarUsuario(con);
        }
    }
}
