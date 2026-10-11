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
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlBg = new javax.swing.JPanel();
        lblTitulo = new javax.swing.JLabel();
        lblUser = new javax.swing.JLabel();
        txtUser = new javax.swing.JTextField();
        lblPassword = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();
        lblCorreo = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        lblRol = new javax.swing.JLabel();
        cmbRol = new javax.swing.JComboBox<>();
        btnRegister = new javax.swing.JButton();
        btnLogin = new javax.swing.JButton();
        btnExisitngUsers = new javax.swing.JButton();
        lblName = new javax.swing.JLabel();
        lblLastName = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        txtLastNames = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        pnlBg.setBackground(new java.awt.Color(255, 255, 255));

        lblTitulo.setText("Crear Usuario");

        lblUser.setText("USUARIO:");

        lblPassword.setText("CONTRASEÑA:");

        lblCorreo.setText("CORREO:");

        lblRol.setText("ROL:");

        cmbRol.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Administrador", "Cajero" }));

        btnRegister.setText("REGISTRAR");
        btnRegister.addActionListener(this::btnRegisterActionPerformed);

        btnLogin.setText("INICIAR SESION");
        btnLogin.addActionListener(this::btnLoginActionPerformed);

        btnExisitngUsers.setText("GESTIONAR USUARIOS EXISTENTES");
        btnExisitngUsers.addActionListener(this::btnExisitngUsersActionPerformed);

        lblName.setText("NOMBRE:");

        lblLastName.setText("APELLIDOS:");

        javax.swing.GroupLayout pnlBgLayout = new javax.swing.GroupLayout(pnlBg);
        pnlBg.setLayout(pnlBgLayout);
        pnlBgLayout.setHorizontalGroup(
            pnlBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBgLayout.createSequentialGroup()
                .addGroup(pnlBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlBgLayout.createSequentialGroup()
                        .addGap(416, 416, 416)
                        .addComponent(lblTitulo))
                    .addGroup(pnlBgLayout.createSequentialGroup()
                        .addGap(249, 249, 249)
                        .addGroup(pnlBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlBgLayout.createSequentialGroup()
                                .addGap(31, 31, 31)
                                .addGroup(pnlBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(lblCorreo)
                                    .addComponent(lblRol))
                                .addGap(18, 18, 18)
                                .addGroup(pnlBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtEmail)
                                    .addGroup(pnlBgLayout.createSequentialGroup()
                                        .addGroup(pnlBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(btnRegister)
                                            .addComponent(cmbRol, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(0, 110, Short.MAX_VALUE))))
                            .addGroup(pnlBgLayout.createSequentialGroup()
                                .addGap(28, 28, 28)
                                .addGroup(pnlBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(pnlBgLayout.createSequentialGroup()
                                        .addComponent(lblUser)
                                        .addGap(18, 18, 18)
                                        .addComponent(txtUser))
                                    .addGroup(pnlBgLayout.createSequentialGroup()
                                        .addComponent(lblName)
                                        .addGap(18, 18, 18)
                                        .addComponent(txtName))))
                            .addGroup(pnlBgLayout.createSequentialGroup()
                                .addGroup(pnlBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(lblLastName)
                                    .addComponent(lblPassword))
                                .addGap(18, 18, 18)
                                .addGroup(pnlBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtPassword)
                                    .addComponent(txtLastNames))))))
                .addGap(286, 286, 286))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlBgLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlBgLayout.createSequentialGroup()
                        .addComponent(btnExisitngUsers)
                        .addGap(62, 62, 62))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlBgLayout.createSequentialGroup()
                        .addComponent(btnLogin)
                        .addGap(385, 385, 385))))
        );
        pnlBgLayout.setVerticalGroup(
            pnlBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBgLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTitulo)
                .addGap(71, 71, 71)
                .addGroup(pnlBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(pnlBgLayout.createSequentialGroup()
                        .addGroup(pnlBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblUser)
                            .addComponent(txtUser, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                .addGroup(pnlBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblLastName, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtLastNames, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtPassword, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblPassword, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtEmail, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblCorreo, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(18, 18, 18)
                .addGroup(pnlBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cmbRol, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblRol, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(39, 39, 39)
                .addComponent(btnRegister)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnLogin)
                .addGap(19, 19, 19)
                .addComponent(btnExisitngUsers)
                .addGap(43, 43, 43))
        );

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
