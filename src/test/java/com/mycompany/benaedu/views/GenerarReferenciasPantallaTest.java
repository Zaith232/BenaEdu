package com.mycompany.benaedu.views;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import javax.swing.*;

/** Verifica la pantalla simplificada sin base de datos ni ventanas del escritorio. */
public final class GenerarReferenciasPantallaTest {
    private GenerarReferenciasPantallaTest() { }

    /** Revisa botones, ausencia de opciones manuales y genera una captura de prueba opcional. */
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel panel = new Generar_Referencias_Bancarias("Admin", false);
            var componentes = new ArrayList<Component>(); recorrer(panel, componentes);
            var textos = componentes.stream().filter(c -> c instanceof JButton).map(c -> ((JButton) c).getText()).toList();
            if (!textos.contains("Genera") || !textos.contains("Imprimir")) throw new AssertionError("Faltan acciones principales");
            for (String prohibido : new String[]{"Guardar en tesrefb", "Calcular referencias", "Consultar guardadas", "Vista previa", "Actualizar ciclos"})
                if (textos.contains(prohibido)) throw new AssertionError("Botón sobrante: " + prohibido);
            if (componentes.stream().anyMatch(c -> c instanceof JCheckBox)) throw new AssertionError("Hay casillas manuales");
            if (componentes.stream().filter(c -> c instanceof JTextField).count() != 3) throw new AssertionError("Hay campos adicionales a matrícula, cuenta y convenio");
            if (args.length == 1 && args[0].equals("--render")) {
                panel.setSize(850, 650); distribuir(panel);
                var imagen = new java.awt.image.BufferedImage(850, 650, java.awt.image.BufferedImage.TYPE_INT_RGB);
                var g = imagen.createGraphics(); panel.printAll(g); g.dispose();
                try { javax.imageio.ImageIO.write(imagen, "png", new java.io.File("target/referencias-pantalla-prueba.png")); }
                catch (java.io.IOException ex) { throw new RuntimeException(ex); }
            }
        });
        System.out.println("OK: acciones simplificadas, sin casillas ni fecha manual.");
    }
    private static void recorrer(Component c, java.util.List<Component> lista) {
        lista.add(c); if (c instanceof Container contenedor) for (Component hijo : contenedor.getComponents()) recorrer(hijo, lista);
    }
    private static void distribuir(Container c) {
        c.doLayout(); for (Component hijo : c.getComponents()) if (hijo instanceof Container contenedor) distribuir(contenedor);
    }
}
