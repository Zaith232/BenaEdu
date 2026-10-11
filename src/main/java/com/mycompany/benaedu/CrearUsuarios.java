/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.benaedu;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMaterialLighterIJTheme;
import com.mycompany.benaedu.db.ConDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.swing.JOptionPane;
import org.mindrot.jbcrypt.BCrypt;
import java.awt.Window;
import javax.swing.SwingUtilities;
/**
 *
 * @author b17za
 */
public class CrearUsuarios extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(CrearUsuarios.class.getName());
    private final javax.swing.JComboBox<Compania> compania = new javax.swing.JComboBox<>();
    private final java.util.Map<String, javax.swing.JCheckBox> permisos = new java.util.LinkedHashMap<>();
    private final javax.swing.JButton marcarTodos = new javax.swing.JButton("Marcar todos");
    private final javax.swing.JButton quitarTodos = new javax.swing.JButton("Quitar todos");

    private record Compania(String codigo, String nombre) {
        @Override public String toString() { return codigo + " · " + nombre; }
    }

    /**
     * Creates new form CrearUsuarios
     */
    public CrearUsuarios() {
        initComponents();

        configurarAlta();
    }

    private void configurarAlta() {
        btnRegister.setEnabled(false);
        try (var con = ConDB.getConnection()) {
            com.mycompany.benaedu.db.Auditoria.exigirAdministrador(con);
            try (var st = con.createStatement(); var rs = st.executeQuery("SELECT DISTINCT CIA,NCIA FROM tmcias ORDER BY CIA")) {
                while (rs.next()) compania.addItem(new Compania(rs.getString(1), rs.getString(2)));
            }
        } catch (Exception ex) {
            throw new IllegalStateException("No se puede abrir el alta de usuarios: " + ex.getMessage(), ex);
        }
        javax.swing.JPanel campos = new javax.swing.JPanel(new java.awt.GridLayout(0, 2, 10, 8));
        campos.setOpaque(false);
        javax.swing.JLabel[] etiquetas = {lblUser, lblName, lblLastName, lblPassword, lblCorreo, lblRol, new javax.swing.JLabel("COMPAÑÍA:")};
        javax.swing.JComponent[] entradas = {txtUser, txtName, txtLastNames, txtPassword, txtEmail, cmbRol, compania};
        for (int i = 0; i < etiquetas.length; i++) { campos.add(etiquetas[i]); campos.add(entradas[i]); }
        javax.swing.JPanel lista = new javax.swing.JPanel();
        lista.setBackground(pnlBg.getBackground());
        lista.setLayout(new javax.swing.BoxLayout(lista, javax.swing.BoxLayout.Y_AXIS));
        String grupoAnterior = "";
        for (var apartado : com.mycompany.benaedu.db.PermisosUsuario.APARTADOS) {
            if (!grupoAnterior.equals(apartado.grupo())) {
                javax.swing.JLabel grupo = new javax.swing.JLabel(apartado.grupo());
                grupo.setFont(grupo.getFont().deriveFont(java.awt.Font.BOLD));
                grupo.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 4, 4, 4));
                lista.add(grupo); grupoAnterior = apartado.grupo();
            }
            javax.swing.JCheckBox casilla = new javax.swing.JCheckBox(apartado.nombre());
            casilla.setOpaque(false); lista.add(casilla); permisos.put(apartado.id(), casilla);
        }
        javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(lista);
        scroll.setBorder(javax.swing.BorderFactory.createTitledBorder("Apartados permitidos"));
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        javax.swing.JPanel acciones = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        acciones.setOpaque(false); acciones.add(marcarTodos); acciones.add(quitarTodos);
        javax.swing.JPanel seleccion = new javax.swing.JPanel(new java.awt.BorderLayout());
        seleccion.setOpaque(false); seleccion.add(acciones, java.awt.BorderLayout.NORTH); seleccion.add(scroll, java.awt.BorderLayout.CENTER);
        marcarTodos.addActionListener(e -> permisos.values().forEach(c -> { if (c.isEnabled()) c.setSelected(true); }));
        quitarTodos.addActionListener(e -> permisos.values().forEach(c -> { if (c.isEnabled()) c.setSelected(false); }));
        javax.swing.JPanel superior = new javax.swing.JPanel(new java.awt.BorderLayout(8, 12));
        superior.setOpaque(false); superior.add(lblTitulo, java.awt.BorderLayout.NORTH); superior.add(campos, java.awt.BorderLayout.CENTER);
        javax.swing.JPanel inferior = new javax.swing.JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        inferior.setOpaque(false); inferior.add(btnExisitngUsers); inferior.add(btnLogin); inferior.add(btnRegister);
        pnlBg.removeAll(); pnlBg.setLayout(new java.awt.BorderLayout(12, 12));
        pnlBg.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 24, 16, 24));
        pnlBg.add(superior, java.awt.BorderLayout.NORTH); pnlBg.add(seleccion, java.awt.BorderLayout.CENTER);
        pnlBg.add(inferior, java.awt.BorderLayout.SOUTH);
        cmbRol.addActionListener(e -> actualizarRolAlta());
        cmbRol.setSelectedItem("Cajero"); actualizarRolAlta();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(760, 640)); setSize(893, 720);
    }

    private void actualizarRolAlta() {
        boolean cajero = "Cajero".equals(cmbRol.getSelectedItem());
        compania.setEnabled(cajero); marcarTodos.setEnabled(cajero); quitarTodos.setEnabled(cajero);
        for (var apartado : com.mycompany.benaedu.db.PermisosUsuario.APARTADOS) {
            permisos.get(apartado.id()).setEnabled(cajero && !apartado.soloAdministrador());
        }
        btnRegister.setEnabled(!cajero || compania.getItemCount() > 0);
        AplicarDiseñoPersonalizado();

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void AplicarDiseñoPersonalizado() {
    java.awt.Color fondo = new java.awt.Color(248, 250, 252);
    java.awt.Color tarjeta = new java.awt.Color(255, 255, 255);

    java.awt.Color azulOscuro = new java.awt.Color(15, 53, 90);
    java.awt.Color azulPrincipal = new java.awt.Color(31, 86, 145);
    java.awt.Color azulHover = new java.awt.Color(24, 68, 116);

    java.awt.Color rojo = new java.awt.Color(218, 46, 61);
    java.awt.Color rojoHover = new java.awt.Color(185, 35, 49);

    java.awt.Color texto = new java.awt.Color(35, 55, 75);
    java.awt.Color borde = new java.awt.Color(220, 226, 232);
    
    //
    pnlBg.setBackground(fondo);

    jPanel1.setOpaque(true);
    jPanel1.setBackground(tarjeta);
    jPanel1.setBorder(
        javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(
                        borde, 1, true
                ),
                javax.swing.BorderFactory.createEmptyBorder(
                        24, 30, 24, 30
                )
        )
    );
    
    //
    java.awt.Font fuenteEtiqueta =
        new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14);

    lblTitulo.setForeground(azulOscuro);
    lblTitulo.setFont(
        new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 28)
    );

    lblUser.setForeground(azulOscuro);
    lblName.setForeground(azulOscuro);
    lblLastName.setForeground(azulOscuro);
    lblPassword.setForeground(azulOscuro);
    lblCorreo.setForeground(azulOscuro);
    lblRol.setForeground(azulOscuro);

    lblUser.setFont(fuenteEtiqueta);
    lblName.setFont(fuenteEtiqueta);
    lblLastName.setFont(fuenteEtiqueta);
    lblPassword.setFont(fuenteEtiqueta);
    lblCorreo.setFont(fuenteEtiqueta);
    lblRol.setFont(fuenteEtiqueta);
    
    //
    java.awt.Font fuenteCampo =
        new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 14);

    javax.swing.border.Border bordeCampo =
        javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(
                        borde, 1, true
                ),
                javax.swing.BorderFactory.createEmptyBorder(
                        8, 12, 8, 12
                )
        );

    txtUser.setBackground(java.awt.Color.WHITE);
    txtUser.setForeground(texto);
    txtUser.setFont(fuenteCampo);
    txtUser.setBorder(bordeCampo);

    txtName.setBackground(java.awt.Color.WHITE);
    txtName.setForeground(texto);
    txtName.setFont(fuenteCampo);
    txtName.setBorder(bordeCampo);

    txtLastNames.setBackground(java.awt.Color.WHITE);
    txtLastNames.setForeground(texto);
    txtLastNames.setFont(fuenteCampo);
    txtLastNames.setBorder(bordeCampo);

    txtPassword.setBackground(java.awt.Color.WHITE);
    txtPassword.setForeground(texto);
    txtPassword.setFont(fuenteCampo);
    txtPassword.setBorder(bordeCampo);

    txtEmail.setBackground(java.awt.Color.WHITE);
    txtEmail.setForeground(texto);
    txtEmail.setFont(fuenteCampo);
    txtEmail.setBorder(bordeCampo);

    cmbRol.setBackground(java.awt.Color.WHITE);
    cmbRol.setForeground(texto);
    cmbRol.setFont(fuenteCampo);
    cmbRol.setBorder(bordeCampo);
    
    //
    estilizarBotonRedondeado(
        btnRegister,
        rojo,
        rojoHover,
        java.awt.Color.WHITE
    );
    
    estilizarBotonContorno(
        btnLogin,
        azulPrincipal,
        texto
    );

    estilizarBotonContorno(
        btnExisitngUsers,
        azulPrincipal,
        texto
    );
}
    //
    
    private void estilizarBotonRedondeado(
        javax.swing.JButton boton,
        java.awt.Color colorNormal,
        java.awt.Color colorHover,
        java.awt.Color colorTexto) {

    boton.setForeground(colorTexto);
    boton.setFont(
            new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14)
    );
    boton.setFocusPainted(false);
    boton.setContentAreaFilled(false);
    boton.setOpaque(false);
    boton.setRolloverEnabled(true);
    boton.setBorder(
            javax.swing.BorderFactory.createEmptyBorder(
                    10, 22, 10, 22
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

            java.awt.Color colorActual = colorNormal;

            if (botonActual.getModel().isRollover()) {
                colorActual = colorHover;
            }

            g2.setColor(colorActual);
            g2.fillRoundRect(
                    0,
                    0,
                    componente.getWidth(),
                    componente.getHeight(),
                    20,
                    20
            );

            g2.dispose();

            super.paint(g, componente);
        }
    });
}
    //
    private void estilizarBotonContorno(
        javax.swing.JButton boton,
        java.awt.Color colorBorde,
        java.awt.Color colorTexto) {

    boton.setForeground(colorTexto);
    boton.setFont(
            new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14)
    );
    boton.setFocusPainted(false);
    boton.setContentAreaFilled(false);
    boton.setOpaque(false);
    boton.setRolloverEnabled(true);
    boton.setBorder(
            javax.swing.BorderFactory.createEmptyBorder(
                    10, 22, 10, 22
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

            java.awt.Color fondoBoton =
                    java.awt.Color.WHITE;

            if (botonActual.getModel().isRollover()) {
                fondoBoton = new java.awt.Color(
                        235, 243, 250
                );
            }

            g2.setColor(fondoBoton);
            g2.fillRoundRect(
                    0,
                    0,
                    componente.getWidth(),
                    componente.getHeight(),
                    20,
                    20
            );

            g2.setColor(colorBorde);
            g2.drawRoundRect(
                    0,
                    0,
                    componente.getWidth() - 1,
                    componente.getHeight() - 1,
                    20,
                    20
            );

            g2.dispose();

            super.paint(g, componente);
        }
    });
}
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlBg = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        btnRegister = new javax.swing.JButton();
        btnLogin = new javax.swing.JButton();
        btnExisitngUsers = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        lblUser = new javax.swing.JLabel();
        txtUser = new javax.swing.JTextField();
        lblName = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        lblLastName = new javax.swing.JLabel();
        txtLastNames = new javax.swing.JTextField();
        lblPassword = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();
        txtEmail = new javax.swing.JTextField();
        lblCorreo = new javax.swing.JLabel();
        cmbRol = new javax.swing.JComboBox<>();
        lblRol = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        pnlBg.setBackground(new java.awt.Color(255, 255, 255));
        pnlBg.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        lblTitulo.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        lblTitulo.setForeground(new java.awt.Color(13, 41, 72));
        lblTitulo.setText("Crear Usuario");
        pnlBg.add(lblTitulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 30, -1, -1));

        btnRegister.setBackground(new java.awt.Color(215, 25, 45));
        btnRegister.setForeground(new java.awt.Color(255, 255, 255));
        btnRegister.setIcon(new javax.swing.ImageIcon(getClass().getResource("/AddUserW.png"))); // NOI18N
        btnRegister.setText("REGISTRAR");
        btnRegister.addActionListener(this::btnRegisterActionPerformed);
        pnlBg.add(btnRegister, new org.netbeans.lib.awtextra.AbsoluteConstraints(890, 570, -1, -1));

        btnLogin.setForeground(new java.awt.Color(13, 41, 72));
        btnLogin.setText("INICIAR SESION");
        btnLogin.addActionListener(this::btnLoginActionPerformed);
        pnlBg.add(btnLogin, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 570, 170, 40));

        btnExisitngUsers.setForeground(new java.awt.Color(13, 41, 72));
        btnExisitngUsers.setText("GESTIONAR USUARIOS EXISTENTES");
        btnExisitngUsers.addActionListener(this::btnExisitngUsersActionPerformed);
        pnlBg.add(btnExisitngUsers, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 570, 290, 40));

        lblUser.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblUser.setForeground(new java.awt.Color(13, 41, 72));
        lblUser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/UserB.png"))); // NOI18N
        lblUser.setText("USUARIO:");

        lblName.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblName.setForeground(new java.awt.Color(13, 41, 72));
        lblName.setIcon(new javax.swing.ImageIcon(getClass().getResource("/NameB.png"))); // NOI18N
        lblName.setText("NOMBRE:");

        lblLastName.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblLastName.setForeground(new java.awt.Color(13, 41, 72));
        lblLastName.setIcon(new javax.swing.ImageIcon(getClass().getResource("/LastNamesB.png"))); // NOI18N
        lblLastName.setText("APELLIDOS:");

        lblPassword.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblPassword.setForeground(new java.awt.Color(13, 41, 72));
        lblPassword.setIcon(new javax.swing.ImageIcon(getClass().getResource("/PasswordB.png"))); // NOI18N
        lblPassword.setText("CONTRASEÑA:");

        lblCorreo.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblCorreo.setForeground(new java.awt.Color(13, 41, 72));
        lblCorreo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/EmailB.png"))); // NOI18N
        lblCorreo.setText("CORREO:");

        cmbRol.setForeground(new java.awt.Color(13, 41, 72));
        cmbRol.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Administrador", "Cajero" }));

        lblRol.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        lblRol.setForeground(new java.awt.Color(13, 41, 72));
        lblRol.setIcon(new javax.swing.ImageIcon(getClass().getResource("/RolsB.png"))); // NOI18N
        lblRol.setText("ROL:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(115, 115, 115)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(28, 28, 28)
                                .addComponent(txtLastNames, javax.swing.GroupLayout.PREFERRED_SIZE, 340, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(txtName, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 340, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtUser, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 340, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(cmbRol, javax.swing.GroupLayout.PREFERRED_SIZE, 340, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 340, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 340, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(138, 138, 138))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblUser)
                            .addComponent(lblName)
                            .addComponent(lblLastName))
                        .addGap(357, 357, 357)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblRol)
                            .addComponent(lblCorreo)
                            .addComponent(lblPassword))
                        .addContainerGap(453, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(52, 52, 52)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUser, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPassword))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtUser, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 72, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblName)
                    .addComponent(lblCorreo))
                .addGap(20, 20, 20)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblLastName)
                    .addComponent(lblRol))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtLastNames, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbRol, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(37, 37, 37))
        );

        pnlBg.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 110, 1190, 430));

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

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoginActionPerformed
           this.dispose(); // Cierra la ventana actual
    FlatMTMaterialLighterIJTheme.setup();
    Login login = new Login();
    login.setSize(893, 540);
    login.setLocationRelativeTo(null);
    login.setVisible(true);
    }//GEN-LAST:event_btnLoginActionPerformed

    private void btnExisitngUsersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExisitngUsersActionPerformed
          this.dispose(); // Cierra la ventana actual
    FlatMTMaterialLighterIJTheme.setup();
    GestionarUsuario gestionarUsuario = new GestionarUsuario();
    gestionarUsuario.setSize(893, 540);
    gestionarUsuario.setLocationRelativeTo(null);
    gestionarUsuario.setVisible(true);
    }//GEN-LAST:event_btnExisitngUsersActionPerformed

    private void btnRegisterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRegisterActionPerformed
    // 1. Obtener los valores de los campos (incluyendo los nuevos)
        String usuario = txtUser.getText().trim();
        String nombre = txtName.getText().trim();
        String apellidos = txtLastNames.getText().trim();
        String clave = new String(txtPassword.getPassword()).trim();
        String correo = txtEmail.getText().trim();
        String rol = cmbRol.getSelectedItem().toString();

        // 2. Validar que no haya campos vacíos
        if (usuario.isEmpty() || nombre.isEmpty() || apellidos.isEmpty() || clave.isEmpty() || correo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor completa todos los campos.");
            return;
        }

        java.util.Set<String> seleccion = new java.util.LinkedHashSet<>();
        permisos.forEach((id, casilla) -> { if (casilla.isEnabled() && casilla.isSelected()) seleccion.add(id); });
        Compania seleccionada = (Compania) compania.getSelectedItem();
        btnRegister.setEnabled(false);
        try (var con = ConDB.getConnection()) {
            var alta = com.mycompany.benaedu.services.UsuariosCajeros.crear(con, usuario, nombre, apellidos,
                    clave, correo, rol, seleccionada == null ? null : seleccionada.codigo(), seleccion);
            JOptionPane.showMessageDialog(this, "Usuario registrado correctamente."
                    + (alta.numeroCajero() == null ? "" : "\nCajero número: " + alta.numeroCajero()
                            + "\nCompañía: " + seleccionada));
            txtUser.setText(""); txtName.setText(""); txtLastNames.setText("");
            txtPassword.setText(""); txtEmail.setText("");
            permisos.values().forEach(c -> c.setSelected(false));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo registrar el usuario: " + ex.getMessage(), "Alta de usuario", JOptionPane.ERROR_MESSAGE);
        } finally {
            actualizarRolAlta();
        }
    }//GEN-LAST:event_btnRegisterActionPerformed

    /**
     * @param args the command line arguments
     */


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExisitngUsers;
    private javax.swing.JButton btnLogin;
    private javax.swing.JButton btnRegister;
    private javax.swing.JComboBox<String> cmbRol;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel lblCorreo;
    private javax.swing.JLabel lblLastName;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblPassword;
    private javax.swing.JLabel lblRol;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblUser;
    private javax.swing.JPanel pnlBg;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtLastNames;
    private javax.swing.JTextField txtName;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUser;
    // End of variables declaration//GEN-END:variables
}
