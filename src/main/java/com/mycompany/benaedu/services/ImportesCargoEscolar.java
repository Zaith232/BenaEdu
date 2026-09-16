package com.mycompany.benaedu.services;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Importes consistentes del cargo nuevo, con total truncado a pesos enteros. */
public record ImportesCargoEscolar(BigDecimal descuentoPlan, BigDecimal descuentoBeca, BigDecimal total) {
    /**
     * Aplica los porcentajes sobre el importe base y trunca una sola vez el total final.
     * La fracción eliminada se incorpora a la beca efectiva si hay beca, o al descuento del plan.
     * Los porcentajes originales se conservan en el cargo; no se modifica el precio base.
     * @param base precio original no negativo
     * @param porcentajePlan descuento del plan entre cero y cien
     * @param porcentajeBeca beca aplicable entre cero y cien
     * @return descuentos efectivos y total, cuya suma coincide con el importe base
     */
    public static ImportesCargoEscolar calcular(BigDecimal base, BigDecimal porcentajePlan, BigDecimal porcentajeBeca) {
        if (base == null || base.signum() < 0) throw new IllegalArgumentException("Importe base inválido.");
        for (BigDecimal porcentaje : new BigDecimal[]{porcentajePlan, porcentajeBeca})
            if (porcentaje == null || porcentaje.signum() < 0 || porcentaje.compareTo(new BigDecimal("100")) > 0)
                throw new IllegalArgumentException("El descuento y la beca deben estar entre 0 y 100%.");
        BigDecimal plan = base.multiply(porcentajePlan.movePointLeft(2));
        // Se conserva el mínimo cero del cálculo anterior sin registrar descuentos mayores al cargo.
        BigDecimal beca = base.multiply(porcentajeBeca.movePointLeft(2)).min(base.subtract(plan));
        BigDecimal neto = base.subtract(plan).subtract(beca);
        BigDecimal total = neto.setScale(0, RoundingMode.DOWN);
        BigDecimal ajuste = neto.subtract(total);
        if (porcentajeBeca.signum() > 0) beca = beca.add(ajuste);
        else plan = plan.add(ajuste);
        return new ImportesCargoEscolar(plan, beca, total);
    }
}
