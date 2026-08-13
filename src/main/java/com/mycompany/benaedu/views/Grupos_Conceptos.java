/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.benaedu.views;

import com.mycompany.benaedu.Dashboard;
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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author b17za
 */
public class Grupos_Conceptos extends javax.swing.JPanel {
private String usuarioLogueado = "Admin";
    private JTable tblGrupos;
    /**
     * Creates new form Grupos_Conceptos
     */
    
    public Grupos_Conceptos(String usuarioLogueado) {
        if (usuarioLogueado != null && !usuarioLogueado.trim().isEmpty()) {
            this.usuarioLogueado = usuarioLogueado.trim();
        }
        initComponents();
        cargarTablaGrupos();
    }
    
    public Grupos_Conceptos() {
        initComponents();
        cargarTablaGrupos();
    }
private void cargarTablaGrupos() {
        DefaultTableModel modelo = (DefaultTableModel) tblGrupos.getModel();
        modelo.setRowCount(0);

        try {
            ConDB db = new ConDB();
            Connection con = db.Conectar();
            if (con != null) {
                String sql = "SELECT CIA, CC, CESC, TGPO, CGPO, DGPO, FVINI, FVFIN, USER, FEAC FROM tesgpge ORDER BY CESC DESC, CC ASC";
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    modelo.addRow(new Object[]{
                        rs.getString("CIA"),
                        rs.getString("CC"),
                        rs.getString("CESC"),
                        rs.getString("TGPO"),
                        rs.getString("CGPO"),
                        rs.getString("DGPO"),
                        rs.getString("FVINI"),
                        rs.getString("FVFIN"),
                        rs.getString("USER"),
                        rs.getString("FEAC")
                    });
                }
                rs.close(); ps.close(); db.Cerrar();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar grupos de conceptos: " + e.getMessage());
        }
    }

    private void mostrarDialogoGrupo(boolean modoEdicion) {
        Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
        String titulo = modoEdicion ? "Modificar Plantilla de Cobro" : "Agregar Plantilla de Cobro";

        JDialog dialogo = new JDialog((java.awt.Frame) ventanaPadre, titulo, true);
        dialogo.setSize(750, 550);
        dialogo.setLayout(null);
        dialogo.setResizable(false);

        JLabel lblCia = new JLabel("Compañía"); lblCia.setBounds(20, 15, 80, 25);
        JComboBox<String> cmbCia = new JComboBox<>(new String[]{"12"}); cmbCia.setBounds(100, 15, 60, 25);
        JLabel lblCC = new JLabel("Centro Costos"); lblCC.setBounds(180, 15, 90, 25);
        JComboBox<String> cmbCC = new JComboBox<>(new String[]{"12100", "12200", "12300", "12400"}); cmbCC.setBounds(270, 15, 80, 25);
        JLabel lblCiclo = new JLabel("Ciclo"); lblCiclo.setBounds(370, 15, 50, 25);
        JTextField txtCiclo = new JTextField("2627"); txtCiclo.setBounds(420, 15, 60, 25);

        JLabel lblCodGpo = new JLabel("Cod. Grupo"); lblCodGpo.setBounds(20, 50, 80, 25);
        JTextField txtCodGpo = new JTextField("INOR"); txtCodGpo.setBounds(100, 50, 80, 25);
        JLabel lblDescGpo = new JLabel("Descripción"); lblDescGpo.setBounds(190, 50, 80, 25);
        JTextField txtDescGpo = new JTextField(); txtDescGpo.setBounds(270, 50, 440, 25);

        dialogo.add(lblCia); dialogo.add(cmbCia); dialogo.add(lblCC); dialogo.add(cmbCC); dialogo.add(lblCiclo); dialogo.add(txtCiclo);
        dialogo.add(lblCodGpo); dialogo.add(txtCodGpo); dialogo.add(lblDescGpo); dialogo.add(txtDescGpo);

        // Tabla Detalle de Conceptos (tesgpde)
        JPanel pnlDetalle = new JPanel(null);
        pnlDetalle.setBorder(BorderFactory.createTitledBorder("Conceptos Incluidos en el Grupo (tesgpde)"));
        pnlDetalle.setBounds(15, 90, 705, 360);

        DefaultTableModel modDet = new DefaultTableModel(new Object[][]{}, new String[]{"Sec", "Concepto", "Descripción", "Tipo", "Importe", "F. Vencimiento"});
        JTable tblDet = new JTable(modDet);
        JScrollPane scrollDet = new JScrollPane(tblDet);
        scrollDet.setBounds(15, 25, 675, 320);
        pnlDetalle.add(scrollDet);
        dialogo.add(pnlDetalle);

        JButton btnAceptar = new JButton("Aceptar"); btnAceptar.setBounds(260, 460, 100, 35);
        JButton btnSalir = new JButton("Salir"); btnSalir.setBounds(380, 460, 100, 35);
        dialogo.add(btnAceptar); dialogo.add(btnSalir);

        btnSalir.addActionListener(e -> dialogo.dispose());

        if (modoEdicion) {
            int fila = tblGrupos.getSelectedRow();
            if (fila != -1) {
                String cia = tblGrupos.getValueAt(fila, 0).toString();
                String cc = tblGrupos.getValueAt(fila, 1).toString();
                String ciclo = tblGrupos.getValueAt(fila, 2).toString();
                String cgpo = tblGrupos.getValueAt(fila, 4).toString();

                cmbCia.setSelectedItem(cia); cmbCC.setSelectedItem(cc);
                txtCiclo.setText(ciclo); txtCodGpo.setText(cgpo);
                txtDescGpo.setText(tblGrupos.getValueAt(fila, 5).toString());

                // Cargar conceptos de tesgpde
                try (Connection con = new ConDB().Conectar()) {
                    if (con != null) {
                        PreparedStatement psD = con.prepareStatement("SELECT SEC, NCPTO, DCPTO, TCPTO, IMPTE, FVFIN FROM tesgpde WHERE CIA=? AND CC=? AND CESC=? AND CGPO=? ORDER BY SEC");
                        psD.setString(1, cia); psD.setString(2, cc); psD.setString(3, ciclo); psD.setString(4, cgpo);
                        ResultSet rsD = psD.executeQuery();
                        while (rsD.next()) {
                            modDet.addRow(new Object[]{
                                rsD.getInt("SEC"), rsD.getString("NCPTO"), rsD.getString("DCPTO"),
                                rsD.getString("TCPTO"), rsD.getDouble("IMPTE"), rsD.getString("FVFIN")
                            });
                        }
                        rsD.close(); psD.close();
                    }
                } catch (Exception ignore) {}
            }
        }

        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
