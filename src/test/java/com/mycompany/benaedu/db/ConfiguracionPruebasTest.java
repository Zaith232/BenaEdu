package com.mycompany.benaedu.db;

import java.io.IOException;
import java.nio.file.Files;

/** Prueba sin conexión: la edición de pruebas falla cerrada ante configuración ausente o ajena. */
public final class ConfiguracionPruebasTest {
    /** Ejecuta las comprobaciones sin tocar ninguna base. */
    public static void main(String[] args) throws Exception {
        var carpeta = Files.createTempDirectory("benaedu-config-test-");
        var archivo = carpeta.resolve("conexion.properties");
        try {
            try { ConfiguracionPruebas.leer(archivo); throw new AssertionError("Aceptó archivo ausente"); }
            catch (IOException esperado) { }
            Files.writeString(archivo, "url=jdbc:mysql://127.0.0.1:3307/benaedu?sslMode=DISABLED\nuser=benaedu_pruebas\npassword=soloPrueba\n");
            if (!"soloPrueba".equals(ConfiguracionPruebas.leer(archivo).getProperty("password"))) throw new AssertionError();
            Files.writeString(archivo, "url=jdbc:mysql://localhost:3306/benaedu\nuser=root\npassword=prueba\n");
            try { ConfiguracionPruebas.leer(archivo); throw new AssertionError("Aceptó base habitual"); }
            catch (IOException esperado) { }
            System.out.println("OK: configuración local aislada, ausente e inválida.");
        } finally { Files.deleteIfExists(archivo); Files.delete(carpeta); }
    }
}
