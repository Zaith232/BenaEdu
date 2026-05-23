/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.benaedu;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMaterialLighterIJTheme;
import com.mycompany.benaedu.views.Companias;
import com.mycompany.benaedu.views.Principal;
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

    /**
     * Creates new form Dashboard
     */
    public Dashboard() {
        initComponents();
        InitStyles();
        configurarMenuDesplegable();
        configurarFechaYHora();
        initContent();
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
        itemCompanias.setIcon(new javax.swing.ImageIcon(getClass().getResource("/companion.png")));
        javax.swing.JMenuItem itemCentrosCosto = new javax.swing.JMenuItem("Catálogo de Centros de Costo");
        javax.swing.JMenuItem itemEjercicioFiscal = new javax.swing.JMenuItem("Ejercicio Fiscal");
        javax.swing.JMenuItem itemEmpleados = new javax.swing.JMenuItem("Catálogo de Empleados");
        javax.swing.JMenuItem itemDirecciones = new javax.swing.JMenuItem("Direcciones de Entrega");
        javax.swing.JMenuItem itemClasificaciones = new javax.swing.JMenuItem("Tabla de Clasificaciones");
        javax.swing.JMenuItem itemContactos = new javax.swing.JMenuItem("Catálogo de Contactos");

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
            System.out.println("Hiciste clic en Catálogo de Compañías");
            
            // 1. Creamos la vista (asegúrate de que el import esté arriba en tu archivo)
            com.mycompany.benaedu.views.Companias vistaCompanias = new com.mycompany.benaedu.views.Companias();
            
            // 2. Usamos nuestro nuevo método para mostrarla en el contenedor
            mostrarPanel(vistaCompanias);
        });

        itemCentrosCosto.addActionListener(e -> {
            System.out.println("Hiciste clic en Catálogo de Centros de Costo");
        });

        itemEjercicioFiscal.addActionListener(e -> {
            System.out.println("Hiciste clic en Ejercicio Fiscal");
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
        jpContainer = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(1020, 664));

        background.setBackground(new java.awt.Color(255, 255, 255));

        jpMenu.setBackground(new java.awt.Color(95, 143, 255));
        jpMenu.setPreferredSize(new java.awt.Dimension(270, 640));

        lblTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblTitle.setText("BENAEDU");

        btnInfoMaestra.setBackground(new java.awt.Color(51, 51, 255));
        btnInfoMaestra.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnInfoMaestra.setForeground(new java.awt.Color(255, 255, 255));
        btnInfoMaestra.setIcon(new javax.swing.ImageIcon(getClass().getResource("/home.png"))); // NOI18N
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

        jpHeader.setBackground(new java.awt.Color(95, 143, 255));
        jpHeader.setPreferredSize(new java.awt.Dimension(744, 150));

        lblMessage.setForeground(new java.awt.Color(255, 255, 255));
        lblMessage.setText("Buenas Tardes, Usuario");

        lblDate.setText("Hoy es {dayname} de {month} del {year}  ");

        lblTime.setText("Hora");

        javax.swing.GroupLayout jpHeaderLayout = new javax.swing.GroupLayout(jpHeader);
        jpHeader.setLayout(jpHeaderLayout);
        jpHeaderLayout.setHorizontalGroup(
            jpHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpHeaderLayout.createSequentialGroup()
                .addGroup(jpHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblMessage)
                    .addComponent(lblDate))
                .addGap(0, 533, Short.MAX_VALUE))
            .addGroup(jpHeaderLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTime)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jpHeaderLayout.setVerticalGroup(
            jpHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpHeaderLayout.createSequentialGroup()
                .addComponent(lblMessage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblDate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 90, Short.MAX_VALUE)
                .addComponent(lblTime)
                .addContainerGap())
        );

        jpContainer.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jpContainerLayout = new javax.swing.GroupLayout(jpContainer);
        jpContainer.setLayout(jpContainerLayout);
        jpContainerLayout.setHorizontalGroup(
            jpContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 750, Short.MAX_VALUE)
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
                    .addComponent(jpHeader, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
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
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPanel jpContainer;
    private javax.swing.JPanel jpHeader;
    private javax.swing.JPanel jpMenu;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblMessage;
    private javax.swing.JLabel lblTime;
    private javax.swing.JLabel lblTitle;
    // End of variables declaration//GEN-END:variables
}
