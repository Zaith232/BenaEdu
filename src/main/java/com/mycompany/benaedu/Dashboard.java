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
    
    private int indiceSeleccionadoBusqueda = -1;
    
    public Dashboard(String nombreUsuario) {
        this.usuarioActual = nombreUsuario; // Guardamos el nombre
        initComponents();
        InitStyles();
        configurarMenuDesplegable();
        configurarFechaYHora();
        configurarMensajeBienvenida(); // Llamamos al nuevo método
        initContent();
        configurarBuscador();
        configurarMenuTransacciones();
        configurarMenuEscolar();
    }

    /**
     * Creates new form Dashboard
     */
    public Dashboard() {
        initComponents();
        InitStyles();
        configurarMenuDesplegable();
        configurarFechaYHora();
        configurarMensajeBienvenida();
        initContent();
        configurarBuscador();
        configurarMenuTransacciones();
        configurarMenuEscolar();
    }

    private void InitStyles() {
        lblMessage.putClientProperty("FlatLaf.style", "font: bold 30");
        lblMessage.setForeground(new Color(179,207,229));
        lblDate.putClientProperty("FlatLaf.style", "font: bold 30 ");
        lblDate.setForeground(new Color(179,207,229));
        lblTime.putClientProperty("FlatLaf.style", "font: bold 30 ");
        lblTime.setForeground(new Color(179,207,229));
        lblTitleInfo.putClientProperty("FlatLaf.style", "font: bold 24");
        lblTitleInfo.setForeground(new Color(179,207,229));

    }

   private void configurarMenuDesplegable() {
        // 1. Crear el contenedor del menú emergente
        javax.swing.JPopupMenu menuInfoMaestra = new javax.swing.JPopupMenu();

        // 2. Crear las opciones correctas
        javax.swing.JMenuItem itemCompanias = new javax.swing.JMenuItem("Catálogo de Compañías");
        itemCompanias.setIcon(new javax.swing.ImageIcon(getClass().getResource("/compania.png")));
        javax.swing.JMenuItem itemCentrosCosto = new javax.swing.JMenuItem("Catálogo de Centros de Costo");
        itemCentrosCosto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/costos.png")));
        javax.swing.JMenuItem itemEjercicioFiscal = new javax.swing.JMenuItem("Ejercicio Fiscal");
        itemEjercicioFiscal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/fiscal.png")));
        javax.swing.JMenuItem itemEmpleados = new javax.swing.JMenuItem("Catálogo de Empleados");
        itemEmpleados.setIcon(new javax.swing.ImageIcon(getClass().getResource("/empleados.png")));
        javax.swing.JMenuItem itemDirecciones = new javax.swing.JMenuItem("Direcciones de Entrega");
        itemDirecciones.setIcon(new javax.swing.ImageIcon(getClass().getResource("/entregas.png")));
        javax.swing.JMenuItem itemClasificaciones = new javax.swing.JMenuItem("Tabla de Clasificaciones");
        itemClasificaciones.setIcon(new javax.swing.ImageIcon(getClass().getResource("/clasificaciones.png")));
        javax.swing.JMenuItem itemContactos = new javax.swing.JMenuItem("Catálogo de Contactos");
        itemContactos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/contactos.png")));

        // 3. Agregar todas las opciones al menú emergente
        menuInfoMaestra.add(itemCompanias);
        menuInfoMaestra.add(itemCentrosCosto);
        menuInfoMaestra.add(itemEjercicioFiscal);
        menuInfoMaestra.add(itemEmpleados);
        menuInfoMaestra.add(itemDirecciones);
        menuInfoMaestra.add(itemClasificaciones);
        menuInfoMaestra.add(itemContactos);

        // 4. Configurar el evento "Hover" (pasar el cursor)
        btnInfoMaestra.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                menuInfoMaestra.show(btnInfoMaestra, 0, btnInfoMaestra.getHeight());
            }
        });

        // 5. EVENTO INTELIGENTE DE OCULTAR
        java.awt.event.MouseAdapter eventoOcultar = crearEventoOcultar();

        // 6. Asignar el evento de ocultar al botón, al menú y sus opciones
        btnInfoMaestra.addMouseListener(eventoOcultar);
        menuInfoMaestra.addMouseListener(eventoOcultar);

        for (java.awt.Component item : menuInfoMaestra.getComponents()) {
            item.addMouseListener(eventoOcultar);
        }

        // 7. Configurar los clics (ActionListeners)
        itemCompanias.addActionListener(e -> {
            com.mycompany.benaedu.views.Companias vistaCompanias = new com.mycompany.benaedu.views.Companias();
            mostrarPanel(vistaCompanias, "Catálogo de Compañías");
        });
        itemCentrosCosto.addActionListener(e -> {
            com.mycompany.benaedu.views.Centros_costos vistaCentroCostos = new com.mycompany.benaedu.views.Centros_costos();
            mostrarPanel(vistaCentroCostos, "Catálogo de Centros de Costo");
        });
        itemEjercicioFiscal.addActionListener(e -> {
            com.mycompany.benaedu.views.Ejercicio_Fiscal vistaEjercicioFiscal = new com.mycompany.benaedu.views.Ejercicio_Fiscal();
            mostrarPanel(vistaEjercicioFiscal, "Ejercicio Fiscal");
        });
        itemEmpleados.addActionListener(e -> {
            com.mycompany.benaedu.views.Catalogo_Empleados vistaCatalagoEmpleados = new com.mycompany.benaedu.views.Catalogo_Empleados();
            mostrarPanel(vistaCatalagoEmpleados, "Catalogo de empleados");
        });
        itemDirecciones.addActionListener(e -> {
            com.mycompany.benaedu.views.Direcciones_Entrega vistaDireccionesEntrega = new com.mycompany.benaedu.views.Direcciones_Entrega();
            mostrarPanel(vistaDireccionesEntrega, "Direcciones de Entrega");
        });
        itemClasificaciones.addActionListener(e -> {
            com.mycompany.benaedu.views.Clasificaciones vistaClasificaciones = new com.mycompany.benaedu.views.Clasificaciones();
            mostrarPanel(vistaClasificaciones, "Clasificaciones");
        });
        itemContactos.addActionListener(e -> {
            com.mycompany.benaedu.views.Catalogo_Contactos vistaContactos = new com.mycompany.benaedu.views.Catalogo_Contactos();
            mostrarPanel(vistaContactos, "Contactos");
        });
    }
    
    private void configurarMenuTransacciones() {
        // 1. Crear el contenedor del menú emergente
        javax.swing.JPopupMenu menuTransacciones = new javax.swing.JPopupMenu();

        // 2. Crear las opciones
        javax.swing.JMenuItem itemCambioDiario = new javax.swing.JMenuItem("Tipos de Cambio Diario");
        javax.swing.JMenuItem itemCondicionesPago = new javax.swing.JMenuItem("Condiciones de Pago");
        javax.swing.JMenuItem itemClaveImpuestos = new javax.swing.JMenuItem("Clave de Impuestos");
        javax.swing.JMenuItem itemCambioMensual = new javax.swing.JMenuItem("Tipo de Cambio Mensual");
        javax.swing.JMenuItem itemConvertirUnidad = new javax.swing.JMenuItem("Convertir Unidad de Medida");

        // 3. Agregar opciones al menú emergente
        menuTransacciones.add(itemCambioDiario);
        menuTransacciones.add(itemCondicionesPago);
        menuTransacciones.add(itemClaveImpuestos);
        menuTransacciones.add(itemCambioMensual);
        menuTransacciones.add(itemConvertirUnidad);

        // 4. Configurar el evento "Hover"
        btnTransacciones.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                menuTransacciones.show(btnTransacciones, 0, btnTransacciones.getHeight());
            }
        });

        // 5. EVENTO INTELIGENTE DE OCULTAR
        java.awt.event.MouseAdapter eventoOcultar = crearEventoOcultar();

        // 6. Asignar el evento de ocultar al botón, menú y opciones
        btnTransacciones.addMouseListener(eventoOcultar);
        menuTransacciones.addMouseListener(eventoOcultar);

        for (java.awt.Component item : menuTransacciones.getComponents()) {
            item.addMouseListener(eventoOcultar);
        }

        // 7. Configurar los clics (ActionListeners)
        itemCambioDiario.addActionListener(e -> {
            com.mycompany.benaedu.views.Tipos_Cambio_Diario vistaDiario = new com.mycompany.benaedu.views.Tipos_Cambio_Diario();
            mostrarPanel(vistaDiario, "Tipos de Cambio Diario");
        });
        itemCondicionesPago.addActionListener(e -> {
            com.mycompany.benaedu.views.Condiciones_Pago vistaPago = new com.mycompany.benaedu.views.Condiciones_Pago();
            mostrarPanel(vistaPago, "Condiciones de Pago");
        });
        itemClaveImpuestos.addActionListener(e -> {
            com.mycompany.benaedu.views.Tipos_Impuestos vistaImpuestos = new com.mycompany.benaedu.views.Tipos_Impuestos();
            mostrarPanel(vistaImpuestos, "Clave de Impuestos");
        });
        itemCambioMensual.addActionListener(e -> {
            com.mycompany.benaedu.views.Tipo_Cambio_Mensual vistaMensual = new com.mycompany.benaedu.views.Tipo_Cambio_Mensual();
            mostrarPanel(vistaMensual, "Tipo de Cambio Mensual");
        });
        itemConvertirUnidad.addActionListener(e -> {
            com.mycompany.benaedu.views.Factores_Convercion_Unidad vistaUnidad = new com.mycompany.benaedu.views.Factores_Convercion_Unidad();
            mostrarPanel(vistaUnidad, "Convertir Unidad de Medida");
        });
    }
    
  private void configurarMenuEscolar() {
        // 1. Crear el contenedor principal
        javax.swing.JPopupMenu menuEscolar = new javax.swing.JPopupMenu();

        // 2. Crear las CATEGORÍAS principales
        javax.swing.JMenu menuConfiguracion = new javax.swing.JMenu("10 Configuración");
        javax.swing.JMenu menuInfoMaestra = new javax.swing.JMenu("20 Información Maestra");
        javax.swing.JMenu menuTransacciones = new javax.swing.JMenu("30 Transacciones");

        // 3. Opciones "10 CONFIGURACIÓN"
        javax.swing.JMenuItem itemCiclos = new javax.swing.JMenuItem("Ciclos Escolares");
        javax.swing.JMenuItem itemGrados = new javax.swing.JMenuItem("Grados Escolares");
        javax.swing.JMenuItem itemBecas = new javax.swing.JMenuItem("Registro de Becas y Convenios");
        javax.swing.JMenuItem itemConceptos = new javax.swing.JMenuItem("Conceptos Escolares");
        javax.swing.JMenuItem itemPlanes = new javax.swing.JMenuItem("Planes de Pago");

        menuConfiguracion.add(itemCiclos);
        menuConfiguracion.add(itemGrados);
        menuConfiguracion.add(itemBecas);
        menuConfiguracion.add(itemConceptos);
        menuConfiguracion.add(itemPlanes);

        // 4. Opciones "20 INFORMACIÓN MAESTRA"
        javax.swing.JMenuItem itemCuentasBancarias = new javax.swing.JMenuItem("Cuentas Bancarias");
        javax.swing.JMenuItem itemTiposAlumno = new javax.swing.JMenuItem("Catalogo Tipos de Alumno");
        javax.swing.JMenuItem itemCajeros = new javax.swing.JMenuItem("Catalogo de Cajeros");
        javax.swing.JMenuItem itemAlumnos = new javax.swing.JMenuItem("Catalogo de Alumnos");

        menuInfoMaestra.add(itemCuentasBancarias);
        menuInfoMaestra.add(itemTiposAlumno);
        menuInfoMaestra.add(itemCajeros);
        menuInfoMaestra.add(itemAlumnos);

        // 5. Opciones "30 TRANSACCIONES"
        javax.swing.JMenuItem itemCobranza = new javax.swing.JMenuItem("Cobranza Escolar");
        javax.swing.JMenuItem itemCorteCaja = new javax.swing.JMenuItem("Corte de Caja");
        javax.swing.JMenuItem itemFacturas = new javax.swing.JMenuItem("Impresión de Facturas");
        javax.swing.JMenuItem itemDepositos = new javax.swing.JMenuItem("Registro de Depositos");
        javax.swing.JMenuItem itemCancelaConceptos = new javax.swing.JMenuItem("Cancelación de Conceptos");
        javax.swing.JMenuItem itemCancelaRecibos = new javax.swing.JMenuItem("Cancelación de Recibos");
        javax.swing.JMenuItem itemCancelaContabCorte = new javax.swing.JMenuItem("Cancela Contabilizacion de Corte de Caja");
        javax.swing.JMenuItem itemCancelaContabDep = new javax.swing.JMenuItem("Cancela Contabilizacion de Reg Deposito");

        menuTransacciones.add(itemCobranza);
        menuTransacciones.add(itemCorteCaja);
        menuTransacciones.add(itemFacturas);
        menuTransacciones.add(itemDepositos);
        menuTransacciones.add(itemCancelaConceptos);
        menuTransacciones.add(itemCancelaRecibos);
        menuTransacciones.add(itemCancelaContabCorte);
        menuTransacciones.add(itemCancelaContabDep);

        // 6. Agregar categorías al menú emergente
        menuEscolar.add(menuConfiguracion);
        menuEscolar.add(menuInfoMaestra);
        menuEscolar.add(menuTransacciones);

        // 7. EVENTO DE HOVER (MOSTRAR)
        btnEscolar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                menuEscolar.show(btnEscolar, 0, btnEscolar.getHeight());
            }
        });

        // 8. EVENTO INTELIGENTE DE OCULTAR
        java.awt.event.MouseAdapter eventoOcultar = crearEventoOcultar();

        btnEscolar.addMouseListener(eventoOcultar);
        menuEscolar.addMouseListener(eventoOcultar);
        menuConfiguracion.addMouseListener(eventoOcultar);
        menuInfoMaestra.addMouseListener(eventoOcultar);
        menuTransacciones.addMouseListener(eventoOcultar);

        // 9. ACTION LISTENERS
        itemCiclos.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Ciclos_Escolares(), "Ciclos Escolares"));
        itemGrados.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Grados_Escolares(), "Grados Escolares"));
        itemBecas.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Registro_Becas(), "Registro de Becas y Convenios"));
        itemConceptos.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Conceptos_Escolares(), "Conceptos Escolares"));
        itemPlanes.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Planes_Pago(), "Planes de Pago"));

        itemCuentasBancarias.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Cuentas_Bancarias(), "Cuentas Bancarias"));
        itemTiposAlumno.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Tipos_Alumno(), "Catalogo Tipos de Alumno"));
        itemCajeros.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Catalogo_Cajeros(), "Catalogo de Cajeros"));
        itemAlumnos.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Catalogo_Alumnos(), "Catalogo de Alumnos"));

        itemCobranza.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Cobranza_Escolar(), "Cobranza Escolar"));
        itemCorteCaja.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Corte_Caja(), "Corte de Caja"));
        itemFacturas.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Impresion_Facturas(), "Impresión de Facturas"));
        itemDepositos.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Registro_Depositos(), "Registro de Depositos"));
        itemCancelaConceptos.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Cancelacion_Conceptos(), "Cancelación de Conceptos"));
        itemCancelaRecibos.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Cancelacion_Recibos(), "Cancelación de Recibos"));
        itemCancelaContabCorte.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Cancela_Contabilizacion_Corte(), "Cancela Contabilizacion de Corte de Caja"));
        itemCancelaContabDep.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Cancela_Contabilizacion_Deposito(), "Cancela Contabilizacion de Reg Deposito"));
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

    // Para que el menú no robe el foco al escribir
    menuResultadosBusqueda.setFocusable(false);

    opcionesSistema.put("Catálogo de Compañías", () -> {
       mostrarPanel(new com.mycompany.benaedu.views.Companias(), "Catálogo de Compañías");
        
    });

    opcionesSistema.put("Catálogo de Centros de Costo", () -> {
       
        mostrarPanel(new com.mycompany.benaedu.views.Centros_costos(), "Catálogo de Centros de Costo");
    });

    opcionesSistema.put("Ejercicio Fiscal", () -> {
      mostrarPanel(new com.mycompany.benaedu.views.Ejercicio_Fiscal(),"Ejercicio Fiscal");
    });
    
    opcionesSistema.put("Catálogo de empleados", () -> {
      mostrarPanel(new com.mycompany.benaedu.views.Catalogo_Empleados(),"Catálogo de empleados");
    });
    
     opcionesSistema.put("Direcciones de Entrega", () -> {
      mostrarPanel(new com.mycompany.benaedu.views.Direcciones_Entrega(),"Direcciones de Entrega");
    });
     
      opcionesSistema.put("Tabla de Clasificaciones", () -> {
      mostrarPanel(new com.mycompany.benaedu.views.Clasificaciones(),"Tabla de Clasificaciones");
    });
      
       opcionesSistema.put("Catálogo de Contactos", () -> {
      mostrarPanel(new com.mycompany.benaedu.views.Catalogo_Contactos(),"Catálogo de Contactos");
    });
       
      // --- NUEVAS OPCIONES DE TRANSACCIONES ---

    opcionesSistema.put("Tipos de Cambio Diario", () -> {
        mostrarPanel(new com.mycompany.benaedu.views.Tipos_Cambio_Diario(), "Tipos de Cambio Diario");
    });

    opcionesSistema.put("Condiciones de Pago", () -> {
        mostrarPanel(new com.mycompany.benaedu.views.Condiciones_Pago(), "Condiciones de Pago");
    });

    opcionesSistema.put("Clave de Impuestos", () -> {
        mostrarPanel(new com.mycompany.benaedu.views.Tipos_Impuestos(), "Clave de Impuestos");
    });

    opcionesSistema.put("Tipo de Cambio Mensual", () -> {
        mostrarPanel(new com.mycompany.benaedu.views.Tipo_Cambio_Mensual(), "Tipo de Cambio Mensual");
    });

    opcionesSistema.put("Convertir Unidad de Medida", () -> {
        mostrarPanel(new com.mycompany.benaedu.views.Factores_Convercion_Unidad(), "Convertir Unidad de Medida");
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

    menuResultadosBusqueda.removeAll();
    indiceSeleccionadoBusqueda = -1;

    texto = texto.trim();

    if (texto.isEmpty()) {
        menuResultadosBusqueda.setVisible(false);
        return;
    }

    String textoBusqueda = texto.toLowerCase();
    int coincidencias = 0;

    for (java.util.Map.Entry<String, Runnable> opcion : opcionesSistema.entrySet()) {

        String nombreOpcion = opcion.getKey();

        if (nombreOpcion.toLowerCase().contains(textoBusqueda)) {

            javax.swing.JMenuItem itemResultado = new javax.swing.JMenuItem(nombreOpcion);

            itemResultado.setFocusable(false);

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
        }
    }

    if (coincidencias > 0) {

        if (!menuResultadosBusqueda.isVisible()) {
            menuResultadosBusqueda.show(txtSearch, 0, txtSearch.getHeight());
        }

        menuResultadosBusqueda.revalidate();
        menuResultadosBusqueda.repaint();

        javax.swing.SwingUtilities.invokeLater(() -> {
            txtSearch.requestFocusInWindow();
        });

    } else {
        menuResultadosBusqueda.setVisible(false);
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        background = new javax.swing.JPanel();
        jpMenu = new javax.swing.JPanel();
        btnInfoMaestra = new javax.swing.JButton();
        btnTransacciones = new javax.swing.JButton();
        btnEscolar = new javax.swing.JButton();
        btnAcademico = new javax.swing.JButton();
        lblLogo = new javax.swing.JLabel();
        jpHeader = new javax.swing.JPanel();
        lblMessage = new javax.swing.JLabel();
        lblDate = new javax.swing.JLabel();
        lblTime = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        imgSearch = new javax.swing.JLabel();
        lblTitleInfo = new javax.swing.JLabel();
        jpContainer = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        setMinimumSize(new java.awt.Dimension(1020, 664));

        background.setBackground(new java.awt.Color(255, 255, 255));

        jpMenu.setBackground(new java.awt.Color(26, 61, 99));
        jpMenu.setPreferredSize(new java.awt.Dimension(270, 640));

        btnInfoMaestra.setBackground(new java.awt.Color(179, 207, 229));
        btnInfoMaestra.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnInfoMaestra.setForeground(new java.awt.Color(26, 61, 99));
        btnInfoMaestra.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hom3.png"))); // NOI18N
        btnInfoMaestra.setText("Informacion Maestra");
        btnInfoMaestra.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
        btnInfoMaestra.setBorderPainted(false);
        btnInfoMaestra.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnInfoMaestra.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnInfoMaestra.setIconTextGap(10);
        btnInfoMaestra.addActionListener(this::btnInfoMaestraActionPerformed);

        btnTransacciones.setBackground(new java.awt.Color(179, 207, 229));
        btnTransacciones.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnTransacciones.setForeground(new java.awt.Color(26, 61, 99));
        btnTransacciones.setIcon(new javax.swing.ImageIcon(getClass().getResource("/transaccion.png"))); // NOI18N
        btnTransacciones.setText("Transacciones");
        btnTransacciones.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
        btnTransacciones.setBorderPainted(false);
        btnTransacciones.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnTransacciones.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnTransacciones.setIconTextGap(10);

        btnEscolar.setBackground(new java.awt.Color(179, 207, 229));
        btnEscolar.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEscolar.setForeground(new java.awt.Color(26, 61, 99));
        btnEscolar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/escolar2.png"))); // NOI18N
        btnEscolar.setText("Escolar");
        btnEscolar.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
        btnEscolar.setBorderPainted(false);
        btnEscolar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEscolar.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnEscolar.setIconTextGap(10);

        btnAcademico.setBackground(new java.awt.Color(179, 207, 229));
        btnAcademico.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAcademico.setForeground(new java.awt.Color(26, 61, 99));
        btnAcademico.setIcon(new javax.swing.ImageIcon(getClass().getResource("/academico2.png"))); // NOI18N
        btnAcademico.setText("Academico");
        btnAcademico.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
        btnAcademico.setBorderPainted(false);
        btnAcademico.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAcademico.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnAcademico.setIconTextGap(10);

        lblLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/benaedu (5).png"))); // NOI18N

        javax.swing.GroupLayout jpMenuLayout = new javax.swing.GroupLayout(jpMenu);
        jpMenu.setLayout(jpMenuLayout);
        jpMenuLayout.setHorizontalGroup(
            jpMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpMenuLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnTransacciones, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnInfoMaestra, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEscolar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnAcademico, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jpMenuLayout.setVerticalGroup(
            jpMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpMenuLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnInfoMaestra, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnTransacciones, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnEscolar, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnAcademico, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jpHeader.setBackground(new java.awt.Color(26, 61, 99));
        jpHeader.setForeground(new java.awt.Color(255, 255, 255));
        jpHeader.setPreferredSize(new java.awt.Dimension(650, 150));

        lblMessage.setBackground(new java.awt.Color(0, 0, 0));
        lblMessage.setForeground(new java.awt.Color(91, 173, 197));
        lblMessage.setText("Buenas Tardes, Usuario");

        lblDate.setForeground(new java.awt.Color(0, 153, 153));
        lblDate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/calendario.png"))); // NOI18N
        lblDate.setText("Hoy es {dayname} de {month} del {year}  ");
        lblDate.setIconTextGap(10);

        lblTime.setForeground(new java.awt.Color(0, 153, 153));
        lblTime.setIcon(new javax.swing.ImageIcon(getClass().getResource("/clock.png"))); // NOI18N
        lblTime.setText("Hora");
        lblTime.setIconTextGap(10);

        txtSearch.setBackground(new java.awt.Color(179, 207, 229));
        txtSearch.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtSearch.setForeground(new java.awt.Color(26, 61, 99));
        txtSearch.setToolTipText("");

        imgSearch.setForeground(new java.awt.Color(91, 173, 197));
        imgSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/buscar.png"))); // NOI18N

        lblTitleInfo.setForeground(new java.awt.Color(91, 173, 197));
        lblTitleInfo.setText("Titulo");

        javax.swing.GroupLayout jpHeaderLayout = new javax.swing.GroupLayout(jpHeader);
        jpHeader.setLayout(jpHeaderLayout);
        jpHeaderLayout.setHorizontalGroup(
            jpHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpHeaderLayout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(jpHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblMessage)
                    .addGroup(jpHeaderLayout.createSequentialGroup()
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(imgSearch))
                    .addComponent(lblTitleInfo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 289, Short.MAX_VALUE)
                .addGroup(jpHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblDate)
                    .addComponent(lblTime))
                .addGap(21, 21, 21))
        );
        jpHeaderLayout.setVerticalGroup(
            jpHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpHeaderLayout.createSequentialGroup()
                .addGroup(jpHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpHeaderLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(lblDate))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpHeaderLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblMessage)))
                .addGroup(jpHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpHeaderLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(lblTime))
                    .addGroup(jpHeaderLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblTitleInfo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jpHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(imgSearch))))
                .addContainerGap(58, Short.MAX_VALUE))
        );

        jpContainer.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jpContainerLayout = new javax.swing.GroupLayout(jpContainer);
        jpContainer.setLayout(jpContainerLayout);
        jpContainerLayout.setHorizontalGroup(
            jpContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 914, Short.MAX_VALUE)
        );
        jpContainerLayout.setVerticalGroup(
            jpContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 508, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout backgroundLayout = new javax.swing.GroupLayout(background);
        background.setLayout(backgroundLayout);
        backgroundLayout.setHorizontalGroup(
            backgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundLayout.createSequentialGroup()
                .addComponent(jpMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(backgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(backgroundLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jpContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jpHeader, javax.swing.GroupLayout.DEFAULT_SIZE, 920, Short.MAX_VALUE)))
        );
        backgroundLayout.setVerticalGroup(
            backgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundLayout.createSequentialGroup()
                .addComponent(jpHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jpContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

    // Método 1: Verifica de forma global dónde está el cursor
    private boolean isMouseOverAnyMenuOrButton() {
        java.awt.Point cursorLoc = java.awt.MouseInfo.getPointerInfo().getLocation();

        // 1. Revisar si el mouse está sobre alguno de los botones principales
        javax.swing.JButton[] botones = {btnInfoMaestra, btnTransacciones, btnEscolar, btnAcademico};
        for (javax.swing.JButton btn : botones) {
            if (btn != null && btn.isShowing()) {
                java.awt.Rectangle bounds = new java.awt.Rectangle(btn.getLocationOnScreen(), btn.getSize());
                if (bounds.contains(cursorLoc)) {
                    return true; // Está sobre un botón principal, no cerrar
                }
            }
        }

        // 2. Revisar si el mouse está sobre CUALQUIER menú o submenú abierto actualmente
        javax.swing.MenuElement[] path = javax.swing.MenuSelectionManager.defaultManager().getSelectedPath();
        for (javax.swing.MenuElement element : path) {
            java.awt.Component comp = element.getComponent();
            if (comp != null && comp.isShowing()) {
                java.awt.Rectangle bounds = new java.awt.Rectangle(comp.getLocationOnScreen(), comp.getSize());
                if (bounds.contains(cursorLoc)) {
                    return true; // Está adentro de un menú o submenú, no cerrar
                }
            }
        }

        return false; // El mouse está afuera de todo
    }

    // Método 2: Crea el temporizador inteligente
    private java.awt.event.MouseAdapter crearEventoOcultar() {
        return new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                // Le damos 250ms (un cuarto de segundo) para que el movimiento se sienta fluido
                javax.swing.Timer timer = new javax.swing.Timer(250, e -> {
                    // Si el mouse NO está en ningún botón ni submenú válido, limpiamos la pantalla
                    if (!isMouseOverAnyMenuOrButton()) {
                        javax.swing.MenuSelectionManager.defaultManager().clearSelectedPath();
                    }
                });
                timer.setRepeats(false);
                timer.start();
            }
        };
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        FlatMTMaterialLighterIJTheme.setup();

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Dashboard().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel background;
    private javax.swing.JButton btnAcademico;
    private javax.swing.JButton btnEscolar;
    private javax.swing.JButton btnInfoMaestra;
    private javax.swing.JButton btnTransacciones;
    private javax.swing.JLabel imgSearch;
    private javax.swing.JPanel jpContainer;
    private javax.swing.JPanel jpHeader;
    private javax.swing.JPanel jpMenu;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblMessage;
    private javax.swing.JLabel lblTime;
    private javax.swing.JLabel lblTitleInfo;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
