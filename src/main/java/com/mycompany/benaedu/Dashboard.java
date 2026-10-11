/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.benaedu;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMaterialLighterIJTheme;
import com.mycompany.benaedu.views.Companias;
import com.mycompany.benaedu.views.Principal;
import com.mycompany.benaedu.views.Centros_costos;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author b17za
 */
public class Dashboard extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Dashboard.class.getName());
    // Mapa que guardará el nombre de la opción y la acción que debe ejecutar
    private final java.util.Map<String, Runnable> opcionesSistema = new java.util.LinkedHashMap<>();

    // El menú flotante que mostrará los resultados de búsqueda
    private final javax.swing.JPopupMenu menuResultadosBusqueda = new javax.swing.JPopupMenu();
    private String usuarioActual = "Usuario";
private String usuarioCodigo = "Admin"; // Guarda el alias de login

    private int indiceSeleccionadoBusqueda = -1;
    private final java.util.Map<javax.swing.JButton, javax.swing.JScrollPane> ramasMenu = new java.util.LinkedHashMap<>();
    private java.util.Set<String> apartadosPermitidos = java.util.Set.of();

    public Dashboard(String nombreUsuario,String usuarioCodigo) {
       if (SesionUsuario.usuario().isBlank()) {
           throw new IllegalStateException("Inicia sesión antes de abrir el menú.");
       }
       this.usuarioActual = nombreUsuario;
    this.usuarioCodigo = usuarioCodigo;
    initComponents();
    InitStyles();
    AplicarDiseñoPersonalizado();
    configurarFechaYHora();
    configurarMensajeBienvenida();
    initContent();

    configurarMenuGeneral();
    configurarMenuContabilidad();
    configurarMenuEscolar();

    configurarBuscador();
    }
