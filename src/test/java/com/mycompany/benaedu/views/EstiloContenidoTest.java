package com.mycompany.benaedu.views;

import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMaterialLighterIJTheme;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.*;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Comprobación visual y funcional del estilo, sin conexiones a la base de datos. */
public final class EstiloContenidoTest {
    private EstiloContenidoTest() { }

    /** Ejecuta con -Djava.awt.headless=true; guarda vistas de muestra en target. */
    public static void main(String[] args) throws Exception {
        AtomicInteger errores = new AtomicInteger();
        Logger.getLogger("com.formdev.flatlaf").addHandler(new Handler() {
            @Override public void publish(LogRecord registro) {
                if (registro.getLevel().intValue() >= Level.WARNING.intValue()) errores.incrementAndGet();
            }
            @Override public void flush() { }
            @Override public void close() { }
        });
        SwingUtilities.invokeAndWait(() -> {
            FlatMTMaterialLighterIJTheme.setup();
            Object fondoGlobal = UIManager.get("Panel.background");
            JPanel controlExterno = new JPanel();
            Color fondoExterno = controlExterno.getBackground();
            JPanel formulario = new JPanel();
            JTextField campo = new JTextField("Contenido conservado");
            campo.setEditable(false);
            JButton guardar = new JButton("Guardar");
            AtomicInteger clics = new AtomicInteger();
            guardar.addActionListener(e -> clics.incrementAndGet());
            JButton eliminar = new JButton("Eliminar");
            eliminar.setEnabled(false);
            JTable tabla = new JTable(new Object[][] {{"Dato", 42}}, new Object[] {"Nombre", "Valor"});
            var modelo = tabla.getModel();
            formulario.add(campo);
            formulario.add(guardar);
            formulario.add(eliminar);
            formulario.add(new JScrollPane(tabla));
            formulario.add(new JTextArea("Aviso"));
            formulario.add(new JComboBox<>(new String[] {"Opción"}));
            formulario.add(new JSpinner());
            EstiloContenido.aplicar(formulario);
            guardar.doClick();
            verificar(clics.get() == 1, "Se perdió el manejador de la acción");
            verificar(!eliminar.isEnabled() && !campo.isEditable(), "Se alteraron permisos del formulario");
            verificar(tabla.getModel() == modelo && tabla.getValueAt(0, 1).equals(42), "Se alteraron los datos");
            verificar(campo.getText().equals("Contenido conservado"), "Se alteró el campo");
            verificar(tabla.getRowHeight() >= 32, "No se aplicó el estilo de tabla");
            verificar(UIManager.get("Panel.background").equals(fondoGlobal)
                    && controlExterno.getBackground().equals(fondoExterno), "El estilo afectó vistas externas");
            JPanel catalogo = new Tipo_Proveedor();
            verificar(catalogo.getLayout() instanceof BorderLayout, "Catálogo sin distribución adaptable");
            renderizar(catalogo, "catalogo-moderno", 1000, 600);
            renderizar(catalogo, "catalogo-moderno-compacto", 750, 430);
            renderizar(new Generar_Referencias_Bancarias("Admin", false), "referencias-modernas", 1000, 700);
            renderizar(new Interfase_Bancaria_BCM("Admin", false), "bcm-moderno", 1000, 650);
            renderizar(new Actualizacion_Saldo(), "actualizacion-moderna", 750, 430);
        });
        verificar(errores.get() == 0, "FlatLaf notificó propiedades de estilo inválidas");
        System.out.println("OK: estilo local, acciones, datos, permisos y vistas de muestra.");
    }

    private static void verificar(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
    }

    private static void renderizar(JPanel panel, String nombre, int ancho, int alto) {
        panel.setSize(ancho, alto);
        panel.addNotify();
        distribuir(panel);
        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        Graphics2D graficos = imagen.createGraphics();
        graficos.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        panel.printAll(graficos);
        graficos.dispose();
        panel.removeNotify();
        try { ImageIO.write(imagen, "png", new File("target/" + nombre + ".png")); }
        catch (java.io.IOException ex) { throw new IllegalStateException(ex); }
    }

    private static void distribuir(Container contenedor) {
        contenedor.doLayout();
        for (Component hijo : contenedor.getComponents()) {
            if (hijo instanceof Container interior) distribuir(interior);
        }
    }
}
