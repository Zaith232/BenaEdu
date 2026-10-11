/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.benaedu;
import com.mycompany.benaedu.db.ConDB;
import javax.swing.JOptionPane;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMaterialLighterIJTheme;
/**
 *
 * @author b17za
 */
public class EditarUsuario extends javax.swing.JFrame {
    private String usuarioOriginal;
    private final java.util.Map<String, javax.swing.JCheckBox> casillasPermisos = new java.util.LinkedHashMap<>();
    private java.util.Set<String> seleccionCajero = java.util.Set.of();
    private boolean modoAdministrador;
    private final javax.swing.JButton marcarTodos = new javax.swing.JButton("Marcar todos");
    private final javax.swing.JButton quitarTodos = new javax.swing.JButton("Quitar todos");
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(EditarUsuario.class.getName());
// Modifica tu constructor para que reciba los parámetros
    public EditarUsuario(String usuario, String correo, String rol) {
        initComponents();
        
        //Se manda a llamar :P
        aplicarDiseñoPersonalizado();
        
        this.usuarioOriginal = usuario;
        
        // Llenar los campos con los datos recibidos
        txtUser.setText(usuario);
        txtEmail.setText(correo); // Asegúrate de haber cambiado el txtPassword por txtCorreo
        cmbRol.setSelectedItem(SesionUsuario.normalizarRol(rol));
        configurarPermisos();
        
     
    }
    /**
     * Creates new form EditarUsuario
     */
    public EditarUsuario() {
        initComponents();
        btnSave.setEnabled(false);
    }

