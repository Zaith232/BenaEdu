/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.benaedu.views;
import com.mycompany.benaedu.db.ConDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author b17za
 */
public class FEL_Global extends javax.swing.JPanel {
private JTable tblRecibos;
    private DefaultTableModel modRecibos;
    private JTextField txtTotItems, txtTotDinero;
    /**
     * Creates new form FEL_Global
     */
    public FEL_Global() {
        initComponents();
        construirInterfazFELGlobal();
    }
private void construirInterfazFELGlobal() {
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

        // Carga de Catálogos de la BD
        Object[][] dCia       = cargarDatosMultiple.apply("SELECT CIA, NCIA FROM tmcias ORDER BY CIA", 2);
        Object[][] dCC        = cargarDatosMultiple.apply("SELECT CVE, DES1 FROM tgcc WHERE CVE IN ('12100','12200','12300','12400') ORDER BY CVE", 2);
        Object[][] dMoneda    = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'TMON' ORDER BY CVE", 2);
        Object[][] dMetPago   = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'CSMP' ORDER BY CVE", 2);
        Object[][] dSerieFact = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'FACT' AND CVE IN ('FE','VC') ORDER BY CVE", 2);
        Object[][] dPeriod    = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'CSPE' ORDER BY CVE", 2);

        // ==========================================
        // 1. DATOS DE SELECCIÓN
        // ==========================================
        JPanel pnlSeleccion = new JPanel(null);
        pnlSeleccion.setBorder(BorderFactory.createTitledBorder("Datos de selección"));
        pnlSeleccion.setBounds(10, 10, 930, 115);

        // Compañía
        pnlSeleccion.add(new JLabel("Compañía")).setBounds(15, 20, 70, 25);
        JTextField txtCia = new JTextField("12"); txtCia.setBounds(85, 20, 40, 25);
        JButton btnCia = new JButton("▼"); btnCia.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnCia.setMargin(new java.awt.Insets(0,0,0,0)); btnCia.setBounds(125, 20, 20, 25);
        buscador.configurar(txtCia, null, btnCia, dCia, new String[]{"Clave", "Nombre Compañía"}, new int[]{60, 220});
        pnlSeleccion.add(txtCia); pnlSeleccion.add(btnCia);

        // C. Costos
        pnlSeleccion.add(new JLabel("C. Costos")).setBounds(165, 20, 70, 25);
        JTextField txtCC = new JTextField("12100"); txtCC.setBounds(235, 20, 50, 25);
        JButton btnCC = new JButton("▼"); btnCC.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnCC.setMargin(new java.awt.Insets(0,0,0,0)); btnCC.setBounds(285, 20, 20, 25);
        buscador.configurar(txtCC, null, btnCC, dCC, new String[]{"Clave", "Centro Costo"}, new int[]{60, 220});
        pnlSeleccion.add(txtCC); pnlSeleccion.add(btnCC);

        // Moneda
        pnlSeleccion.add(new JLabel("Moneda")).setBounds(330, 20, 60, 25);
        JTextField txtMoneda = new JTextField("MXP"); txtMoneda.setBounds(390, 20, 40, 25);
        JButton btnMoneda = new JButton("▼"); btnMoneda.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnMoneda.setMargin(new java.awt.Insets(0,0,0,0)); btnMoneda.setBounds(430, 20, 20, 25);
        buscador.configurar(txtMoneda, null, btnMoneda, dMoneda, new String[]{"Clave", "Moneda"}, new int[]{60, 150});
        pnlSeleccion.add(txtMoneda); pnlSeleccion.add(btnMoneda);

        // Radio Fecha Recibo / Fecha Pago
        JRadioButton rbFecRecibo = new JRadioButton("Fecha Recibo", true); rbFecRecibo.setBounds(40, 55, 110, 20);
        JRadioButton rbFecPago = new JRadioButton("Fecha Pago"); rbFecPago.setBounds(40, 80, 110, 20);
        ButtonGroup bgFecha = new ButtonGroup(); bgFecha.add(rbFecRecibo); bgFecha.add(rbFecPago);
        pnlSeleccion.add(rbFecRecibo); pnlSeleccion.add(rbFecPago);

        // Fechas
        pnlSeleccion.add(new JLabel("Fecha Inicial")).setBounds(165, 65, 80, 25);
        com.toedter.calendar.JDateChooser txtFecIni = new com.toedter.calendar.JDateChooser();
        txtFecIni.setDateFormatString("dd/MM/yyyy"); txtFecIni.setDate(new java.util.Date());
        txtFecIni.setBounds(245, 65, 110, 25);
        pnlSeleccion.add(txtFecIni);

        pnlSeleccion.add(new JLabel("Fecha Final")).setBounds(375, 65, 80, 25);
        com.toedter.calendar.JDateChooser txtFecFin = new com.toedter.calendar.JDateChooser();
        txtFecFin.setDateFormatString("dd/MM/yyyy"); txtFecFin.setDate(new java.util.Date());
        txtFecFin.setBounds(455, 65, 110, 25);
        pnlSeleccion.add(txtFecFin);

        // Botones Laterales de Selección
        JButton btnSelAdicional = new JButton("Selección Adicional");
        btnSelAdicional.setBounds(680, 20, 180, 30);
        pnlSeleccion.add(btnSelAdicional);

        JButton btnFiltra = new JButton("Filtra Información");
        btnFiltra.setBounds(680, 65, 180, 30);
        pnlSeleccion.add(btnFiltra);

        this.add(pnlSeleccion);

        // ==========================================
        // 2. INFORMACIÓN DE FACTURA
        // ==========================================
        JPanel pnlFactura = new JPanel(null);
        pnlFactura.setBorder(BorderFactory.createTitledBorder("Información de Factura"));
        pnlFactura.setBounds(10, 130, 930, 85);

        pnlFactura.add(new JLabel("Fecha Factura")).setBounds(15, 20, 90, 25);
        com.toedter.calendar.JDateChooser txtFecFactura = new com.toedter.calendar.JDateChooser();
        txtFecFactura.setDateFormatString("dd/MM/yyyy"); txtFecFactura.setDate(new java.util.Date());
        txtFecFactura.setBounds(105, 20, 110, 25);
        pnlFactura.add(txtFecFactura);

        pnlFactura.add(new JLabel("Número Factura")).setBounds(225, 20, 100, 25);
        JTextField txtNumFactura = new JTextField(); txtNumFactura.setBounds(325, 20, 70, 25);
        
        // Carga de Siguiente Folio
        try {
            ConDB db = new ConDB();
            Connection con = db.Conectar();
            if (con != null) {
                ResultSet rsFol = con.prepareStatement("SELECT FOLINI FROM tgfolfis WHERE CIA = '12' LIMIT 1").executeQuery();
                if (rsFol.next()) txtNumFactura.setText(rsFol.getString("FOLINI"));
                rsFol.close(); db.Cerrar();
            }
        } catch (Exception ex) {}

        JTextField txtSerie = new JTextField("FE"); txtSerie.setBounds(400, 20, 35, 25);
        JButton btnSerie = new JButton("▼"); btnSerie.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnSerie.setMargin(new java.awt.Insets(0,0,0,0)); btnSerie.setBounds(435, 20, 20, 25);
        buscador.configurar(txtSerie, null, btnSerie, dSerieFact, new String[]{"Clave", "Descripción"}, new int[]{50, 180});

        pnlFactura.add(txtNumFactura); pnlFactura.add(txtSerie); pnlFactura.add(btnSerie);

        // Método de Pago (CSMP)
        pnlFactura.add(new JLabel("Método Pago")).setBounds(15, 50, 80, 25);
        JTextField txtMetodo = new JTextField("PUE"); txtMetodo.setBounds(105, 50, 40, 25);
        JButton btnMetodo = new JButton("▼"); btnMetodo.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnMetodo.setMargin(new java.awt.Insets(0,0,0,0)); btnMetodo.setBounds(145, 50, 20, 25);
        JTextField txtMetodoDesc = new JTextField("PAGO EN UNA SOLA EXHIBICIÓN"); txtMetodoDesc.setBounds(170, 50, 230, 25); txtMetodoDesc.setEditable(false); txtMetodoDesc.setBackground(new java.awt.Color(240,240,240));
        buscador.configurar(txtMetodo, txtMetodoDesc, btnMetodo, dMetPago, new String[]{"Clave", "Descripción"}, new int[]{60, 250});
        
        pnlFactura.add(txtMetodo); pnlFactura.add(btnMetodo); pnlFactura.add(txtMetodoDesc);

        // Sub-caja Total a facturar
        JPanel pnlTotFacturar = new JPanel(null);
        pnlTotFacturar.setBorder(BorderFactory.createTitledBorder("Total a facturar"));
        pnlTotFacturar.setBounds(550, 15, 360, 60);
        
        txtTotItems = new JTextField("0");
        txtTotItems.setBounds(15, 20, 50, 25);
        txtTotItems.setHorizontalAlignment(JTextField.RIGHT);
        txtTotItems.setEditable(false);
        
        txtTotDinero = new JTextField("0.00");
        txtTotDinero.setBounds(75, 20, 270, 25);
        txtTotDinero.setHorizontalAlignment(JTextField.RIGHT);
        txtTotDinero.setEditable(false);
        
        pnlTotFacturar.add(txtTotItems); pnlTotFacturar.add(txtTotDinero);
        pnlFactura.add(pnlTotFacturar);

        this.add(pnlFactura);

        // ==========================================
        // 3. INFORMACIÓN GLOBAL
        // ==========================================
        JPanel pnlGlobal = new JPanel(null);
        pnlGlobal.setBorder(BorderFactory.createTitledBorder("Información Global"));
        pnlGlobal.setBounds(10, 220, 930, 55);

        pnlGlobal.add(new JLabel("Periodicidad")).setBounds(15, 20, 80, 25);
        JTextField txtPeriodo = new JTextField("04"); txtPeriodo.setBounds(95, 20, 40, 25);
        JButton btnPeriodo = new JButton("▼"); btnPeriodo.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnPeriodo.setMargin(new java.awt.Insets(0,0,0,0)); btnPeriodo.setBounds(135, 20, 20, 25);
        buscador.configurar(txtPeriodo, null, btnPeriodo, dPeriod, new String[]{"Clave", "Descripción"}, new int[]{60, 150});
        pnlGlobal.add(txtPeriodo); pnlGlobal.add(btnPeriodo);

        pnlGlobal.add(new JLabel("Meses")).setBounds(410, 20, 50, 25);
        JComboBox<String> cmbMeses = new JComboBox<>(new String[]{"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"});
        cmbMeses.setSelectedItem("08"); cmbMeses.setBounds(460, 20, 50, 25);
        JLabel lblNombreMes = new JLabel("AGOSTO"); lblNombreMes.setBounds(520, 20, 100, 25);
        lblNombreMes.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));

        String[] nombresMeses = {"ENERO", "FEBRERO", "MARZO", "ABRIL", "MAYO", "JUNIO", "JULIO", "AGOSTO", "SEPTIEMBRE", "OCTUBRE", "NOVIEMBRE", "DICIEMBRE"};
        cmbMeses.addActionListener(e -> {
            int idx = cmbMeses.getSelectedIndex();
            if(idx >= 0 && idx < 12) lblNombreMes.setText(nombresMeses[idx]);
        });

        pnlGlobal.add(cmbMeses); pnlGlobal.add(lblNombreMes);

        pnlGlobal.add(new JLabel("Año")).setBounds(760, 20, 30, 25);
        JTextField txtAnioGlobal = new JTextField("2026"); txtAnioGlobal.setBounds(795, 20, 60, 25);
        pnlGlobal.add(txtAnioGlobal);

        this.add(pnlGlobal);

        // ==========================================
        // 4. TABLA RECIBOS SIN FACTURAR
        // ==========================================
        modRecibos = new DefaultTableModel(
            new Object[][]{}, 
            new String[]{" ", "Cia", "C. Ctos", "C. Esc", "Matrícula", "Nombre", "Num Recibo", "Tipo", "Fec Recibo", "Cod", "Concepto", "Importe"}
        ) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class;
                return super.getColumnClass(columnIndex);
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };

        tblRecibos = new JTable(modRecibos);
        tblRecibos.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        tblRecibos.getColumnModel().getColumn(0).setPreferredWidth(30);  
        tblRecibos.getColumnModel().getColumn(1).setPreferredWidth(40);  
        tblRecibos.getColumnModel().getColumn(2).setPreferredWidth(60);  
        tblRecibos.getColumnModel().getColumn(3).setPreferredWidth(55);  
        tblRecibos.getColumnModel().getColumn(4).setPreferredWidth(80);  
        tblRecibos.getColumnModel().getColumn(5).setPreferredWidth(210); 
        tblRecibos.getColumnModel().getColumn(6).setPreferredWidth(80);  
        tblRecibos.getColumnModel().getColumn(7).setPreferredWidth(45);  
        tblRecibos.getColumnModel().getColumn(8).setPreferredWidth(80);  
        tblRecibos.getColumnModel().getColumn(9).setPreferredWidth(45);  
        tblRecibos.getColumnModel().getColumn(10).setPreferredWidth(180); 
        tblRecibos.getColumnModel().getColumn(11).setPreferredWidth(90); 

        JScrollPane scrollRecibos = new JScrollPane(tblRecibos);
        scrollRecibos.setBounds(10, 280, 930, 240);
        scrollRecibos.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(java.awt.Color.GRAY), "Recibos Sin Facturar", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
        
        this.add(scrollRecibos);

        // ==========================================
        // 5. SECCIÓN INFERIOR (BOTONES FEL Y SALIR)
        // ==========================================
        JButton btnFacturarFEL = new JButton("FEL Facturar en Línea");
        btnFacturarFEL.setBounds(210, 528, 175, 45);
        btnFacturarFEL.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btnFacturarFEL.setForeground(new java.awt.Color(26, 61, 99));

        JButton btnSalir = new JButton("Salir");
        btnSalir.setBounds(410, 528, 100, 45);

        this.add(btnFacturarFEL);
        this.add(btnSalir);

        // ==========================================
        // 6. EVENTOS Y LÓGICA
        // ==========================================

        Runnable recalcularTotales = () -> {
            int itemsCount = 0;
            double sumaMonto = 0.0;
            java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");

            for (int r = 0; r < modRecibos.getRowCount(); r++) {
                Boolean marcado = (Boolean) modRecibos.getValueAt(r, 0);
                if (marcado != null && marcado) {
                    itemsCount++;
                    double imp = 0.0;
                    try { imp = Double.parseDouble(modRecibos.getValueAt(r, 11).toString().replace(",", "")); } catch(Exception ex) {}
                    sumaMonto += imp;
                }
            }

            txtTotItems.setText(String.valueOf(itemsCount));
            txtTotDinero.setText(df.format(sumaMonto));
        };

        modRecibos.addTableModelListener(e -> {
            if (e.getColumn() == 0 || e.getColumn() == -1) {
                recalcularTotales.run();
            }
        });

        // Evento Filtrar Recibos sobre tesralu + tesalum
        btnFiltra.addActionListener(e -> {
            modRecibos.setRowCount(0);

            String cia = txtCia.getText().trim();
            String cc = txtCC.getText().trim();
            
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            String fIni = txtFecIni.getDate() != null ? sdf.format(txtFecIni.getDate()) : "";
            String fFin = txtFecFin.getDate() != null ? sdf.format(txtFecFin.getDate()) : "";
            String campoFecha = rbFecRecibo.isSelected() ? "r.FREC" : "r.FPAG";

            try {
                ConDB db = new ConDB();
                Connection con = db.Conectar();
                if (con != null) {
                    StringBuilder sql = new StringBuilder(
                        "SELECT r.CIA, r.CC, r.CESC, r.MAT, COALESCE(a.NOMCOM, r.NOMALU) AS NOMBRE, " +
                        "r.NREC, r.TREC, r.FREC, r.NCPTO, r.DCPTO, r.IPAGMN " +
                        "FROM tesralu r " +
                        "LEFT JOIN tesalum a ON r.MAT = a.MAT " +
                        "WHERE (r.FFAC IS NULL OR r.FFAC = '') AND r.IPAGMN > 0 "
                    );

                    if (!cia.isEmpty()) sql.append(" AND r.CIA = ?");
                    if (!cc.isEmpty()) sql.append(" AND r.CC = ?");
                    if (!fIni.isEmpty() && !fFin.isEmpty()) sql.append(" AND ").append(campoFecha).append(" BETWEEN ? AND ?");

                    sql.append(" ORDER BY ").append(campoFecha).append(" DESC, r.NREC DESC");

                    PreparedStatement ps = con.prepareStatement(sql.toString());
                    int p = 1;
                    if (!cia.isEmpty()) ps.setString(p++, cia);
                    if (!cc.isEmpty()) ps.setString(p++, cc);
                    if (!fIni.isEmpty() && !fFin.isEmpty()) {
                        ps.setString(p++, fIni);
                        ps.setString(p++, fFin);
                    }

                    ResultSet rs = ps.executeQuery();
                    java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");

                    while (rs.next()) {
                        Object[] fila = new Object[12];
                        fila[0] = false;
                        fila[1] = rs.getString("CIA");
                        fila[2] = rs.getString("CC");
                        fila[3] = rs.getString("CESC");
                        fila[4] = rs.getString("MAT");
                        fila[5] = rs.getString("NOMBRE");
                        fila[6] = rs.getString("NREC");
                        fila[7] = rs.getString("TREC");
                        fila[8] = rs.getString("FREC");
                        fila[9] = rs.getString("NCPTO");
                        fila[10] = rs.getString("DCPTO");
                        fila[11] = df.format(rs.getDouble("IPAGMN"));

                        modRecibos.addRow(fila);
                    }

                    rs.close(); ps.close(); db.Cerrar();
                    recalcularTotales.run();

                    if (modRecibos.getRowCount() == 0) {
                        JOptionPane.showMessageDialog(this, "No se encontraron recibos pendientes de facturar globalmente.", "Información", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al consultar recibos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Evento FEL Global (Facturar en Línea)
        btnFacturarFEL.addActionListener(e -> {
            int itemsCount = Integer.parseInt(txtTotItems.getText().trim());
            if (itemsCount == 0) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar al menos un recibo para generar la Factura Global.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String numFactura = txtNumFactura.getText().trim();
            if (numFactura.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Especifique el número de factura.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, 
                "¿Desea generar la Factura Global FEL para los " + itemsCount + " recibos seleccionados por $" + txtTotDinero.getText() + "?", 
                "Confirmar Facturación Global", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                String fecFactura = txtFecFactura.getDate() != null ? sdf.format(txtFecFactura.getDate()) : "";

                try {
                    ConDB db = new ConDB();
                    Connection con = db.Conectar();
                    if (con != null) {
                        con.setAutoCommit(false); // Transacción

                        String cia = txtCia.getText().trim();
                        String serie = txtSerie.getText().trim();
                        double totalFacturado = Double.parseDouble(txtTotDinero.getText().replace(",", ""));

                        // 1. Insertar encabezado en tgfcte
                        String sqlFact = "INSERT INTO tgfcte (CIA, CC, CTE, CFAC, TFAC, NFAC, FFAC, MIMP, MPAG, CMON, FHEMI, USER) " +
                                         "VALUES (?, ?, 'PUBLICO GENERAL', ?, ?, ?, ?, ?, ?, 'MXP', NOW(), 'Admin')";
                        PreparedStatement psFact = con.prepareStatement(sqlFact);
                        psFact.setString(1, cia);
                        psFact.setString(2, txtCC.getText().trim());
                        psFact.setString(3, serie + "-" + numFactura);
                        psFact.setString(4, serie);
                        psFact.setString(5, numFactura);
                        psFact.setString(6, fecFactura);
                        psFact.setDouble(7, totalFacturado);
                        psFact.setDouble(8, totalFacturado);
                        psFact.executeUpdate();
                        psFact.close();

                        // 2. Actualizar recibos en tesralu
                        String sqlUpdRec = "UPDATE tesralu SET FFAC = ?, NFAC = ?, TFAC = ? WHERE CIA = ? AND NREC = ?";
                        PreparedStatement psUpdRec = con.prepareStatement(sqlUpdRec);

                        for (int r = 0; r < modRecibos.getRowCount(); r++) {
                            Boolean marcado = (Boolean) modRecibos.getValueAt(r, 0);
                            if (marcado != null && marcado) {
                                String nrec = modRecibos.getValueAt(r, 6).toString();
                                psUpdRec.setString(1, fecFactura);
                                psUpdRec.setString(2, numFactura);
                                psUpdRec.setString(3, serie);
                                psUpdRec.setString(4, cia);
                                psUpdRec.setString(5, nrec);
                                psUpdRec.addBatch();
                            }
                        }

                        psUpdRec.executeBatch();
                        psUpdRec.close();

                        // 3. Incrementar Folio Fiscal en tgfolfis
                        try {
                            int sigFolio = Integer.parseInt(numFactura) + 1;
                            PreparedStatement psFolUpd = con.prepareStatement("UPDATE tgfolfis SET FOLINI = ? WHERE CIA = ?");
                            psFolUpd.setInt(1, sigFolio);
                            psFolUpd.setString(2, cia);
                            psFolUpd.executeUpdate();
                            psFolUpd.close();
                        } catch(Exception ex) {}

                        con.commit();
                        db.Cerrar();

                        JOptionPane.showMessageDialog(this, "Factura Global FEL " + serie + "-" + numFactura + " generada exitosamente.", "Éxito FEL", JOptionPane.INFORMATION_MESSAGE);
                        btnFiltra.doClick(); // Refresca lista
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error durante la facturación global: " + ex.getMessage(), "Error FEL", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Evento Salir
        btnSalir.addActionListener(e -> {
            this.removeAll();
            this.revalidate();
            this.repaint();
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
