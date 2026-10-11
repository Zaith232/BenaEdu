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
        btnAddCCajero.setVisible(false); btnAddCCajero.setEnabled(false);
        btnEditCCajero.setVisible(false); btnEditCCajero.setEnabled(false);
        btnDeleteCCajero.setVisible(false); btnDeleteCCajero.setEnabled(false);
        cargarTablaCajeros();
        EstiloContenido.aplicar(this);
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
        informarSoloConsulta();
    }//GEN-LAST:event_btnAddCCajeroActionPerformed

    private void btnEditCCajeroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditCCajeroActionPerformed
        informarSoloConsulta();
    }//GEN-LAST:event_btnEditCCajeroActionPerformed

    private void btnDeleteCCajeroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteCCajeroActionPerformed
        informarSoloConsulta();
    }//GEN-LAST:event_btnDeleteCCajeroActionPerformed

    private void informarSoloConsulta() {
        JOptionPane.showMessageDialog(this, "Los cajeros se registran desde Crear usuario. Este catálogo es de consulta.");
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
