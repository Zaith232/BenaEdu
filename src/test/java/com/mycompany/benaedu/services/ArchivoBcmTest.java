package com.mycompany.benaedu.services;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

/** Pruebas del formato EXP con datos ficticios y archivo real opcional, sin conexión a la base. */
public final class ArchivoBcmTest {
    private ArchivoBcmTest() { }
    /** Comprueba formato continuo, fechas abreviadas, referencias y rechazo de datos malformados. */
    public static void main(String[] args) throws Exception {
        String fila = "80526|EFE|123|01210008050647544283| |1993.50|";
        var movimientos = ArchivoBcm.interpretar(fila + "\r\n" + fila.replace("|123|", "|124|"));
        exigir(movimientos.size() == 2, "No leyó los dos registros continuos");
        exigir(movimientos.get(0).fecha().equals(LocalDate.of(2026, 5, 8)), "Fecha abreviada incorrecta");
        exigir(movimientos.get(0).referencia().startsWith("0"), "Se perdió el cero inicial");
        exigir(movimientos.get(0).importe().equals(new BigDecimal("1993.50")), "Se alteraron centavos");
        for (String texto : new String[]{"", "80526|EFE|123", fila.replace("80526", "310226"), fila.replace("1993.50", "-1"),
                fila.replace("1993.50", "1.234"), fila.replace("1993.50", "texto"), fila.replace("|123|", "|0|"), fila + "otro"}) {
            try { ArchivoBcm.interpretar(texto); throw new AssertionError("Aceptó archivo inválido"); } catch (IllegalArgumentException esperado) { }
        }
        if (args.length == 1) {
            var real = ArchivoBcm.leer(Path.of(args[0]));
            BigDecimal total = real.stream().map(ArchivoBcm.Movimiento::importe).reduce(BigDecimal.ZERO, BigDecimal::add);
            exigir(real.size() == 202 && total.compareTo(new BigDecimal("547148")) == 0, "No coincide el archivo de muestra");
            System.out.println("Archivo real: 202 movimientos, total 547148.00; lectura únicamente.");
        }
        System.out.println("OK: parser EXP, fechas, ceros, centavos y datos inválidos.");
    }
    private static void exigir(boolean cumple, String mensaje) { if (!cumple) throw new AssertionError(mensaje); }
}
