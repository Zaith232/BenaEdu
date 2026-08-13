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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author b17za
 */
public class Impresion_Facturas extends javax.swing.JPanel {
private String usuarioLogueado = "Admin";
    /**
     * Creates new form Impresion_Facturas
     */
public Impresion_Facturas(String usuarioLogueado) {
        if (usuarioLogueado != null && !usuarioLogueado.trim().isEmpty()) {
            this.usuarioLogueado = usuarioLogueado.trim();
        }
        initComponents();
        cargarTablaFacturas();
    }
    public Impresion_Facturas() {
        initComponents();
    }
    
    private String obtenerUsuarioActivo() {
        if (this.usuarioLogueado != null && !this.usuarioLogueado.equals("Admin")) {
            return this.usuarioLogueado;
        }
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        if (parentWindow instanceof Dashboard dash) {
            return dash.getUsuarioCodigo();
        }
        return this.usuarioLogueado;
    }
  private void cargarTablaFacturas() {
        DefaultTableModel modelo = new DefaultTableModel(
            new Object[][] {}, 
            new String[] {"Compañía", "Factura", "Matrícula", "Tipo", "Fecha", "Total", "Estatus", "Usuario"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        tblImpresionFactura.setModel(modelo);

        try {
            ConDB db = new ConDB();
            Connection con = db.Conectar();

            if (con != null) {
                // Agrupamos por factura para no mostrar renglones repetidos si una factura tiene varios conceptos
                String sql = "SELECT CIA, NFAC, MAT, TFAC, FFAC, SUM(IMPTMN) as TOTAL, STAFAC, USER " +
                             "FROM tesfalu GROUP BY CIA, NFAC, MAT, TFAC, FFAC, STAFAC, USER ORDER BY FFAC DESC";
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    Object[] fila = new Object[8]; 
                    fila[0] = rs.getString("CIA");
                    fila[1] = rs.getString("NFAC");
                    fila[2] = rs.getString("MAT");
                    fila[3] = rs.getString("TFAC");
                    fila[4] = rs.getString("FFAC");
                    
                    double total = rs.getDouble("TOTAL");
                    fila[5] = String.format("%.2f", total);
                    
                    String estatus = rs.getString("STAFAC");
                    fila[6] = (estatus != null && estatus.equals("FA")) ? "FACTURADA" : "CANCELADA";
                    
                    fila[7] = rs.getString("USER");
                    modelo.addRow(fila);
                }
                rs.close(); ps.close(); db.Cerrar();
                
                adaptarTamañoColumnas();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar la tabla de facturas: " + e.getMessage());
        }
    }

    private void adaptarTamañoColumnas() {
        tblImpresionFactura.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF); 
        for (int i = 0; i < tblImpresionFactura.getColumnCount(); i++) {
            javax.swing.table.TableColumn columna = tblImpresionFactura.getColumnModel().getColumn(i);
            int anchoPreferido = 60; 
            java.awt.Component compCabecera = tblImpresionFactura.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(tblImpresionFactura, columna.getHeaderValue(), false, false, 0, i);
            anchoPreferido = Math.max(anchoPreferido, compCabecera.getPreferredSize().width + 10);
            
            for (int r = 0; r < tblImpresionFactura.getRowCount(); r++) {
                javax.swing.table.TableCellRenderer renderizador = tblImpresionFactura.getCellRenderer(r, i);
                java.awt.Component c = tblImpresionFactura.prepareRenderer(renderizador, r, i);
                anchoPreferido = Math.max(anchoPreferido, c.getPreferredSize().width + 15); 
            }
            columna.setPreferredWidth(anchoPreferido); 
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
        jScrollPane1 = new javax.swing.JScrollPane();
        tblImpresionFactura = new javax.swing.JTable();
        btnAddIFactura = new javax.swing.JButton();
        btnEditIFactura = new javax.swing.JButton();
        btnDeleteIFactura = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        tblImpresionFactura.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tblImpresionFactura);

        btnAddIFactura.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAddIFactura.setForeground(new java.awt.Color(26, 61, 99));
        btnAddIFactura.setIcon(new javax.swing.ImageIcon(getClass().getResource("/add.png"))); // NOI18N
        btnAddIFactura.setText("Añadir");
        btnAddIFactura.addActionListener(this::btnAddIFacturaActionPerformed);

        btnEditIFactura.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEditIFactura.setForeground(new java.awt.Color(26, 61, 99));
        btnEditIFactura.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edit.png"))); // NOI18N
        btnEditIFactura.setText("Editar");
        btnEditIFactura.setMaximumSize(new java.awt.Dimension(93, 31));
        btnEditIFactura.setMinimumSize(new java.awt.Dimension(93, 31));
        btnEditIFactura.addActionListener(this::btnEditIFacturaActionPerformed);

        btnDeleteIFactura.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDeleteIFactura.setForeground(new java.awt.Color(26, 61, 99));
        btnDeleteIFactura.setIcon(new javax.swing.ImageIcon(getClass().getResource("/delete.png"))); // NOI18N
        btnDeleteIFactura.setText("Eliminar");
        btnDeleteIFactura.addActionListener(this::btnDeleteIFacturaActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAddIFactura)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditIFactura, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteIFactura)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddIFactura, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditIFactura, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteIFactura, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void btnAddIFacturaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddIFacturaActionPerformed
     mostrarDialogoFacturacion(false);
    }//GEN-LAST:event_btnAddIFacturaActionPerformed

