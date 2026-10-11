package com.mycompany.benaedu.views;

import java.awt.*;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;

/** Estilo local de los módulos; no cambia el tema global ni la navegación. */
final class EstiloContenido {
    private static final Color FONDO = new Color(245, 247, 251);
    private static final Color TEXTO = new Color(39, 52, 73);
    private static final Color AZUL = new Color(37, 99, 185);
    private static final Color LINEA = new Color(220, 227, 237);
    private static final Color SELECCION = new Color(226, 237, 253);

    private EstiloContenido() { }

    /** Aplica el diseño al módulo una vez terminada su construcción. */
    static void aplicar(JPanel vista) {
        estilizar(vista);
        vista.setBackground(FONDO);
        // Solo reorganiza los catálogos simples, sin filtros ni posiciones absolutas.
        if (vista.getComponentCount() == 1 && vista.getComponent(0) instanceof JPanel contenido
                && organizarCatalogo(contenido)) {
            vista.setLayout(new BorderLayout());
            vista.add(contenido, BorderLayout.CENTER);
            vista.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        }
    }

    /** Estiliza los diálogos de contenido antes de mostrarlos. */
    static void aplicar(JDialog dialogo) {
        estilizar(dialogo.getContentPane());
        dialogo.getContentPane().setBackground(FONDO);
    }

    private static boolean organizarCatalogo(JPanel panel) {
        JScrollPane tabla = null;
        int botones = 0;
        for (Component hijo : panel.getComponents()) {
            if (hijo instanceof JScrollPane scroll && scroll.getViewport().getView() instanceof JTable) {
                if (tabla != null) return false;
                tabla = scroll;
            } else if (hijo instanceof JButton) {
                botones++;
            } else {
                return false;
            }
        }
        if (tabla == null || botones == 0) return false;
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        acciones.setOpaque(false);
        for (Component hijo : panel.getComponents()) {
            if (hijo instanceof JButton boton) {
                Dimension medida = boton.getPreferredSize();
                boton.setPreferredSize(new Dimension(Math.max(104, medida.width), 38));
                acciones.add(boton);
            }
        }
        panel.removeAll();
        panel.setLayout(new BorderLayout(0, 14));
        panel.setBackground(FONDO);
        panel.add(acciones, BorderLayout.NORTH);
        panel.add(tabla, BorderLayout.CENTER);
        return true;
    }

    private static void estilizar(Component componente) {
        Font anterior = componente.getFont();
        if (anterior != null) {
            componente.setFont(new Font("Segoe UI", anterior.getStyle(), anterior.getSize()));
        }
        // Conserva colores semánticos (avisos, errores y totales) de las vistas.
        Color color = componente.getForeground();
        if (color == null || (Math.abs(color.getRed() - color.getGreen()) < 25
                && Math.abs(color.getGreen() - color.getBlue()) < 25)) {
            componente.setForeground(TEXTO);
        }
        if (componente instanceof JPanel panel) {
            panel.setBackground(Color.WHITE);
            modernizarBorde(panel);
            for (Component hijo : panel.getComponents()) estilizar(hijo);
        } else if (componente instanceof JButton boton) {
            estilizarBoton(boton);
        } else if (componente instanceof JTable tabla) {
            tabla.setBackground(Color.WHITE);
            tabla.setForeground(TEXTO);
            tabla.setRowHeight(Math.max(32, tabla.getRowHeight()));
            tabla.setShowVerticalLines(false);
            tabla.setShowHorizontalLines(true);
            tabla.setGridColor(new Color(237, 241, 246));
            tabla.setIntercellSpacing(new Dimension(0, 1));
            tabla.setSelectionBackground(SELECCION);
            tabla.setSelectionForeground(TEXTO);
            tabla.setFillsViewportHeight(true);
            tabla.putClientProperty("FlatLaf.style", "selectionArc: 6; selectionInsets: 1,0,1,0");
            if (tabla.getTableHeader() != null) {
                var cabecera = tabla.getTableHeader();
                cabecera.setBackground(FONDO);
                cabecera.setForeground(TEXTO);
                cabecera.setFont(new Font("Segoe UI", Font.BOLD, 12));
                cabecera.setPreferredSize(new Dimension(cabecera.getPreferredSize().width, 36));
            }
        } else if (componente instanceof JScrollPane scroll) {
            modernizarBorde(scroll);
            scroll.getViewport().setBackground(Color.WHITE);
            scroll.getVerticalScrollBar().setUnitIncrement(24);
            if (scroll.getViewport().getView() != null) estilizar(scroll.getViewport().getView());
        } else if (componente instanceof JTabbedPane pestanas) {
            pestanas.setBackground(FONDO);
            pestanas.setForeground(TEXTO);
            pestanas.putClientProperty("FlatLaf.style", "tabHeight: 34; selectedBackground: #ffffff; underlineColor: #2563b9");
            for (int i = 0; i < pestanas.getTabCount(); i++) estilizar(pestanas.getComponentAt(i));
        } else if (componente instanceof JSplitPane divisor) {
            divisor.setBorder(BorderFactory.createEmptyBorder());
            divisor.setDividerSize(8);
            if (divisor.getLeftComponent() != null) estilizar(divisor.getLeftComponent());
            if (divisor.getRightComponent() != null) estilizar(divisor.getRightComponent());
        } else if (componente instanceof JTextComponent campo) {
            campo.setBackground(campo.isEditable() ? Color.WHITE : FONDO);
            campo.setSelectionColor(SELECCION);
            campo.setSelectedTextColor(TEXTO);
            if (campo instanceof JTextField) {
                campo.putClientProperty("FlatLaf.style", "arc: 8; borderColor: #dce3ed; focusColor: #2563b9");
            }
        } else if (componente instanceof JComboBox<?> combo) {
            combo.setBackground(Color.WHITE);
            combo.putClientProperty("FlatLaf.style", "arc: 8; borderColor: #dce3ed; focusColor: #2563b9");
        } else if (componente instanceof JSpinner spinner) {
            spinner.putClientProperty("FlatLaf.style", "arc: 8; borderColor: #dce3ed; focusColor: #2563b9");
        } else if (componente instanceof JCheckBox || componente instanceof JRadioButton) {
            ((JComponent) componente).setOpaque(false);
        } else if (componente instanceof JProgressBar barra) {
            barra.setForeground(AZUL);
            barra.setBackground(SELECCION);
        }
    }

