package com.mycompany.benaedu.services;

import com.mycompany.benaedu.services.ReferenciasBancarias.*;
import com.mycompany.benaedu.views.FichaReferenciasBancarias;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

/** Verifica paginación y renderizado con alumnos ficticios, sin impresora ni base de datos. */
public final class FichaReferenciasBancariasTest {
    private FichaReferenciasBancariasTest() { }

    /** Ejecuta la prueba y opcionalmente genera imágenes de revisión con --render. */
    public static void main(String[] args) throws Exception {
        var filas = new ArrayList<Referencia>();
        for (int i = 0; i < 9; i++) {
            String mat = i == 8 ? "121000002" : "121000001";
            var cargo = new Cargo("JDN", "A", "1J", "A", i, "COL" + i, BigDecimal.TEN, new BigDecimal("222"));
            filas.add(new Referencia(mat, "ALUMNO FICTICIO PARA PRUEBAS", "COLEGIATURA DE PRUEBA " + i, "Normal",
                    LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 12), new BigDecimal("1993"), "1210008050647544283", cargo));
            filas.add(new Referencia(mat, "ALUMNO FICTICIO PARA PRUEBAS", "COLEGIATURA DE PRUEBA " + i, "Recargo",
                    LocalDate.of(2026, 10, 13), LocalDate.of(2027, 8, 31), new BigDecimal("2192"), "1210008050650830218", cargo));
        }
        var ficha = new FichaReferenciasBancarias(filas, "0160839167", "881686", "2627", true);
        if (ficha.paginas() != 3) throw new AssertionError("La paginación debe separar alumnos y mantener parejas N/R.");
        var papel = new java.awt.print.Paper(); papel.setSize(612, 792); papel.setImageableArea(36, 36, 540, 720);
        var formato = new java.awt.print.PageFormat(); formato.setPaper(papel);
        for (int i = 0; i < ficha.paginas(); i++) {
            var imagen = new java.awt.image.BufferedImage(612, 792, java.awt.image.BufferedImage.TYPE_INT_RGB);
            var g = imagen.createGraphics(); g.setColor(java.awt.Color.WHITE); g.fillRect(0, 0, 612, 792);
            if (ficha.print(g, formato, i) != java.awt.print.Printable.PAGE_EXISTS) throw new AssertionError("Falta una página");
            if (ficha.print(g, formato, 3) != java.awt.print.Printable.NO_SUCH_PAGE) throw new AssertionError("Página fantasma");
            g.dispose();
            if (args.length == 1 && args[0].equals("--render"))
                javax.imageio.ImageIO.write(imagen, "png", new java.io.File("target/ficha-referencias-prueba-" + (i + 1) + ".png"));
        }
        System.out.println("OK: fichas de prueba, parejas de conceptos y separación de alumnos en tres páginas.");
    }
}
