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
    
    private int indiceSeleccionadoBusqueda = -1;

    /**
     * Creates new form Dashboard
     */
    public Dashboard() {
        initComponents();
        InitStyles();
        configurarMenuDesplegable();
        configurarFechaYHora();
        initContent();
        configurarBuscador();
    }

    private void InitStyles() {
        lblMessage.putClientProperty("FlatLaf.style", "font: 30");
        lblMessage.setForeground(Color.white);
        lblTitle.putClientProperty("FlatLaf.style", "font: 30 ");
        lblTitle.setForeground(Color.white);
        lblDate.putClientProperty("FlatLaf.style", "font: 30 ");
        lblDate.setForeground(Color.white);
        lblTime.putClientProperty("FlatLaf.style", "font: 30 ");
        lblTime.setForeground(Color.white);

    }

    private void configurarMenuDesplegable() {
        // 1. Crear el contenedor del menú emergente
        javax.swing.JPopupMenu menuInfoMaestra = new javax.swing.JPopupMenu();

        // 2. Crear las opciones correctas basándonos en tu imagen
        // Asegúrate de tener un icono llamado "compania.png" en tu carpeta de imágenes
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

        // 4. Configurar el evento "Hover" (pasar el cursor) en tu botón azul
        btnInfoMaestra.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                // Mostrar el menú justo debajo del botón
                menuInfoMaestra.show(btnInfoMaestra, 0, btnInfoMaestra.getHeight());
            }
        });

        // 5. Crear el evento que detecta cuando el mouse sale para ocultar el menú
        java.awt.event.MouseAdapter eventoOcultar = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                javax.swing.Timer timer = new javax.swing.Timer(200, e -> {
                    java.awt.Point cursorLoc = java.awt.MouseInfo.getPointerInfo().getLocation();

                    // ¿El cursor sigue sobre el botón btnInfoMaestra?
                    if (btnInfoMaestra.isShowing()) {
                        java.awt.Rectangle limitesBoton = new java.awt.Rectangle(btnInfoMaestra.getLocationOnScreen(), btnInfoMaestra.getSize());
                        if (limitesBoton.contains(cursorLoc)) {
                            return; // Está en el botón, NO cerrar
                        }
                    }

                    // ¿El cursor está sobre el menú desplegable?
                    if (menuInfoMaestra.isShowing()) {
                        java.awt.Rectangle limitesMenu = new java.awt.Rectangle(menuInfoMaestra.getLocationOnScreen(), menuInfoMaestra.getSize());
                        if (limitesMenu.contains(cursorLoc)) {
                            return; // Está en el menú, NO cerrar
                        }
                    }

                    // Si ya no está en el botón ni en el menú, lo cerramos
                    javax.swing.MenuSelectionManager.defaultManager().clearSelectedPath();
                });
                timer.setRepeats(false);
                timer.start();
            }
        };

        // 6. Asignar el evento de ocultar al botón y al menú
        btnInfoMaestra.addMouseListener(eventoOcultar);
        menuInfoMaestra.addMouseListener(eventoOcultar);

        // Asignar el evento de ocultar a todas las opciones de forma rápida con un bucle
        for (java.awt.Component item : menuInfoMaestra.getComponents()) {
            item.addMouseListener(eventoOcultar);
        }

       // 7. Configurar los clics (ActionListeners) para cada opción
        itemCompanias.addActionListener(e -> {
            
            // 1. Creamos la vista (asegúrate de que el import esté arriba en tu archivo)
            com.mycompany.benaedu.views.Companias vistaCompanias = new com.mycompany.benaedu.views.Companias();
            
            // 2. Usamos nuestro nuevo método para mostrarla en el contenedor
            mostrarPanel(vistaCompanias);
        });

        itemCentrosCosto.addActionListener(e -> {
                 
            // 1. Creamos la vista (asegúrate de que el import esté arriba en tu archivo)
            com.mycompany.benaedu.views.Centros_costos vistaCentroCostos = new com.mycompany.benaedu.views.Centros_costos();
            
            // 2. Usamos nuestro nuevo método para mostrarla en el contenedor
            mostrarPanel(vistaCentroCostos);
        });

        itemEjercicioFiscal.addActionListener(e -> {
               // 1. Creamos la vista (asegúrate de que el import esté arriba en tu archivo)
            com.mycompany.benaedu.views.Ejercicio_Fiscal vistaEjercicioFiscal = new com.mycompany.benaedu.views.Ejercicio_Fiscal();
            
            // 2. Usamos nuestro nuevo método para mostrarla en el contenedor
            mostrarPanel(vistaEjercicioFiscal);
        });
        
        itemEmpleados.addActionListener(e -> {
               // 1. Creamos la vista (asegúrate de que el import esté arriba en tu archivo)
            com.mycompany.benaedu.views.Catalogo_Empleados vistaCatalagoEmpleados = new com.mycompany.benaedu.views.Catalogo_Empleados();
            
            // 2. Usamos nuestro nuevo método para mostrarla en el contenedor
            mostrarPanel(vistaCatalagoEmpleados);
        });
        
         itemDirecciones.addActionListener(e -> {
               // 1. Creamos la vista (asegúrate de que el import esté arriba en tu archivo)
            com.mycompany.benaedu.views.Direcciones_Entrega vistaDireccionesEntrega = new com.mycompany.benaedu.views.Direcciones_Entrega();
            
            // 2. Usamos nuestro nuevo método para mostrarla en el contenedor
            mostrarPanel(vistaDireccionesEntrega);
        });
         
            itemClasificaciones.addActionListener(e -> {
               // 1. Creamos la vista (asegúrate de que el import esté arriba en tu archivo)
            com.mycompany.benaedu.views.Clasificaciones vistaClasificaciones = new com.mycompany.benaedu.views.Clasificaciones();
            
            // 2. Usamos nuestro nuevo método para mostrarla en el contenedor
            mostrarPanel(vistaClasificaciones);
        });
            
             itemContactos.addActionListener(e -> {
               // 1. Creamos la vista (asegúrate de que el import esté arriba en tu archivo)
            com.mycompany.benaedu.views.Catalogo_Contactos vistaContactos = new com.mycompany.benaedu.views.Catalogo_Contactos();
            
            // 2. Usamos nuestro nuevo método para mostrarla en el contenedor
            mostrarPanel(vistaContactos);
        });

        // (Opcional) Puedes seguir agregando los ActionListeners de los demás ítems aquí abajo...
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
    }
  
  // Este método recibe cualquier JPanel y lo muestra en tu jpContainer
    private void mostrarPanel(javax.swing.JPanel p) {
        jpContainer.removeAll();
        jpContainer.add(p, java.awt.BorderLayout.CENTER);
        jpContainer.revalidate();
        jpContainer.repaint();
    }
    
   private void configurarBuscador() {

    // Para que el menú no robe el foco al escribir
    menuResultadosBusqueda.setFocusable(false);

    opcionesSistema.put("Catálogo de Compañías", () -> {
        mostrarPanel(new com.mycompany.benaedu.views.Companias());
    });

    opcionesSistema.put("Catálogo de Centros de Costo", () -> {
       
        mostrarPanel(new com.mycompany.benaedu.views.Centros_costos());
    });

    opcionesSistema.put("Ejercicio Fiscal", () -> {
      mostrarPanel(new com.mycompany.benaedu.views.Ejercicio_Fiscal());
    });
    
    opcionesSistema.put("Catálogo de empleados", () -> {
      mostrarPanel(new com.mycompany.benaedu.views.Catalogo_Empleados());
    });
    
     opcionesSistema.put("Direcciones de Entrega", () -> {
      mostrarPanel(new com.mycompany.benaedu.views.Direcciones_Entrega());
    });
     
      opcionesSistema.put("Tabla de Clasificaciones", () -> {
      mostrarPanel(new com.mycompany.benaedu.views.Clasificaciones());
    });
      
       opcionesSistema.put("Catálogo de Contactos", () -> {
      mostrarPanel(new com.mycompany.benaedu.views.Catalogo_Contactos());
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
        lblTitle = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        btnInfoMaestra = new javax.swing.JButton();
        jpHeader = new javax.swing.JPanel();
        lblMessage = new javax.swing.JLabel();
        lblDate = new javax.swing.JLabel();
        lblTime = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jpContainer = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(1020, 664));

        background.setBackground(new java.awt.Color(255, 255, 255));

        jpMenu.setBackground(new java.awt.Color(91, 173, 197));
        jpMenu.setPreferredSize(new java.awt.Dimension(270, 640));

        lblTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitle.setText("BENAEDU");

        btnInfoMaestra.setBackground(new java.awt.Color(203, 238, 244));
        btnInfoMaestra.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnInfoMaestra.setForeground(new java.awt.Color(91, 173, 197));
        btnInfoMaestra.setIcon(new javax.swing.ImageIcon(getClass().getResource("/home2.png"))); // NOI18N
        btnInfoMaestra.setText("Informacion Maestra");
        btnInfoMaestra.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
        btnInfoMaestra.setBorderPainted(false);
        btnInfoMaestra.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnInfoMaestra.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnInfoMaestra.setIconTextGap(10);

        javax.swing.GroupLayout jpMenuLayout = new javax.swing.GroupLayout(jpMenu);
        jpMenu.setLayout(jpMenuLayout);
        jpMenuLayout.setHorizontalGroup(
            jpMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpMenuLayout.createSequentialGroup()
                .addGap(62, 62, 62)
                .addGroup(jpMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTitle)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 78, Short.MAX_VALUE))
            .addComponent(btnInfoMaestra, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jpMenuLayout.setVerticalGroup(
            jpMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpMenuLayout.createSequentialGroup()
                .addGap(65, 65, 65)
                .addComponent(lblTitle)
                .addGap(6, 6, 6)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(btnInfoMaestra, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jpHeader.setBackground(new java.awt.Color(91, 173, 197));
        jpHeader.setPreferredSize(new java.awt.Dimension(650, 150));

        lblMessage.setForeground(new java.awt.Color(255, 255, 255));
        lblMessage.setText("Buenas Tardes, Usuario");

        lblDate.setText("Hoy es {dayname} de {month} del {year}  ");

        lblTime.setText("Hora");

        txtSearch.setBackground(new java.awt.Color(203, 238, 244));
        txtSearch.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/search.png"))); // NOI18N

        javax.swing.GroupLayout jpHeaderLayout = new javax.swing.GroupLayout(jpHeader);
        jpHeader.setLayout(jpHeaderLayout);
        jpHeaderLayout.setHorizontalGroup(
            jpHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpHeaderLayout.createSequentialGroup()
                .addGroup(jpHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpHeaderLayout.createSequentialGroup()
                        .addComponent(lblMessage)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblDate))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpHeaderLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblTime)))
                .addContainerGap())
            .addGroup(jpHeaderLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jpHeaderLayout.setVerticalGroup(
            jpHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpHeaderLayout.createSequentialGroup()
                .addGroup(jpHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblMessage)
                    .addGroup(jpHeaderLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblDate)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblTime)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 74, Short.MAX_VALUE)
                .addGroup(jpHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addContainerGap())
        );

        jpContainer.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jpContainerLayout = new javax.swing.GroupLayout(jpContainer);
        jpContainer.setLayout(jpContainerLayout);
        jpContainerLayout.setHorizontalGroup(
            jpContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 920, Short.MAX_VALUE)
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
                    .addComponent(jpHeader, javax.swing.GroupLayout.DEFAULT_SIZE, 920, Short.MAX_VALUE)
                    .addComponent(jpContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        backgroundLayout.setVerticalGroup(
            backgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jpMenu, javax.swing.GroupLayout.DEFAULT_SIZE, 664, Short.MAX_VALUE)
            .addGroup(backgroundLayout.createSequentialGroup()
                .addComponent(jpHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jpContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
    private javax.swing.JButton btnInfoMaestra;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPanel jpContainer;
    private javax.swing.JPanel jpHeader;
    private javax.swing.JPanel jpMenu;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblMessage;
    private javax.swing.JLabel lblTime;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
