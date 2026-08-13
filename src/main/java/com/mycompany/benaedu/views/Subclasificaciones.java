/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.benaedu.views;
import com.mycompany.benaedu.db.ConDB;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
/**
 *
 * @author b17za
 */
public class Subclasificaciones extends javax.swing.JPanel {
private JComboBox<String> cmbTablaFiltro;
    private JTable tblSubclasificaciones;
    private DefaultTableModel modSubclas;
    /**
     * Creates new form Subclasificaciones
     */
    public Subclasificaciones() {
        initComponents();
        construirInterfaz();
    }
private void construirInterfaz() {
        this.removeAll();
        this.setLayout(new BorderLayout(10, 10));
        this.setBackground(Color.WHITE);

        // --- 1. PANEL SUPERIOR (FILTRO POR TABLA PADRE) ---
        JPanel pnlNorte = new JPanel(null);
        pnlNorte.setPreferredSize(new Dimension(800, 65));
        pnlNorte.setBackground(Color.WHITE);
        pnlNorte.setBorder(BorderFactory.createTitledBorder("Clasificación Maestra (Padre)"));

        JLabel lblPadre = new JLabel("Seleccionar Tabla:");
        lblPadre.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPadre.setBounds(20, 22, 120, 25);
        pnlNorte.add(lblPadre);

        cmbTablaFiltro = new JComboBox<>();
        cmbTablaFiltro.setBounds(140, 22, 380, 25);
        cargarComboPadre();
        pnlNorte.add(cmbTablaFiltro);

        JButton btnRefrescar = new JButton("Filtrar");
        btnRefrescar.setBounds(530, 22, 90, 25);
        pnlNorte.add(btnRefrescar);

        this.add(pnlNorte, BorderLayout.NORTH);

        // --- 2. TABLA PRINCIPAL DE SUBCLASIFICACIONES (`tmclas`) ---
        modSubclas = new DefaultTableModel(
            new Object[][]{},
            new String[]{"Tabla (Padre)", "Clave", "Descripción", "Relación", "Usuario", "Fecha Act.", "Hora Act."}
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tblSubclasificaciones = new JTable(modSubclas);
        tblSubclasificaciones.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblSubclasificaciones.getColumnModel().getColumn(0).setPreferredWidth(90);
        tblSubclasificaciones.getColumnModel().getColumn(1).setPreferredWidth(80);
        tblSubclasificaciones.getColumnModel().getColumn(2).setPreferredWidth(260);
        tblSubclasificaciones.getColumnModel().getColumn(3).setPreferredWidth(120);
        tblSubclasificaciones.getColumnModel().getColumn(4).setPreferredWidth(80);
        tblSubclasificaciones.getColumnModel().getColumn(5).setPreferredWidth(90);
        tblSubclasificaciones.getColumnModel().getColumn(6).setPreferredWidth(80);

        JScrollPane scrollTabla = new JScrollPane(tblSubclasificaciones);
        scrollTabla.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Registros de Subclasificaciones", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
        this.add(scrollTabla, BorderLayout.CENTER);

        // --- 3. PANEL INFERIOR DE BOTONES ---
        JPanel pnlSur = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 10));
        pnlSur.setBackground(Color.WHITE);

        JButton btnAdd = new JButton("Añadir");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAdd.setForeground(new Color(26, 61, 99));
        btnAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/add.png")));

        JButton btnEdit = new JButton("Editar");
        btnEdit.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnEdit.setForeground(new Color(26, 61, 99));
        btnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edit.png")));

        JButton btnDelete = new JButton("Eliminar");
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnDelete.setForeground(new Color(26, 61, 99));
        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/delete.png")));

        pnlSur.add(btnAdd);
        pnlSur.add(btnEdit);
        pnlSur.add(btnDelete);

        this.add(pnlSur, BorderLayout.SOUTH);

        // --- EVENTOS ---
        cmbTablaFiltro.addActionListener(e -> cargarTablaSubclasificaciones());
        btnRefrescar.addActionListener(e -> cargarTablaSubclasificaciones());

        btnAdd.addActionListener(e -> mostrarDialogoSubclasificacion(false));
        btnEdit.addActionListener(e -> {
            if (tblSubclasificaciones.getSelectedRow() == -1) {
                JOptionPane.showMessageDialog(this, "Selecciona una subclasificación para editar.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }
            mostrarDialogoSubclasificacion(true);
        });

        btnDelete.addActionListener(e -> eliminarSubclasificacion());

        cargarTablaSubclasificaciones();
        this.revalidate();
        this.repaint();
    }

    private void cargarComboPadre() {
        cmbTablaFiltro.removeAllItems();
        try (Connection con = new ConDB().Conectar()) {
            if (con != null) {
                String sql = "SELECT TBL, DES FROM tmclasge ORDER BY TBL";
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    cmbTablaFiltro.addItem(rs.getString("TBL") + " - " + rs.getString("DES"));
                }
                rs.close();
                ps.close();
            }
        } catch (Exception ex) {
            cmbTablaFiltro.addItem("BCOS - BANCOS");
        }
    }

