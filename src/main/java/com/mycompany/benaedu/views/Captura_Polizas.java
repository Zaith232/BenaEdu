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
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author b17za
 */
public class Captura_Polizas extends javax.swing.JPanel {

    private DefaultTableModel modDetalle;
    
    /**
     * Creates new form Captura_Polizas
     */
    public Captura_Polizas() {
        initComponents();
    }
private void construirInterfazCapturaPolizas() {
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

        Object[][] dTPol  = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'TPOL' ORDER BY CVE", 2);
        Object[][] dTCont = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'TCONT' ORDER BY CVE", 2);
        Object[][] dTMon  = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'TMON' ORDER BY CVE", 2);
        Object[][] dTSsc  = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'TSSC' ORDER BY CVE", 2);
        Object[][] dTUm   = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'TUM' ORDER BY CVE", 2);
        Object[][] dCta   = cargarDatosMultiple.apply("SELECT CCTA, CDES FROM tmctas ORDER BY CCTA", 2);

        // --- PESTAÑAS PRINCIPALES ---
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setBounds(10, 10, 945, 590);

        JPanel pnlConsultaPolizas = new JPanel(null); // Pestaña 1
        JPanel pnlCaptura = new JPanel(null);          // Pestaña 2 (Captura Activa)
        JPanel pnlModelo = new JPanel(null);           // Pestaña 3

        // ==========================================
        // DISEÑO DE LA PESTAÑA DE CAPTURA
        // ==========================================
        
        // --- 1. CABECERA DE LA PÓLIZA ---
        pnlCaptura.add(new JLabel("Compañía")).setBounds(20, 20, 80, 25);
        JComboBox<String> cmbCia = new JComboBox<>(); cmbCia.setBounds(110, 20, 80, 25);
        pnlCaptura.add(cmbCia);

        try {
            ConDB db = new ConDB();
            Connection con = db.Conectar();
            if (con != null) {
                ResultSet rsCia = con.prepareStatement("SELECT CIA FROM tmcias ORDER BY CIA").executeQuery();
                while(rsCia.next()) cmbCia.addItem(rsCia.getString("CIA"));
                rsCia.close(); db.Cerrar();
            }
        } catch (Exception ex) { cmbCia.addItem("12"); }

        // Tipo Pol. (TPOL)
        pnlCaptura.add(new JLabel("Tipo Pol.")).setBounds(20, 50, 80, 25);
        JTextField txtTipoPol = new JTextField("DR"); txtTipoPol.setBounds(110, 50, 55, 25);
        JButton btnTipoPol = new JButton("▼"); btnTipoPol.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnTipoPol.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnTipoPol.setBounds(165, 50, 20, 25);
        buscador.configurar(txtTipoPol, null, btnTipoPol, dTPol, new String[]{"Clave", "Descripción"}, new int[]{60, 200});
        pnlCaptura.add(txtTipoPol); pnlCaptura.add(btnTipoPol);

        // Tipo Cont. (TCONT)
        pnlCaptura.add(new JLabel("Tipo de Cont.")).setBounds(20, 80, 80, 25);
        JTextField txtTipoCont = new JTextField("MN"); txtTipoCont.setBounds(110, 80, 55, 25);
        JButton btnTipoCont = new JButton("▼"); btnTipoCont.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnTipoCont.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnTipoCont.setBounds(165, 80, 20, 25);
        buscador.configurar(txtTipoCont, null, btnTipoCont, dTCont, new String[]{"Clave", "Descripción"}, new int[]{60, 200});
        pnlCaptura.add(txtTipoCont); pnlCaptura.add(btnTipoCont);

        // Tipo Moneda (TMON)
        pnlCaptura.add(new JLabel("Tipo Moneda")).setBounds(20, 110, 80, 25);
        JTextField txtTipoMoneda = new JTextField("MXP"); txtTipoMoneda.setBounds(110, 110, 55, 25);
        JButton btnTipoMoneda = new JButton("▼"); btnTipoMoneda.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnTipoMoneda.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnTipoMoneda.setBounds(165, 110, 20, 25);
        JTextField txtDescMoneda = new JTextField("PESOS"); txtDescMoneda.setBounds(190, 110, 150, 25); txtDescMoneda.setEditable(false); txtDescMoneda.setBackground(new java.awt.Color(240,240,240));
        buscador.configurar(txtTipoMoneda, txtDescMoneda, btnTipoMoneda, dTMon, new String[]{"Clave", "Descripción"}, new int[]{60, 200});
        pnlCaptura.add(txtTipoMoneda); pnlCaptura.add(btnTipoMoneda); pnlCaptura.add(txtDescMoneda);

        // Núm Póliza
        pnlCaptura.add(new JLabel("Núm. Poliza")).setBounds(250, 20, 80, 25);
        JTextField txtNumPoliza = new JTextField(); txtNumPoliza.setBounds(330, 20, 80, 25);
        pnlCaptura.add(txtNumPoliza);

        // Fecha
        pnlCaptura.add(new JLabel("Fecha")).setBounds(470, 20, 50, 25);
        com.toedter.calendar.JDateChooser txtFechaPol = new com.toedter.calendar.JDateChooser();
        txtFechaPol.setDateFormatString("dd/MM/yyyy");
        txtFechaPol.setDate(new java.util.Date());
        txtFechaPol.setBounds(530, 20, 110, 25);
        pnlCaptura.add(txtFechaPol);

        // Año / Período
        pnlCaptura.add(new JLabel("Año")).setBounds(660, 0, 40, 20);
        JTextField txtAno = new JTextField("2026"); txtAno.setBounds(650, 20, 50, 25);
        pnlCaptura.add(txtAno);
        
        pnlCaptura.add(new JLabel("Periodo")).setBounds(720, 0, 50, 20);
        JTextField txtPeriodo = new JTextField("6"); txtPeriodo.setBounds(720, 20, 40, 25);
        pnlCaptura.add(txtPeriodo);

        // Descripción de la Póliza
        pnlCaptura.add(new JLabel("Descripción")).setBounds(450, 50, 80, 25);
        JTextField txtDesc = new JTextField(); txtDesc.setBounds(530, 50, 390, 25);
        pnlCaptura.add(txtDesc);

        // --- 2. BARRA DE CAPTURA RÁPIDA DE PARTIDAS ---
        JPanel pnlPartida = new JPanel(null);
        pnlPartida.setBorder(BorderFactory.createEtchedBorder());
        pnlPartida.setBounds(10, 145, 920, 55);

        // No. Partida
        pnlPartida.add(new JLabel("No.")).setBounds(10, 5, 35, 15);
        JTextField txtNumPartida = new JTextField("1"); txtNumPartida.setBounds(10, 20, 35, 25);
        pnlPartida.add(txtNumPartida);

        // Centro Costo
        pnlPartida.add(new JLabel("Centro Costo")).setBounds(50, 5, 90, 15);
        JComboBox<String> cmbCentroCosto = new JComboBox<>(); cmbCentroCosto.setBounds(50, 20, 90, 25);
        try {
            ConDB db = new ConDB();
            Connection con = db.Conectar();
            if (con != null) {
                ResultSet rsCC = con.prepareStatement("SELECT CVE FROM tgcc WHERE CVE IN ('12100', '12200', '12300', '12400') ORDER BY CVE").executeQuery();
                while(rsCC.next()) cmbCentroCosto.addItem(rsCC.getString("CVE"));
                rsCC.close(); db.Cerrar();
            }
        } catch(Exception ex) {}
        pnlPartida.add(cmbCentroCosto);

        // Cuenta
        pnlPartida.add(new JLabel("Cuenta")).setBounds(145, 5, 110, 15);
        JTextField txtCuentaPart = new JTextField(); txtCuentaPart.setBounds(145, 20, 90, 25);
        JButton btnCuentaPart = new JButton("▼"); btnCuentaPart.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnCuentaPart.setMargin(new java.awt.Insets(0,0,0,0)); btnCuentaPart.setBounds(235, 20, 20, 25);
        buscador.configurar(txtCuentaPart, null, btnCuentaPart, dCta, new String[]{"Cuenta", "Descripción"}, new int[]{110, 250});
        pnlPartida.add(txtCuentaPart); pnlPartida.add(btnCuentaPart);

        // Explicación / Concepto
        pnlPartida.add(new JLabel("Explicación")).setBounds(260, 5, 170, 15);
        JTextField txtExplicacionPart = new JTextField(); txtExplicacionPart.setBounds(260, 20, 170, 25);
        pnlPartida.add(txtExplicacionPart);

        // Tipo SSC (TSSC)
        pnlPartida.add(new JLabel("Tipo SSC")).setBounds(440, 5, 80, 15);
        JTextField txtTipoSSC = new JTextField(); txtTipoSSC.setBounds(440, 20, 60, 25);
        JButton btnTipoSSC = new JButton("▼"); btnTipoSSC.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnTipoSSC.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnTipoSSC.setBounds(500, 20, 20, 25);
        buscador.configurar(txtTipoSSC, null, btnTipoSSC, dTSsc, new String[]{"Clave", "Descripción"}, new int[]{60, 200});
        pnlPartida.add(txtTipoSSC); pnlPartida.add(btnTipoSSC);

        // SubSubCta
        pnlPartida.add(new JLabel("SubSubCta")).setBounds(535, 5, 90, 15);
        JTextField txtSubSub = new JTextField("0"); txtSubSub.setBounds(535, 20, 90, 25);
        pnlPartida.add(txtSubSub);

        // Cargo / Crédito
        pnlPartida.add(new JLabel("Cargo (+)")).setBounds(635, 5, 80, 15);
        JTextField txtCargoPart = new JTextField("0.00"); txtCargoPart.setBounds(635, 20, 80, 25);
        pnlPartida.add(txtCargoPart);

        pnlPartida.add(new JLabel("Crédito (-)")).setBounds(720, 5, 80, 15);
        JTextField txtCreditoPart = new JTextField("0.00"); txtCreditoPart.setBounds(720, 20, 80, 25);
        pnlPartida.add(txtCreditoPart);

        // Unidades y U.M.
        pnlPartida.add(new JLabel("U.M.")).setBounds(805, 5, 40, 15);
        JTextField txtUM = new JTextField(); txtUM.setBounds(805, 20, 30, 25);
        JButton btnUM = new JButton("▼"); btnUM.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnUM.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnUM.setBounds(835, 20, 15, 25);
        buscador.configurar(txtUM, null, btnUM, dTUm, new String[]{"Clave", "Descripción"}, new int[]{60, 150});
        pnlPartida.add(txtUM); pnlPartida.add(btnUM);

        // Botón OK
        JButton btnOk = new JButton("OK"); btnOk.setBounds(855, 20, 55, 25); 
        pnlPartida.add(btnOk);

        pnlCaptura.add(pnlPartida);

        // --- 3. TABLA DEL DETALLE CONTABLE ---
        modDetalle = new DefaultTableModel(
            new Object[][]{}, 
            new String[]{"No.", "CC", "Cuenta", "Explicación", "Tpo SSC", "Sub-Sub", "Cargo", "Crédito"}
        ) { @Override public boolean isCellEditable(int r, int c) { return false; } };

        JTable tblDetallePoliza = new JTable(modDetalle);
        tblDetallePoliza.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblDetallePoliza.getColumnModel().getColumn(0).setPreferredWidth(40);
        tblDetallePoliza.getColumnModel().getColumn(1).setPreferredWidth(60);
        tblDetallePoliza.getColumnModel().getColumn(2).setPreferredWidth(110);
        tblDetallePoliza.getColumnModel().getColumn(3).setPreferredWidth(280);
        tblDetallePoliza.getColumnModel().getColumn(4).setPreferredWidth(70);
        tblDetallePoliza.getColumnModel().getColumn(5).setPreferredWidth(90);
        tblDetallePoliza.getColumnModel().getColumn(6).setPreferredWidth(110);
        tblDetallePoliza.getColumnModel().getColumn(7).setPreferredWidth(110);

        JScrollPane scrollDetalle = new JScrollPane(tblDetallePoliza);
        scrollDetalle.setBounds(10, 205, 920, 280);
        pnlCaptura.add(scrollDetalle);

        // --- 4. TOTALES (Cuadratura Contable) ---
        JPanel pnlTotales = new JPanel(null);
        pnlTotales.setBorder(BorderFactory.createEtchedBorder());
        pnlTotales.setBounds(10, 490, 920, 50);

        pnlTotales.add(new JLabel("Cargos")).setBounds(30, 15, 60, 25);
        JTextField txtTotCargos = new JTextField("0.00"); txtTotCargos.setBounds(90, 15, 120, 25);
        txtTotCargos.setHorizontalAlignment(JTextField.RIGHT); txtTotCargos.setEditable(false);
        pnlTotales.add(txtTotCargos);

        pnlTotales.add(new JLabel("Créditos")).setBounds(330, 15, 60, 25);
        JTextField txtTotCreditos = new JTextField("0.00"); txtTotCreditos.setBounds(390, 15, 120, 25);
        txtTotCreditos.setHorizontalAlignment(JTextField.RIGHT); txtTotCreditos.setEditable(false);
        pnlTotales.add(txtTotCreditos);

        pnlTotales.add(new JLabel("Diferencia")).setBounds(630, 15, 70, 25);
        JTextField txtTotDif = new JTextField("0.00"); txtTotDif.setBounds(700, 15, 120, 25);
        txtTotDif.setHorizontalAlignment(JTextField.RIGHT); txtTotDif.setEditable(false);
        pnlTotales.add(txtTotDif);

        pnlCaptura.add(pnlTotales);

        pestanas.addTab("Consulta de Pólizas", pnlConsultaPolizas);
        pestanas.addTab("Consulta de Póliza", pnlCaptura);
        pestanas.addTab("Pólizas Modelo", pnlModelo);
        pestanas.setSelectedIndex(1);

        this.add(pestanas);

        // --- 5. BOTONES INFERIORES ---
        JButton btnGuardar = new JButton("Guardar");
        btnGuardar.setBounds(410, 550, 120, 35);
        this.add(btnGuardar);

        // --- 6. EVENTOS Y LÓGICA CONTABLE ---

        // Función para recalcular totales de la póliza
        Runnable recalcularTotales = () -> {
            double cSum = 0.0, aSum = 0.0;
            java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");
            
            for (int i = 0; i < modDetalle.getRowCount(); i++) {
                try {
                    cSum += Double.parseDouble(modDetalle.getValueAt(i, 6).toString().replace(",", ""));
                    aSum += Double.parseDouble(modDetalle.getValueAt(i, 7).toString().replace(",", ""));
                } catch(Exception ex) {}
            }
            
            txtTotCargos.setText(df.format(cSum));
            txtTotCreditos.setText(df.format(aSum));
            txtTotDif.setText(df.format(Math.abs(cSum - aSum)));
        };

        // Agregar Partida a la Tabla de Captura
        btnOk.addActionListener(e -> {
            String cta = txtCuentaPart.getText().trim();
            if (cta.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe ingresar una cuenta contable válida.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");
            double cargo = 0.0, credito = 0.0;
            try { cargo = Double.parseDouble(txtCargoPart.getText().trim().replace(",", "")); } catch(Exception ex) {}
            try { credito = Double.parseDouble(txtCreditoPart.getText().trim().replace(",", "")); } catch(Exception ex) {}

            Object[] fila = new Object[8];
            fila[0] = txtNumPartida.getText().trim();
            fila[1] = cmbCentroCosto.getSelectedItem() != null ? cmbCentroCosto.getSelectedItem().toString() : "";
            fila[2] = cta;
            fila[3] = txtExplicacionPart.getText().trim();
            fila[4] = txtTipoSSC.getText().trim();
            fila[5] = txtSubSub.getText().trim();
            fila[6] = df.format(cargo);
            fila[7] = df.format(credito);

            modDetalle.addRow(fila);
            recalcularTotales.run();

            // Limpiar fila de partida e incrementar número
            txtNumPartida.setText(String.valueOf(modDetalle.getRowCount() + 1));
            txtCuentaPart.setText("");
            txtExplicacionPart.setText("");
            txtCargoPart.setText("0.00");
            txtCreditoPart.setText("0.00");
            txtCuentaPart.requestFocus();
        });

        // GUARDADO TRANSACCIONAL DE LA PÓLIZA (tgpol + tdpol)
        btnGuardar.addActionListener(e -> {
            String numPoliza = txtNumPoliza.getText().trim();
            String tipoPol = txtTipoPol.getText().trim();
            String cia = cmbCia.getSelectedItem() != null ? cmbCia.getSelectedItem().toString() : "12";

            if (numPoliza.isEmpty() || tipoPol.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El Tipo y Número de Póliza son obligatorios.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (modDetalle.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Debe capturar al menos una partida en la póliza.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            String fechaPol = txtFechaPol.getDate() != null ? sdf.format(txtFechaPol.getDate()) : "";

            try {
                ConDB db = new ConDB();
                Connection con = db.Conectar();
                if (con != null) {
                    con.setAutoCommit(false); // Iniciar Transacción

                    // 1. Guardar o actualizar encabezado de la póliza en tgpol
                    String sqlTgpol = "INSERT INTO tgpol (CIA, TPOL, NPOL, FPOL, ANO, NPER, CMON, TCONT, MTO, STAC) " +
                                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'A') " +
                                      "ON DUPLICATE KEY UPDATE FPOL = VALUES(FPOL), MTO = VALUES(MTO)";

                    double totalMto = 0.0;
                    try { totalMto = Double.parseDouble(txtTotCargos.getText().replace(",", "")); } catch(Exception ex) {}

                    PreparedStatement psHead = con.prepareStatement(sqlTgpol);
                    psHead.setString(1, cia);
                    psHead.setString(2, tipoPol);
                    psHead.setString(3, numPoliza);
                    psHead.setString(4, fechaPol);
                    psHead.setString(5, txtAno.getText().trim());
                    psHead.setString(6, txtPeriodo.getText().trim());
                    psHead.setString(7, txtTipoMoneda.getText().trim());
                    psHead.setString(8, txtTipoCont.getText().trim());
                    psHead.setDouble(9, totalMto);
                    psHead.executeUpdate();
                    psHead.close();

                    // 2. Limpiar detalle anterior de la póliza en tdpol si existe
                    String sqlCleanDet = "DELETE FROM tdpol WHERE CIA = ? AND TPOL = ? AND NPOL = ?";
                    PreparedStatement psClean = con.prepareStatement(sqlCleanDet);
                    psClean.setString(1, cia);
                    psClean.setString(2, tipoPol);
                    psClean.setString(3, numPoliza);
                    psClean.executeUpdate();
                    psClean.close();

                    // 3. Insertar partidas en tdpol
                    String sqlTdpol = "INSERT INTO tdpol (CIA, TPOL, NPOL, FPOL, NPAR, TCONT, CCTA, MTO, TSSC, SSC, CONC, CMON) " +
                                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement psDet = con.prepareStatement(sqlTdpol);

                    for (int i = 0; i < modDetalle.getRowCount(); i++) {
                        double cargo = Double.parseDouble(modDetalle.getValueAt(i, 6).toString().replace(",", ""));
                        double credito = Double.parseDouble(modDetalle.getValueAt(i, 7).toString().replace(",", ""));
                        double montoPartida = cargo > 0 ? cargo : -credito;

                        psDet.setString(1, cia);
                        psDet.setString(2, tipoPol);
                        psDet.setString(3, numPoliza);
                        psDet.setString(4, fechaPol);
                        psDet.setInt(5, i + 1);
                        psDet.setString(6, txtTipoCont.getText().trim());
                        psDet.setString(7, modDetalle.getValueAt(i, 2).toString());
                        psDet.setDouble(8, montoPartida);
                        psDet.setString(9, modDetalle.getValueAt(i, 4).toString());
                        psDet.setString(10, modDetalle.getValueAt(i, 5).toString());
                        psDet.setString(11, modDetalle.getValueAt(i, 3).toString());
                        psDet.setString(12, txtTipoMoneda.getText().trim());
                        psDet.addBatch();
                    }

                    psDet.executeBatch();
                    psDet.close();

                    con.commit(); // Confirmar transacción
                    db.Cerrar();

                    JOptionPane.showMessageDialog(this, "Póliza " + numPoliza + " guardada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);

                    // Limpiar pantalla para la siguiente captura
                    modDetalle.setRowCount(0);
                    txtNumPoliza.setText("");
                    txtDesc.setText("");
                    recalcularTotales.run();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar la póliza: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        tblCPolizas = new javax.swing.JTable();
        btnAddCPolizas = new javax.swing.JButton();
        btnEditCPolizas = new javax.swing.JButton();
        btnDeleteCPolizas = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        tblCPolizas.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tblCPolizas);

        btnAddCPolizas.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAddCPolizas.setForeground(new java.awt.Color(26, 61, 99));
        btnAddCPolizas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/add.png"))); // NOI18N
        btnAddCPolizas.setText("Añadir");
        btnAddCPolizas.addActionListener(this::btnAddCPolizasActionPerformed);

        btnEditCPolizas.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEditCPolizas.setForeground(new java.awt.Color(26, 61, 99));
        btnEditCPolizas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edit.png"))); // NOI18N
        btnEditCPolizas.setText("Editar");
        btnEditCPolizas.setMaximumSize(new java.awt.Dimension(93, 31));
        btnEditCPolizas.setMinimumSize(new java.awt.Dimension(93, 31));
        btnEditCPolizas.addActionListener(this::btnEditCPolizasActionPerformed);

        btnDeleteCPolizas.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDeleteCPolizas.setForeground(new java.awt.Color(26, 61, 99));
        btnDeleteCPolizas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/delete.png"))); // NOI18N
        btnDeleteCPolizas.setText("Eliminar");
        btnDeleteCPolizas.addActionListener(this::btnDeleteCPolizasActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAddCPolizas)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditCPolizas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteCPolizas)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddCPolizas, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditCPolizas, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteCPolizas, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void btnAddCPolizasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddCPolizasActionPerformed
   
    }//GEN-LAST:event_btnAddCPolizasActionPerformed

    private void btnEditCPolizasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditCPolizasActionPerformed

    }//GEN-LAST:event_btnEditCPolizasActionPerformed

    private void btnDeleteCPolizasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteCPolizasActionPerformed
   
    }//GEN-LAST:event_btnDeleteCPolizasActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddCPolizas;
    private javax.swing.JButton btnDeleteCPolizas;
    private javax.swing.JButton btnEditCPolizas;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblCPolizas;
    // End of variables declaration//GEN-END:variables
}
