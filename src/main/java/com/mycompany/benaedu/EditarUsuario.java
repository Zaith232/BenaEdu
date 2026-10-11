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
        lblCorreo = new javax.swing.JLabel();
        lblRol = new javax.swing.JLabel();
        txtUser = new javax.swing.JTextField();
        cmbRol = new javax.swing.JComboBox<>();
        btnSave = new javax.swing.JButton();
        btnBack = new javax.swing.JButton();
        txtEmail = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        pnlBg.setBackground(new java.awt.Color(255, 255, 255));

        lblTitulo.setText("Editar Usuario");

        lblUser.setText("USUARIO:");

        lblCorreo.setText("CORREO:");

        lblRol.setText("ROL:");

        cmbRol.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Administrador", "Cajero" }));

        btnSave.setText("Guardar");
        btnSave.addActionListener(this::btnSaveActionPerformed);

        btnBack.setText("Regresar");
        btnBack.addActionListener(this::btnBackActionPerformed);

        javax.swing.GroupLayout pnlBgLayout = new javax.swing.GroupLayout(pnlBg);
        pnlBg.setLayout(pnlBgLayout);
        pnlBgLayout.setHorizontalGroup(
            pnlBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBgLayout.createSequentialGroup()
                .addGroup(pnlBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlBgLayout.createSequentialGroup()
                        .addGap(409, 409, 409)
                        .addComponent(lblTitulo))
                    .addGroup(pnlBgLayout.createSequentialGroup()
                        .addGap(269, 269, 269)
                        .addGroup(pnlBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblCorreo)
                            .addComponent(lblUser)
                            .addComponent(lblRol))
                        .addGap(18, 18, 18)
                        .addGroup(pnlBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtUser, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE)
                            .addGroup(pnlBgLayout.createSequentialGroup()
                                .addComponent(cmbRol, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(txtEmail, javax.swing.GroupLayout.DEFAULT_SIZE, 260, Short.MAX_VALUE))))
                .addGap(294, 294, 294))
            .addGroup(pnlBgLayout.createSequentialGroup()
                .addGap(390, 390, 390)
                .addComponent(btnSave)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlBgLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnBack)
                .addGap(65, 65, 65))
        );
        pnlBgLayout.setVerticalGroup(
            pnlBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBgLayout.createSequentialGroup()
                .addGroup(pnlBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(pnlBgLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblTitulo)
                        .addGap(130, 130, 130)
                        .addComponent(lblUser))
                    .addGroup(pnlBgLayout.createSequentialGroup()
                        .addGap(41, 41, 41)
                        .addComponent(btnBack)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtUser, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(pnlBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblCorreo)
                    .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(26, 26, 26)
                .addGroup(pnlBgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(cmbRol, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblRol))
                .addGap(46, 46, 46)
                .addComponent(btnSave)
                .addContainerGap(197, Short.MAX_VALUE))
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
    private javax.swing.JLabel lblCorreo;
    private javax.swing.JLabel lblRol;
    private javax.swing.JLabel lblTitulo;
    private javax.swing.JLabel lblUser;
    private javax.swing.JPanel pnlBg;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtUser;
    // End of variables declaration//GEN-END:variables
}