    private void cargarTablaSubclasificaciones() {
        modSubclas.setRowCount(0);
        String sel = cmbTablaFiltro.getSelectedItem() != null ? cmbTablaFiltro.getSelectedItem().toString() : "";
        if (sel.isEmpty()) return;

        String idPadre = sel.contains(" - ") ? sel.split(" - ")[0].trim() : sel.trim();

        try (Connection con = new ConDB().Conectar()) {
            if (con != null) {
                String sql = "SELECT TBL, CVE, DES, REL, USER, FEAC, HOAC FROM tmclas WHERE TBL = ? ORDER BY CVE";
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setString(1, idPadre);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    modSubclas.addRow(new Object[]{
                        rs.getString("TBL"),
                        rs.getString("CVE"),
                        rs.getString("DES"),
                        rs.getString("REL") != null ? rs.getString("REL") : "",
                        rs.getString("USER") != null ? rs.getString("USER") : "",
                        rs.getString("FEAC") != null ? rs.getString("FEAC") : "",
                        rs.getString("HOAC") != null ? rs.getString("HOAC") : ""
                    });
                }
                rs.close();
                ps.close();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar subclasificaciones: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarSubclasificacion() {
        int fila = tblSubclasificaciones.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un registro para eliminar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tbl = tblSubclasificaciones.getValueAt(fila, 0).toString();
        String cve = tblSubclasificaciones.getValueAt(fila, 1).toString();
        String desc = tblSubclasificaciones.getValueAt(fila, 2).toString();

        int resp = JOptionPane.showConfirmDialog(this,
                "¿Eliminar la subclasificación [" + cve + "] " + desc + " de la tabla " + tbl + "?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (resp == JOptionPane.YES_OPTION) {
            try (Connection con = new ConDB().Conectar()) {
                if (con != null) {
                    PreparedStatement ps = con.prepareStatement("DELETE FROM tmclas WHERE TBL = ? AND CVE = ?");
                    ps.setString(1, tbl);
                    ps.setString(2, cve);

                    if (ps.executeUpdate() > 0) {
                        JOptionPane.showMessageDialog(this, "Registro eliminado correctamente.");
                        cargarTablaSubclasificaciones();
                    } else {
                        JOptionPane.showMessageDialog(this, "No se encontró el registro para eliminar.");
                    }
                    ps.close();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void mostrarDialogoSubclasificacion(boolean modoEdicion) {
        Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
        String tituloVentana = modoEdicion ? "Modificar Subclasificación" : "Agregar Subclasificación";

        JDialog dialogo = new JDialog((java.awt.Frame) ventanaPadre, tituloVentana, true);
        dialogo.setSize(520, 310);
        dialogo.setLayout(null);
        dialogo.setLocationRelativeTo(this);
        dialogo.setResizable(false);

        // --- COMPONENTES DEL DIÁLOGO ---
        JLabel lblTablaPadre = new JLabel("Tabla (Padre):");
        lblTablaPadre.setBounds(30, 25, 100, 25);
        JComboBox<String> cmbDialogoPadre = new JComboBox<>();
        cmbDialogoPadre.setBounds(140, 25, 320, 25);

        try (Connection con = new ConDB().Conectar()) {
            if (con != null) {
                ResultSet rs = con.prepareStatement("SELECT TBL, DES FROM tmclasge ORDER BY TBL").executeQuery();
                while (rs.next()) {
                    cmbDialogoPadre.addItem(rs.getString("TBL") + " - " + rs.getString("DES"));
                }
                rs.close();
            }
        } catch (Exception ignore) {}

        if (cmbTablaFiltro.getSelectedItem() != null) {
            cmbDialogoPadre.setSelectedItem(cmbTablaFiltro.getSelectedItem());
        }

        JLabel lblClave = new JLabel("Clave:");
        lblClave.setBounds(30, 65, 100, 25);
        JTextField txtClave = new JTextField();
        txtClave.setBounds(140, 65, 100, 25);

        JLabel lblDesc = new JLabel("Descripción:");
        lblDesc.setBounds(30, 105, 100, 25);
        JTextField txtDesc = new JTextField();
        txtDesc.setBounds(140, 105, 320, 25);

        JLabel lblRel = new JLabel("Relación / Extra:");
        lblRel.setBounds(30, 145, 100, 25);
        JTextField txtRel = new JTextField();
        txtRel.setBounds(140, 145, 320, 25);

        JButton btnAceptar = new JButton("Aceptar");
        btnAceptar.setBounds(140, 200, 100, 35);
        JButton btnSalir = new JButton("Salir");
        btnSalir.setBounds(260, 200, 100, 35);

        dialogo.add(lblTablaPadre); dialogo.add(cmbDialogoPadre);
        dialogo.add(lblClave); dialogo.add(txtClave);
        dialogo.add(lblDesc); dialogo.add(txtDesc);
        dialogo.add(lblRel); dialogo.add(txtRel);
        dialogo.add(btnAceptar); dialogo.add(btnSalir);

        if (modoEdicion) {
            cmbDialogoPadre.setEnabled(false);
            txtClave.setEditable(false);

            int fila = tblSubclasificaciones.getSelectedRow();
            String tbl = tblSubclasificaciones.getValueAt(fila, 0).toString();
            String cve = tblSubclasificaciones.getValueAt(fila, 1).toString();
            String desc = tblSubclasificaciones.getValueAt(fila, 2).toString();
            String rel = tblSubclasificaciones.getValueAt(fila, 3).toString();

            for (int i = 0; i < cmbDialogoPadre.getItemCount(); i++) {
                if (cmbDialogoPadre.getItemAt(i).startsWith(tbl)) {
                    cmbDialogoPadre.setSelectedIndex(i);
                    break;
                }
            }
            txtClave.setText(cve);
            txtDesc.setText(desc);
            txtRel.setText(rel);
        }

        btnSalir.addActionListener(e -> dialogo.dispose());

        btnAceptar.addActionListener(e -> {
            String selPadre = cmbDialogoPadre.getSelectedItem() != null ? cmbDialogoPadre.getSelectedItem().toString() : "";
            String idPadre = selPadre.contains(" - ") ? selPadre.split(" - ")[0].trim() : selPadre.trim();
            String clave = txtClave.getText().trim();
            String descripcion = txtDesc.getText().trim();
            String relacion = txtRel.getText().trim();

            if (idPadre.isEmpty() || clave.isEmpty() || descripcion.isEmpty()) {
                JOptionPane.showMessageDialog(dialogo, "La Tabla Padre, la Clave y la Descripción son obligatorias.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection con = new ConDB().Conectar()) {
                if (con != null) {
                    if (!modoEdicion) {
                        PreparedStatement psCheck = con.prepareStatement("SELECT 1 FROM tmclas WHERE TBL = ? AND CVE = ?");
                        psCheck.setString(1, idPadre);
                        psCheck.setString(2, clave);
                        ResultSet rsCheck = psCheck.executeQuery();
                        if (rsCheck.next()) {
                            JOptionPane.showMessageDialog(dialogo, "La clave [" + clave + "] ya existe para la tabla " + idPadre + ".", "Duplicado", JOptionPane.WARNING_MESSAGE);
                            rsCheck.close();
                            psCheck.close();
                            return;
                        }
                        rsCheck.close();
                        psCheck.close();
                    }

                    PreparedStatement ps;
                    if (modoEdicion) {
                        String sql = "UPDATE tmclas SET DES = ?, REL = ?, USER = 'Admin', FEAC = CURDATE(), HOAC = DATE_FORMAT(NOW(), '%r') " +
                                     "WHERE TBL = ? AND CVE = ?";
                        ps = con.prepareStatement(sql);
                        ps.setString(1, descripcion);
                        ps.setString(2, relacion);
                        ps.setString(3, idPadre);
                        ps.setString(4, clave);
                    } else {
                        String sql = "INSERT INTO tmclas (TBL, CVE, DES, REL, FIJO, USER, FEAC, HOAC) " +
                                     "VALUES (?, ?, ?, ?, 'N', 'Admin', CURDATE(), DATE_FORMAT(NOW(), '%r'))";
                        ps = con.prepareStatement(sql);
                        ps.setString(1, idPadre);
                        ps.setString(2, clave);
                        ps.setString(3, descripcion);
                        ps.setString(4, relacion);
                    }

                    if (ps.executeUpdate() > 0) {
                        JOptionPane.showMessageDialog(dialogo, "Subclasificación guardada con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        dialogo.dispose();
                        cargarTablaSubclasificaciones();
                    }
                    ps.close();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialogo, "Error SQL: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

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