    private static void estilizarBoton(JButton boton) {
        String texto = boton.getText() == null ? "" : boton.getText().toLowerCase(Locale.ROOT).strip();
        boolean principal = texto.matches("^(añadir|agregar|nuevo|nueva|guardar|aceptar|generar?|aplicar|procesar|consultar|buscar|cargar|imprimir)(\\s.*|…)?$");
        boolean eliminar = texto.startsWith("eliminar") || texto.startsWith("borrar");
        Color fondo = principal ? AZUL : eliminar ? new Color(255, 241, 242) : Color.WHITE;
        Color tinta = principal ? Color.WHITE : eliminar ? new Color(177, 37, 56) : TEXTO;
        boton.setBackground(fondo);
        boton.setForeground(tinta);
        boton.setFont(boton.getFont().deriveFont(Font.BOLD));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.putClientProperty("FlatLaf.style", "arc: 10; borderWidth: 1; borderColor: "
                + (principal ? "#2563b9" : eliminar ? "#fecdd3" : "#dce3ed")
                + "; focusWidth: 2; focusedBorderColor: #2563b9; hoverBackground: "
                + (principal ? "#1d529e" : eliminar ? "#ffe4e6" : "#edf3fc")
                + "; pressedBackground: " + (principal ? "#194582" : "#dce8f7"));
    }

    private static void modernizarBorde(JComponent componente) {
        Border borde = componente.getBorder();
        if (borde instanceof TitledBorder titulo) {
            componente.setBorder(BorderFactory.createTitledBorder(new BordeSuave(new Insets(2, 2, 2, 2)),
                    titulo.getTitle(), titulo.getTitleJustification(), titulo.getTitlePosition(),
                    new Font("Segoe UI", Font.BOLD, 12), TEXTO));
        } else if (borde != null && !(borde instanceof javax.swing.border.EmptyBorder)) {
            componente.setBorder(new BordeSuave(borde.getBorderInsets(componente)));
        }
    }

    /** Borde redondeado que conserva el espacio del formulario original. */
    private static final class BordeSuave extends AbstractBorder {
        private final Insets margenes;

        private BordeSuave(Insets margenes) {
            this.margenes = (Insets) margenes.clone();
        }

        @Override public Insets getBorderInsets(Component c, Insets destino) {
            destino.set(margenes.top, margenes.left, margenes.bottom, margenes.right);
            return destino;
        }

        @Override public void paintBorder(Component c, Graphics g, int x, int y, int ancho, int alto) {
            Graphics2D copia = (Graphics2D) g.create();
            copia.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            copia.setColor(LINEA);
            copia.drawRoundRect(x, y, ancho - 1, alto - 1, 12, 12);
            copia.dispose();
        }
    }
}