public String getUsuarioCodigo() {
    return this.usuarioCodigo != null ? this.usuarioCodigo : "Admin";
}
    /**
     * Creates new form Dashboard
     */
    public Dashboard() {
        this(SesionUsuario.usuario(), SesionUsuario.usuario());
    }

    private void InitStyles() {
        lblMessage.putClientProperty("FlatLaf.style", "font: bold 30");
        lblMessage.setForeground(new Color(179, 207, 229));
        lblDate.putClientProperty("FlatLaf.style", "font: bold 30 ");
        lblDate.setForeground(new Color(179, 207, 229));
        lblTime.putClientProperty("FlatLaf.style", "font: bold 30 ");
        lblTime.setForeground(new Color(179, 207, 229));
        lblTitleInfo.putClientProperty("FlatLaf.style", "font: bold 24");
        lblTitleInfo.setForeground(new Color(179, 207, 229));

    }

    private void configurarMenuArbol(javax.swing.JButton boton, javax.swing.JPopupMenu menu) {
        filtrarMenu(menu, apartadosPermitidos, this::ejecutarApartado);
        indexarOpciones(menu, opcionesSistema);
        boton.setEnabled(menu.getComponentCount() > 0);
        boton.setToolTipText(boton.isEnabled() ? "Mostrar u ocultar opciones de " + boton.getText()
                : "Sin apartados autorizados en este módulo");
        if (ramasMenu.isEmpty()) {
            jpMenu.addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentResized(java.awt.event.ComponentEvent e) { actualizarLayoutMenu(); }
            });
        }
        javax.swing.JTree arbol = crearArbolMenu(menu, boton);
        javax.swing.JScrollPane rama = new javax.swing.JScrollPane(arbol);
        rama.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        rama.setVisible(false);
        ramasMenu.put(boton, rama);
        boton.addActionListener(e -> {
            boolean abrir = !rama.isVisible();
            ramasMenu.values().forEach(otra -> otra.setVisible(false));
            rama.setVisible(abrir);
            ramasMenu.forEach((otroBoton, otraRama) -> {
                otroBoton.putClientProperty("menuAbierto", otraRama.isVisible());
                otroBoton.repaint();
            });
            actualizarLayoutMenu();
        });
        actualizarLayoutMenu();
    }

    /** Construye el árbol usando las mismas opciones y acciones del menú existente. */
    static javax.swing.JTree crearArbolMenu(javax.swing.JPopupMenu menu, javax.swing.JButton boton) {
        DefaultMutableTreeNode raiz = new DefaultMutableTreeNode();
        for (java.awt.Component componente : menu.getComponents()) {
            if (componente instanceof javax.swing.JMenu categoria) {
                DefaultMutableTreeNode rama = new DefaultMutableTreeNode(categoria);
                raiz.add(rama);
                for (java.awt.Component opcion : categoria.getMenuComponents()) {
                    if (opcion instanceof javax.swing.JMenuItem item) {
                        rama.add(new DefaultMutableTreeNode(item, false));
                    }
                }
            }
        }
        javax.swing.JTree arbol = new javax.swing.JTree(new DefaultTreeModel(raiz));
        arbol.setRootVisible(false);
        arbol.setShowsRootHandles(true);
        arbol.setToggleClickCount(0);
        arbol.setRowHeight(28);
        arbol.setFont(boton.getFont());
        arbol.setBackground(boton.getBackground());
        arbol.setForeground(boton.getForeground());
        arbol.setCellRenderer(new javax.swing.tree.DefaultTreeCellRenderer() {
            @Override
            public java.awt.Component getTreeCellRendererComponent(javax.swing.JTree tree, Object value,
                    boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
                if (((DefaultMutableTreeNode) value).getUserObject() instanceof javax.swing.JMenuItem item) {
                    setText(item.getText());
                    setEnabled(item.isEnabled());
                }
                setBackgroundNonSelectionColor(boton.getBackground());
                setTextNonSelectionColor(boton.getForeground());
                setBackgroundSelectionColor(new Color(25, 73, 120));
                setTextSelectionColor(Color.WHITE);
                setOpenIcon(null);
                setClosedIcon(null);
                setLeafIcon(null);
                return this;
            }
        });
        for (int fila = arbol.getRowCount() - 1; fila >= 0; fila--) arbol.collapseRow(fila);
        Runnable activar = () -> {
            javax.swing.tree.TreePath ruta = arbol.getSelectionPath();
            if (ruta == null) return;
            DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) ruta.getLastPathComponent();
            if (nodo.getUserObject() instanceof javax.swing.JMenu categoria) {
                if (!categoria.isEnabled()) return;
                if (arbol.isExpanded(ruta)) arbol.collapsePath(ruta);
                else arbol.expandPath(ruta);
            } else if (nodo.getUserObject() instanceof javax.swing.JMenuItem item && item.isEnabled()) {
                item.doClick();
            }
        };
        arbol.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                var ruta = arbol.getPathForLocation(e.getX(), e.getY());
                if (javax.swing.SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1
                        && ruta != null && e.getX() >= arbol.getPathBounds(ruta).x) {
                    arbol.setSelectionPath(ruta);
                    activar.run();
                }
            }
        });
        arbol.getInputMap().put(javax.swing.KeyStroke.getKeyStroke("ENTER"), "activarOpcion");
        arbol.getInputMap().put(javax.swing.KeyStroke.getKeyStroke("SPACE"), "activarOpcion");
        arbol.getActionMap().put("activarOpcion", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) { activar.run(); }
        });
        return arbol;
    }

    private void actualizarLayoutMenu() {
        configurarLayoutMenu(jpMenu, new javax.swing.JLabel[]{lblLogo, jLabel1, jLabel2, jLabel3},
                new javax.swing.JButton[]{btnInfoMaestra, btnTransacciones, btnEscolar}, ramasMenu);
    }

    /** Distribuye encabezado, botones y rama abierta sin superponer componentes. */
    static void configurarLayoutMenu(javax.swing.JPanel jpMenu, javax.swing.JLabel[] encabezado,
            javax.swing.JButton[] botones, java.util.Map<javax.swing.JButton, javax.swing.JScrollPane> ramasMenu) {
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(jpMenu);
        jpMenu.setLayout(layout);
        javax.swing.GroupLayout.ParallelGroup columnas = layout.createParallelGroup();
        javax.swing.GroupLayout.SequentialGroup filas = layout.createSequentialGroup().addContainerGap();
        int[] alturas = {157, 32, 20, 20};
        for (int i = 0; i < encabezado.length; i++) {
            encabezado[i].setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            columnas.addComponent(encabezado[i], 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
            filas.addComponent(encabezado[i], javax.swing.GroupLayout.PREFERRED_SIZE, alturas[i],
                    javax.swing.GroupLayout.PREFERRED_SIZE);
        }
        filas.addGap(16);
        for (int i = 0; i < botones.length; i++) {
            if (i > 0) filas.addGap(8);
            columnas.addComponent(botones[i], 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
            filas.addComponent(botones[i], javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE);
            javax.swing.JScrollPane rama = ramasMenu.get(botones[i]);
            if (rama != null) {
                columnas.addComponent(rama, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
                int alto = Math.max(56, Math.min(360, jpMenu.getHeight() - 437));
                filas.addComponent(rama, javax.swing.GroupLayout.PREFERRED_SIZE, alto, javax.swing.GroupLayout.PREFERRED_SIZE);
            }
        }
        filas.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
        layout.setHorizontalGroup(layout.createSequentialGroup().addContainerGap().addGroup(columnas).addContainerGap());
        layout.setVerticalGroup(filas);
        jpMenu.revalidate();
        jpMenu.repaint();
    }

    /** Filtra ramas vacías y protege sus acciones antes de construir las pantallas. */
    static void filtrarMenu(javax.swing.JPopupMenu menu, java.util.Set<String> permitidos,
            java.util.function.BiConsumer<String, Runnable> ejecutar) {
        for (java.awt.Component componente : menu.getComponents()) {
            if (componente instanceof javax.swing.JMenu categoria) {
                filtrarMenu(categoria.getPopupMenu(), permitidos, ejecutar);
                if (categoria.getItemCount() == 0) menu.remove(categoria);
            } else if (componente instanceof javax.swing.JMenuItem item) {
                if (!permitidos.contains(com.mycompany.benaedu.db.PermisosUsuario.identificador(item.getText()))) {
                    menu.remove(item);
                } else {
                    java.awt.event.ActionListener[] acciones = item.getActionListeners();
                    for (var accion : acciones) item.removeActionListener(accion);
                    item.addActionListener(e -> ejecutar.accept(item.getText(), () -> {
                        for (var accion : acciones) accion.actionPerformed(e);
                    }));
                }
            }
        }
    }

    private void ejecutarApartado(String nombre, Runnable accion) {
        try (var con = com.mycompany.benaedu.db.ConDB.getConnection()) {
            com.mycompany.benaedu.db.PermisosUsuario.exigirAcceso(con, nombre);
            accion.run();
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Acceso al apartado", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void configurarMenuGeneral() {
        if (!SesionUsuario.usuario().isBlank()) {
            try (var con = com.mycompany.benaedu.db.ConDB.getConnection()) {
                apartadosPermitidos = com.mycompany.benaedu.db.PermisosUsuario.cargarSesion(con);
            } catch (Exception ex) {
                javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Permisos", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
        btnInfoMaestra.setText("General"); // Cambiamos el nombre del botón
        javax.swing.JPopupMenu menuGeneral = new javax.swing.JPopupMenu();

        // Categorías
        javax.swing.JMenu menuInfoMaestra = new javax.swing.JMenu("20 Información Maestra");
        javax.swing.JMenu menuTransacciones = new javax.swing.JMenu("30 Transacciones");

        // Opciones "20 Información Maestra"
        javax.swing.JMenuItem iCompanias = new javax.swing.JMenuItem("Catálogo de Compañías");
        javax.swing.JMenuItem iCentros = new javax.swing.JMenuItem("Catálogo de Centros de Costo");
        javax.swing.JMenuItem iParametros = new javax.swing.JMenuItem("Parámetros por Compañía");
        javax.swing.JMenuItem iFiscal = new javax.swing.JMenuItem("Ejercicio Fiscal");
        javax.swing.JMenuItem iEmpleados = new javax.swing.JMenuItem("Catálogo de Empleados");
        javax.swing.JMenuItem iDirecciones = new javax.swing.JMenuItem("Direcciones de Entrega");
        javax.swing.JMenuItem iClasificaciones = new javax.swing.JMenuItem("Tabla de Clasificaciones");
        javax.swing.JMenuItem iContactos = new javax.swing.JMenuItem("Catálogo de Contactos");

        menuInfoMaestra.add(iCompanias);
        menuInfoMaestra.add(iCentros);
        menuInfoMaestra.add(iParametros);
        menuInfoMaestra.add(iFiscal);
        menuInfoMaestra.add(iEmpleados);
        menuInfoMaestra.add(iDirecciones);
        menuInfoMaestra.add(iClasificaciones);
        menuInfoMaestra.add(iContactos);

        // Opciones "30 Transacciones"
        javax.swing.JMenuItem iCambioD = new javax.swing.JMenuItem("Tipos de Cambio Diario");
        javax.swing.JMenuItem iCondiciones = new javax.swing.JMenuItem("Condiciones de Pago");
        javax.swing.JMenuItem iImpuestos = new javax.swing.JMenuItem("Clave de Impuestos");
        javax.swing.JMenuItem iCambioM = new javax.swing.JMenuItem("Tipo de Cambio Mensual");
        javax.swing.JMenuItem iUnidad = new javax.swing.JMenuItem("Convertir Unidad de Medida");

        menuTransacciones.add(iCambioD);
        menuTransacciones.add(iCondiciones);
        menuTransacciones.add(iImpuestos);
        menuTransacciones.add(iCambioM);
        menuTransacciones.add(iUnidad);

        menuGeneral.add(menuInfoMaestra);
        menuGeneral.add(menuTransacciones);

        if (SesionUsuario.esAdministrador()) {
            javax.swing.JMenuItem historial = new javax.swing.JMenuItem("Historial de cambios");
            menuInfoMaestra.add(historial);
            historial.addActionListener(e -> abrirHistorialCambios());
        }

        // Acciones
        iCompanias.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Companias(), "Catálogo de Compañías"));
        iCentros.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Centros_costos(), "Catálogo de Centros de Costo"));
        iParametros.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Parametros_Compañia(), "Parametros por Compañia"));
        iFiscal.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Ejercicio_Fiscal(), "Ejercicio Fiscal"));
        iEmpleados.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Catalogo_Empleados(), "Catálogo de Empleados"));
        iDirecciones.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Direcciones_Entrega(), "Direcciones de Entrega"));
        iClasificaciones.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Clasificaciones(), "Tabla de Clasificaciones"));
        iContactos.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Catalogo_Contactos(), "Catálogo de Contactos"));

        iCambioD.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Tipos_Cambio_Diario(), "Tipos de Cambio Diario"));
        iCondiciones.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Condiciones_Pago(), "Condiciones de Pago"));
        iImpuestos.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Tipos_Impuestos(), "Clave de Impuestos"));
        iCambioM.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Tipo_Cambio(), "Tipo de Cambio Mensual"));
        iUnidad.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Factores_Convercion_Unidad(), "Convertir Unidad de Medida"));
        configurarMenuArbol(btnInfoMaestra, menuGeneral);
    }

    private void configurarMenuContabilidad() {
        btnTransacciones.setText("Contabilidad"); // Cambiamos el nombre del botón
        javax.swing.JPopupMenu menuContab = new javax.swing.JPopupMenu();

        javax.swing.JMenu m20 = new javax.swing.JMenu("20 Información Maestra");
        javax.swing.JMenu m30 = new javax.swing.JMenu("30 Transacciones");
        javax.swing.JMenu m40 = new javax.swing.JMenu("40 Consultas");
        javax.swing.JMenu m50 = new javax.swing.JMenu("50 Reportes");
        javax.swing.JMenu m60 = new javax.swing.JMenu("60 Cierre Mensual");
        javax.swing.JMenu m70 = new javax.swing.JMenu("70 Cierre Anual");
        javax.swing.JMenu m80 = new javax.swing.JMenu("80 Utilerías");

        // 20
        javax.swing.JMenuItem iCtas = new javax.swing.JMenuItem("Catálogo de Cuentas");
        m20.add(iCtas);
        // 30
        javax.swing.JMenuItem iPolizas = new javax.swing.JMenuItem("Captura de Pólizas");
        m30.add(iPolizas);
        // 40
        javax.swing.JMenuItem iAux = new javax.swing.JMenuItem("Auxiliar de Movimientos");
        javax.swing.JMenuItem iPolDia = new javax.swing.JMenuItem("Póliza de Diario");
        m40.add(iAux);
        m40.add(iPolDia);
        // 50
        javax.swing.JMenuItem iBalanza = new javax.swing.JMenuItem("Balanza de Comprobación");
        javax.swing.JMenuItem iBalanzaSsc = new javax.swing.JMenuItem("Balanza de Comprobación Ssc");
        javax.swing.JMenuItem iBalance = new javax.swing.JMenuItem("Balance por Compañía");
        javax.swing.JMenuItem iResGpo = new javax.swing.JMenuItem("Estado de Resultados x Grupo");
        javax.swing.JMenuItem iRes = new javax.swing.JMenuItem("Estado de Resultados");
        javax.swing.JMenuItem iLDiario = new javax.swing.JMenuItem("Libro de Diario");
        javax.swing.JMenuItem iLMayor = new javax.swing.JMenuItem("Libro Mayor");
        javax.swing.JMenuItem iImpPol = new javax.swing.JMenuItem("Impresión de Póliza");
        m50.add(iBalanza);
        m50.add(iBalanzaSsc);
        m50.add(iBalance);
        m50.add(iResGpo);
        m50.add(iRes);
        m50.add(iLDiario);
        m50.add(iLMayor);
        m50.add(iImpPol);
        // 60, 70, 80
        javax.swing.JMenuItem iCMes = new javax.swing.JMenuItem("Cierre de Mes");
        m60.add(iCMes);
        javax.swing.JMenuItem iCAnual = new javax.swing.JMenuItem("Cierre Anual");
        m70.add(iCAnual);
        javax.swing.JMenuItem iAct = new javax.swing.JMenuItem("Actualización de Saldos");
        m80.add(iAct);
        javax.swing.JMenuItem iFel = new javax.swing.JMenuItem("FEL");
        m80.add(iFel);

        menuContab.add(m20);
        menuContab.add(m30);
        menuContab.add(m40);
        menuContab.add(m50);
        menuContab.add(m60);
        menuContab.add(m70);
        menuContab.add(m80);

        // Acciones
        iCtas.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Catalogo_Cuentas(), "Cátalogo de Cuentas"));
        iPolizas.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Captura_Polizas(), "Captura de Pólizas"));
        iAux.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Auxiliar_Movimientos(), "Auxiliar de Movimientos"));
        iPolDia.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Poliza_Diario(), "Póliza de Diario"));
        iBalanza.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Balanza_Comprobacion(), "Balanza de Comprobación"));
        iBalanzaSsc.addActionListener(e -> javax.swing.JOptionPane.showMessageDialog(this, "Módulo en construcción...")); // FALTANTE
        iBalance.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Balance_Compañia(), "Balance por Compañía"));
        iResGpo.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Resultado_Grupo(), "Estado de Resultados x Grupo"));
        iRes.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Estado_Resultados(), "Estado de Resultados"));
        iLDiario.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Libro_Diario(), "Libro de Diario"));
        iLMayor.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Libro_Mayor(), "Libro Mayor"));
        iImpPol.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Impresion_Poliza(), "Impresión de Póliza"));
        iCMes.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Cierre_Mes(), "Cierre de Mes"));
        iCAnual.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Cierre_Anual(), "Cierre Anual"));
        iAct.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Actualizacion_Saldo(), "Actualización de Saldos"));
        iFel.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.FEL(), "Actualización de Saldos"));
        configurarMenuArbol(btnTransacciones, menuContab);
    }

    private void configurarMenuEscolar() {
        btnEscolar.setText("Escolar");
        javax.swing.JPopupMenu menuEscolar = new javax.swing.JPopupMenu();

        javax.swing.JMenu m10 = new javax.swing.JMenu("10 Configuración");
        javax.swing.JMenu m20 = new javax.swing.JMenu("20 Información Maestra");
        javax.swing.JMenu m30 = new javax.swing.JMenu("30 Transacciones");
        javax.swing.JMenu m40 = new javax.swing.JMenu("40 Consultas");
        javax.swing.JMenu m50 = new javax.swing.JMenu("50 Reportes");
        javax.swing.JMenu m80 = new javax.swing.JMenu("80 Utilerías"); // Nuevo Submenú

        // 10
        javax.swing.JMenuItem iCiclos = new javax.swing.JMenuItem("Ciclos Escolares");
        javax.swing.JMenuItem iGrados = new javax.swing.JMenuItem("Grados Escolares");
        javax.swing.JMenuItem iBecas = new javax.swing.JMenuItem("Registro de Becas y Convenios");
        javax.swing.JMenuItem iConEsc = new javax.swing.JMenuItem("Conceptos Escolares");
        javax.swing.JMenuItem iPlanes = new javax.swing.JMenuItem("Planes de Pago");
        m10.add(iCiclos);
        m10.add(iGrados);
        m10.add(iBecas);
        m10.add(iConEsc);
        m10.add(iPlanes);

        // 20
        javax.swing.JMenuItem iCtasBan = new javax.swing.JMenuItem("Cuentas Bancarias");
        javax.swing.JMenuItem iTiposAl = new javax.swing.JMenuItem("Catalogo Tipos de Alumno");
        javax.swing.JMenuItem iCajeros = new javax.swing.JMenuItem("Catalogo de Cajeros");
        javax.swing.JMenuItem iAlumnos = new javax.swing.JMenuItem("Catalogo de Alumnos");
        m20.add(iCtasBan);
        m20.add(iTiposAl);
        m20.add(iCajeros);
        m20.add(iAlumnos);

        // 30
        javax.swing.JMenuItem iCobranza = new javax.swing.JMenuItem("Cobranza Escolar");
        javax.swing.JMenuItem iCorte = new javax.swing.JMenuItem("Corte de Caja");
        javax.swing.JMenuItem iFacturas = new javax.swing.JMenuItem("Impresión de Facturas");
        javax.swing.JMenuItem iDep = new javax.swing.JMenuItem("Registro de Depositos");
        javax.swing.JMenuItem iCanCon = new javax.swing.JMenuItem("Cancelación de Conceptos");
        javax.swing.JMenuItem iCanRec = new javax.swing.JMenuItem("Cancelación de Recibos");
        javax.swing.JMenuItem iCanCorte = new javax.swing.JMenuItem("Cancela Contabilizacion de Corte de Caja");
        javax.swing.JMenuItem iCanDep = new javax.swing.JMenuItem("Cancela Contabilizacion de Reg Deposito");
        m30.add(iCobranza);
        m30.add(iCorte);
        m30.add(iFacturas);
        m30.add(iDep);
        m30.add(iCanCon);
        m30.add(iCanRec);
        m30.add(iCanCorte);
        m30.add(iCanDep);

        // 40
        javax.swing.JMenuItem iEdoAl = new javax.swing.JMenuItem("Estado de Cuenta Alumnos");
        javax.swing.JMenuItem iConsPag = new javax.swing.JMenuItem("Consulta de Pagos");
        javax.swing.JMenuItem iEdoDet = new javax.swing.JMenuItem("Estado de Cuenta Detallado");
        m40.add(iEdoAl);
        m40.add(iConsPag);
        m40.add(iEdoDet);

        // 50 - Reportes
        javax.swing.JMenuItem iResIns = new javax.swing.JMenuItem("Resumen de Inscripciones");
        javax.swing.JMenuItem iResNoIns = new javax.swing.JMenuItem("Resumen de Alumnos No Inscritos");
        javax.swing.JMenuItem iReiRec = new javax.swing.JMenuItem("Reimpresión de Recibos");
        javax.swing.JMenuItem iReiFac = new javax.swing.JMenuItem("Reimpresión de Facturas");
        javax.swing.JMenuItem iResCon = new javax.swing.JMenuItem("Resumen de Conceptos Pagados");
        javax.swing.JMenuItem iDetCon = new javax.swing.JMenuItem("Detalle de Conceptos Pagados");
        javax.swing.JMenuItem iAlumBec = new javax.swing.JMenuItem("Imprime Alumnos Becados"); // Agregado
        javax.swing.JMenuItem iIngPer = new javax.swing.JMenuItem("Ingresos por Periodo");
        m50.add(iResIns);
        m50.add(iResNoIns);
        m50.add(iReiRec);
        m50.add(iReiFac);
        m50.add(iResCon);
        m50.add(iDetCon);
        m50.add(iAlumBec);
        m50.add(iIngPer);

        // 80 - Utilerías (Nuevo)
        javax.swing.JMenuItem iProcRef = new javax.swing.JMenuItem("Procesa Referencias Bancarias BCM");
        javax.swing.JMenuItem iGenerarRef = new javax.swing.JMenuItem("Generar Referencias Bancarias");
        m80.add(iGenerarRef);
        iGenerarRef.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Generar_Referencias_Bancarias(getUsuarioCodigo()), "Generar Referencias Bancarias"));
        javax.swing.JMenuItem iIntBcm = new javax.swing.JMenuItem("Interfase Bancaria BCM");
        javax.swing.JMenuItem iIntFel = new javax.swing.JMenuItem("Interfase Factura FEL");
        javax.swing.JMenuItem iIntFelGlobal = new javax.swing.JMenuItem("Interfase Factura Global FEL");
        javax.swing.JMenuItem iSubCla = new javax.swing.JMenuItem("Subclasificaciones");
        m80.add(iProcRef);
        m80.add(iIntBcm);
        m80.add(iIntFel);
        m80.add(iIntFelGlobal);
        m80.add(iSubCla);

        menuEscolar.add(m10);
        menuEscolar.add(m20);
        menuEscolar.add(m30);
        menuEscolar.add(m40);
        menuEscolar.add(m50);
        menuEscolar.add(m80);

        // Acciones de Reportes
        iCiclos.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Ciclos_Escolares(), "Ciclos Escolares"));
        iGrados.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Grados_Escolares(), "Grados Escolares"));
        iBecas.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Becas_Convenio(), "Registro de Becas y Convenios"));
        iConEsc.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Conceptos_Escolares(), "Conceptos Escolares"));
        iPlanes.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Planes_Pago(getUsuarioCodigo()), "Planes de Pago"));

        iCtasBan.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Catalogo_Cuentas_Bancarias(), "Cuentas Bancarias"));
        iTiposAl.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Catalogo_Tipo_Alumno(), "Catalogo Tipos de Alumno"));
        iCajeros.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Catalogo_Cajeros(), "Catalogo de Cajeros"));
        iAlumnos.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Catalogo_Alumnos(getUsuarioCodigo()), "Catalogo de Alumnos"));

        iCobranza.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Cobranza_Escolar(getUsuarioCodigo()), "Cobranza Escolar"));
        iCorte.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Corte_Caja(), "Corte de Caja"));
        iFacturas.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Impresion_Facturas(), "Impresión de Facturas"));
        iDep.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Registro_Depositos(), "Registro de Depositos"));
        iCanCon.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Cancelacion_Conceptos(), "Cancelación de Conceptos"));
        iCanRec.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Cancelacion_Recibos(getUsuarioCodigo()), "Cancelación de Recibos"));
        iCanCorte.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Cancela_Contabilizacion_Corte(), "Cancela Contabilizacion de Corte de Caja"));
        iCanDep.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Cancela_Contabilizacion_Deposito(), "Cancela Contabilizacion de Reg Deposito"));

        iEdoAl.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Estado_Cuenta_Detallado(getUsuarioCodigo()), "Estado de Cuenta Alumnos"));
        iConsPag.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Consulta_Pagos(), "Consulta de Pagos"));
        iEdoDet.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Estado_Cuenta_Detallado(getUsuarioCodigo()), "Estado de Cuenta Detallado"));

        iResIns.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Resumen_Inscripciones(), "Resumen de Inscripciones"));
        iResNoIns.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Resumen_Alumnos_No_Inscritos(), "Resumen de Alumnos No Inscritos"));
        iReiRec.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Reeimpresion_Recibos(), "Reimpresión de Recibos"));
        iReiFac.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Reeimpresion_Facturas(), "Reimpresión de Facturas"));
        iResCon.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Resumen_Conceptos_Pagados(), "Resumen de Conceptos Pagados"));
        iDetCon.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Detalle_Conceptos_Pagos(), "Detalle de Conceptos Pagados"));
        iAlumBec.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Alumnos_Becados(), "Imprime Alumnos Becados")); // Acción nueva
        iIngPer.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Ingresos_Periodo(), "Ingresos por Periodo"));

        // Acciones de Utilerías (Nuevas)
        iProcRef.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Procesa_Referencias_Bancarias(), "Procesa Referencias Bancarias BCM"));
        iIntBcm.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Interfase_Bancaria_BCM(getUsuarioCodigo()), "Interfase Bancaria BCM"));
        iIntFel.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.FEL(), "Interfase Factura FEL"));
        iIntFelGlobal.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.FEL_Global(), "Interfase Factura Global FEL"));
        configurarMenuArbol(btnEscolar, menuEscolar);
    }

    private void configurarFechaYHora() {
        // --- 1. CONFIGURAR LA FECHA ESTATICA (lblDate) ---
        java.time.LocalDate fechaActual = java.time.LocalDate.now();

        // Creamos el formato en español. 
        // EEEE = Día de la semana, dd = número, MMMM = mes en texto, yyyy = año
        java.time.format.DateTimeFormatter formatoFecha = java.time.format.DateTimeFormatter.ofPattern(
                "'Hoy es' EEEE dd 'de' MMMM 'del' yyyy",
                new java.util.Locale("es", "MX") // Forzamos el idioma español
        );

        // Al aplicar el formato, la primera letra del día suele salir en minúscula. 
        // Este pequeño truco pone la primera letra en mayúscula para que se vea mejor:
        String fechaFormateada = fechaActual.format(formatoFecha);
        fechaFormateada = fechaFormateada.substring(0, 7)
                + fechaFormateada.substring(7, 8).toUpperCase()
                + fechaFormateada.substring(8);

        lblDate.setText(fechaFormateada);

        // --- 2. CONFIGURAR EL RELOJ EN VIVO (lblTime) ---
        // Creamos un temporizador que se repite cada 1000 milisegundos (1 segundo)
        javax.swing.Timer timerReloj = new javax.swing.Timer(1000, e -> {
            java.time.LocalTime horaActual = java.time.LocalTime.now();

            // Formato de 12 horas con AM/PM (Ejemplo: "02:15:30 PM")
            // Si prefieres formato de 24 horas, cambia "hh:mm:ss a" por "HH:mm:ss"
            java.time.format.DateTimeFormatter formatoHora = java.time.format.DateTimeFormatter.ofPattern("hh:mm:ss a");

            lblTime.setText(horaActual.format(formatoHora));
        });

        // Iniciamos el reloj
        timerReloj.start();
    }

    private void configurarMensajeBienvenida() {
        // Obtenemos la hora actual de la computadora
        java.time.LocalTime horaActual = java.time.LocalTime.now();
        int hora = horaActual.getHour(); // Formato 24 hrs (0 a 23)
        String saludo;

        // Calculamos el saludo
        if (hora >= 5 && hora < 12) {
            saludo = "Buenos días";
        } else if (hora >= 12 && hora < 19) {
            saludo = "Buenas tardes";
        } else {
            saludo = "Buenas noches";
        }

        // Actualizamos la etiqueta (lblMessage)
        lblMessage.setText(saludo + ", " + usuarioActual);
    }

    private void initContent() {
        Principal pl = new Principal();

        // 1. ELIMINAR el tamaño fijo y la posición. El Layout se encargará de esto.
        // pl.setSize(750,430); 
        // pl.setLocation(0, 0);
        // 2. FORZAR a jpContainer a usar BorderLayout para que el panel se expanda
        jpContainer.setLayout(new java.awt.BorderLayout());

        // 3. Limpiar, agregar en el CENTRO, y recargar la vista
        jpContainer.removeAll();
        jpContainer.add(pl, java.awt.BorderLayout.CENTER);
        jpContainer.revalidate();
        jpContainer.repaint();

        mostrarPanel(pl, "Principal");
    }

    // Este método recibe cualquier JPanel y lo muestra en tu jpContainer
    private void mostrarPanel(javax.swing.JPanel p, String titulo) {
        jpContainer.removeAll();
        jpContainer.add(p, java.awt.BorderLayout.CENTER);
        jpContainer.revalidate();
        jpContainer.repaint();

        lblTitleInfo.setText(titulo);
    }

    private void configurarBuscador() {
        menuResultadosBusqueda.setFocusable(false);
        txtSearch.putClientProperty("JTextField.placeholderText", "Buscar una opción...");
        imgSearch.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        imgSearch.setToolTipText("Buscar opciones");
        imgSearch.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (javax.swing.SwingUtilities.isLeftMouseButton(e)) {
                    txtSearch.requestFocusInWindow();
                    buscarOpciones(txtSearch.getText());
                }
            }
        });

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                buscarOpciones(txtSearch.getText());
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                buscarOpciones(txtSearch.getText());
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                buscarOpciones(txtSearch.getText());
            }
        });

        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {

                int key = evt.getKeyCode();

                if (key == java.awt.event.KeyEvent.VK_DOWN) {
                    evt.consume();

                    if (menuResultadosBusqueda.isVisible()
                            && menuResultadosBusqueda.getComponentCount() > 0) {

                        indiceSeleccionadoBusqueda++;

                        if (indiceSeleccionadoBusqueda >= menuResultadosBusqueda.getComponentCount()) {
                            indiceSeleccionadoBusqueda = 0;
                        }

                        seleccionarItemBusqueda(indiceSeleccionadoBusqueda);
                    }
                }

                if (key == java.awt.event.KeyEvent.VK_UP) {
                    evt.consume();

                    if (menuResultadosBusqueda.isVisible()
                            && menuResultadosBusqueda.getComponentCount() > 0) {

                        indiceSeleccionadoBusqueda--;

                        if (indiceSeleccionadoBusqueda < 0) {
                            indiceSeleccionadoBusqueda = menuResultadosBusqueda.getComponentCount() - 1;
                        }

                        seleccionarItemBusqueda(indiceSeleccionadoBusqueda);
                    }
                }

                if (key == java.awt.event.KeyEvent.VK_ENTER) {
                    evt.consume();

                    if (menuResultadosBusqueda.isVisible()
                            && menuResultadosBusqueda.getComponentCount() > 0) {

                        if (indiceSeleccionadoBusqueda < 0) {
                            indiceSeleccionadoBusqueda = 0;
                        }

                        java.awt.Component comp = menuResultadosBusqueda.getComponent(indiceSeleccionadoBusqueda);

                        if (comp instanceof javax.swing.JMenuItem item) {
                            item.doClick();
                        }
                    }
                }

                if (key == java.awt.event.KeyEvent.VK_ESCAPE) {
                    menuResultadosBusqueda.setVisible(false);
                    indiceSeleccionadoBusqueda = -1;
                }
            }
        });
    }

    private void buscarOpciones(String texto) {
        // 1. Ocultamos y limpiamos el menú para reconstruirlo desde cero y evitar huecos
        menuResultadosBusqueda.setVisible(false);
        menuResultadosBusqueda.removeAll();
        indiceSeleccionadoBusqueda = -1;

        texto = texto.trim();

        if (texto.isEmpty()) {
            return; // Si no hay texto, se queda oculto
        }

        String textoBusqueda = texto;
        int coincidencias = 0;

        for (java.util.Map.Entry<String, Runnable> opcion : opcionesSistema.entrySet()) {
            String nombreOpcion = opcion.getKey();

            if (coincideBusqueda(nombreOpcion, textoBusqueda)) {
                javax.swing.JMenuItem itemResultado = new javax.swing.JMenuItem(nombreOpcion);
                itemResultado.setFocusable(false);

                // Le damos un pequeño margen para que no se vea amontonado
                itemResultado.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10));

                itemResultado.addActionListener(e -> {
                    opcion.getValue().run();
                    txtSearch.setText("");
                    menuResultadosBusqueda.setVisible(false);
                    indiceSeleccionadoBusqueda = -1;

                    javax.swing.SwingUtilities.invokeLater(() -> {
                        txtSearch.requestFocusInWindow();
                    });
                });

                menuResultadosBusqueda.add(itemResultado);
                coincidencias++;

                // TRUCO 1: Limitamos a 8 resultados máximo para que el menú no crezca 
                // tanto que Java decida voltearlo hacia arriba tapando tu buscador.
                if (coincidencias >= 8) {
                    break;
                }
            }
        }

        if (coincidencias > 0) {
            // TRUCO 2: pack() obliga al menú a encogerse al tamaño exacto de sus items.
            // Esto elimina todo el espacio en blanco gigante que tenías.
            menuResultadosBusqueda.pack();

            // TRUCO 3: Obligamos al menú a tener el mismo ancho exacto que la barra de búsqueda txtSearch
            menuResultadosBusqueda.setPopupSize(txtSearch.getWidth(), menuResultadosBusqueda.getPreferredSize().height);

            // Mostramos el menú 2 pixeles por debajo del buscador para que no lo tape
            if (txtSearch.isShowing()) menuResultadosBusqueda.show(txtSearch, 0, txtSearch.getHeight() + 2);

            javax.swing.SwingUtilities.invokeLater(() -> {
                txtSearch.requestFocusInWindow();
            });

        } else {
            javax.swing.JMenuItem sinResultados = new javax.swing.JMenuItem("Sin opciones disponibles");
            sinResultados.setEnabled(false);
            menuResultadosBusqueda.add(sinResultados);
            menuResultadosBusqueda.setPopupSize(txtSearch.getWidth(), menuResultadosBusqueda.getPreferredSize().height);
            if (txtSearch.isShowing()) menuResultadosBusqueda.show(txtSearch, 0, txtSearch.getHeight() + 2);
        }
    }

    /** Busca sin distinguir acentos, mayúsculas ni espacios repetidos. */
    static boolean coincideBusqueda(String nombre, String texto) {
        return com.mycompany.benaedu.db.PermisosUsuario.identificador(nombre)
                .contains(com.mycompany.benaedu.db.PermisosUsuario.identificador(texto));
    }

    /** Comparte con el buscador las acciones ya filtradas y protegidas del menú. */
    static void indexarOpciones(javax.swing.JPopupMenu menu, java.util.Map<String, Runnable> opciones) {
        for (java.awt.Component componente : menu.getComponents()) {
            if (componente instanceof javax.swing.JMenu categoria) {
                indexarOpciones(categoria.getPopupMenu(), opciones);
            } else if (componente instanceof javax.swing.JMenuItem item && item.isEnabled()) {
                opciones.put(item.getText(), item::doClick);
            }
        }
    }

    private void abrirHistorialCambios() {
        try (var con = com.mycompany.benaedu.db.ConDB.getConnection()) {
            com.mycompany.benaedu.db.Auditoria.exigirAdministrador(con);
            mostrarPanel(new com.mycompany.benaedu.views.Historial_Cambios(), "Historial de cambios");
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex.getMessage(), "Historial", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarItemBusqueda(int indice) {

        for (int i = 0; i < menuResultadosBusqueda.getComponentCount(); i++) {

            java.awt.Component comp = menuResultadosBusqueda.getComponent(i);

            if (comp instanceof javax.swing.JMenuItem item) {

                if (i == indice) {
                    item.setArmed(true);
                    item.setBackground(new java.awt.Color(95, 143, 255));
                    item.setForeground(java.awt.Color.WHITE);
                } else {
                    item.setArmed(false);
                    item.setBackground(java.awt.Color.WHITE);
                    item.setForeground(java.awt.Color.BLACK);
                }
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    
//
    
    private void AplicarDiseñoPersonalizado() {
        java.awt.Color fondo = new java.awt.Color(247, 249, 252);
        java.awt.Color tarjeta = new java.awt.Color(255, 255, 255);

        java.awt.Color azulSidebar = new java.awt.Color(11, 49, 91);
        java.awt.Color azulTexto = new java.awt.Color(18, 52, 91);
        java.awt.Color azulPrincipal = new java.awt.Color(27, 78, 125);

        java.awt.Color rojoActivo = new java.awt.Color(229, 30, 42);
        java.awt.Color rojoHover = new java.awt.Color(195, 24, 35);

        java.awt.Color textoSecundario =
            new java.awt.Color(100, 116, 139);

        java.awt.Color borde =
            new java.awt.Color(226, 232, 240);
    
        //
        background.setOpaque(true);
        background.setBackground(fondo);

        jpMenu.setOpaque(true);
        jpMenu.setBackground(azulSidebar);

        jpContainer.setOpaque(true);
        jpContainer.setBackground(tarjeta);
        jpContainer.setBorder(
            javax.swing.BorderFactory.createLineBorder(
                borde, 1, true
            )
        );
        
        //
        lblTitleInfo.setForeground(azulTexto);
        lblTitleInfo.setFont(
        new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 20)
        );

        lblMessage.setForeground(azulTexto);
        lblMessage.setFont(
            new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24)
        );

        lblDate.setForeground(azulTexto);
        lblDate.setFont(
            new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 16)
        );

        lblTime.setForeground(azulTexto);
        lblTime.setFont(
            new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 16)
        );
        
        //
        txtSearch.setBackground(java.awt.Color.WHITE);
        txtSearch.setForeground(azulTexto);
        txtSearch.setCaretColor(azulTexto);
        txtSearch.setFont(
        new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14)
        );
        txtSearch.setBorder(
            javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(
                        borde, 1, true
                ),
                javax.swing.BorderFactory.createEmptyBorder(
                        8, 12, 8, 12
                )
            )
        );
       
    //
        estilizarBotonMenu(btnInfoMaestra);
        estilizarBotonMenu(btnTransacciones);
        estilizarBotonMenu(btnEscolar);
    }
    
    //
    static void estilizarBotonMenu(
        javax.swing.JButton boton) {

    java.awt.Color colorNormal =
            new java.awt.Color(11, 49, 91);

    java.awt.Color colorHover =
            new java.awt.Color(25, 73, 120);

    java.awt.Color colorActivo =
            new java.awt.Color(229, 30, 42);

    java.awt.Color colorActivoHover =
            new java.awt.Color(195, 24, 35);

    boton.setBackground(colorNormal);
    boton.setForeground(java.awt.Color.WHITE);
    boton.setFont(
            new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14)
    );
    boton.setFocusPainted(false);
    boton.setContentAreaFilled(false);
    boton.setOpaque(false);
    boton.setRolloverEnabled(true);
    boton.setHorizontalAlignment(
            javax.swing.SwingConstants.LEFT
    );
    boton.setBorder(
            javax.swing.BorderFactory.createEmptyBorder(
                    12, 18, 12, 18
            )
    );
    boton.setCursor(
            new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR)
    );

    boton.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
        @Override
        public void paint(
                java.awt.Graphics g,
                javax.swing.JComponent componente) {

            javax.swing.AbstractButton botonActual =
                    (javax.swing.AbstractButton) componente;

            java.awt.Graphics2D g2 =
                    (java.awt.Graphics2D) g.create();

            g2.setRenderingHint(
                    java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON
            );

            java.awt.Color colorActual;

            if (Boolean.TRUE.equals(botonActual.getClientProperty("menuAbierto"))) {
                colorActual = botonActual.getModel().isRollover()
                        ? colorActivoHover
                        : colorActivo;
            } else {
                colorActual = botonActual.getModel().isRollover()
                        ? colorHover
                        : colorNormal;
            }

            g2.setColor(colorActual);
            g2.fillRoundRect(
                    0,
                    0,
                    componente.getWidth(),
                    componente.getHeight(),
                    24,
                    24
            );

            g2.dispose();

            super.paint(g, componente);
        }
    });
}
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        background = new javax.swing.JPanel();
        jpMenu = new javax.swing.JPanel();
        btnInfoMaestra = new javax.swing.JButton();
        btnTransacciones = new javax.swing.JButton();
        btnEscolar = new javax.swing.JButton();
        lblLogo = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jpContainer = new javax.swing.JPanel();
        lblTitleInfo = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        imgSearch = new javax.swing.JLabel();
        lblDate = new javax.swing.JLabel();
        lblTime = new javax.swing.JLabel();
        lblMessage = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        setMinimumSize(new java.awt.Dimension(1020, 664));

        background.setBackground(new java.awt.Color(255, 255, 255));

        jpMenu.setBackground(new java.awt.Color(13, 41, 72));
        jpMenu.setPreferredSize(new java.awt.Dimension(270, 640));

        btnInfoMaestra.setBackground(new java.awt.Color(13, 41, 72));
        btnInfoMaestra.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnInfoMaestra.setForeground(new java.awt.Color(220, 229, 239));
        btnInfoMaestra.setIcon(new javax.swing.ImageIcon(getClass().getResource("/HomeW.png"))); // NOI18N
        btnInfoMaestra.setText("Informacion Maestra");
        btnInfoMaestra.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
        btnInfoMaestra.setBorderPainted(false);
        btnInfoMaestra.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnInfoMaestra.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnInfoMaestra.setIconTextGap(10);
        btnInfoMaestra.addActionListener(this::btnInfoMaestraActionPerformed);

        btnTransacciones.setBackground(new java.awt.Color(13, 41, 72));
        btnTransacciones.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnTransacciones.setForeground(new java.awt.Color(220, 229, 239));
        btnTransacciones.setIcon(new javax.swing.ImageIcon(getClass().getResource("/trasnsactionW.png"))); // NOI18N
        btnTransacciones.setText("Contabilidad");
        btnTransacciones.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
        btnTransacciones.setBorderPainted(false);
        btnTransacciones.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnTransacciones.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnTransacciones.setIconTextGap(10);

        btnEscolar.setBackground(new java.awt.Color(13, 41, 72));
        btnEscolar.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        btnEscolar.setForeground(new java.awt.Color(220, 229, 239));
        btnEscolar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/MenuBookW.png"))); // NOI18N
        btnEscolar.setText("Escolar");
        btnEscolar.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
        btnEscolar.setBorderPainted(false);
        btnEscolar.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnEscolar.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnEscolar.setIconTextGap(10);

        lblLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Logo12.png"))); // NOI18N

        jLabel1.setFont(new java.awt.Font("Sans Serif Collection", 0, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("BENAEDU");

        jLabel2.setForeground(new java.awt.Color(220, 229, 239));
        jLabel2.setText("SISTEMA DE GESTIÓN");

        jLabel3.setForeground(new java.awt.Color(220, 229, 239));
        jLabel3.setText("OPERATIVO");

        javax.swing.GroupLayout jpMenuLayout = new javax.swing.GroupLayout(jpMenu);
        jpMenu.setLayout(jpMenuLayout);
        jpMenuLayout.setHorizontalGroup(
            jpMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpMenuLayout.createSequentialGroup()
                .addGroup(jpMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpMenuLayout.createSequentialGroup()
                        .addGap(95, 95, 95)
                        .addComponent(jLabel3))
                    .addGroup(jpMenuLayout.createSequentialGroup()
                        .addGap(75, 75, 75)
                        .addGroup(jpMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpMenuLayout.createSequentialGroup()
                .addContainerGap(12, Short.MAX_VALUE)
                .addGroup(jpMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpMenuLayout.createSequentialGroup()
                        .addGroup(jpMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnInfoMaestra, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnTransacciones, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnEscolar, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(20, 20, 20))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpMenuLayout.createSequentialGroup()
                        .addComponent(lblLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        jpMenuLayout.setVerticalGroup(
            jpMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpMenuLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addComponent(btnInfoMaestra, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnTransacciones, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnEscolar, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(221, Short.MAX_VALUE))
        );

        jpContainer.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jpContainerLayout = new javax.swing.GroupLayout(jpContainer);
        jpContainer.setLayout(jpContainerLayout);
        jpContainerLayout.setHorizontalGroup(
            jpContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jpContainerLayout.setVerticalGroup(
            jpContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 477, Short.MAX_VALUE)
        );

        lblTitleInfo.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblTitleInfo.setForeground(new java.awt.Color(13, 41, 72));
        lblTitleInfo.setText("Titulo");

        txtSearch.setBackground(new java.awt.Color(179, 207, 229));
        txtSearch.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtSearch.setForeground(new java.awt.Color(26, 61, 99));
        txtSearch.setToolTipText("");

        imgSearch.setForeground(new java.awt.Color(91, 173, 197));
        imgSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/searchA.png"))); // NOI18N

        lblDate.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblDate.setForeground(new java.awt.Color(13, 41, 72));
        lblDate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/calendar_monthA.png"))); // NOI18N
        lblDate.setText("Hoy es {dayname} de {month} del {year}  ");
        lblDate.setIconTextGap(10);

        lblTime.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        lblTime.setForeground(new java.awt.Color(13, 41, 72));
        lblTime.setIcon(new javax.swing.ImageIcon(getClass().getResource("/clockA.png"))); // NOI18N
        lblTime.setText("Hora");
        lblTime.setIconTextGap(10);

        lblMessage.setBackground(new java.awt.Color(0, 0, 0));
        lblMessage.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        lblMessage.setForeground(new java.awt.Color(13, 41, 72));
        lblMessage.setText("¡Buenas Tardes, Usuario!");

        javax.swing.GroupLayout backgroundLayout = new javax.swing.GroupLayout(background);
        background.setLayout(backgroundLayout);
        backgroundLayout.setHorizontalGroup(
            backgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundLayout.createSequentialGroup()
                .addComponent(jpMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(backgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(backgroundLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jpContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(backgroundLayout.createSequentialGroup()
                        .addGroup(backgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(backgroundLayout.createSequentialGroup()
                                .addGap(40, 40, 40)
                                .addGroup(backgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(backgroundLayout.createSequentialGroup()
                                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(28, 28, 28)
                                        .addComponent(imgSearch))
                                    .addGroup(backgroundLayout.createSequentialGroup()
                                        .addComponent(lblTitleInfo)
                                        .addGap(486, 486, 486)
                                        .addGroup(backgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(lblDate)
                                            .addComponent(lblTime)))))
                            .addGroup(backgroundLayout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addComponent(lblMessage)))
                        .addContainerGap())))
        );
        backgroundLayout.setVerticalGroup(
            backgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundLayout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addGroup(backgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblMessage)
                    .addComponent(lblDate))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(backgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTitleInfo)
                    .addComponent(lblTime))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(backgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(backgroundLayout.createSequentialGroup()
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addComponent(jpContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(backgroundLayout.createSequentialGroup()
                        .addComponent(imgSearch)
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addComponent(jpMenu, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 664, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(background, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(background, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnInfoMaestraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInfoMaestraActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnInfoMaestraActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        FlatMTMaterialLighterIJTheme.setup();

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            if (SesionUsuario.usuario().isBlank()) new Login().setVisible(true);
            else new Dashboard().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel background;
    private javax.swing.JButton btnEscolar;
    private javax.swing.JButton btnInfoMaestra;
    private javax.swing.JButton btnTransacciones;
    private javax.swing.JLabel imgSearch;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jpContainer;
    private javax.swing.JPanel jpMenu;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblMessage;
    private javax.swing.JLabel lblTime;
    private javax.swing.JLabel lblTitleInfo;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
