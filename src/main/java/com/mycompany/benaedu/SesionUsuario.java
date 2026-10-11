package com.mycompany.benaedu;

/** Identidad verificada por el login para atribuir operaciones y restringir módulos. */
public final class SesionUsuario {
    private static volatile Identidad actual = new Identidad("", "");
    private SesionUsuario() { }

    private record Identidad(String usuario, String rol) { }

    /** Establece la identidad después de comprobar la contraseña.
     * @param usuario alias autenticado
     * @param rol rol leído de la base de datos
     */
    public static void iniciar(String usuario, String rol) {
        actual = new Identidad(usuario == null ? "" : usuario, normalizarRol(rol));
    }

    /** Presenta como cajero el rol anterior de las cuentas existentes.
     * @param rol rol almacenado
     * @return rol actual para la aplicación
     */
    public static String normalizarRol(String rol) {
        if (rol == null) return "";
        return "Empleado".equalsIgnoreCase(rol.trim()) ? "Cajero" : rol.trim();
    }

    /** Elimina la identidad al volver al login. */
    public static void cerrar() { actual = new Identidad("", ""); }

    /** Devuelve el alias autenticado o una cadena vacía.
     * @return alias de la sesión
     */
    public static String usuario() { return actual.usuario(); }

    /** Indica si el login verificó una cuenta administradora.
     * @return verdadero para una sesión administradora
     */
    public static boolean esAdministrador() {
        return !actual.usuario().isBlank() && "Administrador".equalsIgnoreCase(actual.rol());
    }
}
