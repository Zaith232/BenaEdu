/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.benaedu.views;
import com.mycompany.benaedu.db.ConDB;
import java.awt.Window;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author b17za
 */
public class Catalogo_Cajeros extends javax.swing.JPanel {

    /**
     * Creates new form Catalogo_Cajeros
     */
    public Catalogo_Cajeros() {
        initComponents();
        cargarTablaCajeros();
    }
private void cargarTablaCajeros() {
    DefaultTableModel modelo = new DefaultTableModel(
        new Object[][] {}, 
        new String[] {"Compañía", "Número", "Nombre", "Estatus", "Teléfono", "Ciudad", "Estado", "Clasif. 1", "Clasif. 2", "Clasif. 3", "Clasif. 4", "Clasif. 5", "Usuario", "Fecha Mod.", "Hora Mod."}
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false; 
        }
    };
    tblCCajeros.setModel(modelo);
    tblCCajeros.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

    try {
        ConDB db = new ConDB();
        Connection con = db.Conectar();

        if (con != null) {
            // Se actualizó e.CD para la Ciudad/Población
            String sql = "SELECT c.CIA, c.NEMP, COALESCE(e.NOME, '') AS NOMBRE, c.ECAJ, " +
                         "COALESCE(e.TEL, '') AS TEL, COALESCE(e.CD, '') AS POB, COALESCE(e.EDO, '') AS EDO, " +
                         "COALESCE(c.CCA01, '') AS CLS1, COALESCE(c.CCA02, '') AS CLS2, COALESCE(c.CCA03, '') AS CLS3, " +
                         "COALESCE(c.CCA04, '') AS CLS4, COALESCE(c.CCA05, '') AS CLS5, " +
                         "c.USER, c.FEAC, c.HOAC " +
                         "FROM tescaj c " +
                         "LEFT JOIN tgemp e ON c.CIA = e.CIA AND c.NEMP = e.NEMP " +
                         "ORDER BY CAST(c.NEMP AS UNSIGNED) ASC";
            
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Object[] fila = new Object[15]; 
                fila[0] = rs.getString("CIA");
                fila[1] = rs.getString("NEMP");
                fila[2] = rs.getString("NOMBRE");
                fila[3] = rs.getString("ECAJ"); // Muestra 'A' o 'I'
                fila[4] = rs.getString("TEL");
                fila[5] = rs.getString("POB");
                fila[6] = rs.getString("EDO");
                fila[7] = rs.getString("CLS1");
                fila[8] = rs.getString("CLS2");
                fila[9] = rs.getString("CLS3");
                fila[10] = rs.getString("CLS4");
                fila[11] = rs.getString("CLS5");
                fila[12] = rs.getString("USER");
                
                // Formato de fecha dd/MM/yyyy
                String fechaRaw = rs.getString("FEAC");
                if (fechaRaw != null && fechaRaw.contains("-")) {
                    String[] partes = fechaRaw.split("-");
                    if (partes.length == 3) {
                        fechaRaw = partes[2] + "/" + partes[1] + "/" + partes[0];
                    }
                }
                fila[13] = fechaRaw;
                fila[14] = rs.getString("HOAC");

                modelo.addRow(fila);
            }
            rs.close(); ps.close(); db.Cerrar();

            // Ajuste dinámico del ancho de columnas
            tblCCajeros.getColumnModel().getColumn(0).setPreferredWidth(70);  // Compañía
            tblCCajeros.getColumnModel().getColumn(1).setPreferredWidth(60);  // Número
            tblCCajeros.getColumnModel().getColumn(2).setPreferredWidth(230); // Nombre
            tblCCajeros.getColumnModel().getColumn(3).setPreferredWidth(60);  // Estatus
            tblCCajeros.getColumnModel().getColumn(4).setPreferredWidth(100); // Teléfono
            tblCCajeros.getColumnModel().getColumn(5).setPreferredWidth(60);  // Ciudad
            tblCCajeros.getColumnModel().getColumn(6).setPreferredWidth(60);  // Estado
            tblCCajeros.getColumnModel().getColumn(12).setPreferredWidth(90); // Usuario
            tblCCajeros.getColumnModel().getColumn(13).setPreferredWidth(90); // Fecha Mod.
            tblCCajeros.getColumnModel().getColumn(14).setPreferredWidth(90); // Hora Mod.
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Error al cargar los cajeros: " + e.getMessage());
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

        jPanel1 = new javax.swing.JPanel();
        btnAddCCajero = new javax.swing.JButton();
        btnEditCCajero = new javax.swing.JButton();
        btnDeleteCCajero = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblCCajeros = new javax.swing.JTable();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        btnAddCCajero.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAddCCajero.setForeground(new java.awt.Color(26, 61, 99));
        btnAddCCajero.setIcon(new javax.swing.ImageIcon(getClass().getResource("/add.png"))); // NOI18N
        btnAddCCajero.setText("Añadir");
        btnAddCCajero.addActionListener(this::btnAddCCajeroActionPerformed);

        btnEditCCajero.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEditCCajero.setForeground(new java.awt.Color(26, 61, 99));
        btnEditCCajero.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edit.png"))); // NOI18N
        btnEditCCajero.setText("Editar");
        btnEditCCajero.setMaximumSize(new java.awt.Dimension(93, 31));
        btnEditCCajero.setMinimumSize(new java.awt.Dimension(93, 31));
        btnEditCCajero.addActionListener(this::btnEditCCajeroActionPerformed);

        btnDeleteCCajero.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDeleteCCajero.setForeground(new java.awt.Color(26, 61, 99));
        btnDeleteCCajero.setIcon(new javax.swing.ImageIcon(getClass().getResource("/delete.png"))); // NOI18N
        btnDeleteCCajero.setText("Eliminar");
        btnDeleteCCajero.addActionListener(this::btnDeleteCCajeroActionPerformed);

        tblCCajeros.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Compañia", "Numero", "Nombre", "Estatus", "Telefono", "Ciudad", "Estado", "Clas. 1", "Clas. 2", "Clas. 3", "Clas. 4", "Clas. 5", "Usuario", "Fecha. Mod. ", "Hora Mod."
            }
        ));
        jScrollPane1.setViewportView(tblCCajeros);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAddCCajero)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditCCajero, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteCCajero)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddCCajero, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditCCajero, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteCCajero, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddCCajeroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddCCajeroActionPerformed
       mostrarDialogoCajero(false);
    }//GEN-LAST:event_btnAddCCajeroActionPerformed

    private void btnEditCCajeroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditCCajeroActionPerformed
       if (tblCCajeros.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un cajero para editar.");
            return;
        }
        mostrarDialogoCajero(true);
    }//GEN-LAST:event_btnEditCCajeroActionPerformed

    private void btnDeleteCCajeroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteCCajeroActionPerformed
    int fila = tblCCajeros.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un cajero para eliminar.");
            return;
        }

        String compania = tblCCajeros.getValueAt(fila, 0).toString();
        String numEmp = tblCCajeros.getValueAt(fila, 1).toString();
        String nombre = tblCCajeros.getValueAt(fila, 3).toString();
        
        int resp = JOptionPane.showConfirmDialog(this, "¿Desea quitar el perfil de cajero a: " + nombre + "?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        
        if (resp == JOptionPane.YES_OPTION) {
            try {
                ConDB db = new ConDB();
                Connection con = db.Conectar();
                if (con != null) {
                    con.setAutoCommit(false); // Iniciar Transacción

                    // 1. Quitar la marca de cajero en tgemp
                    String sqlEmp = "UPDATE tgemp SET CAJ = 'N' WHERE CIA = ? AND NEMP = ?";
                    PreparedStatement psEmp = con.prepareStatement(sqlEmp);
                    psEmp.setString(1, compania);
                    psEmp.setString(2, numEmp);
                    psEmp.executeUpdate();
                    psEmp.close();

                    // 2. Eliminar el perfil de cajero en tescaj
                    String sqlCaj = "DELETE FROM tescaj WHERE CIA = ? AND NEMP = ?";
                    PreparedStatement psCaj = con.prepareStatement(sqlCaj);
                    psCaj.setString(1, compania);
                    psCaj.setString(2, numEmp);
                    psCaj.executeUpdate();
                    psCaj.close();

                    con.commit();
                    db.Cerrar();

                    JOptionPane.showMessageDialog(this, "Cajero eliminado correctamente.");
                    cargarTablaCajeros();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar cajero: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_btnDeleteCCajeroActionPerformed

private void mostrarDialogoCajero(boolean modoEdicion) {
        Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
        String tituloVentana = modoEdicion ? "Modificar Cajero" : "Agregar Cajero";

        JDialog dialogo = new JDialog((java.awt.Frame) ventanaPadre, tituloVentana, true);
        dialogo.setSize(520, 560);
        dialogo.setLayout(null);
        dialogo.setResizable(false);

        // --- 1. SECCIÓN SUPERIOR ---
        JLabel lblTitCia = new JLabel("Compañía");
        lblTitCia.setBounds(20, 15, 80, 20);
        JComboBox<String> cmbCia = new JComboBox<>();
        cmbCia.setBounds(20, 35, 80, 25);

        try {
            ConDB db = new ConDB();
            Connection con = db.Conectar();
            if (con != null) {
                ResultSet rsCia = con.prepareStatement("SELECT CIA FROM tmcias ORDER BY CIA").executeQuery();
                while(rsCia.next()) cmbCia.addItem(rsCia.getString("CIA"));
                rsCia.close(); db.Cerrar();
            }
        } catch (Exception ex) { cmbCia.addItem("12"); }

        JLabel lblTitNum = new JLabel("Núm. Emp.");
        lblTitNum.setBounds(120, 15, 80, 20);
        JTextField txtNumEmp = new JTextField();
        txtNumEmp.setBounds(120, 35, 90, 25);

        JLabel lblTitRfc = new JLabel("RFC:");
        lblTitRfc.setBounds(230, 15, 80, 20);
        JTextField txtRfc = new JTextField();
        txtRfc.setBounds(230, 35, 250, 25);

        JLabel lblNombre = new JLabel("Nombre");
        lblNombre.setBounds(20, 75, 60, 25);
        JTextField txtNombre = new JTextField();
        txtNombre.setBounds(80, 75, 400, 25);

        if (modoEdicion) {
            cmbCia.setEnabled(false);
            txtNumEmp.setEditable(false); 
        }

        dialogo.add(lblTitCia); dialogo.add(cmbCia);
        dialogo.add(lblTitNum); dialogo.add(txtNumEmp);
        dialogo.add(lblTitRfc); dialogo.add(txtRfc);
        dialogo.add(lblNombre); dialogo.add(txtNombre);

        // --- 2. PESTAÑAS (TABS) ---
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setBounds(15, 120, 475, 330);

        // >> PESTAÑA 1: DATOS GENERALES
        JPanel pnlGenerales = new JPanel(null);
        JPanel pnlInterior = new JPanel(null);
        pnlInterior.setBorder(BorderFactory.createEtchedBorder());
        pnlInterior.setBounds(15, 15, 440, 270);

        JLabel lblCalle = new JLabel("Calle"); lblCalle.setBounds(20, 20, 80, 25);
        JTextField txtCalle = new JTextField(); txtCalle.setBounds(100, 20, 320, 25);

        JLabel lblColonia = new JLabel("Colonia"); lblColonia.setBounds(20, 55, 80, 25);
        JTextField txtColonia = new JTextField(); txtColonia.setBounds(100, 55, 320, 25);

        JLabel lblPob = new JLabel("Población"); lblPob.setBounds(20, 95, 80, 25);
        JComboBox<String> cmbPob = new JComboBox<>(new String[]{"TEH", ""});
        cmbPob.setEditable(true); cmbPob.setBounds(100, 95, 70, 25);
        JLabel lblPobDesc = new JLabel("TEHUACAN"); lblPobDesc.setBounds(180, 95, 200, 25);

        JLabel lblEstado = new JLabel("Estado"); lblEstado.setBounds(20, 130, 80, 25);
        JComboBox<String> cmbEstado = new JComboBox<>(new String[]{"PUE", ""});
        cmbEstado.setEditable(true); cmbEstado.setBounds(100, 130, 70, 25);
        JLabel lblEstadoDesc = new JLabel("PUEBLA"); lblEstadoDesc.setBounds(180, 130, 200, 25);

        JLabel lblPais = new JLabel("País"); lblPais.setBounds(20, 165, 80, 25);
        JComboBox<String> cmbPais = new JComboBox<>(new String[]{"MEX", ""});
        cmbPais.setEditable(true); cmbPais.setBounds(100, 165, 70, 25);
        JLabel lblPaisDesc = new JLabel("MEXICO"); lblPaisDesc.setBounds(180, 165, 200, 25);

        JLabel lblCP = new JLabel("C Postal"); lblCP.setBounds(20, 200, 80, 25);
        JTextField txtCP = new JTextField(); txtCP.setBounds(100, 200, 80, 25);

        JLabel lblEstatus = new JLabel("Estatus"); lblEstatus.setBounds(240, 200, 60, 25);
        JComboBox<String> cmbEstatus = new JComboBox<>(new String[]{"A", "I"});
        cmbEstatus.setBounds(300, 200, 50, 25);

        JLabel lblTel = new JLabel("Teléfono"); lblTel.setBounds(20, 235, 80, 25);
        JTextField txtTel = new JTextField(); txtTel.setBounds(100, 235, 320, 25);

        pnlInterior.add(lblCalle); pnlInterior.add(txtCalle);
        pnlInterior.add(lblColonia); pnlInterior.add(txtColonia);
        pnlInterior.add(lblPob); pnlInterior.add(cmbPob); pnlInterior.add(lblPobDesc);
        pnlInterior.add(lblEstado); pnlInterior.add(cmbEstado); pnlInterior.add(lblEstadoDesc);
        pnlInterior.add(lblPais); pnlInterior.add(cmbPais); pnlInterior.add(lblPaisDesc);
        pnlInterior.add(lblCP); pnlInterior.add(txtCP);
        pnlInterior.add(lblEstatus); pnlInterior.add(cmbEstatus); 
        pnlInterior.add(lblTel); pnlInterior.add(txtTel);

        pnlGenerales.add(pnlInterior);

        // >> PESTAÑA 2: INFORMACIÓN ADICIONAL
        JPanel pnlInfoAdicional = new JPanel(null);
        JPanel pnlClasificaciones = new JPanel(null);
        pnlClasificaciones.setBorder(BorderFactory.createTitledBorder("Clasificaciones"));
        pnlClasificaciones.setBounds(15, 15, 440, 270);

        JLabel lblTitClas = new JLabel("Clasificaciones"); lblTitClas.setBounds(110, 20, 100, 20);
        JLabel lblTitDesc = new JLabel("Descripción"); lblTitDesc.setBounds(250, 20, 100, 20);
        pnlClasificaciones.add(lblTitClas); pnlClasificaciones.add(lblTitDesc);

        String[] nombresClas = {"Sucursal", "Clasificación 2", "Clasificación 3", "Clasificación 4", "Clasificación 5"};
        JComboBox[] combosClas = new JComboBox[5];
        
        int yOffset = 50;
        for (int i = 0; i < 5; i++) {
            JLabel lblClas = new JLabel(nombresClas[i]);
            lblClas.setBounds(20, yOffset, 90, 25);
            
            combosClas[i] = new JComboBox<>(new String[]{"", "12100", "12200", "12300", "12400"}); 
            combosClas[i].setEditable(true);
            combosClas[i].setBounds(110, yOffset, 100, 25);
            
            JLabel lblDescClas = new JLabel("..."); 
            lblDescClas.setBounds(250, yOffset, 180, 25);
            
            pnlClasificaciones.add(lblClas);
            pnlClasificaciones.add(combosClas[i]);
            pnlClasificaciones.add(lblDescClas);
            
            yOffset += 35;
        }

        pnlInfoAdicional.add(pnlClasificaciones);

        pestanas.addTab("Datos Generales", pnlGenerales);
        pestanas.addTab("Información Adicional", pnlInfoAdicional);
        dialogo.add(pestanas);

        // --- 3. BOTONES INFERIORES ---
        JButton btnAceptar = new JButton("Aceptar");
        btnAceptar.setBounds(130, 465, 100, 40);
        JButton btnSalir = new JButton("Salir");
        btnSalir.setBounds(260, 465, 100, 40);

        dialogo.add(btnAceptar);
        dialogo.add(btnSalir);

        // --- 4. SI ES MODO EDICIÓN, CARGAR DATOS DE tgemp Y tescaj ---
        if (modoEdicion) {
            int fila = tblCCajeros.getSelectedRow();
            String cia = tblCCajeros.getValueAt(fila, 0).toString();
            String numEmp = tblCCajeros.getValueAt(fila, 1).toString();
            
            cmbCia.setSelectedItem(cia);
            txtNumEmp.setText(numEmp);
            
            try {
                ConDB db = new ConDB();
                Connection con = db.Conectar();
                if (con != null) {
                    String sql = "SELECT e.NOME, e.RFC, c.ECAJ, e.CALLE, e.COL, e.POB, e.EDO, e.PAIS, e.CP, e.TEL, " +
                                 "c.CCA01, c.CCA02, c.CCA03, c.CCA04, c.CCA05 " +
                                 "FROM tescaj c " +
                                 "INNER JOIN tgemp e ON c.CIA = e.CIA AND c.NEMP = e.NEMP " +
                                 "WHERE c.CIA = ? AND c.NEMP = ?";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, cia);
                    ps.setString(2, numEmp);
                    ResultSet rs = ps.executeQuery();
                    
                    if (rs.next()) {
                        txtNombre.setText(rs.getString("NOME") != null ? rs.getString("NOME") : "");
                        txtRfc.setText(rs.getString("RFC") != null ? rs.getString("RFC") : "");
                        cmbEstatus.setSelectedItem(rs.getString("ECAJ") != null ? rs.getString("ECAJ") : "A");
                        txtCalle.setText(rs.getString("CALLE") != null ? rs.getString("CALLE") : "");
                        txtColonia.setText(rs.getString("COL") != null ? rs.getString("COL") : "");
                        cmbPob.setSelectedItem(rs.getString("POB") != null ? rs.getString("POB") : "");
                        cmbEstado.setSelectedItem(rs.getString("EDO") != null ? rs.getString("EDO") : "");
                        cmbPais.setSelectedItem(rs.getString("PAIS") != null ? rs.getString("PAIS") : "");
                        txtCP.setText(rs.getString("CP") != null ? rs.getString("CP") : "");
                        txtTel.setText(rs.getString("TEL") != null ? rs.getString("TEL") : "");
                        
                        combosClas[0].setSelectedItem(rs.getString("CCA01") != null ? rs.getString("CCA01") : "");
                        combosClas[1].setSelectedItem(rs.getString("CCA02") != null ? rs.getString("CCA02") : "");
                        combosClas[2].setSelectedItem(rs.getString("CCA03") != null ? rs.getString("CCA03") : "");
                        combosClas[3].setSelectedItem(rs.getString("CCA04") != null ? rs.getString("CCA04") : "");
                        combosClas[4].setSelectedItem(rs.getString("CCA05") != null ? rs.getString("CCA05") : "");
                    }
                    rs.close(); ps.close(); db.Cerrar();
                }
            } catch (Exception e) {}
        }

        // --- 5. EVENTOS ---
        btnSalir.addActionListener(e -> dialogo.dispose());

        btnAceptar.addActionListener(e -> {
            String cia = cmbCia.getSelectedItem() != null ? cmbCia.getSelectedItem().toString() : "";
            String numEmp = txtNumEmp.getText().trim();
            String nombre = txtNombre.getText().trim();
            String rfc = txtRfc.getText().trim();
            
            String calle = txtCalle.getText().trim();
            String colonia = txtColonia.getText().trim();
            String pob = cmbPob.getSelectedItem() != null ? cmbPob.getSelectedItem().toString() : "";
            String edo = cmbEstado.getSelectedItem() != null ? cmbEstado.getSelectedItem().toString() : "";
            String pais = cmbPais.getSelectedItem() != null ? cmbPais.getSelectedItem().toString() : "";
            String cp = txtCP.getText().trim();
            String estatus = cmbEstatus.getSelectedItem() != null ? cmbEstatus.getSelectedItem().toString() : "A";
            String tel = txtTel.getText().trim();

            if (numEmp.isEmpty() || nombre.isEmpty()) {
                JOptionPane.showMessageDialog(dialogo, "El número de empleado y el nombre son obligatorios.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                ConDB db = new ConDB();
                Connection con = db.Conectar();
                if (con != null) {
                    con.setAutoCommit(false); // Iniciar Transacción

                    // 1. Guardar/Actualizar en tgemp marcando CAJ = 'S'
                    String sqlEmp = "INSERT INTO tgemp (CIA, NEMP, NOME, RFC, CALLE, COL, POB, EDO, PAIS, CP, TEL, CAJ) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'S') " +
                                    "ON DUPLICATE KEY UPDATE NOME=?, RFC=?, CALLE=?, COL=?, POB=?, EDO=?, PAIS=?, CP=?, TEL=?, CAJ='S'";
                    PreparedStatement psEmp = con.prepareStatement(sqlEmp);
                    psEmp.setString(1, cia); psEmp.setString(2, numEmp); psEmp.setString(3, nombre);
                    psEmp.setString(4, rfc); psEmp.setString(5, calle); psEmp.setString(6, colonia);
                    psEmp.setString(7, pob); psEmp.setString(8, edo); psEmp.setString(9, pais);
                    psEmp.setString(10, cp); psEmp.setString(11, tel);

                    psEmp.setString(12, nombre); psEmp.setString(13, rfc); psEmp.setString(14, calle);
                    psEmp.setString(15, colonia); psEmp.setString(16, pob); psEmp.setString(17, edo);
                    psEmp.setString(18, pais); psEmp.setString(19, cp); psEmp.setString(20, tel);
                    psEmp.executeUpdate();
                    psEmp.close();

                    // 2. Guardar/Actualizar en tescaj
                    String sqlCaj = "INSERT INTO tescaj (CIA, NEMP, CCA01, CCA02, CCA03, CCA04, CCA05, ECAJ) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                                    "ON DUPLICATE KEY UPDATE CCA01=?, CCA02=?, CCA03=?, CCA04=?, CCA05=?, ECAJ=?";
                    PreparedStatement psCaj = con.prepareStatement(sqlCaj);
                    psCaj.setString(1, cia); psCaj.setString(2, numEmp);
                    for (int i = 0; i < 5; i++) {
                        psCaj.setString(3 + i, combosClas[i].getSelectedItem() != null ? combosClas[i].getSelectedItem().toString() : "");
                    }
                    psCaj.setString(8, estatus);

                    for (int i = 0; i < 5; i++) {
                        psCaj.setString(9 + i, combosClas[i].getSelectedItem() != null ? combosClas[i].getSelectedItem().toString() : "");
                    }
                    psCaj.setString(14, estatus);
                    psCaj.executeUpdate();
                    psCaj.close();

                    con.commit();
                    db.Cerrar();

                    JOptionPane.showMessageDialog(dialogo, "Cajero guardado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    dialogo.dispose();
                    cargarTablaCajeros();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialogo, "Error al guardar el cajero: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // --- 6. MOSTRAR ---
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddCCajero;
    private javax.swing.JButton btnDeleteCCajero;
    private javax.swing.JButton btnEditCCajero;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblCCajeros;
    // End of variables declaration//GEN-END:variables
}
