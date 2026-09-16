package com.mycompany.benaedu.views;

import java.awt.*;
import javax.swing.*;

/** Comprueba el estado inicial seguro de la pantalla sin conectar a la base. */
public final class InterfaseBcmPantallaTest {
    private InterfaseBcmPantallaTest() { }
    /** Valida acciones y, opcionalmente, genera una captura con --render. */
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel panel = new Interfase_Bancaria_BCM("Admin", false);
            var componentes = new java.util.ArrayList<Component>(); recorrer(panel, componentes);
            boolean aplicar = false;
            for (Component c : componentes) if (c instanceof JButton b && b.getText().equals("Aplicar pagos")) {
                aplicar = true; if (b.isEnabled()) throw new AssertionError("Aplicación habilitada sin revisión");
            }
            if (!aplicar) throw new AssertionError("Falta la acción explícita de aplicación");
            if (args.length == 1 && args[0].equals("--render")) {
                panel.setSize(900, 600); distribuir(panel);
                var imagen = new java.awt.image.BufferedImage(900, 600, java.awt.image.BufferedImage.TYPE_INT_RGB);
                var g = imagen.createGraphics(); panel.printAll(g); g.dispose();
                try { javax.imageio.ImageIO.write(imagen, "png", new java.io.File("target/interfase-bcm-prueba.png")); }
                catch (java.io.IOException ex) { throw new RuntimeException(ex); }
            }
        });
        System.out.println("OK: pantalla BCM; aplicación deshabilitada hasta revisar.");
    }
    private static void recorrer(Component c, java.util.List<Component> lista) {
        lista.add(c); if (c instanceof Container padre) for (Component hijo : padre.getComponents()) recorrer(hijo, lista);
    }
    private static void distribuir(Container c) {
        c.doLayout(); for (Component hijo : c.getComponents()) if (hijo instanceof Container padre) distribuir(padre);
    }
}
