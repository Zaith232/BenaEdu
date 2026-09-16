package com.mycompany.benaedu.services;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Lector del EXP continuo BBVA de seis campos, sin acceso a la base de datos. */
public final class ArchivoBcm {
    private ArchivoBcm() { }

    /** Movimiento bancario conservado tal como se recibió, incluida la referencia con ceros. */
    public record Movimiento(int numero, LocalDate fecha, String forma, int guia, String referencia,
                             String descripcion, BigDecimal importe) {
        /** Identifica el movimiento en el archivo sin depender del nombre del archivo descargado. */
        public String clave() { return fecha + "/" + guia; }
    }

    /** Lee archivos EXP de tamaño acotado, sin descartar campos ni registros inválidos. */
    public static List<Movimiento> leer(Path archivo) throws java.io.IOException {
        if (Files.size(archivo) > 10_000_000) throw new IllegalArgumentException("El archivo supera 10 MB.");
        return interpretar(Files.readString(archivo, StandardCharsets.ISO_8859_1));
    }

    /** Interpreta fecha|forma|guía|referencia|descripción|importe, también si hay saltos entre registros. */
    public static List<Movimiento> interpretar(String contenido) {
        String texto = contenido.strip();
        if (texto.startsWith("\uFEFF")) texto = texto.substring(1);
        if (texto.startsWith("ï»¿")) texto = texto.substring(3);
        if (texto.endsWith("|")) texto = texto.substring(0, texto.length() - 1);
        if (texto.isBlank()) throw new IllegalArgumentException("El archivo está vacío.");
        String[] campos = texto.split("\\|", -1);
        if (campos.length % 6 != 0) throw new IllegalArgumentException("Formato EXP incompleto: se requieren seis campos por movimiento.");
        List<Movimiento> movimientos = new ArrayList<>();
        for (int i = 0; i < campos.length; i += 6) {
            int numero = i / 6 + 1;
            try {
                String fecha = campos[i].trim();
                if (!fecha.matches("[0-9]{5,6}")) throw new IllegalArgumentException("fecha inválida");
                if (fecha.length() == 5) fecha = "0" + fecha;
                LocalDate dia = LocalDate.of(2000 + Integer.parseInt(fecha.substring(4)),
                        Integer.parseInt(fecha.substring(2, 4)), Integer.parseInt(fecha.substring(0, 2)));
                String forma = campos[i + 1].trim();
                if (!forma.matches("[A-Z]{3}")) throw new IllegalArgumentException("forma de pago inválida");
                String guiaTexto = campos[i + 2].trim();
                if (!guiaTexto.matches("[0-9]{1,10}")) throw new IllegalArgumentException("guía inválida");
                int guia = Integer.parseInt(guiaTexto);
                if (guia <= 0) throw new IllegalArgumentException("guía no positiva");
                String referencia = campos[i + 3].trim();
                if (!referencia.matches("[0-9]{1,21}")) throw new IllegalArgumentException("referencia inválida");
                String monto = campos[i + 5].trim();
                if (!monto.matches("[0-9]+(?:\\.[0-9]{1,2})?")) throw new IllegalArgumentException("importe inválido");
                BigDecimal importe = new BigDecimal(monto).setScale(2);
                if (importe.signum() <= 0 || importe.compareTo(new BigDecimal("999999999.99")) > 0)
                    throw new IllegalArgumentException("importe fuera de rango");
                movimientos.add(new Movimiento(numero, dia, forma, guia, referencia, campos[i + 4].trim(), importe));
            } catch (IllegalArgumentException | java.time.DateTimeException ex) {
                throw new IllegalArgumentException("Movimiento " + numero + ": " + ex.getMessage(), ex);
            }
        }
        return List.copyOf(movimientos);
    }
}