    private void btnEditIFacturaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditIFacturaActionPerformed
      JOptionPane.showMessageDialog(this, "Las facturas timbradas no pueden modificarse.\nSi requieres hacer cambios, debes cancelarla y generar una nueva.");
    }//GEN-LAST:event_btnEditIFacturaActionPerformed

    private void btnDeleteIFacturaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteIFacturaActionPerformed
    int fila = tblImpresionFactura.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona una factura del historial para cancelar.");
            return;
        }

        String cia = tblImpresionFactura.getValueAt(fila, 0).toString();
        String idFactura = tblImpresionFactura.getValueAt(fila, 1).toString();
        
        int resp = JOptionPane.showConfirmDialog(this, "¿Seguro que deseas CANCELAR la factura: " + idFactura + "?\nEsta acción actualizará su estatus ante el sistema.", "Confirmar Cancelación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (resp == JOptionPane.YES_OPTION) {
            try {
                ConDB db = new ConDB();
                Connection con = db.Conectar();
                if (con != null) {
                    // Cambiamos el estatus a CA (Cancelada) en lugar de borrar el registro
                    String sql = "UPDATE tesfalu SET STAFAC = 'CA' WHERE CIA = ? AND NFAC = ?";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, cia);
                    ps.setString(2, idFactura);
                    
                    if (ps.executeUpdate() > 0) {
                        JOptionPane.showMessageDialog(this, "Factura cancelada correctamente en el sistema local.");
                        cargarTablaFacturas();
                    } else {
                        JOptionPane.showMessageDialog(this, "No se encontró el registro para cancelar.");
                    }
                    ps.close(); db.Cerrar();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al cancelar la factura: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_btnDeleteIFacturaActionPerformed
  private void mostrarDialogoFacturacion(boolean modoEdicion) {
        Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
        JDialog dialogo = new JDialog((java.awt.Frame) ventanaPadre, "Impresión y Generación de Facturas", true);
        dialogo.setSize(920, 720);
        dialogo.setLayout(null);
        dialogo.setResizable(false);

        class BuscadorFlotante {
            void configurar(JTextField txtClave, JTextField txtDesc, JButton boton, Object[][] datos, String[] columnas, int[] anchos, java.util.function.Consumer<Object[]> onSelect) {
                Runnable mostrarPopup = () -> {
                    javax.swing.JPopupMenu popup = new javax.swing.JPopupMenu();
                    popup.setFocusable(false);
                    javax.swing.table.DefaultTableModel mod = new javax.swing.table.DefaultTableModel(datos, columnas) {
                        @Override public boolean isCellEditable(int r, int c) { return false; }
                    };
                    javax.swing.JTable tabla = new javax.swing.JTable(mod);
                    tabla.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
                    for (int i = 0; i < anchos.length; i++) {
                        tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
                    }

                    javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel> sorter = new javax.swing.table.TableRowSorter<>(mod);
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
                                if (onSelect != null) {
                                    Object[] rowData = new Object[mod.getColumnCount()];
                                    for(int i=0; i<mod.getColumnCount(); i++) rowData[i] = mod.getValueAt(modelRow, i);
                                    onSelect.accept(rowData);
                                }
                                popup.setVisible(false);
                            }
                        }
                    });
                    
                    int widthTotal = 0; for(int w : anchos) widthTotal += w;
                    javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(tabla);
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
        Object[][] dCC        = cargarDatosMultiple.apply("SELECT CVE, DES1 FROM tgcc ORDER BY CVE", 2);
        Object[][] dNumFact   = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'FACT' AND CVE IN ('FE', 'VC') ORDER BY CVE", 2);
        Object[][] dPob       = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'CD' ORDER BY CVE", 2);
        Object[][] dEdo       = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'EDO' ORDER BY CVE", 2);
        Object[][] dPais      = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'PAIS' ORDER BY CVE", 2);
        Object[][] dMetodo    = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'IPAG' ORDER BY CVE", 2);
        Object[][] dBanco     = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'BCOS' ORDER BY CVE", 2);

        // 1. DATOS DE SELECCIÓN
        JPanel pnlSeleccion = new JPanel(null);
        pnlSeleccion.setBorder(BorderFactory.createTitledBorder("Datos de selección"));
        pnlSeleccion.setBounds(10, 10, 710, 90);

        pnlSeleccion.add(new JLabel("Compañía")).setBounds(20, 20, 70, 25);
        JComboBox<String> cmbCia = new JComboBox<>(new String[]{"12", "13"});
        cmbCia.setBounds(90, 20, 60, 25);
        pnlSeleccion.add(cmbCia);

        pnlSeleccion.add(new JLabel("C. Costos")).setBounds(220, 20, 70, 25);
        JComboBox<String> cmbCC = new JComboBox<>(new String[]{"12100", "12200", "12300", "12400"});
        cmbCC.setBounds(290, 20, 80, 25);
        pnlSeleccion.add(cmbCC);

        pnlSeleccion.add(new JLabel("Moneda")).setBounds(420, 20, 60, 25);
        JComboBox<String> cmbMoneda = new JComboBox<>(new String[]{"MXP", "USD"});
        cmbMoneda.setBounds(480, 20, 70, 25);
        pnlSeleccion.add(cmbMoneda);

        pnlSeleccion.add(new JLabel("Matrícula")).setBounds(20, 55, 70, 25);
        JTextField txtMat = new JTextField(); txtMat.setBounds(90, 55, 80, 25);
        JButton btnMat = new JButton("▼"); btnMat.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnMat.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnMat.setBounds(170, 55, 20, 25);
        buscador.configurar(txtMat, null, btnMat, dMatricula, new String[]{"Matrícula", "A. Paterno", "A. Materno", "Nombre"}, new int[]{80, 120, 120, 150}, null);
        pnlSeleccion.add(txtMat); pnlSeleccion.add(btnMat);

        pnlSeleccion.add(new JLabel("Fecha Inicial")).setBounds(220, 55, 80, 25);
        com.toedter.calendar.JDateChooser txtFecIni = new com.toedter.calendar.JDateChooser();
        txtFecIni.setDateFormatString("dd/MM/yyyy"); txtFecIni.setBounds(290, 55, 110, 25);
        pnlSeleccion.add(txtFecIni);

        pnlSeleccion.add(new JLabel("Fecha Final")).setBounds(410, 55, 70, 25);
        com.toedter.calendar.JDateChooser txtFecFin = new com.toedter.calendar.JDateChooser();
        txtFecFin.setDateFormatString("dd/MM/yyyy"); txtFecFin.setBounds(480, 55, 110, 25);
        pnlSeleccion.add(txtFecFin);

        dialogo.add(pnlSeleccion);

        JButton btnFiltra = new JButton("Filtra Información");
        btnFiltra.setBounds(730, 25, 160, 35);
        dialogo.add(btnFiltra);

        // 2. INFORMACIÓN DE FACTURA
        JPanel pnlFactura = new JPanel(null);
        pnlFactura.setBorder(BorderFactory.createTitledBorder("Información de Factura"));
        pnlFactura.setBounds(10, 105, 710, 60);

        pnlFactura.add(new JLabel("Fecha Factura")).setBounds(20, 20, 90, 25);
        com.toedter.calendar.JDateChooser txtFecFactura = new com.toedter.calendar.JDateChooser();
        txtFecFactura.setDateFormatString("dd/MM/yyyy"); 
        txtFecFactura.setDate(new java.util.Date());
        txtFecFactura.setBounds(110, 20, 110, 25);
        pnlFactura.add(txtFecFactura);

        pnlFactura.add(new JLabel("Número Factura")).setBounds(230, 20, 100, 25);
        JTextField txtNumFac = new JTextField(); txtNumFac.setBounds(330, 20, 60, 25);
        pnlFactura.add(txtNumFac);

        JTextField txtTipoFac = new JTextField("FE"); txtTipoFac.setBounds(400, 20, 40, 25);
        JButton btnTipoFac = new JButton("▼"); btnTipoFac.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnTipoFac.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnTipoFac.setBounds(440, 20, 20, 25);
        buscador.configurar(txtTipoFac, null, btnTipoFac, dNumFact, new String[]{"Clave", "Descripción"}, new int[]{60, 200}, null);
        pnlFactura.add(txtTipoFac); pnlFactura.add(btnTipoFac);

        JCheckBox chkDetallada = new JCheckBox("Factura Detallada", true);
        chkDetallada.setBounds(480, 20, 150, 25);
        pnlFactura.add(chkDetallada);

        dialogo.add(pnlFactura);

        JPanel pnlTotalFact = new JPanel(null);
        pnlTotalFact.setBorder(BorderFactory.createTitledBorder("Total a facturar"));
        pnlTotalFact.setBounds(730, 105, 160, 60);
        JTextField txtTotal = new JTextField("0.00");
        txtTotal.setHorizontalAlignment(JTextField.RIGHT); txtTotal.setEditable(false);
        txtTotal.setBounds(20, 20, 120, 25);
        pnlTotalFact.add(txtTotal);
        dialogo.add(pnlTotalFact);

        // 3. TABS (INFORMACIÓN FISCAL)
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setBounds(10, 175, 880, 260);

        JPanel pnlFiscal = new JPanel(null);

        pnlFiscal.add(new JLabel("Nombre")).setBounds(20, 15, 60, 25);
        pnlFiscal.add(new JTextField()).setBounds(90, 15, 750, 25); 

        pnlFiscal.add(new JLabel("Calle")).setBounds(20, 50, 60, 25);
        pnlFiscal.add(new JTextField()).setBounds(90, 50, 480, 25);
        pnlFiscal.add(new JLabel("No. Ext")).setBounds(580, 50, 50, 25);
        pnlFiscal.add(new JTextField()).setBounds(630, 50, 210, 25);

        pnlFiscal.add(new JLabel("Colonia")).setBounds(20, 85, 60, 25);
        pnlFiscal.add(new JTextField()).setBounds(90, 85, 480, 25);
        pnlFiscal.add(new JLabel("No. Int")).setBounds(580, 85, 50, 25);
        pnlFiscal.add(new JTextField()).setBounds(630, 85, 210, 25);

        pnlFiscal.add(new JLabel("Población")).setBounds(20, 120, 70, 25);
        JTextField txtPob = new JTextField(); txtPob.setBounds(90, 120, 60, 25);
        JButton btnPob = new JButton("▼"); btnPob.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnPob.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnPob.setBounds(150, 120, 20, 25);
        buscador.configurar(txtPob, null, btnPob, dPob, new String[]{"Clave", "Descripción"}, new int[]{60, 150}, null);
        pnlFiscal.add(txtPob); pnlFiscal.add(btnPob);

        pnlFiscal.add(new JLabel("Estado")).setBounds(185, 120, 50, 25);
        JTextField txtEdo = new JTextField(); txtEdo.setBounds(235, 120, 60, 25);
        JButton btnEdo = new JButton("▼"); btnEdo.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnEdo.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnEdo.setBounds(295, 120, 20, 25);
        buscador.configurar(txtEdo, null, btnEdo, dEdo, new String[]{"Clave", "Descripción"}, new int[]{60, 150}, null);
        pnlFiscal.add(txtEdo); pnlFiscal.add(btnEdo);

        pnlFiscal.add(new JLabel("País")).setBounds(330, 120, 35, 25);
        JTextField txtPais = new JTextField("MEX"); txtPais.setBounds(365, 120, 60, 25);
        JButton btnPais = new JButton("▼"); btnPais.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnPais.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnPais.setBounds(425, 120, 20, 25);
        buscador.configurar(txtPais, null, btnPais, dPais, new String[]{"Clave", "Descripción"}, new int[]{60, 150}, null);
        pnlFiscal.add(txtPais); pnlFiscal.add(btnPais);

        pnlFiscal.add(new JLabel("Código Postal")).setBounds(465, 120, 90, 25);
        pnlFiscal.add(new JTextField()).setBounds(555, 120, 285, 25);

        pnlFiscal.add(new JLabel("R.F.C.")).setBounds(20, 155, 60, 25);
        pnlFiscal.add(new JTextField()).setBounds(90, 155, 290, 25);
        pnlFiscal.add(new JLabel("Teléfono")).setBounds(395, 155, 60, 25);
        pnlFiscal.add(new JTextField()).setBounds(455, 155, 385, 25);

        pnlFiscal.add(new JLabel("CURP")).setBounds(20, 190, 60, 25);
        pnlFiscal.add(new JTextField()).setBounds(90, 190, 290, 25);
        pnlFiscal.add(new JLabel("Correo Electrónico")).setBounds(395, 190, 120, 25);
        pnlFiscal.add(new JTextField()).setBounds(515, 190, 325, 25);

        pnlFiscal.add(new JLabel("Método")).setBounds(20, 225, 60, 25);
        JTextField txtMetodo = new JTextField(); txtMetodo.setBounds(90, 225, 40, 25);
        JButton btnMetodo = new JButton("▼"); btnMetodo.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnMetodo.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnMetodo.setBounds(130, 225, 20, 25);
        buscador.configurar(txtMetodo, null, btnMetodo, dMetodo, new String[]{"Clave", "Descripción"}, new int[]{60, 200}, null);
        pnlFiscal.add(txtMetodo); pnlFiscal.add(btnMetodo);

        pnlFiscal.add(new JLabel("Banco")).setBounds(160, 225, 40, 25);
        JTextField txtBanco = new JTextField(); txtBanco.setBounds(200, 225, 40, 25);
        JButton btnBanco = new JButton("▼"); btnBanco.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnBanco.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnBanco.setBounds(240, 225, 20, 25);
        buscador.configurar(txtBanco, null, btnBanco, dBanco, new String[]{"Clave", "Descripción"}, new int[]{60, 200}, null);
        pnlFiscal.add(txtBanco); pnlFiscal.add(btnBanco);

        pnlFiscal.add(new JLabel("Cuenta")).setBounds(275, 225, 50, 25);
        pnlFiscal.add(new JTextField()).setBounds(325, 225, 100, 25);

        pnlFiscal.add(new JLabel("Utilizar Método de")).setBounds(440, 225, 110, 25);
        JRadioButton rbRecibo = new JRadioButton("Recibo", true); rbRecibo.setBounds(550, 225, 70, 25);
        JRadioButton rbAlumno = new JRadioButton("Alumno"); rbAlumno.setBounds(620, 225, 70, 25);
        ButtonGroup bgMetodo = new ButtonGroup(); bgMetodo.add(rbRecibo); bgMetodo.add(rbAlumno);
        pnlFiscal.add(rbRecibo); pnlFiscal.add(rbAlumno);

        JButton btnAceptarInterno = new JButton("Aceptar");
        btnAceptarInterno.setBounds(720, 225, 100, 25);
        pnlFiscal.add(btnAceptarInterno);

        pestanas.addTab("Información Fiscal", pnlFiscal);
        pestanas.addTab("Concepto en Factura", new JPanel());
        dialogo.add(pestanas);

        // 4. TABLA INFERIOR (Recibos Sin Facturar)
        JTable tblRecibos = new JTable(new DefaultTableModel(
            new Object[][]{}, 
            new String[]{"Compañía", "C. Costos", "Descripción", "Ciclo Escolar", "Matrícula", "Num Recibo", "Fec Recibo", "Moneda", "Importe MN"}
        ));
        JScrollPane scrollRecibos = new JScrollPane(tblRecibos);
        scrollRecibos.setBorder(BorderFactory.createTitledBorder("Recibos Sin Facturar"));
        scrollRecibos.setBounds(10, 440, 880, 160);
        dialogo.add(scrollRecibos);

        // 5. BOTONES PRINCIPALES
        JButton btnAceptar = new JButton("Aceptar");
        btnAceptar.setBounds(330, 620, 110, 40);
        JButton btnSalir = new JButton("Salir");
        btnSalir.setBounds(460, 620, 110, 40);

        dialogo.add(btnAceptar);
        dialogo.add(btnSalir);

        // 6. EVENTOS
        btnSalir.addActionListener(e -> dialogo.dispose());

        btnFiltra.addActionListener(e -> {
            String cia = cmbCia.getSelectedItem().toString();
            String cc = cmbCC.getSelectedItem().toString();
            String mat = txtMat.getText().trim();
            java.util.Date fIniUtil = txtFecIni.getDate();
            java.util.Date fFinUtil = txtFecFin.getDate();

            if (fIniUtil == null || fFinUtil == null) {
                JOptionPane.showMessageDialog(dialogo, "Selecciona el rango de fechas.");
                return;
            }

            java.sql.Date fIni = new java.sql.Date(fIniUtil.getTime());
            java.sql.Date fFin = new java.sql.Date(fFinUtil.getTime());

            DefaultTableModel modeloRec = new DefaultTableModel(
                    new Object[][]{},
                    new String[]{"Compañía", "C. Costos", "Descripción", "Ciclo Escolar", "Matrícula", "Num Recibo", "Fec Recibo", "Moneda", "Importe MN"}
            ) {
                @Override
                public boolean isCellEditable(int row, int col) { return false; }
            };

            ConDB db = new ConDB();
            try {
                Connection con = db.Conectar();
                if (con != null) {
                    StringBuilder sql = new StringBuilder(
                        "SELECT CIA, CC, DCPTO, CESC, MAT, NREC, FREC, CMON, IMPTMN " +
                        "FROM tesralu WHERE CIA = ? AND CC = ? AND (NFAC IS NULL OR NFAC = 0) " +
                        "AND FREC BETWEEN ? AND ?"
                    );
                    if (!mat.isEmpty()) sql.append(" AND MAT = ?");
                    sql.append(" ORDER BY FREC, NREC");

                    PreparedStatement ps = con.prepareStatement(sql.toString());
                    int idx = 1;
                    ps.setString(idx++, cia);
                    ps.setString(idx++, cc);
                    ps.setDate(idx++, fIni);
                    ps.setDate(idx++, fFin);
                    if (!mat.isEmpty()) ps.setString(idx++, mat);

                    ResultSet rs = ps.executeQuery();
                    double totalGeneral = 0;
                    while (rs.next()) {
                        double importe = rs.getDouble("IMPTMN");
                        totalGeneral += importe;
                        modeloRec.addRow(new Object[]{
                            rs.getString("CIA"), rs.getString("CC"), rs.getString("DCPTO"),
                            rs.getString("CESC"), rs.getString("MAT"), rs.getInt("NREC"),
                            rs.getDate("FREC"), rs.getString("CMON"), String.format("%.2f", importe)
                        });
                    }
                    rs.close(); ps.close();

                    tblRecibos.setModel(modeloRec);
                    txtTotal.setText(String.format("%.2f", totalGeneral));

                    if (modeloRec.getRowCount() == 0) {
                        JOptionPane.showMessageDialog(dialogo, "No se encontraron recibos sin facturar con esos filtros.");
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialogo, "Error al buscar recibos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                db.Cerrar();
            }
        });

        btnAceptar.addActionListener(e -> {
            if (tblRecibos.getRowCount() == 0) {
                JOptionPane.showMessageDialog(dialogo, "No hay recibos cargados para facturar. Usa 'Filtra Información' primero.");
                return;
            }

            String cia = cmbCia.getSelectedItem().toString();
            java.util.Date fechaFacturaUtil = txtFecFactura.getDate();
            String tipoFac = txtTipoFac.getText().trim();
            String usuarioActivo = obtenerUsuarioActivo();

            if (fechaFacturaUtil == null || tipoFac.isEmpty()) {
                JOptionPane.showMessageDialog(dialogo, "Captura la fecha y el tipo de factura.");
                return;
            }

            int resp = JOptionPane.showConfirmDialog(dialogo,
                    "¿Generar la factura con " + tblRecibos.getRowCount() + " concepto(s) por un total de $" + txtTotal.getText() + "?",
                    "Confirmar Facturación", JOptionPane.YES_NO_OPTION);
            if (resp != JOptionPane.YES_OPTION) return;

            java.sql.Date sqlFecha = new java.sql.Date(fechaFacturaUtil.getTime());
            ConDB db = new ConDB();
            Connection con = null;
            try {
                con = db.Conectar();
                if (con == null) {
                    JOptionPane.showMessageDialog(dialogo, "No se pudo conectar a la base de datos.");
                    return;
                }
                con.setAutoCommit(false);

                // Siguiente número de factura para esta compañía
                PreparedStatement psMax = con.prepareStatement("SELECT COALESCE(MAX(NFAC),0)+1 FROM tesfalu WHERE CIA = ?");
                psMax.setString(1, cia);
                ResultSet rsMax = psMax.executeQuery();
                rsMax.next();
                int nuevoNFac = rsMax.getInt(1);
                rsMax.close(); psMax.close();

                String sqlInsert = "INSERT INTO tesfalu (CIA, CC, MAT, NFAC, TFAC, FFAC, DCPTO, CMON, IMPTMN, STAFAC, USER, FEAC) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement psIns = con.prepareStatement(sqlInsert);

                String sqlUpdateRecibo = "UPDATE tesralu SET NFAC = ?, TFAC = ?, FFAC = ? WHERE CIA = ? AND CC = ? AND NREC = ? AND MAT = ?";
                PreparedStatement psUpd = con.prepareStatement(sqlUpdateRecibo);

                for (int i = 0; i < tblRecibos.getRowCount(); i++) {
                    String rCia = tblRecibos.getValueAt(i, 0).toString();
                    String rCc = tblRecibos.getValueAt(i, 1).toString();
                    String rDesc = tblRecibos.getValueAt(i, 2).toString();
                    String rMat = tblRecibos.getValueAt(i, 4).toString();
                    int rNrec = Integer.parseInt(tblRecibos.getValueAt(i, 5).toString());
                    String rMon = tblRecibos.getValueAt(i, 7).toString();
                    
                    // Índice 8 en un modelo de 9 columnas corresponde a Importe MN
                    double rImporte = Double.parseDouble(tblRecibos.getValueAt(i, 8).toString().replace(",", ""));

                    psIns.setString(1, rCia);
                    psIns.setString(2, rCc);
                    psIns.setString(3, rMat);
                    psIns.setInt(4, nuevoNFac);
                    psIns.setString(5, tipoFac);
                    psIns.setDate(6, sqlFecha);
                    psIns.setString(7, rDesc);
                    psIns.setString(8, rMon);
                    psIns.setDouble(9, rImporte);
                    psIns.setString(10, "FA");
                    psIns.setString(11, usuarioActivo);
                    psIns.setDate(12, sqlFecha);
                    psIns.addBatch();

                    psUpd.setInt(1, nuevoNFac);
                    psUpd.setString(2, tipoFac);
                    psUpd.setDate(3, sqlFecha);
                    psUpd.setString(4, rCia);
                    psUpd.setString(5, rCc);
                    psUpd.setInt(6, rNrec);
                    psUpd.setString(7, rMat);
                    psUpd.addBatch();
                }

                psIns.executeBatch();
                psUpd.executeBatch();
                con.commit();
                psIns.close(); psUpd.close();

                JOptionPane.showMessageDialog(dialogo, "Factura " + nuevoNFac + " generada correctamente.");
                dialogo.dispose();
                cargarTablaFacturas();

            } catch (Exception ex) {
                try { if (con != null) con.rollback(); } catch (Exception ex2) {}
                JOptionPane.showMessageDialog(dialogo, "Error al generar la factura: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                try { if (con != null) con.setAutoCommit(true); } catch (Exception ex3) {}
                db.Cerrar();
            }
        });

        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddIFactura;
    private javax.swing.JButton btnDeleteIFactura;
    private javax.swing.JButton btnEditIFactura;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblImpresionFactura;
    // End of variables declaration//GEN-END:variables
}
