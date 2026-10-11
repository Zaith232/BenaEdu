package com.mycompany.benaedu.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Configuración aislada de la edición local de pruebas; no admite servidores remotos. */
final class ConfiguracionPruebas {
    private ConfiguracionPruebas() { }

    /** Lee la configuración creada por el instalador, sin recurrir a la base habitual. */
    static Properties leer(Path archivo) throws IOException {
        Properties datos = new Properties();
        try (var entrada = Files.newInputStream(archivo)) { datos.load(entrada); }
        if (!"jdbc:mysql://127.0.0.1:3307/benaedu?sslMode=DISABLED".equals(datos.getProperty("url"))
                || !"benaedu_pruebas".equals(datos.getProperty("user"))
                || datos.getProperty("password", "").isBlank()) {
            throw new IOException("Configuración de pruebas incompleta o no local");
        }
        return datos;
    }
}
