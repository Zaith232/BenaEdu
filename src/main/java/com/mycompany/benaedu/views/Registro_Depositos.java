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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author b17za
 */
public class Registro_Depositos extends javax.swing.JPanel {

    /**
     * Creates new form Registro_Depositos
     */
    public Registro_Depositos() {
        initComponents();
         construirInterfazRegistroDepositos();
    }
private void construirInterfazRegistroDepositos() {
        this.removeAll();
        this.setLayout(null);
        this.setBackground(new java.awt.Color(238, 238, 238));

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
                                if (txtDesc != null && mod.getColumnCount() > 1) {
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

        // --- CARGA DE DATOS PARA LOS BUSCADORES ---
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

        Object[][] dMoneda    = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'TMON' ORDER BY CVE", 2);
        Object[][] dFormaPago = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'IPAG' ORDER BY CVE", 2);
        Object[][] dBanco     = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'BCOS' ORDER BY CVE", 2);
        Object[][] dCtaCargo  = cargarDatosMultiple.apply("SELECT c.CCTA, c.CDES FROM tmctas c INNER JOIN tmcban b ON c.CCTA = b.CCTA WHERE c.CCTA LIKE '12%' ORDER BY c.CCTA", 2);

        // ==========================================
        // 1. DATOS DE SELECCIÓN
        // ==========================================
        JPanel pnlSel = new JPanel(null);
        pnlSel.setBorder(BorderFactory.createTitledBorder("Datos de selección"));
        pnlSel.setBounds(10, 10, 710, 95);

        pnlSel.add(new JLabel("Compañía")).setBounds(15, 20, 70, 25);
        JComboBox<String> cmbCia = new JComboBox<>();
        cmbCia.setBounds(90, 20, 60, 25);

        try {
            ConDB db = new ConDB();
            Connection con = db.Conectar();
            if (con != null) {
                ResultSet rsCia = con.prepareStatement("SELECT CIA FROM tmcias ORDER BY CIA").executeQuery();
                while(rsCia.next()) cmbCia.addItem(rsCia.getString("CIA"));
                rsCia.close(); db.Cerrar();
            }
        } catch (Exception ex) { cmbCia.addItem("12"); }
        pnlSel.add(cmbCia);

        // Buscador Moneda
        pnlSel.add(new JLabel("Moneda")).setBounds(500, 20, 60, 25);
        JTextField txtMoneda = new JTextField("MXP"); txtMoneda.setBounds(565, 20, 40, 25);
        JButton btnMoneda = new JButton("▼"); btnMoneda.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnMoneda.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnMoneda.setBounds(605, 20, 20, 25);
        JTextField txtMonedaDesc = new JTextField("PESOS"); txtMonedaDesc.setBounds(630, 20, 65, 25); txtMonedaDesc.setEditable(false); txtMonedaDesc.setBackground(new java.awt.Color(240,240,240));
        buscador.configurar(txtMoneda, txtMonedaDesc, btnMoneda, dMoneda, new String[]{"Clave", "Descripción"}, new int[]{50, 150});
        pnlSel.add(txtMoneda); pnlSel.add(btnMoneda); pnlSel.add(txtMonedaDesc);

        // Fecha Inicial
        pnlSel.add(new JLabel("Fecha Inicial")).setBounds(15, 55, 80, 25);
        com.toedter.calendar.JDateChooser txtFecIni = new com.toedter.calendar.JDateChooser();
        txtFecIni.setDateFormatString("dd/MM/yyyy"); txtFecIni.setDate(new java.util.Date()); txtFecIni.setBounds(95, 55, 110, 25);
        pnlSel.add(txtFecIni);

        // Fecha Final
        pnlSel.add(new JLabel("Fecha Final")).setBounds(220, 55, 80, 25);
        com.toedter.calendar.JDateChooser txtFecFin = new com.toedter.calendar.JDateChooser();
        txtFecFin.setDateFormatString("dd/MM/yyyy"); txtFecFin.setDate(new java.util.Date()); txtFecFin.setBounds(295, 55, 110, 25);
        pnlSel.add(txtFecFin);

        // Buscador Forma de Pago
        pnlSel.add(new JLabel("Forma de Pago")).setBounds(420, 55, 100, 25);
        JTextField txtFormaPago = new JTextField(); txtFormaPago.setBounds(515, 55, 30, 25);
        JButton btnFormaPago = new JButton("▼"); btnFormaPago.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnFormaPago.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnFormaPago.setBounds(545, 55, 20, 25);
        JTextField txtFormaPagoDesc = new JTextField(); txtFormaPagoDesc.setBounds(570, 55, 125, 25); txtFormaPagoDesc.setEditable(false); txtFormaPagoDesc.setBackground(new java.awt.Color(240,240,240));
        buscador.configurar(txtFormaPago, txtFormaPagoDesc, btnFormaPago, dFormaPago, new String[]{"Clave", "Descripción"}, new int[]{50, 200});
        pnlSel.add(txtFormaPago); pnlSel.add(btnFormaPago); pnlSel.add(txtFormaPagoDesc);

        this.add(pnlSel);

        // ==========================================
        // 2. TIPO RECIBO Y BOTÓN FILTRAR
        // ==========================================
        JPanel pnlTipoRecibo = new JPanel(null);
        pnlTipoRecibo.setBorder(BorderFactory.createTitledBorder("Tipo Recibo"));
        pnlTipoRecibo.setBounds(730, 10, 200, 55);
        
        JRadioButton rbOficial = new JRadioButton("Oficial", true);
        rbOficial.setBounds(10, 20, 80, 20);
        JRadioButton rbPart = new JRadioButton("Particular");
        rbPart.setBounds(100, 20, 90, 20);
        ButtonGroup bgRecibo = new ButtonGroup(); bgRecibo.add(rbOficial); bgRecibo.add(rbPart);
        pnlTipoRecibo.add(rbOficial); pnlTipoRecibo.add(rbPart);
        this.add(pnlTipoRecibo);

        JButton btnFiltra = new JButton("Filtra Información");
        btnFiltra.setBounds(730, 75, 200, 30);
        this.add(btnFiltra);

        // ==========================================
        // 3. INFORMACIÓN CONTABLE
        // ==========================================
        JPanel pnlInfoCont = new JPanel(null);
        pnlInfoCont.setBorder(BorderFactory.createTitledBorder("Información Contable"));
        pnlInfoCont.setBounds(10, 110, 920, 60);
        
        pnlInfoCont.add(new JLabel("Compañía Dep")).setBounds(15, 20, 100, 25);
        JComboBox<String> cmbCiaDep = new JComboBox<>(new String[]{"12"});
        cmbCiaDep.setBounds(110, 20, 60, 25);
        pnlInfoCont.add(cmbCiaDep);
        
        pnlInfoCont.add(new JLabel("Fecha Contable")).setBounds(200, 20, 100, 25);
        com.toedter.calendar.JDateChooser txtFecContable = new com.toedter.calendar.JDateChooser();
        txtFecContable.setDateFormatString("dd/MM/yyyy"); txtFecContable.setDate(new java.util.Date()); txtFecContable.setBounds(300, 20, 110, 25);
        pnlInfoCont.add(txtFecContable);
        
        this.add(pnlInfoCont);

        // ==========================================
        // 4. DATOS DEL DEPÓSITO
        // ==========================================
        JPanel pnlDatosDep = new JPanel(null);
        pnlDatosDep.setBorder(BorderFactory.createTitledBorder("Datos del Deposito"));
        pnlDatosDep.setBounds(10, 175, 920, 90);

        // Buscador Cuenta Cargo
        pnlDatosDep.add(new JLabel("Cuenta Cargo")).setBounds(15, 20, 100, 25);
        JTextField txtCtaCargo = new JTextField(); txtCtaCargo.setBounds(110, 20, 110, 25);
        JButton btnCtaCargo = new JButton("▼"); btnCtaCargo.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnCtaCargo.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnCtaCargo.setBounds(220, 20, 20, 25);
        JTextField txtCtaCargoDesc = new JTextField(); txtCtaCargoDesc.setBounds(245, 20, 280, 25); txtCtaCargoDesc.setEditable(false); txtCtaCargoDesc.setBackground(new java.awt.Color(240,240,240));
        buscador.configurar(txtCtaCargo, txtCtaCargoDesc, btnCtaCargo, dCtaCargo, new String[]{"Cuenta", "Descripción"}, new int[]{120, 200});
        pnlDatosDep.add(txtCtaCargo); pnlDatosDep.add(btnCtaCargo); pnlDatosDep.add(txtCtaCargoDesc);

        pnlDatosDep.add(new JLabel("Tipo de Cambio")).setBounds(610, 20, 100, 25);
        JTextField txtTipoCambio = new JTextField("0.0000"); txtTipoCambio.setBounds(710, 20, 80, 25);
        txtTipoCambio.setHorizontalAlignment(JTextField.RIGHT);
        pnlDatosDep.add(txtTipoCambio);

        // Buscador Banco
        pnlDatosDep.add(new JLabel("Banco")).setBounds(15, 55, 50, 25);
        JTextField txtBanco = new JTextField(); txtBanco.setBounds(65, 55, 40, 25);
        JButton btnBanco = new JButton("▼"); btnBanco.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnBanco.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnBanco.setBounds(105, 55, 20, 25);
        JTextField txtBancoDesc = new JTextField(); txtBancoDesc.setBounds(130, 55, 130, 25); txtBancoDesc.setEditable(false); txtBancoDesc.setBackground(new java.awt.Color(240,240,240));
        buscador.configurar(txtBanco, txtBancoDesc, btnBanco, dBanco, new String[]{"Clave", "Descripción"}, new int[]{60, 200});
        pnlDatosDep.add(txtBanco); pnlDatosDep.add(btnBanco); pnlDatosDep.add(txtBancoDesc);

        pnlDatosDep.add(new JLabel("Referencia")).setBounds(270, 55, 70, 25);
        JTextField txtRefDep = new JTextField(); txtRefDep.setBounds(340, 55, 185, 25);
        pnlDatosDep.add(txtRefDep);

        pnlDatosDep.add(new JLabel("Monto a Registrar")).setBounds(540, 55, 120, 25);
        JTextField txtMontoReg1 = new JTextField("0.00"); txtMontoReg1.setBounds(660, 55, 110, 25);
        txtMontoReg1.setHorizontalAlignment(JTextField.RIGHT); txtMontoReg1.setEditable(false);
        
        JTextField txtMontoReg2 = new JTextField("0.00"); txtMontoReg2.setBounds(780, 55, 110, 25);
        txtMontoReg2.setHorizontalAlignment(JTextField.RIGHT); txtMontoReg2.setEditable(false);
        
        pnlDatosDep.add(txtMontoReg1); pnlDatosDep.add(txtMontoReg2);

        this.add(pnlDatosDep);

        // ==========================================
        // 5. TABLA DE DEPOSITOS AGRUPADOS (tespalu)
        // ==========================================
        DefaultTableModel modAgrupados = new DefaultTableModel(
            new Object[][]{}, 
            new String[]{"Compañía", "Ciclo Escolar", "Forma Pago", "Descripción", "Banco", "Descripción", "Fec Pago", "Moneda", "Importe ME", "Tipo Recibo", "Matrícula"}
        ) { @Override public boolean isCellEditable(int r, int c) { return false; } };

        JTable tblAgrupados = new JTable(modAgrupados);
        tblAgrupados.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblAgrupados.getColumnModel().getColumn(3).setPreferredWidth(140);
        tblAgrupados.getColumnModel().getColumn(5).setPreferredWidth(140);
        tblAgrupados.getColumnModel().getColumn(8).setPreferredWidth(90);

        JScrollPane scrollAgrupados = new JScrollPane(tblAgrupados);
        scrollAgrupados.setBounds(10, 275, 920, 270);
        scrollAgrupados.setBorder(BorderFactory.createTitledBorder("Depositos Agrupados"));
        this.add(scrollAgrupados);

        // ==========================================
        // 6. BOTONES INFERIORES
        // ==========================================
        JButton btnAceptar = new JButton("Aceptar");
        btnAceptar.setBounds(340, 555, 110, 40);
        btnAceptar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));

