package com.mycompany.benaedu.views;

import com.mycompany.benaedu.services.ReferenciasBancarias.Referencia;
import java.awt.*;
import java.awt.print.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** Fichas paginadas por alumno y concepto, utilizables por impresora y vista previa. */
public final class FichaReferenciasBancarias implements Printable {
    private final List<List<List<Referencia>>> paginas = new ArrayList<>();
    private final String cuenta;
    private final String convenio;
    private final String ciclo;
    private final boolean guardadas;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/uuuu");

    /** Agrupa las referencias sin mezclar alumnos ni dividir un concepto entre páginas. */
    public FichaReferenciasBancarias(List<Referencia> referencias, String cuenta, String convenio, String ciclo, boolean guardadas) {
        this.cuenta = cuenta; this.convenio = convenio; this.ciclo = ciclo; this.guardadas = guardadas;
        var alumnos = new LinkedHashMap<String, LinkedHashMap<Integer, List<Referencia>>>();
        for (Referencia r : referencias) alumnos.computeIfAbsent(r.matricula(), k -> new LinkedHashMap<>())
                .computeIfAbsent(r.cargo().id(), k -> new ArrayList<>()).add(r);
        for (var conceptos : alumnos.values()) {
            List<List<Referencia>> lista = new ArrayList<>(conceptos.values());
            for (int i = 0; i < lista.size(); i += 7) paginas.add(List.copyOf(lista.subList(i, Math.min(i + 7, lista.size()))));
        }
    }

    /** Devuelve el número de hojas de la ficha. */
    public int paginas() { return paginas.size(); }

    /** Dibuja la misma ficha para vista previa e impresión, sin cortar los márgenes imprimibles. */
    @Override public int print(Graphics graphics, PageFormat formato, int pagina) {
        if (pagina < 0 || pagina >= paginas.size()) return NO_SUCH_PAGE;
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.translate(formato.getImageableX(), formato.getImageableY());
            double escala = Math.min(formato.getImageableWidth() / 540, formato.getImageableHeight() / 700);
            g.scale(escala, escala);
            g.setColor(Color.WHITE); g.fillRect(0, 0, 540, 700); g.setColor(Color.BLACK);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            texto(g, "UNIDAD ESCOLAR BENAVENTE, A.C.", 0, 20, 540, 15, true);
            texto(g, "REFERENCIAS CICLO ESCOLAR: " + ciclo, 0, 40, 540, 12, true);
            texto(g, "BBVA BANCOMER   |   Convenio CIE: " + convenio + "   |   Cuenta: " + cuenta, 0, 59, 540, 10, false);
            Referencia alumno = paginas.get(pagina).get(0).get(0);
            texto(g, "Matrícula: " + alumno.matricula() + "     Grado: " + alumno.cargo().grado() + "     Grupo: " + alumno.cargo().grupo(), 0, 83, 540, 10, true);
            texto(g, "Alumno: " + alumno.nombre(), 0, 99, 540, 10, false);
            texto(g, "Pague el importe correspondiente a la fecha indicada. Importes en pesos mexicanos.", 0, 118, 540, 9, false);
            if (!guardadas) texto(g, "VISTA PREVIA — NO ENTREGAR PARA PAGO", 0, 137, 540, 11, true);
            int y = 156;
            for (List<Referencia> concepto : paginas.get(pagina)) {
                g.drawLine(0, y, 540, y);
                texto(g, concepto.get(0).concepto(), 0, y + 16, 540, 10, true);
                int linea = y + 34;
                for (Referencia r : concepto) {
                    texto(g, (r.tipo().equals("Normal") ? "Normal" : "Recargo") + ": " + FECHA.format(r.inicio()) + " a " + FECHA.format(r.fin()), 0, linea, 242, 9, false);
                    texto(g, "$ " + r.importe().setScale(2).toPlainString(), 248, linea, 77, 10, true);
                    texto(g, r.referencia(), 332, linea, 208, 11, true);
                    linea += 17;
                }
                y += 70;
            }
            texto(g, "Página " + (pagina + 1) + " de " + paginas.size() + " — Esta ficha no es comprobante de pago.", 0, 680, 540, 9, false);
            return PAGE_EXISTS;
        } finally { g.dispose(); }
    }

    private static void texto(Graphics2D g, String texto, int x, int y, int ancho, int tamano, boolean negrita) {
        String valor = java.util.Objects.toString(texto, "");
        Font font = new Font(Font.SANS_SERIF, negrita ? Font.BOLD : Font.PLAIN, tamano);
        g.setFont(font);
        while (g.getFontMetrics().stringWidth(valor) > ancho && font.getSize2D() > 7) {
            font = font.deriveFont(font.getSize2D() - 0.5f); g.setFont(font);
        }
        Shape clip = g.getClip();
        g.clipRect(x, y - 20, ancho, 25);
        g.drawString(valor, x, y); g.setClip(clip);
    }
}