    private void configurarPermisos() {
        btnSave.setEnabled(false);
        try (var con = ConDB.getConnection()) {
            seleccionCajero = com.mycompany.benaedu.db.PermisosUsuario.cargarParaEditar(con, usuarioOriginal);
        } catch (Exception ex) {
            throw new IllegalStateException("No se pueden editar los permisos: " + ex.getMessage(), ex);
        }
        javax.swing.JPanel datos = new javax.swing.JPanel(new java.awt.GridBagLayout());
        datos.setBackground(pnlBg.getBackground());
        java.awt.GridBagConstraints posicion = new java.awt.GridBagConstraints();
        posicion.insets = new java.awt.Insets(8, 8, 8, 8);
        posicion.anchor = java.awt.GridBagConstraints.WEST;
        javax.swing.JLabel[] etiquetas = {lblUser, lblCorreo, lblRol};
        javax.swing.JComponent[] campos = {txtUser, txtEmail, cmbRol};
        for (int i = 0; i < campos.length; i++) {
            posicion.gridy = i; posicion.gridx = 0; posicion.weightx = 0;
            datos.add(etiquetas[i], posicion);
            posicion.gridx = 1; posicion.weightx = 1; posicion.fill = java.awt.GridBagConstraints.HORIZONTAL;
            datos.add(campos[i], posicion);
            posicion.fill = java.awt.GridBagConstraints.NONE;
        }
        javax.swing.JPanel permisos = new javax.swing.JPanel();
        permisos.setBackground(pnlBg.getBackground());
        permisos.setLayout(new javax.swing.BoxLayout(permisos, javax.swing.BoxLayout.Y_AXIS));
        String grupoAnterior = "";
        for (var apartado : com.mycompany.benaedu.db.PermisosUsuario.APARTADOS) {
            if (!grupoAnterior.equals(apartado.grupo())) {
                javax.swing.JLabel grupo = new javax.swing.JLabel(apartado.grupo());
                grupo.setFont(grupo.getFont().deriveFont(java.awt.Font.BOLD));
                grupo.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 4, 5, 4));
                permisos.add(grupo);
                grupoAnterior = apartado.grupo();
            }
            javax.swing.JCheckBox casilla = new javax.swing.JCheckBox(apartado.nombre(), seleccionCajero.contains(apartado.id()));
            casilla.setOpaque(false);
            if (apartado.soloAdministrador()) casilla.setToolTipText("Exclusivo de administradores");
            permisos.add(casilla);
            casillasPermisos.put(apartado.id(), casilla);
        }
        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(permisos);
        scroll.setBorder(javax.swing.BorderFactory.createTitledBorder("Apartados permitidos (marcado = puede entrar)"));
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        javax.swing.JPanel contenido = new javax.swing.JPanel(new java.awt.BorderLayout(10, 10));
        contenido.setOpaque(false); contenido.add(datos, java.awt.BorderLayout.NORTH);
        javax.swing.JPanel accionesPermisos = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        accionesPermisos.setOpaque(false); accionesPermisos.add(marcarTodos); accionesPermisos.add(quitarTodos);
        javax.swing.JPanel listaPermisos = new javax.swing.JPanel(new java.awt.BorderLayout());
        listaPermisos.setOpaque(false); listaPermisos.add(accionesPermisos, java.awt.BorderLayout.NORTH);
        listaPermisos.add(scroll, java.awt.BorderLayout.CENTER);
        contenido.add(listaPermisos, java.awt.BorderLayout.CENTER);
        marcarTodos.addActionListener(e -> casillasPermisos.values().forEach(c -> { if (c.isEnabled()) c.setSelected(true); }));
        quitarTodos.addActionListener(e -> casillasPermisos.values().forEach(c -> { if (c.isEnabled()) c.setSelected(false); }));
        javax.swing.JPanel cabecera = new javax.swing.JPanel(new java.awt.BorderLayout());
        cabecera.setOpaque(false); cabecera.add(lblTitulo, java.awt.BorderLayout.WEST); cabecera.add(btnBack, java.awt.BorderLayout.EAST);
        javax.swing.JPanel pie = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        pie.setOpaque(false); pie.add(new javax.swing.JLabel("Los administradores tienen acceso completo.")); pie.add(btnSave);
        pnlBg.removeAll();
        pnlBg.setLayout(new java.awt.BorderLayout(12, 12));
        pnlBg.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 24, 16, 24));
        pnlBg.add(cabecera, java.awt.BorderLayout.NORTH); pnlBg.add(contenido, java.awt.BorderLayout.CENTER);
        pnlBg.add(pie, java.awt.BorderLayout.SOUTH);
        cmbRol.addActionListener(e -> actualizarCasillasPorRol());
        actualizarCasillasPorRol();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(720, 540));
        setSize(893, 640);
        btnSave.setEnabled(true);
    }

    private java.util.Set<String> seleccionActual() {
        java.util.Set<String> seleccion = new java.util.LinkedHashSet<>();
        casillasPermisos.forEach((id, casilla) -> { if (casilla.isSelected()) seleccion.add(id); });
        return seleccion;
    }

    private void actualizarCasillasPorRol() {
        boolean administrador = "Administrador".equals(cmbRol.getSelectedItem());
        if (administrador && !modoAdministrador) seleccionCajero = seleccionActual();
        for (var apartado : com.mycompany.benaedu.db.PermisosUsuario.APARTADOS) {
            javax.swing.JCheckBox casilla = casillasPermisos.get(apartado.id());
            casilla.setSelected(administrador || (!apartado.soloAdministrador() && seleccionCajero.contains(apartado.id())));
            casilla.setEnabled(!administrador && !apartado.soloAdministrador());
        }
        modoAdministrador = administrador;
        marcarTodos.setEnabled(!administrador);
        quitarTodos.setEnabled(!administrador);

        
        //Aquí por si se abre la ventana vacía :P
        aplicarDiseñoPersonalizado();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                      
    
    // --- Diseño visual: no modifica eventos ni lógica de base de datos ---
    private void aplicarDiseñoPersonalizado() {
        java.awt.Color fondo = new java.awt.Color(21, 45, 74);
        java.awt.Color tarjeta = new java.awt.Color(248, 250, 252);
        java.awt.Color azulPrincipal = new java.awt.Color(45, 111, 171);
        java.awt.Color azulPrincipalHover = new java.awt.Color(36, 91, 143);
        java.awt.Color azulPrincipalPresionado = new java.awt.Color(29, 73, 116);
        java.awt.Color azulSecundario = new java.awt.Color(220, 235, 244);
        java.awt.Color azulSecundarioHover = new java.awt.Color(201, 222, 235);
        java.awt.Color azulSecundarioPresionado = new java.awt.Color(184, 210, 226);
        java.awt.Color texto = new java.awt.Color(25, 54, 82);
        java.awt.Color borde = new java.awt.Color(184, 204, 221);

        pnlBg.setBackground(fondo);

        // La tarjeta utiliza un borde redondeado y conserva todos sus componentes.
        pnlTarejeta.setOpaque(false);
        pnlTarejeta.setBackground(tarjeta);
        pnlTarejeta.setBorder(new RoundedPanelBorder(
                tarjeta,
                new java.awt.Color(215, 227, 237),
                22,
                1
        ));

        estilizarBotonRedondeado(
                btnSave,
                azulPrincipal,
                azulPrincipalHover,
                azulPrincipalPresionado,
                java.awt.Color.WHITE
        );
        estilizarBotonRedondeado(
                btnBack,
                azulSecundario,
                azulSecundarioHover,
                azulSecundarioPresionado,
                texto
        );

        estilizarCampo(txtUser, texto, borde);
        estilizarCampo(txtEmail, texto, borde);
        estilizarCampo(cmbRol, texto, borde);

        java.awt.Font fuenteLabels = new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14);
        lblUser.setFont(fuenteLabels);
        lblUser.setForeground(texto);
        lblCorreo.setFont(fuenteLabels);
        lblCorreo.setForeground(texto);
        lblRol.setFont(fuenteLabels);
        lblRol.setForeground(texto);

        lblTitulo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 24));
        lblTitulo.setForeground(java.awt.Color.WHITE);
    }

    private void estilizarBotonRedondeado(
            javax.swing.JButton boton,
            java.awt.Color normal,
            java.awt.Color hover,
            java.awt.Color presionado,
            java.awt.Color colorTexto) {

        boton.setBackground(normal);
        boton.setForeground(colorTexto);
        boton.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setContentAreaFilled(false);
        boton.setOpaque(false);
        boton.setRolloverEnabled(true);
        boton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        boton.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 18, 8, 18));

        // Se conserva la integración con FlatLaf y se fuerza el arco visual.
        boton.putClientProperty("JButton.buttonType", "roundRect");
        boton.putClientProperty("FlatLaf.style", "arc: 18; focusWidth: 0; borderWidth: 0");

        boton.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(java.awt.Graphics g, javax.swing.JComponent c) {
                javax.swing.AbstractButton button = (javax.swing.AbstractButton) c;
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(
                        java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON
                );

                java.awt.Color colorActual = normal;
                if (button.getModel().isPressed()) {
                    colorActual = presionado;
                } else if (button.getModel().isRollover()) {
                    colorActual = hover;
                }

                g2.setColor(colorActual);
                g2.fillRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 18, 18);
                g2.dispose();

                super.paint(g, c);
            }
        });
    }

    private void estilizarCampo(javax.swing.JComponent campo, java.awt.Color texto, java.awt.Color borde) {
        campo.setBackground(java.awt.Color.WHITE);
        campo.setForeground(texto);
        campo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14));
        campo.putClientProperty("Component.arc", 12);
        campo.putClientProperty("FlatLaf.style", "arc: 12; focusWidth: 1");
        campo.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(borde, 1, true),
                javax.swing.BorderFactory.createEmptyBorder(5, 9, 5, 9)
        ));

        if (campo instanceof javax.swing.JComponent) {
            campo.setPreferredSize(new java.awt.Dimension(206, 35));
        }
    }

    private static final class RoundedPanelBorder extends javax.swing.border.AbstractBorder {
        private final java.awt.Color fill;
        private final java.awt.Color line;
        private final int arc;
        private final int thickness;

        private RoundedPanelBorder(java.awt.Color fill, java.awt.Color line, int arc, int thickness) {
            this.fill = fill;
            this.line = line;
            this.arc = arc;
            this.thickness = thickness;
        }

        @Override
        public java.awt.Insets getBorderInsets(java.awt.Component c) {
            int padding = 14 + thickness;
            return new java.awt.Insets(padding, padding, padding, padding);
        }

        @Override
        public void paintBorder(
                java.awt.Component c,
                java.awt.Graphics g,
                int x,
                int y,
                int width,
                int height) {

            java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
            g2.setRenderingHint(
                    java.awt.RenderingHints.KEY_ANTIALIASING,
                    java.awt.RenderingHints.VALUE_ANTIALIAS_ON
            );

            g2.setColor(fill);
            g2.fillRoundRect(x, y, width - 1, height - 1, arc, arc);
            g2.setColor(line);
            g2.setStroke(new java.awt.BasicStroke(thickness));
            g2.drawRoundRect(x, y, width - 1, height - 1, arc, arc);
            g2.dispose();
        }
    }
    // -----------------------------------------------------------

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jColorChooser1 = new javax.swing.JColorChooser();
        pnlBg = new javax.swing.JPanel();
        pnlTarejeta = new javax.swing.JPanel();
        lblUser = new javax.swing.JLabel();
        lblCorreo = new javax.swing.JLabel();
        lblRol = new javax.swing.JLabel();
        txtUser = new javax.swing.JTextField();
        txtEmail = new javax.swing.JTextField();
        cmbRol = new javax.swing.JComboBox<>();
        btnSave = new javax.swing.JButton();
        lblTitulo = new javax.swing.JLabel();
        btnBack = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        pnlBg.setBackground(javax.swing.UIManager.getDefaults().getColor("List.cellFocusColor"));
        pnlBg.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pnlTarejeta.setBackground(new java.awt.Color(235, 233, 223));

        lblUser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/userN.png"))); // NOI18N
        lblUser.setText("USUARIO:");

        lblCorreo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/EmailN.png"))); // NOI18N
        lblCorreo.setText("CORREO:");

        lblRol.setIcon(new javax.swing.ImageIcon(getClass().getResource("/RolN.png"))); // NOI18N
        lblRol.setText("      ROL:");

        cmbRol.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Administrador", "Cajero" }));

        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/GuardarN.png"))); // NOI18N
        btnSave.setText("Guardar");
        btnSave.addActionListener(this::btnSaveActionPerformed);

        javax.swing.GroupLayout pnlTarejetaLayout = new javax.swing.GroupLayout(pnlTarejeta);
        pnlTarejeta.setLayout(pnlTarejetaLayout);
        pnlTarejetaLayout.setHorizontalGroup(
            pnlTarejetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTarejetaLayout.createSequentialGroup()
                .addGroup(pnlTarejetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlTarejetaLayout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addGroup(pnlTarejetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblCorreo)
                            .addComponent(lblUser)
                            .addComponent(lblRol))
                        .addGap(18, 18, 18)
                        .addGroup(pnlTarejetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtUser, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbRol, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(pnlTarejetaLayout.createSequentialGroup()
                        .addGap(144, 144, 144)
                        .addComponent(btnSave)))
                .addContainerGap(39, Short.MAX_VALUE))
        );
        pnlTarejetaLayout.setVerticalGroup(
            pnlTarejetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTarejetaLayout.createSequentialGroup()
                .addGap(66, 66, 66)
                .addGroup(pnlTarejetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUser)
                    .addComponent(txtUser, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(42, 42, 42)
                .addGroup(pnlTarejetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCorreo)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(35, 35, 35)
                .addGroup(pnlTarejetaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRol)
                    .addComponent(cmbRol, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(38, 38, 38)
                .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(89, Short.MAX_VALUE))
        );

        pnlBg.add(pnlTarejeta, new org.netbeans.lib.awtextra.AbsoluteConstraints(473, 133, 380, 400));

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 22)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(255, 255, 255));
        lblTitulo.setText("Editar Usuario");
        pnlBg.add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(596, 58, -1, -1));

        btnBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/RegresarN.png"))); // NOI18N
        btnBack.setText("Regresar");
        btnBack.addActionListener(this::btnBackActionPerformed);
        pnlBg.add(btnBack, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 570, 155, 37));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/FondoTest6.png"))); // NOI18N
        pnlBg.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(-180, -30, -1, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlBg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlBg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        String nuevoUsuario = txtUser.getText().trim();
        String nuevoCorreo = txtEmail.getText().trim(); 
        String nuevoRol = cmbRol.getSelectedItem().toString();

        if (nuevoUsuario.isEmpty() || nuevoCorreo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor completa todos los campos.");
            return;
        }

        btnSave.setEnabled(false);
        try (var con = ConDB.getConnection()) {
            com.mycompany.benaedu.db.PermisosUsuario.guardar(con, usuarioOriginal, nuevoUsuario, nuevoCorreo, nuevoRol, seleccionActual());
                JOptionPane.showMessageDialog(this, "Usuario y permisos actualizados correctamente.");
                
                // Si todo sale bien, regresamos a la pantalla de gestión
                this.dispose();
                FlatMTMaterialLighterIJTheme.setup();
                GestionarUsuario gestionar = new GestionarUsuario();
                gestionar.setLocationRelativeTo(null);
                gestionar.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error en la base de datos: " + e.getMessage());
        } finally {
            btnSave.setEnabled(true);
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
// Simplemente cerramos esta ventana y volvemos a la tabla
        this.dispose();
        FlatMTMaterialLighterIJTheme.setup();
        GestionarUsuario gestionar = new GestionarUsuario();
        gestionar.setLocationRelativeTo(null);
        gestionar.setVisible(true);      
    }//GEN-LAST:event_btnBackActionPerformed

    /**
     * @param args the command line arguments
     */
    
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnSave;
    private javax.swing.JComboBox<String> cmbRol;
    private javax.swing.JColorChooser jColorChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lblCorreo;
    private javax.swing.JLabel lblRol;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblUser;
    private javax.swing.JPanel pnlBg;
    private javax.swing.JPanel pnlTarejeta;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtUser;
    // End of variables declaration//GEN-END:variables
}
