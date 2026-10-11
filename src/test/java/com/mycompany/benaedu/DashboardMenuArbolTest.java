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
        });
        System.out.println("OK: categorías plegables, acciones originales y opciones deshabilitadas.");
    }

    private static void activar(JTree arbol) {
        arbol.getActionMap().get("activarOpcion").actionPerformed(new java.awt.event.ActionEvent(arbol, 0, "ENTER"));
    }
}
