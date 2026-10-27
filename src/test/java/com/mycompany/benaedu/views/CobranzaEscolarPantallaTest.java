package com.mycompany.benaedu.views;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Comprueba secciones y detalle del cobro sin base de datos ni cobros reales. */
public final class CobranzaEscolarPantallaTest {
    private CobranzaEscolarPantallaTest() { }

    /** Simula seleccionar y retirar un concepto y verifica el resumen compartido. */
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel panel = new Cobranza_Escolar("Prueba", false);
            var componentes = new ArrayList<Component>();
            recorrer(panel, componentes);
            JComboBox<?> secciones = componentes.stream().filter(c -> c instanceof JComboBox<?> combo
                    && combo.getItemCount() == 4 && "12100".equals(combo.getItemAt(0)))
                    .map(c -> (JComboBox<?>) c).findFirst().orElseThrow();
            String[] claves = {"12100", "12200", "12300", "12400"};
            String[] etiquetas = {"PRES", "PRIM", "SEC", "BACH"};
            for (int i = 0; i < claves.length; i++) {
                if (!claves[i].equals(secciones.getItemAt(i))
                        || !etiquetas[i].equals(Cobranza_Escolar.formatoSeccion(claves[i])))
                    throw new AssertionError("Sección incorrecta");
            }
            JTable resumen = componentes.stream().filter(c -> "resumenConceptosPago".equals(c.getName()))
                    .map(c -> (JTable) c).findFirst().orElseThrow();
            var tablas = componentes.stream().filter(c -> c instanceof JTable t && t.getColumnCount() == 9)
                    .map(c -> (JTable) c).toList();
            JTable adeudos = tablas.get(0);
            JTable seleccionados = tablas.get(1);
            ((DefaultTableModel) adeudos.getModel()).addRow(new Object[]{
                    "COL01", "Colegiatura", "2,520.00", "0.00", "2,520.00", "1", "2026-10-31", "2627", "O"});
            dobleClic(adeudos);
            if (resumen.getModel() != seleccionados.getModel() || resumen.getRowCount() != 1
                    || !"Colegiatura".equals(resumen.getValueAt(0, 1))
                    || resumen.isCellEditable(0, 4)) throw new AssertionError("Detalle incorrecto");
            if (args.length == 1 && "--render".equals(args[0])) {
                JTabbedPane pestanas = componentes.stream().filter(c -> c instanceof JTabbedPane tabs
                        && tabs.getTabCount() == 3).map(c -> (JTabbedPane) c).findFirst().orElseThrow();
                pestanas.setSelectedIndex(1);
                panel.setSize(920, 650);
                distribuir(panel);
                var imagen = new java.awt.image.BufferedImage(920, 650, java.awt.image.BufferedImage.TYPE_INT_RGB);
                var g = imagen.createGraphics(); panel.printAll(g); g.dispose();
                try { javax.imageio.ImageIO.write(imagen, "png", new java.io.File("target/cobranza-listado-prueba.png")); }
                catch (java.io.IOException ex) { throw new RuntimeException(ex); }
            }
            dobleClic(seleccionados);
            if (resumen.getRowCount() != 0 || adeudos.getRowCount() != 1)
                throw new AssertionError("Resumen desactualizado al retirar concepto");
        });
        System.out.println("OK: cuatro secciones, abreviaturas y listado sincronizado del cobro.");
    }

    private static void dobleClic(JTable tabla) {
        tabla.setRowSelectionInterval(0, 0);
        var evento = new MouseEvent(tabla, MouseEvent.MOUSE_CLICKED, 0, 0, 1, 1, 2, false);
        for (var listener : tabla.getMouseListeners()) listener.mouseClicked(evento);
    }

    private static void recorrer(Component componente, java.util.List<Component> lista) {
        lista.add(componente);
        if (componente instanceof Container contenedor)
            for (Component hijo : contenedor.getComponents()) recorrer(hijo, lista);
    }

    private static void distribuir(Container contenedor) {
        contenedor.doLayout();
        for (Component hijo : contenedor.getComponents())
            if (hijo instanceof Container panel) distribuir(panel);
    }
}
