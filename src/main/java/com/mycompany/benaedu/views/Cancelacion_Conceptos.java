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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author b17za
 */
public class Cancelacion_Conceptos extends javax.swing.JPanel {

    /**
     * Creates new form Cancelacion_Conceptos
     */
    public Cancelacion_Conceptos() {
        initComponents();
        construirInterfazCancelacionConceptos();
    }
    private void construirInterfazCancelacionConceptos() {
        this.removeAll();
        this.setLayout(null);
        this.setBackground(new java.awt.Color(255, 255, 255));

        // --- CLASE LOCAL BUSCADOR FLOTANTE ---
        class BuscadorFlotante {
            void configurar(JTextField txtClave, JTextField txtDesc, JButton boton, Object[][] datos, String[] columnas, int[] anchos) {
                Runnable mostrarPopup = () -> {
                    javax.swing.JPopupMenu popup = new javax.swing.JPopupMenu();
                    popup.setFocusable(false);
                    DefaultTableModel mod = new DefaultTableModel(datos, columnas) {
                        @Override public boolean isCellEditable(int r, int c) { return false; }
                    };
                    JTable tabla = new JTable(mod);
                    tabla.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
                    for (int i = 0; i < anchos.length; i++) {
                        tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
                    }

                    javax.swing.table.TableRowSorter<DefaultTableModel> sorter = new javax.swing.table.TableRowSorter<>(mod);
                    tabla.setRowSorter(sorter);

                    tabla.addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mouseReleased(java.awt.event.MouseEvent me) {
                            int viewRow = tabla.getSelectedRow();
                            if (viewRow != -1) {
                                int modelRow = tabla.convertRowIndexToModel(viewRow);
                                txtClave.setText(mod.getValueAt(modelRow, 0).toString());
                                if (txtDesc != null && mod.getColumnCount() >= 2) {
                                    txtDesc.setText(mod.getValueAt(modelRow, 1).toString());
                                }
                                popup.setVisible(false);
                            }
                        }
                    });
                    
                    int widthTotal = 0; for(int w : anchos) widthTotal += w;
                    JScrollPane scroll = new JScrollPane(tabla);
                    scroll.setPreferredSize(new java.awt.Dimension(widthTotal + 20, 150));
                    popup.add(scroll);

                    String texto = txtClave.getText().trim();
                    if (!texto.isEmpty()) sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + texto));
                    popup.show(txtClave, 0, txtClave.getHeight());
                    txtClave.requestFocus();
                };

                boton.addActionListener(e -> { txtClave.setText(""); mostrarPopup.run(); });
                txtClave.addKeyListener(new java.awt.event.KeyAdapter() {
                    @Override
                    public void keyReleased(java.awt.event.KeyEvent e) {
                        int c = e.getKeyCode();
                        if (c == 27 || c == 10 || c == 38 || c == 40 || c == 37 || c == 39 || c == 9) return;
                        mostrarPopup.run();
                    }
                });
            }
        }
        BuscadorFlotante buscador = new BuscadorFlotante();

        // --- CARGA DE DATOS PARA BUSCADORES ---
        java.util.function.BiFunction<String, Integer, Object[][]> cargarDatosMultiple = (query, numCols) -> {
            java.util.List<Object[]> lista = new java.util.ArrayList<>();
            try {
                ConDB db = new ConDB();
                Connection con = db.Conectar();
                if (con != null) {
                    PreparedStatement ps = con.prepareStatement(query);
                    ResultSet rs = ps.executeQuery();
                    while(rs.next()) {
                        Object[] row = new Object[numCols];
                        for(int i=0; i<numCols; i++) row[i] = rs.getString(i+1);
                        lista.add(row);
                    }
                    rs.close(); ps.close(); db.Cerrar();
                }
            } catch(Exception e) {}
            return lista.toArray(new Object[0][0]);
        };

        Object[][] dMatricula = cargarDatosMultiple.apply("SELECT MAT, APATE, AMATE, NOMA FROM tesalum ORDER BY MAT", 4);
        Object[][] dMotivo    = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'MCAN' ORDER BY CVE", 2);

        // --- 1. PANEL DE SELECCIÓN ---
        JPanel pnlSel = new JPanel(null);
        pnlSel.setBorder(BorderFactory.createTitledBorder("Datos de selección - UNIDAD ESCOLAR BENAVENTE"));
        pnlSel.setBounds(10, 10, 785, 90);

        pnlSel.add(new JLabel("Compañía")).setBounds(20, 25, 70, 25);
        JComboBox<String> cmbCia = new JComboBox<>();
        cmbCia.setBounds(90, 25, 60, 25);
        
        pnlSel.add(new JLabel("C. Costos")).setBounds(250, 25, 70, 25);
        JComboBox<String> cmbCC = new JComboBox<>();
        cmbCC.setBounds(320, 25, 80, 25);

        try {
            ConDB db = new ConDB();
            Connection con = db.Conectar();
            if (con != null) {
                ResultSet rsCia = con.prepareStatement("SELECT CIA FROM tmcias ORDER BY CIA").executeQuery();
                while(rsCia.next()) cmbCia.addItem(rsCia.getString("CIA"));
                rsCia.close();

                ResultSet rsCC = con.prepareStatement("SELECT CVE FROM tgcc WHERE CVE IN ('12100', '12200', '12300', '12400') ORDER BY CVE").executeQuery();
                while(rsCC.next()) cmbCC.addItem(rsCC.getString("CVE"));
                rsCC.close();
                
                db.Cerrar();
            }
        } catch (Exception ex) {}

        pnlSel.add(cmbCia);
        pnlSel.add(cmbCC);

        // Buscador Matrícula
        pnlSel.add(new JLabel("Matrícula")).setBounds(20, 55, 70, 25);
        JTextField txtMatricula = new JTextField(); txtMatricula.setBounds(90, 55, 100, 25);
        JButton btnMatricula = new JButton("▼"); btnMatricula.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnMatricula.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnMatricula.setBounds(190, 55, 20, 25);
        buscador.configurar(txtMatricula, null, btnMatricula, dMatricula, new String[]{"Matrícula", "A. Paterno", "A. Materno", "Nombre"}, new int[]{80, 120, 120, 150});
        pnlSel.add(txtMatricula); pnlSel.add(btnMatricula);

        JButton btnFiltra = new JButton("Filtra Información");
        btnFiltra.setBounds(620, 50, 150, 30);
        pnlSel.add(btnFiltra);

        this.add(pnlSel);

        // --- 2. MOTIVO DE CANCELACIÓN ---
        JLabel lblMotivo = new JLabel("Motivo de Cancelación");
        lblMotivo.setBounds(120, 110, 150, 25);
        
        JTextField txtMotivo = new JTextField(); txtMotivo.setBounds(260, 110, 50, 25);
        JButton btnMotivo = new JButton("▼"); btnMotivo.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnMotivo.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnMotivo.setBounds(310, 110, 20, 25);
        JTextField txtMotivoDesc = new JTextField(); txtMotivoDesc.setBounds(335, 110, 460, 25); txtMotivoDesc.setEditable(false); txtMotivoDesc.setBackground(new java.awt.Color(240,240,240));
        buscador.configurar(txtMotivo, txtMotivoDesc, btnMotivo, dMotivo, new String[]{"Clave", "Descripción"}, new int[]{60, 350});

        this.add(lblMotivo);
        this.add(txtMotivo);
        this.add(btnMotivo);
        this.add(txtMotivoDesc);

        // --- 3. TABLA DE CONCEPTOS SIN PAGO ---
        DefaultTableModel modCptosPendientes = new DefaultTableModel(
            new Object[][]{}, 
            new String[]{"Compañía", "C. Costos", "Ciclo Escolar", "ID", "Cpto", "Descripción", "Fecha Venc", "Moneda", "Imp Total", "Imp Pend Pag"}
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tblConceptosPendientes = new JTable(modCptosPendientes);
        tblConceptosPendientes.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        JScrollPane scrollConceptos = new JScrollPane(tblConceptosPendientes);
        
        JPanel pnlTabla = new JPanel(null);
        pnlTabla.setBorder(BorderFactory.createTitledBorder("Conceptos Sin Pago"));
        pnlTabla.setBounds(10, 145, 785, 260);
        scrollConceptos.setBounds(10, 20, 765, 230);
        pnlTabla.add(scrollConceptos);
        
        this.add(pnlTabla);

        // --- 4. BOTÓN ACEPTAR ---
        JButton btnAceptar = new JButton("Aceptar");
        btnAceptar.setBounds(350, 420, 110, 35);
        this.add(btnAceptar);

        // --- 5. EVENTOS ---

        // LÓGICA DE BÚSQUEDA DE CONCEPTOS SIN PAGO (tescalu)
        btnFiltra.addActionListener(e -> {
            modCptosPendientes.setRowCount(0);
            String matricula = txtMatricula.getText().trim();
            String cia = cmbCia.getSelectedItem() != null ? cmbCia.getSelectedItem().toString() : "";
            String cc = cmbCC.getSelectedItem() != null ? cmbCC.getSelectedItem().toString() : "";

            if (matricula.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor proporcione una matrícula para filtrar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                ConDB db = new ConDB();
                Connection con = db.Conectar();
                if (con != null) {
                    StringBuilder sql = new StringBuilder(
                        "SELECT CIA, CC, CESC, IDCPT, NCPTO, DCPTO, FVEN, CMON, IMPTMN, IPENMN " +
                        "FROM tescalu " +
                        "WHERE MAT = ? AND (IPAGMN = 0 OR IPENMN = IMPTMN) AND (MCAN IS NULL OR MCAN = '') "
                    );

                    if (!cia.isEmpty()) sql.append(" AND CIA = ?");
                    if (!cc.isEmpty()) sql.append(" AND CC = ?");

                    sql.append(" ORDER BY FVEN ASC");

                    PreparedStatement ps = con.prepareStatement(sql.toString());
                    int pIdx = 1;
                    ps.setString(pIdx++, matricula);
                    if (!cia.isEmpty()) ps.setString(pIdx++, cia);
                    if (!cc.isEmpty()) ps.setString(pIdx++, cc);

                    ResultSet rs = ps.executeQuery();
                    java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");

                    while (rs.next()) {
                        Object[] fila = new Object[10];
                        fila[0] = rs.getString("CIA");
                        fila[1] = rs.getString("CC");
                        fila[2] = rs.getString("CESC");
                        fila[3] = rs.getString("IDCPT");
                        fila[4] = rs.getString("NCPTO");
                        fila[5] = rs.getString("DCPTO");
                        fila[6] = rs.getString("FVEN");
                        fila[7] = rs.getString("CMON");
                        fila[8] = df.format(rs.getDouble("IMPTMN"));
                        fila[9] = df.format(rs.getDouble("IPENMN"));

                        modCptosPendientes.addRow(fila);
                    }

                    rs.close(); ps.close(); db.Cerrar();

                    if (modCptosPendientes.getRowCount() == 0) {
                        JOptionPane.showMessageDialog(this, "No se encontraron conceptos pendientes por pagar para la matrícula " + matricula, "Información", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al buscar conceptos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // PROCESAR CANCELACIÓN DE CONCEPTO
        btnAceptar.addActionListener(e -> {
            int filaSel = tblConceptosPendientes.getSelectedRow();
            if (filaSel == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione al menos un concepto de la tabla para cancelar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (txtMotivo.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe especificar un Motivo de Cancelación.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String matricula = txtMatricula.getText().trim();
            String idCpt = tblConceptosPendientes.getValueAt(filaSel, 3).toString();
            String concepto = tblConceptosPendientes.getValueAt(filaSel, 5).toString();
            String motivoCode = txtMotivo.getText().trim();

            int confirm = JOptionPane.showConfirmDialog(this, 
                "¿Está seguro de cancelar el concepto '" + concepto + "' para el alumno con matrícula " + matricula + "?", 
                "Confirmar Cancelación", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    ConDB db = new ConDB();
                    Connection con = db.Conectar();
                    if (con != null) {
                        String sql = "UPDATE tescalu SET MCAN = ?, IPENMN = 0 WHERE MAT = ? AND IDCPT = ?";
                        PreparedStatement ps = con.prepareStatement(sql);
                        ps.setString(1, motivoCode);
                        ps.setString(2, matricula);
                        ps.setString(3, idCpt);

                        int rows = ps.executeUpdate();
                        ps.close(); db.Cerrar();

                        if (rows > 0) {
                            JOptionPane.showMessageDialog(this, "Concepto cancelado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                            btnFiltra.doClick(); // Recarga la tabla de conceptos automáticamente
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al cancelar el concepto: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        this.revalidate();
        this.repaint();
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
        jScrollPane1 = new javax.swing.JScrollPane();
        tblCConceptos = new javax.swing.JTable();
        btnAddCConceptos = new javax.swing.JButton();
        btnEditCConceptos = new javax.swing.JButton();
        btnDeleteCConceptos = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        tblCConceptos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tblCConceptos);

        btnAddCConceptos.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAddCConceptos.setForeground(new java.awt.Color(26, 61, 99));
        btnAddCConceptos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/add.png"))); // NOI18N
        btnAddCConceptos.setText("Añadir");
        btnAddCConceptos.addActionListener(this::btnAddCConceptosActionPerformed);

        btnEditCConceptos.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEditCConceptos.setForeground(new java.awt.Color(26, 61, 99));
        btnEditCConceptos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edit.png"))); // NOI18N
        btnEditCConceptos.setText("Editar");
        btnEditCConceptos.setMaximumSize(new java.awt.Dimension(93, 31));
        btnEditCConceptos.setMinimumSize(new java.awt.Dimension(93, 31));
        btnEditCConceptos.addActionListener(this::btnEditCConceptosActionPerformed);

        btnDeleteCConceptos.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDeleteCConceptos.setForeground(new java.awt.Color(26, 61, 99));
        btnDeleteCConceptos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/delete.png"))); // NOI18N
        btnDeleteCConceptos.setText("Eliminar");
        btnDeleteCConceptos.addActionListener(this::btnDeleteCConceptosActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAddCConceptos)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditCConceptos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteCConceptos)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddCConceptos, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditCConceptos, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteCConceptos, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 24, Short.MAX_VALUE))
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

    private void btnAddCConceptosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddCConceptosActionPerformed
  
    }//GEN-LAST:event_btnAddCConceptosActionPerformed

    private void btnEditCConceptosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditCConceptosActionPerformed


    }//GEN-LAST:event_btnEditCConceptosActionPerformed

    private void btnDeleteCConceptosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteCConceptosActionPerformed

    }//GEN-LAST:event_btnDeleteCConceptosActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddCConceptos;
    private javax.swing.JButton btnDeleteCConceptos;
    private javax.swing.JButton btnEditCConceptos;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblCConceptos;
    // End of variables declaration//GEN-END:variables
}
