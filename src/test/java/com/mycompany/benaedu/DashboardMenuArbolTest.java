package com.mycompany.benaedu;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/** Comprueba la expansión del menú y la conservación de sus acciones sin abrir la base de datos. */
public final class DashboardMenuArbolTest {
    private DashboardMenuArbolTest() { }

    /** Ejecuta una comprobación independiente de la interfaz principal. */
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPopupMenu menu = new JPopupMenu();
            JMenu categoria = new JMenu("20 Información Maestra");
            JMenuItem opcion = new JMenuItem("Catalogo de Alumnos");
            JMenuItem deshabilitada = new JMenuItem("Otra opción");
            deshabilitada.setEnabled(false);
            int[] llamadas = {0};
            opcion.addActionListener(e -> llamadas[0]++);
            deshabilitada.addActionListener(e -> llamadas[0]++);
            categoria.add(opcion);
            categoria.add(deshabilitada);
            menu.add(categoria);
            JTree arbol = Dashboard.crearArbolMenu(menu, new JButton());
            DefaultMutableTreeNode raiz = (DefaultMutableTreeNode) arbol.getModel().getRoot();
            DefaultMutableTreeNode rama = (DefaultMutableTreeNode) raiz.getChildAt(0);
            if (rama.getUserObject() != categoria || rama.getChildCount() != 2 || arbol.getRowCount() != 1) {
                throw new AssertionError("No se conservó la categoría con sus opciones inicialmente plegadas");
            }
            TreePath ruta = new TreePath(rama.getPath());
            arbol.setSelectionPath(ruta);
            activar(arbol);
            if (!arbol.isExpanded(ruta) || arbol.getRowCount() != 3) throw new AssertionError("No se expandió la rama");
            arbol.setSelectionPath(ruta.pathByAddingChild(rama.getChildAt(0)));
            activar(arbol);
            if (llamadas[0] != 1) throw new AssertionError("No se ejecutó la acción original una vez");
            arbol.setSelectionPath(ruta.pathByAddingChild(rama.getChildAt(1)));
            activar(arbol);
            if (llamadas[0] != 1) throw new AssertionError("Se ejecutó una opción deshabilitada");
            arbol.setSelectionPath(ruta);
            activar(arbol);
            if (arbol.isExpanded(ruta)) throw new AssertionError("No se plegó la rama");

            JPopupMenu restringido = new JPopupMenu();
            JMenu reportes = new JMenu("50 Reportes");
            JMenuItem becados = new JMenuItem("Imprime Alumnos Becados");
            JMenuItem pagos = new JMenuItem("Cobranza Escolar");
            int[] aperturas = {0};
            becados.addActionListener(e -> aperturas[0]++);
            pagos.addActionListener(e -> aperturas[0]++);
            reportes.add(becados); reportes.add(pagos); restringido.add(reportes);
            JMenu vacia = new JMenu("20 Información Maestra");
            vacia.add(new JMenuItem("Catalogo de Alumnos")); restringido.add(vacia);
            Dashboard.filtrarMenu(restringido, java.util.Set.of(
                    com.mycompany.benaedu.db.PermisosUsuario.identificador("Imprime Alumnos Becados")),
                    (nombre, accion) -> { /* Simula un permiso revocado después de cargar el menú. */ });
            if (restringido.getComponentCount() != 1 || reportes.getItemCount() != 1 || reportes.getItem(0) != becados) {
                throw new AssertionError("Se mostraron opciones o ramas sin permiso");
            }
            becados.doClick();
            if (aperturas[0] != 0) throw new AssertionError("Se construyó un módulo antes de validar el permiso vigente");

            java.util.Map<String, Runnable> indice = new java.util.LinkedHashMap<>();
            Dashboard.indexarOpciones(restringido, indice);
            if (indice.size() != 1 || !indice.containsKey("Imprime Alumnos Becados")) {
                throw new AssertionError("El buscador no conserva los mismos permisos del árbol");
            }
            indice.get("Imprime Alumnos Becados").run();
            if (aperturas[0] != 0) throw new AssertionError("El buscador omitió la autorización vigente");
            if (!Dashboard.coincideBusqueda("Cobranza Escolar", "Cobra")
                    || !Dashboard.coincideBusqueda("Catálogo de Compañías", "catalogo   de companias")
                    || Dashboard.coincideBusqueda("Corte de Caja", "alumnos")) {
                throw new AssertionError("No se encontraron las opciones sin acentos o con texto parcial");
            }