        JButton btnSalir = new JButton("Salir");
        btnSalir.setBounds(470, 555, 110, 40);

        this.add(btnAceptar);
        this.add(btnSalir);

        // ==========================================
        // 7. EVENTOS Y LÓGICA DE NEGOCIO
        // ==========================================
        btnSalir.addActionListener(e -> {
            this.removeAll();
            this.revalidate();
            this.repaint();
        });

        // Evento 1: Consulta de pagos pendientes de depositar en tespalu (SPAG != 'D')
        btnFiltra.addActionListener(e -> {
            modAgrupados.setRowCount(0);

            String cia = cmbCia.getSelectedItem() != null ? cmbCia.getSelectedItem().toString() : "12";
            String fmaPago = txtFormaPago.getText().trim();
            
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            String fIni = txtFecIni.getDate() != null ? sdf.format(txtFecIni.getDate()) : "";
            String fFin = txtFecFin.getDate() != null ? sdf.format(txtFecFin.getDate()) : "";

            try {
                ConDB db = new ConDB();
                Connection con = db.Conectar();
                if (con != null) {
                    StringBuilder sql = new StringBuilder(
                        "SELECT p.CIA, p.CESC, p.FMAPAG, m.DES AS DESC_FMAPAG, p.BCOPAG, b.DES AS DESC_BANCO, " +
                        "p.FPAG, p.CMON, p.IMPME, p.IMPMN, p.TALU, p.MAT " +
                        "FROM tespalu p " +
                        "LEFT JOIN tmclas m ON p.FMAPAG = m.CVE AND m.TBL = 'IPAG' " +
                        "LEFT JOIN tmclas b ON p.BCOPAG = b.CVE AND b.TBL = 'BCOS' " +
                        "WHERE p.CIA = ? AND (p.SPAG IS NULL OR p.SPAG != 'D') "
                    );

                    if (!fmaPago.isEmpty()) sql.append(" AND p.FMAPAG = ?");
                    if (!fIni.isEmpty() && !fFin.isEmpty()) sql.append(" AND p.FPAG BETWEEN ? AND ?");

                    sql.append(" ORDER BY p.FPAG DESC");

                    PreparedStatement ps = con.prepareStatement(sql.toString());
                    int p = 1;
                    ps.setString(p++, cia);
                    if (!fmaPago.isEmpty()) ps.setString(p++, fmaPago);
                    if (!fIni.isEmpty() && !fFin.isEmpty()) {
                        ps.setString(p++, fIni);
                        ps.setString(p++, fFin);
                    }

                    ResultSet rs = ps.executeQuery();
                    java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");
                    double sumaMonto = 0.0;

                    while (rs.next()) {
                        double imp = rs.getDouble("IMPMN");
                        Object[] fila = new Object[11];
                        fila[0] = rs.getString("CIA");
                        fila[1] = rs.getString("CESC");
                        fila[2] = rs.getString("FMAPAG");
                        fila[3] = rs.getString("DESC_FMAPAG") != null ? rs.getString("DESC_FMAPAG") : "";
                        fila[4] = rs.getString("BCOPAG");
                        fila[5] = rs.getString("DESC_BANCO") != null ? rs.getString("DESC_BANCO") : "";
                        fila[6] = rs.getString("FPAG");
                        fila[7] = rs.getString("CMON");
                        fila[8] = df.format(imp);
                        fila[9] = rs.getString("TALU");
                        fila[10] = rs.getString("MAT");

                        sumaMonto += imp;
                        modAgrupados.addRow(fila);
                    }

                    rs.close(); ps.close(); db.Cerrar();
                    txtMontoReg1.setText(df.format(sumaMonto));
                    txtMontoReg2.setText(df.format(sumaMonto));

                    if (modAgrupados.getRowCount() == 0) {
                        JOptionPane.showMessageDialog(this, "No se encontraron pagos pendientes de agrupar en depósito.", "Información", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al filtrar pagos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Evento 2: Registro de Depósito (Póliza tgpol/tdpol + Actualización tespalu.SPAG='D' + Consecutivo tgparam)
        btnAceptar.addActionListener(e -> {
            String ctaCargo = txtCtaCargo.getText().trim();
            String banco = txtBanco.getText().trim();
            String cia = cmbCia.getSelectedItem() != null ? cmbCia.getSelectedItem().toString() : "12";

            if (ctaCargo.isEmpty() || banco.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Asegúrese de proporcionar la Cuenta Cargo y el Banco.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (modAgrupados.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No hay pagos agrupados en la lista para depositar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                ConDB db = new ConDB();
                Connection con = db.Conectar();
                if (con != null) {
                    con.setAutoCommit(false); // Iniciar Transacción

                    // 1. Consecutivo NPOL de tgparam
                    int numPoliza = 1;
                    PreparedStatement psParam = con.prepareStatement("SELECT NPOL FROM tgparam WHERE CIAACT = ?");
                    psParam.setString(1, cia);
                    ResultSet rsParam = psParam.executeQuery();
                    if (rsParam.next()) {
                        numPoliza = rsParam.getInt("NPOL");
                    }
                    rsParam.close(); psParam.close();

                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                    String fecPol = txtFecContable.getDate() != null ? sdf.format(txtFecContable.getDate()) : sdf.format(new java.util.Date());
                    double montoTotal = Double.parseDouble(txtMontoReg1.getText().replace(",", ""));

                    // 2. INSERT tgpol (Encabezado de Póliza)
                    String sqlPol = "INSERT INTO tgpol (CIA, TPOL, NPOL, FPOL, ANO, NPER, CPOL, MTO, TCONT, USER) " +
                                    "VALUES (?, 'I', ?, ?, YEAR(?), MONTH(?), 'DEPOSITO', ?, 'MN', 'Admin')";
                    PreparedStatement psPol = con.prepareStatement(sqlPol);
                    psPol.setString(1, cia);
                    psPol.setInt(2, numPoliza);
                    psPol.setString(3, fecPol);
                    psPol.setString(4, fecPol);
                    psPol.setString(5, fecPol);
                    psPol.setDouble(6, montoTotal);
                    psPol.executeUpdate();
                    psPol.close();

                    // 3. INSERT tdpol (Partidas)
                    String sqlDpol = "INSERT INTO tdpol (CIA, TPOL, NPOL, SEC, CCTA, CONCEP, IMPORTE, CARAB) VALUES (?, 'I', ?, ?, ?, 'DEPOSITO BANCARIO', ?, ?)";
                    PreparedStatement psDpol = con.prepareStatement(sqlDpol);

                    // Cargo (+)
                    psDpol.setString(1, cia);
                    psDpol.setInt(2, numPoliza);
                    psDpol.setInt(3, 1);
                    psDpol.setString(4, ctaCargo);
                    psDpol.setDouble(5, montoTotal);
                    psDpol.setString(6, "C");
                    psDpol.executeUpdate();

                    // Abono (-)
                    psDpol.setString(1, cia);
                    psDpol.setInt(2, numPoliza);
                    psDpol.setInt(3, 2);
                    psDpol.setString(4, "11100100");
                    psDpol.setDouble(5, montoTotal);
                    psDpol.setString(6, "A");
                    psDpol.executeUpdate();
                    psDpol.close();

                    // 4. Actualizar tespalu (SPAG = 'D')
                    String sqlUpdPal = "UPDATE tespalu SET SPAG = 'D' WHERE CIA = ? AND MAT = ? AND FPAG = ?";
                    PreparedStatement psUpdPal = con.prepareStatement(sqlUpdPal);

                    for (int r = 0; r < modAgrupados.getRowCount(); r++) {
                        String mat = modAgrupados.getValueAt(r, 10).toString();
                        String fpag = modAgrupados.getValueAt(r, 6).toString();
                        psUpdPal.setString(1, cia);
                        psUpdPal.setString(2, mat);
                        psUpdPal.setString(3, fpag);
                        psUpdPal.addBatch();
                    }
                    psUpdPal.executeBatch();
                    psUpdPal.close();

                    // 5. Incrementar tgparam.NPOL
                    PreparedStatement psUpdParam = con.prepareStatement("UPDATE tgparam SET NPOL = NPOL + 1 WHERE CIAACT = ?");
                    psUpdParam.setString(1, cia);
                    psUpdParam.executeUpdate();
                    psUpdParam.close();

                    con.commit();
                    db.Cerrar();

                    JOptionPane.showMessageDialog(this, "Depósito registrado y contabilizado con éxito en la Póliza " + numPoliza + ".", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    modAgrupados.setRowCount(0);
                    txtMontoReg1.setText("0.00");
                    txtMontoReg2.setText("0.00");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al registrar depósito: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        tblRegistroDeposito = new javax.swing.JTable();
        btnAddRDeposito = new javax.swing.JButton();
        btnEditRDeposito = new javax.swing.JButton();
        btnDeleteRDeposito = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        tblRegistroDeposito.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tblRegistroDeposito);

        btnAddRDeposito.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAddRDeposito.setForeground(new java.awt.Color(26, 61, 99));
        btnAddRDeposito.setIcon(new javax.swing.ImageIcon(getClass().getResource("/add.png"))); // NOI18N
        btnAddRDeposito.setText("Añadir");
        btnAddRDeposito.addActionListener(this::btnAddRDepositoActionPerformed);

        btnEditRDeposito.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEditRDeposito.setForeground(new java.awt.Color(26, 61, 99));
        btnEditRDeposito.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edit.png"))); // NOI18N
        btnEditRDeposito.setText("Editar");
        btnEditRDeposito.setMaximumSize(new java.awt.Dimension(93, 31));
        btnEditRDeposito.setMinimumSize(new java.awt.Dimension(93, 31));
        btnEditRDeposito.addActionListener(this::btnEditRDepositoActionPerformed);

        btnDeleteRDeposito.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDeleteRDeposito.setForeground(new java.awt.Color(26, 61, 99));
        btnDeleteRDeposito.setIcon(new javax.swing.ImageIcon(getClass().getResource("/delete.png"))); // NOI18N
        btnDeleteRDeposito.setText("Eliminar");
        btnDeleteRDeposito.addActionListener(this::btnDeleteRDepositoActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAddRDeposito)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditRDeposito, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteRDeposito)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddRDeposito, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditRDeposito, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteRDeposito, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void btnAddRDepositoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddRDepositoActionPerformed
    }//GEN-LAST:event_btnAddRDepositoActionPerformed

    private void btnEditRDepositoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditRDepositoActionPerformed
 
    }//GEN-LAST:event_btnEditRDepositoActionPerformed

    private void btnDeleteRDepositoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteRDepositoActionPerformed
 
    }//GEN-LAST:event_btnDeleteRDepositoActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddRDeposito;
    private javax.swing.JButton btnDeleteRDeposito;
    private javax.swing.JButton btnEditRDeposito;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblRegistroDeposito;
    // End of variables declaration//GEN-END:variables
}
