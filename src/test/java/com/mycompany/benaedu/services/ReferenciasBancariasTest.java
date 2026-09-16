package com.mycompany.benaedu.services;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Comprobación ejecutable sin conexión a la base ni dependencias de pruebas. */
public final class ReferenciasBancariasTest {
    private ReferenciasBancariasTest() { }

    /** Ejecuta ejemplos del generador CIE, registros históricos y límites de entrada. */
    public static void main(String[] args) throws Exception {
        igual("2027-08-31", ReferenciasBancarias.vencimientoRecargo("2627").toString());
        igual("2028-08-31", ReferenciasBancarias.vencimientoRecargo("2728").toString());
        igual("2029-08-31", ReferenciasBancarias.vencimientoRecargo("2829").toString());
        igual("2010-08-31", ReferenciasBancarias.vencimientoRecargo("0910").toString());
        for (String ciclo : new String[]{"", "2026-2027", "2628", "ABCD"})
            rechaza(() -> ReferenciasBancarias.vencimientoRecargo(ciclo));
        rechaza(() -> ReferenciasBancarias.vencimientoRecargo(null));
        var alumno = new ReferenciasBancarias.Alumno("001234", "GARCÍA LÓPEZ ANA", "1J");
        for (String busqueda : new String[]{"", "00123", "garcia", "LOPEZ", "ana garcia", "001 garcía"})
            if (!alumno.coincide(busqueda)) throw new AssertionError("No encontró alumno por: " + busqueda);
        for (String busqueda : new String[]{"99999", "perez", "garcia perez", "[", ".*"})
            if (alumno.coincide(busqueda)) throw new AssertionError("Coincidencia indebida: " + busqueda);
        verificar("1234567891", "1000", "2026-09-13", "123456789147241272");
        verificar("12345678916", "1000", "2026-09-13", "1234567891647241253");
        verificar("123456789168", "1000", "2026-09-13", "12345678916847241262");
        verificar("12100080506", "1993", "2026-10-12", "1210008050647544283");
        verificar("12100080506", "2192", "2027-08-31", "1210008050650830218");
        verificar("12100080507", "1993", "2026-11-10", "1210008050747834239");
        igual("2192", ReferenciasBancarias.conRecargo(new BigDecimal("1993")).toPlainString());
        igual("1105", ReferenciasBancarias.conRecargo(new BigDecimal("1005")).toPlainString());
        igual("1993", ReferenciasBancarias.conBeca(new BigDecimal("2215"), new BigDecimal("10")).toPlainString());
        igual("2215", ReferenciasBancarias.conBeca(new BigDecimal("2215"), BigDecimal.ZERO).toPlainString());
        igual("0", ReferenciasBancarias.conBeca(new BigDecimal("2215"), new BigDecimal("100")).toPlainString());
        rechaza(() -> ReferenciasBancarias.conBeca(new BigDecimal("2215"), new BigDecimal("101")));
        for (String base : new String[]{"", "1234567890123", "12A", "１２３", " 123"})
            rechaza(() -> ReferenciasBancarias.calcular(base, BigDecimal.ONE, LocalDate.of(2026, 9, 13)));
        for (String monto : new String[]{"0", "-1", "1.001", "1000000000"})
            rechaza(() -> ReferenciasBancarias.calcular("123", new BigDecimal(monto), LocalDate.of(2026, 9, 13)));
        rechaza(() -> ReferenciasBancarias.calcular("123", BigDecimal.ONE, LocalDate.of(2014, 11, 18)));
        rechaza(() -> ReferenciasBancarias.calcular("123", BigDecimal.ONE, LocalDate.of(2041, 1, 1)));
        String ceros = ReferenciasBancarias.calcular("00123", new BigDecimal("0.01"), LocalDate.of(2014, 11, 19));
        if (!ceros.startsWith("00123") || ceros.length() != 13) throw new AssertionError("Se perdieron ceros iniciales.");
        BigDecimal cero = BigDecimal.ZERO, normal = new BigDecimal("1993"), bruto = new BigDecimal("2215"), beca = new BigDecimal("222");
        ReferenciasBancarias.validarImportes(normal, normal, cero, bruto, beca, cero, cero);
        rechaza(() -> ReferenciasBancarias.validarImportes(normal, new BigDecimal("993"), new BigDecimal("1000"), bruto, beca, cero, cero));
        rechaza(() -> ReferenciasBancarias.validarImportes(normal, normal, cero, bruto, beca, BigDecimal.ONE, cero));
        rechaza(() -> ReferenciasBancarias.validarImportes(normal, normal, cero, bruto, beca, cero, BigDecimal.ONE));
        rechaza(() -> ReferenciasBancarias.validarImportes(normal, normal, cero, bruto, cero, cero, cero));
        rechaza(() -> ReferenciasBancarias.validarImportes(normal, normal, null, bruto, beca, cero, cero));
        System.out.println("OK: referencias CIE e históricas, truncamiento, límites y ceros iniciales.");
        // Integración opcional y exclusivamente de lectura: ejecutar con --db-read-only.
        if (args.length == 1 && "--db-read-only".equals(args[0])) {
            var ciclos = ReferenciasBancarias.ciclos();
            if (ciclos.isEmpty()) throw new AssertionError("No hay ciclos para probar la consulta.");
            for (var ciclo : ciclos) {
                if (!"2627".equals(ciclo.ciclo())) continue;
                var alumnos = ReferenciasBancarias.alumnos(ciclo);
                System.out.println("Selector CC=" + ciclo.cc() + ": alumnos/grados=" + alumnos.size());
                var resultado = ReferenciasBancarias.generar(new ReferenciasBancarias.Filtro(ciclo, "", "", true, LocalDate.of(2027, 8, 31)));
                System.out.println("Lectura CIA=" + ciclo.cia() + " CC=" + ciclo.cc() + " CESC=" + ciclo.ciclo()
                        + ": referencias=" + resultado.referencias().size() + ", avisos=" + resultado.avisos().size());
                resultado.avisos().stream().collect(java.util.stream.Collectors.groupingBy(
                        aviso -> aviso.substring(aviso.indexOf(": ") + 2), java.util.stream.Collectors.counting()))
                        .forEach((motivo, cantidad) -> System.out.println("  " + cantidad + " : " + motivo));
                var guardadas = ArchivoReferenciasBancarias.consultar(new ReferenciasBancarias.Filtro(ciclo, "", "", true, LocalDate.of(2027, 8, 31)));
                System.out.println("  Consulta guardadas compatibles: " + guardadas.referencias().size());
            }
        }
    }

    private static void verificar(String base, String monto, String fecha, String esperado) {
        igual(esperado, ReferenciasBancarias.calcular(base, new BigDecimal(monto), LocalDate.parse(fecha)));
    }

    private static void igual(String esperado, String actual) {
        if (!esperado.equals(actual)) throw new AssertionError("Esperado " + esperado + "; obtenido " + actual);
    }

    private static void rechaza(Runnable accion) {
        try { accion.run(); } catch (IllegalArgumentException ex) { return; }
        throw new AssertionError("Se aceptó una entrada inválida.");
    }
}
