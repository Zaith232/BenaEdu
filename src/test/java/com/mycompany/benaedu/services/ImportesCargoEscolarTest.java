package com.mycompany.benaedu.services;

import java.math.BigDecimal;

/** Pruebas sin base de datos de los importes generados durante la inscripción. */
public final class ImportesCargoEscolarTest {
    private ImportesCargoEscolarTest() { }

    /** Comprueba truncamiento, consistencia y compatibilidad con las referencias bancarias. */
    public static void main(String[] args) {
        verificar("2215", "0", "10", "0", "222", "1993");
        verificar("2215", "0", "0", "0", "0", "2215");
        verificar("2215", "0", "100", "0", "2215", "0");
        verificar("2215", "10", "0", "222", "0", "1993");
        verificar("2215", "5", "10", "110.75", "222.25", "1882");
        verificar("100", "80", "30", "80", "20", "0");
        verificar("100.99", "0", "0", "0.99", "0", "100");
        verificar("0", "0", "0", "0", "0", "0");
        var base = new BigDecimal("2215");
        var beca = BigDecimal.TEN;
        var r = ImportesCargoEscolar.calcular(base, BigDecimal.ZERO, beca);
        igual(ReferenciasBancarias.conBeca(base, beca), r.total());
        ReferenciasBancarias.validarImportes(r.total(), r.total(), BigDecimal.ZERO, base,
                r.descuentoBeca(), r.descuentoPlan(), BigDecimal.ZERO);
        igual(new BigDecimal("2192"), ReferenciasBancarias.conRecargo(r.total()));
        System.out.println("OK: cargos enteros, descuentos consistentes y compatibilidad con referencias.");
    }

    private static void verificar(String base, String plan, String beca, String dPlan, String dBeca, String total) {
        var r = ImportesCargoEscolar.calcular(new BigDecimal(base), new BigDecimal(plan), new BigDecimal(beca));
        igual(new BigDecimal(dPlan), r.descuentoPlan());
        igual(new BigDecimal(dBeca), r.descuentoBeca());
        igual(new BigDecimal(total), r.total());
        igual(new BigDecimal(base), r.total().add(r.descuentoPlan()).add(r.descuentoBeca()));
    }

    private static void igual(BigDecimal esperado, BigDecimal actual) {
        if (esperado.compareTo(actual) != 0) throw new AssertionError("Esperado " + esperado + "; obtenido " + actual);
    }
}