            arbol.setSize(258, 220);
            arbol.doLayout();
            var limites = arbol.getPathBounds(ruta);
            var click = new java.awt.event.MouseEvent(arbol, java.awt.event.MouseEvent.MOUSE_CLICKED,
                    System.currentTimeMillis(), 0, limites.x + 10, limites.y + 10, 1, false,
                    java.awt.event.MouseEvent.BUTTON1);
            for (var listener : arbol.getMouseListeners()) listener.mouseClicked(click);
            if (!arbol.isExpanded(ruta)) throw new AssertionError("El clic no abrió la categoría");

            comprobarLayout(args.length > 0 ? args[0] : null);
        });
        System.out.println("OK: clic y teclado del árbol, permisos compartidos, búsqueda sin acentos y layout sin superposiciones.");
    }

    /** Comprueba y, opcionalmente, renderiza el mismo layout del menú sin datos escolares. */
    private static void comprobarLayout(String salida) {
        javax.swing.JPanel panel = new javax.swing.JPanel();
        panel.setBackground(new java.awt.Color(11, 49, 91));
        javax.swing.JLabel[] encabezado = {
            new javax.swing.JLabel(new javax.swing.ImageIcon(Dashboard.class.getResource("/Logo12.png"))),
            new javax.swing.JLabel("BENAEDU"), new javax.swing.JLabel("SISTEMA DE GESTIÓN"),
            new javax.swing.JLabel("OPERATIVO")
        };
        for (var etiqueta : encabezado) etiqueta.setForeground(java.awt.Color.WHITE);
        encabezado[1].setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));
        JButton[] botones = {new JButton("General"), new JButton("Contabilidad"), new JButton("Escolar")};
        String[] iconos = {"/HomeW.png", "/trasnsactionW.png", "/MenuBookW.png"};
        for (int i = 0; i < botones.length; i++) {
            botones[i].setIcon(new javax.swing.ImageIcon(Dashboard.class.getResource(iconos[i])));
            botones[i].setIconTextGap(10);
            Dashboard.estilizarBotonMenu(botones[i]);
        }
        for (var boton : botones) {
            boton.setBackground(panel.getBackground());
            boton.setForeground(java.awt.Color.WHITE);
            boton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        }
        JPopupMenu menu = new JPopupMenu();
        JMenu categoria = new JMenu("30 Transacciones");
        categoria.add(new JMenuItem("Cobranza Escolar"));
        categoria.add(new JMenuItem("Corte de Caja"));
        menu.add(categoria);
        JTree arbol = Dashboard.crearArbolMenu(menu, botones[2]);
        arbol.expandRow(0);
        var rama = new javax.swing.JScrollPane(arbol);
        rama.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        var ramas = java.util.Map.of(botones[2], rama);
        botones[2].putClientProperty("menuAbierto", true);
        for (int alto : new int[]{664, 900}) {
            for (boolean abierta : new boolean[]{false, true}) {
                panel.setSize(270, alto);
                rama.setVisible(abierta);
                Dashboard.configurarLayoutMenu(panel, encabezado, botones, ramas);
                panel.doLayout();
                java.util.List<java.awt.Component> componentes = new java.util.ArrayList<>();
                componentes.addAll(java.util.List.of(encabezado));
                componentes.addAll(java.util.List.of(botones));
                if (abierta) componentes.add(rama);
                for (int i = 0; i < componentes.size(); i++) {
                    var bounds = componentes.get(i).getBounds();
                    if (bounds.width <= 0 || bounds.height <= 0 || bounds.y + bounds.height > alto) {
                        throw new AssertionError("Componente fuera del menú: " + bounds);
                    }
                    for (int j = i + 1; j < componentes.size(); j++) {
                        if (bounds.intersects(componentes.get(j).getBounds())) {
                            throw new AssertionError("El encabezado o la rama se superponen con los botones");
                        }
                    }
                }
            }
        }
        if (salida != null) {
            panel.setSize(270, 664);
            Dashboard.configurarLayoutMenu(panel, encabezado, botones, ramas);
            panel.doLayout();
            rama.doLayout();
            rama.getViewport().doLayout();
            var imagen = new java.awt.image.BufferedImage(270, 664, java.awt.image.BufferedImage.TYPE_INT_RGB);
            var graficos = imagen.createGraphics();
            panel.printAll(graficos);
            graficos.dispose();
            try {
                javax.imageio.ImageIO.write(imagen, "png", new java.io.File(salida));
            } catch (java.io.IOException ex) { throw new AssertionError(ex); }
        }
    }

    private static void activar(JTree arbol) {
        arbol.getActionMap().get("activarOpcion").actionPerformed(new java.awt.event.ActionEvent(arbol, 0, "ENTER"));
    }
}
