/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.benaedu.views;

import com.mycompany.benaedu.db.ConDB;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author b17za
 */
public class FEL extends javax.swing.JPanel {

    private JTable tblRecibos;
    private DefaultTableModel modRecibos;
    private JTextField txtTotItems, txtTotDinero, txtSinDatItems, txtSinDatMonto;

    /**
     * Creates new form FEL
     */
    public FEL() {
        initComponents();
        iniciarComponentesPropios();
    }

    private void iniciarComponentesPropios() {
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
                        @Override
                        public boolean isCellEditable(int r, int c) {
                            return false;
                        }
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

                    int widthTotal = 0;
                    for (int w : anchos) {
                        widthTotal += w;
                    }
                    JScrollPane scroll = new JScrollPane(tabla);
                    scroll.setPreferredSize(new java.awt.Dimension(widthTotal + 20, 150));
                    popup.add(scroll);

                    String texto = txtClave.getText().trim();
                    if (!texto.isEmpty()) {
                        sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + texto));
                    }
                    popup.show(txtClave, 0, txtClave.getHeight());
                    txtClave.requestFocus();
                };

                boton.addActionListener(e -> {
                    txtClave.setText("");
                    mostrarPopup.run();
                });
                txtClave.addKeyListener(new java.awt.event.KeyAdapter() {
                    @Override
                    public void keyReleased(java.awt.event.KeyEvent e) {
                        int c = e.getKeyCode();
                        if (c == 27 || c == 10 || c == 38 || c == 40 || c == 37 || c == 39 || c == 9) {
                            return;
                        }
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
                    while (rs.next()) {
                        Object[] row = new Object[numCols];
                        for (int i = 0; i < numCols; i++) {
                            row[i] = rs.getString(i + 1);
                        }
                        lista.add(row);
                    }
                    rs.close();
                    ps.close();
                    db.Cerrar();
                }
            } catch (Exception e) {
            }
            return lista.toArray(new Object[0][0]);
        };

        Object[][] dMatricula = cargarDatosMultiple.apply("SELECT MAT, APATE, AMATE, NOMA FROM tesalum ORDER BY MAT", 4);

        // ==========================================
        // 1. DATOS DE SELECCIÓN
        // ==========================================
        JPanel pnlSeleccion = new JPanel(null);
        pnlSeleccion.setBorder(BorderFactory.createTitledBorder("Datos de selección"));
        pnlSeleccion.setBounds(10, 10, 930, 90);

        pnlSeleccion.add(new JLabel("Compañía")).setBounds(15, 20, 70, 25);
        JComboBox<String> cmbCia = new JComboBox<>();
        cmbCia.setBounds(85, 20, 60, 25);
        pnlSeleccion.add(cmbCia);

        pnlSeleccion.add(new JLabel("C. Costos")).setBounds(160, 20, 70, 25);
        JComboBox<String> cmbCC = new JComboBox<>();
        cmbCC.setBounds(230, 20, 80, 25);
        pnlSeleccion.add(cmbCC);

        try {
            ConDB db = new ConDB();
            Connection con = db.Conectar();
            if (con != null) {
                ResultSet rsCia = con.prepareStatement("SELECT CIA FROM tmcias ORDER BY CIA").executeQuery();
                while (rsCia.next()) {
                    cmbCia.addItem(rsCia.getString("CIA"));
                }
                rsCia.close();

                ResultSet rsCC = con.prepareStatement("SELECT CVE FROM tgcc WHERE CVE IN ('12100','12200','12300','12400') ORDER BY CVE").executeQuery();
                while (rsCC.next()) {
                    cmbCC.addItem(rsCC.getString("CVE"));
                }
                rsCC.close();
                db.Cerrar();
            }
        } catch (Exception ex) {
            cmbCia.addItem("12");
            cmbCC.addItem("12100");
        }

        pnlSeleccion.add(new JLabel("Moneda")).setBounds(330, 20, 60, 25);
        JComboBox<String> cmbMoneda = new JComboBox<>(new String[]{"MXP"});
        cmbMoneda.setBounds(390, 20, 80, 25);
        pnlSeleccion.add(cmbMoneda);

        pnlSeleccion.add(new JLabel("Matrícula")).setBounds(15, 55, 70, 25);
        JTextField txtMatricula = new JTextField();
        txtMatricula.setBounds(85, 55, 90, 25);
        JButton btnMatriculaHist = new JButton("▼");
        btnMatriculaHist.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
        btnMatriculaHist.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnMatriculaHist.setBounds(175, 55, 20, 25);
        buscador.configurar(txtMatricula, null, btnMatriculaHist, dMatricula, new String[]{"Matrícula", "A. Paterno", "A. Materno", "Nombre"}, new int[]{80, 120, 120, 150});
        pnlSeleccion.add(txtMatricula);
        pnlSeleccion.add(btnMatriculaHist);

        pnlSeleccion.add(new JLabel("Fecha Inicial")).setBounds(210, 55, 80, 25);
        com.toedter.calendar.JDateChooser txtFecIni = new com.toedter.calendar.JDateChooser();
        txtFecIni.setDateFormatString("dd/MM/yyyy");
        txtFecIni.setDate(new java.util.Date());
        txtFecIni.setBounds(290, 55, 110, 25);
        pnlSeleccion.add(txtFecIni);

        pnlSeleccion.add(new JLabel("Fecha Final")).setBounds(410, 55, 80, 25);
        com.toedter.calendar.JDateChooser txtFecFin = new com.toedter.calendar.JDateChooser();
        txtFecFin.setDateFormatString("dd/MM/yyyy");
        txtFecFin.setDate(new java.util.Date());
        txtFecFin.setBounds(490, 55, 110, 25);
        pnlSeleccion.add(txtFecFin);

        JButton btnSelAdicional = new JButton("Selección Adicional");
        btnSelAdicional.setBounds(680, 15, 180, 30);
        pnlSeleccion.add(btnSelAdicional);

        JButton btnFiltra = new JButton("Filtra Información");
        btnFiltra.setBounds(680, 50, 180, 30);
        pnlSeleccion.add(btnFiltra);

        this.add(pnlSeleccion);

        // ==========================================
        // 2. INFORMACIÓN DE FACTURA
        // ==========================================
        JPanel pnlFactura = new JPanel(null);
        pnlFactura.setBorder(BorderFactory.createTitledBorder("Información de Factura"));
        pnlFactura.setBounds(10, 105, 930, 85);

        pnlFactura.add(new JLabel("Fecha Factura")).setBounds(15, 20, 90, 25);
        com.toedter.calendar.JDateChooser txtFecFactura = new com.toedter.calendar.JDateChooser();
        txtFecFactura.setDateFormatString("dd/MM/yyyy");
        txtFecFactura.setDate(new java.util.Date());
        txtFecFactura.setBounds(105, 20, 110, 25);
        pnlFactura.add(txtFecFactura);

        pnlFactura.add(new JLabel("Número Factura")).setBounds(225, 20, 100, 25);
        JTextField txtNumFactura = new JTextField();
        txtNumFactura.setBounds(325, 20, 80, 25);

        // Obtener siguiente folio fiscal automático desde MAX(NFAC)+1 de tesralu
        try {
            ConDB db = new ConDB();
            Connection con = db.Conectar();
            if (con != null) {
                PreparedStatement psFol = con.prepareStatement("SELECT COALESCE(MAX(CAST(NFAC AS UNSIGNED)), 0) + 1 AS FOLIO FROM tesralu");
                ResultSet rsFol = psFol.executeQuery();
                if (rsFol.next()) {
                    txtNumFactura.setText(rsFol.getString("FOLIO"));
                }
                rsFol.close();
                psFol.close();
                db.Cerrar();
            }
        } catch (Exception ex) {
        }

        JComboBox<String> cmbSerieFactura = new JComboBox<>(new String[]{"FE"});
        cmbSerieFactura.setBounds(410, 20, 60, 25);
        pnlFactura.add(txtNumFactura);
        pnlFactura.add(cmbSerieFactura);

        pnlFactura.add(new JLabel("Método Pago")).setBounds(15, 50, 80, 25);
        JComboBox<String> cmbMetodo = new JComboBox<>(new String[]{"PUE", "PPD"});
        cmbMetodo.setBounds(105, 50, 80, 25);
        JLabel lblMetodoDesc = new JLabel("PAGO EN UNA SOLA EXHIBICIÓN");
        lblMetodoDesc.setBounds(195, 50, 250, 25);
        pnlFactura.add(cmbMetodo);
        pnlFactura.add(lblMetodoDesc);

        // Sub-caja Todos
        JPanel pnlTodos = new JPanel(null);
        pnlTodos.setBorder(BorderFactory.createTitledBorder("Todos"));
        pnlTodos.setBounds(480, 15, 60, 60);
        JCheckBox chkTodos = new JCheckBox();
        chkTodos.setBounds(18, 20, 20, 20);
        pnlTodos.add(chkTodos);
        pnlFactura.add(pnlTodos);

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

        pnlTotFacturar.add(txtTotItems);
        pnlTotFacturar.add(txtTotDinero);
        pnlFactura.add(pnlTotFacturar);

        this.add(pnlFactura);

        // ==========================================
        // 3. TABLA RECIBOS SIN FACTURAR
        // ==========================================
        modRecibos = new DefaultTableModel(
                new Object[][]{},
                new String[]{" ", "Cia", "C. Ctos", "C. Esc", "Matrícula", "Nombre", "Num Recibo", "Tipo", "Fec Recibo", "Moneda", "Importe MN", "RFC"}
        ) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class;
                }
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
        tblRecibos.getColumnModel().getColumn(5).setPreferredWidth(230);
        tblRecibos.getColumnModel().getColumn(6).setPreferredWidth(80);
        tblRecibos.getColumnModel().getColumn(7).setPreferredWidth(45);
        tblRecibos.getColumnModel().getColumn(8).setPreferredWidth(80);
        tblRecibos.getColumnModel().getColumn(9).setPreferredWidth(55);
        tblRecibos.getColumnModel().getColumn(10).setPreferredWidth(90);
        tblRecibos.getColumnModel().getColumn(11).setPreferredWidth(110);

        JScrollPane scrollRecibos = new JScrollPane(tblRecibos);
        scrollRecibos.setBounds(10, 200, 930, 310);
        scrollRecibos.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(java.awt.Color.GRAY), "Recibos Sin Facturar", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));

        this.add(scrollRecibos);

        // ==========================================
        // 4. SECCIÓN INFERIOR
        // ==========================================
        JButton btnFacturarFEL = new JButton("FEL (Facturar en Línea)");
        btnFacturarFEL.setBounds(280, 525, 170, 45);
        btnFacturarFEL.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        btnFacturarFEL.setForeground(new java.awt.Color(26, 61, 99));

        JButton btnSalir = new JButton("Salir");
        btnSalir.setBounds(460, 525, 100, 45);

        JPanel pnlSinDatos = new JPanel(null);
        pnlSinDatos.setBorder(BorderFactory.createTitledBorder("Recibos sin Datos Fiscales"));
        pnlSinDatos.setBounds(680, 520, 260, 55);

        txtSinDatItems = new JTextField("0");
        txtSinDatItems.setBounds(10, 20, 50, 25);
        txtSinDatItems.setHorizontalAlignment(JTextField.RIGHT);
        txtSinDatItems.setEditable(false);

        txtSinDatMonto = new JTextField("0.00");
        txtSinDatMonto.setBounds(70, 20, 180, 25);
        txtSinDatMonto.setHorizontalAlignment(JTextField.RIGHT);
        txtSinDatMonto.setEditable(false);

        pnlSinDatos.add(txtSinDatItems);
        pnlSinDatos.add(txtSinDatMonto);

        this.add(btnFacturarFEL);
        this.add(btnSalir);
        this.add(pnlSinDatos);

        // ==========================================
        // 5. EVENTOS Y LÓGICA DE NEGOCIO
        // ==========================================
        Runnable recalcularTotales = () -> {
            int itemsCount = 0;
            double sumaMonto = 0.0;
            int sinDatosCount = 0;
            double sinDatosMonto = 0.0;
            java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");

            for (int r = 0; r < modRecibos.getRowCount(); r++) {
                Boolean marcado = (Boolean) modRecibos.getValueAt(r, 0);
                if (marcado != null && marcado) {
                    itemsCount++;
                    double imp = 0.0;
                    try {
                        imp = Double.parseDouble(modRecibos.getValueAt(r, 10).toString().replace(",", ""));
                    } catch (Exception ex) {
                    }
                    sumaMonto += imp;

                    String rfc = modRecibos.getValueAt(r, 11) != null ? modRecibos.getValueAt(r, 11).toString() : "";
                    if (rfc.trim().isEmpty() || "XAXX010101000".equalsIgnoreCase(rfc.trim())) {
                        sinDatosCount++;
                        sinDatosMonto += imp;
                    }
                }
            }

            txtTotItems.setText(String.valueOf(itemsCount));
            txtTotDinero.setText(df.format(sumaMonto));
            txtSinDatItems.setText(String.valueOf(sinDatosCount));
            txtSinDatMonto.setText(df.format(sinDatosMonto));
        };

        modRecibos.addTableModelListener(e -> {
            if (e.getColumn() == 0 || e.getColumn() == -1) {
                recalcularTotales.run();
            }
        });

        chkTodos.addActionListener(e -> {
            boolean estado = chkTodos.isSelected();
            for (int i = 0; i < modRecibos.getRowCount(); i++) {
                modRecibos.setValueAt(estado, i, 0);
            }
            recalcularTotales.run();
        });

        // Evento Filtrar Recibos
        btnFiltra.addActionListener(e -> {
            modRecibos.setRowCount(0);

            String cia = cmbCia.getSelectedItem() != null ? cmbCia.getSelectedItem().toString() : "";
            String cc = cmbCC.getSelectedItem() != null ? cmbCC.getSelectedItem().toString() : "";
            String matricula = txtMatricula.getText().trim();

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            String fIni = txtFecIni.getDate() != null ? sdf.format(txtFecIni.getDate()) : "";
            String fFin = txtFecFin.getDate() != null ? sdf.format(txtFecFin.getDate()) : "";

            try {
                ConDB db = new ConDB();
                Connection con = db.Conectar();
                if (con != null) {
                    StringBuilder sql = new StringBuilder(
                            "SELECT r.CIA, r.CC, r.CESC, r.MAT, r.NOMALU, r.NREC, r.TREC, r.FREC, r.CMON, r.IPAGMN, a.RFC "
                            + "FROM tesralu r "
                            + "LEFT JOIN tesalum a ON r.MAT = a.MAT "
                            + "WHERE (r.FFAC IS NULL OR r.FFAC = '' OR r.FFAC = '0000-00-00') "
                    );

                    if (!cia.isEmpty()) {
                        sql.append(" AND r.CIA = ?");
                    }
                    if (!cc.isEmpty()) {
                        sql.append(" AND r.CC = ?");
                    }
                    if (!matricula.isEmpty()) {
                        sql.append(" AND r.MAT = ?");
                    }
                    if (!fIni.isEmpty() && !fFin.isEmpty()) {
                        sql.append(" AND r.FREC BETWEEN ? AND ?");
                    }

                    sql.append(" ORDER BY r.FREC DESC, r.NREC DESC");

                    PreparedStatement ps = con.prepareStatement(sql.toString());
                    int p = 1;
                    if (!cia.isEmpty()) {
                        ps.setString(p++, cia);
                    }
                    if (!cc.isEmpty()) {
                        ps.setString(p++, cc);
                    }
                    if (!matricula.isEmpty()) {
                        ps.setString(p++, matricula);
                    }
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
                        fila[5] = rs.getString("NOMALU");
                        fila[6] = rs.getString("NREC");
                        fila[7] = rs.getString("TREC");
                        fila[8] = rs.getString("FREC");
                        fila[9] = rs.getString("CMON");
                        fila[10] = df.format(rs.getDouble("IPAGMN"));
                        fila[11] = rs.getString("RFC") != null ? rs.getString("RFC") : "";

                        modRecibos.addRow(fila);
                    }

                    rs.close();
                    ps.close();
                    db.Cerrar();

                    recalcularTotales.run();

                    if (modRecibos.getRowCount() == 0) {
                        JOptionPane.showMessageDialog(this, "No se encontraron recibos pendientes de facturar.", "Información", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al consultar recibos sin facturar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Evento FEL (Genera archivo TXT y actualiza la base de datos tesralu)
        btnFacturarFEL.addActionListener(e -> {
            int itemsCount = Integer.parseInt(txtTotItems.getText().trim());
            if (itemsCount == 0) {
                JOptionPane.showMessageDialog(this, "Debe seleccionar al menos un recibo para facturar.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String numFacturaStr = txtNumFactura.getText().trim();
            if (numFacturaStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Especifique el número de factura inicial.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "¿Desea generar los archivos TXT de Facturación Electrónica (FEL) para los " + itemsCount + " recibos seleccionados por un monto de $" + txtTotDinero.getText() + "?",
                    "Confirmar Facturación", JOptionPane.YES_NO_OPTION);

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            java.text.SimpleDateFormat sdfStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss");
            java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");

            String fecFactura = txtFecFactura.getDate() != null ? sdf.format(txtFecFactura.getDate()) : sdf.format(new java.util.Date());
            String cia = cmbCia.getSelectedItem() != null ? cmbCia.getSelectedItem().toString() : "12";
            String serie = cmbSerieFactura.getSelectedItem() != null ? cmbSerieFactura.getSelectedItem().toString() : "FE";
            String metodoPagoFallback = cmbMetodo.getSelectedItem() != null ? cmbMetodo.getSelectedItem().toString() : "PUE";
            int folio = Integer.parseInt(numFacturaStr);

            String felDir = "C:\\FEL";
            try {
                new File(felDir).mkdirs();
            } catch (Exception ex) {
            }

            try {
                ConDB db = new ConDB();
                Connection con = db.Conectar();

                if (con != null) {
                    // 1. Cargar Parámetros del Emisor (tmcias) y Configuración FEL (tgpram)
                    String rfcEmisor = "VIEG89081886A";
                    String cpEmisor = "75760";
                    String crutaEmisor = "603";
                    String regFisEmisor = "01";

                    PreparedStatement psCia = con.prepareStatement("SELECT RFC, CP, CRUTA, REGFIS FROM tmcias WHERE CIA = ?");
                    psCia.setString(1, cia);
                    ResultSet rsCia = psCia.executeQuery();
                    if (rsCia.next()) {
                        if (rsCia.getString("RFC") != null) {
                            rfcEmisor = rsCia.getString("RFC");
                        }
                        if (rsCia.getString("CP") != null) {
                            cpEmisor = rsCia.getString("CP");
                        }
                        if (rsCia.getString("CRUTA") != null) {
                            crutaEmisor = rsCia.getString("CRUTA");
                        }
                        if (rsCia.getString("REGFIS") != null) {
                            regFisEmisor = rsCia.getString("REGFIS");
                        }
                    }
                    rsCia.close();
                    psCia.close();

                    // Lectura opcional de tgpram para metodos/recargos configurados
                    String cprecVal = "84101700";
                    String metodoPagoPram = metodoPagoFallback;
                    try {
                        PreparedStatement psPram = con.prepareStatement("SELECT CPREC, METPAG FROM tgpram WHERE CIAACT = ?");
                        psPram.setString(1, cia);
                        ResultSet rsPram = psPram.executeQuery();
                        if (rsPram.next()) {
                            if (rsPram.getString("CPREC") != null && !rsPram.getString("CPREC").trim().isEmpty()) {
                                cprecVal = rsPram.getString("CPREC").trim();
                            }
                            if (rsPram.getString("METPAG") != null && !rsPram.getString("METPAG").trim().isEmpty()) {
                                metodoPagoPram = rsPram.getString("METPAG").trim();
                            }
                        }
                        rsPram.close();
                        psPram.close();
                    } catch (Exception ex) {
                    }

                    con.setAutoCommit(false); // Transacción SQL sobre tesralu

                    int foliosUsados = 0;
                    for (int r = 0; r < modRecibos.getRowCount(); r++) {
                        Boolean marcado = (Boolean) modRecibos.getValueAt(r, 0);
                        if (marcado == null || !marcado) {
                            continue;
                        }

                        String nrec = modRecibos.getValueAt(r, 6).toString();
                        String mat = modRecibos.getValueAt(r, 4).toString();
                        String cc = modRecibos.getValueAt(r, 2).toString();
                        String cesc = modRecibos.getValueAt(r, 3).toString();

                        // Datos Fiscales del Receptor (tesalum)
                        String rfcReceptor = "XAXX010101000";
                        String nomReceptor = "PUBLICO GENERAL";
                        String cpReceptor = "75743";
                        String regFisReceptor = "616";
                        String usoCfdi = "D10";
                        String emailReceptor = "";
                        String nomComAlumno = "";
                        String curpAlumno = "";

                        PreparedStatement psAlum = con.prepareStatement("SELECT RFC, NOMF, CPF, REGFIS, USOCFDI, EMAIL, NOMCOM, CURP FROM tesalum WHERE MAT = ?");
                        psAlum.setString(1, mat);
                        ResultSet rsAlum = psAlum.executeQuery();
                        if (rsAlum.next()) {
                            if (rsAlum.getString("RFC") != null && !rsAlum.getString("RFC").trim().isEmpty()) {
                                rfcReceptor = rsAlum.getString("RFC").trim();
                            }
                            if (rsAlum.getString("NOMF") != null && !rsAlum.getString("NOMF").trim().isEmpty()) {
                                nomReceptor = rsAlum.getString("NOMF").trim();
                            }
                            if (rsAlum.getString("CPF") != null && !rsAlum.getString("CPF").trim().isEmpty()) {
                                cpReceptor = rsAlum.getString("CPF").trim();
                            }
                            if (rsAlum.getString("REGFIS") != null && !rsAlum.getString("REGFIS").trim().isEmpty()) {
                                regFisReceptor = rsAlum.getString("REGFIS").trim();
                            }
                            if (rsAlum.getString("USOCFDI") != null && !rsAlum.getString("USOCFDI").trim().isEmpty()) {
                                usoCfdi = rsAlum.getString("USOCFDI").trim();
                            }
                            if (rsAlum.getString("EMAIL") != null) {
                                emailReceptor = rsAlum.getString("EMAIL").trim();
                            }
                            if (rsAlum.getString("NOMCOM") != null) {
                                nomComAlumno = rsAlum.getString("NOMCOM").trim();
                            }
                            if (rsAlum.getString("CURP") != null) {
                                curpAlumno = rsAlum.getString("CURP").trim();
                            }
                        }
                        rsAlum.close();
                        psAlum.close();

                        // Centro de Costos (tgcc): CCT y nivel educativo
                        String nivelEdu = "Primaria";
                        String cct = "21PPR0422W";
                        PreparedStatement psCC = con.prepareStatement("SELECT CCT, SECC, CLS01 FROM tgcc WHERE CVE = ?");
                        psCC.setString(1, cc);
                        ResultSet rsCC = psCC.executeQuery();
                        if (rsCC.next()) {
                            if (rsCC.getString("CCT") != null && !rsCC.getString("CCT").trim().isEmpty()) {
                                cct = rsCC.getString("CCT").trim();
                            }
                            if (rsCC.getString("SECC") != null && !rsCC.getString("SECC").trim().isEmpty()) {
                                String secc = rsCC.getString("SECC").trim();
                                if ("JDN".equals(secc)) {
                                    nivelEdu = "Preescolar";
                                } else if ("PRI".equals(secc)) {
                                    nivelEdu = "Primaria";
                                } else if ("SEC".equals(secc)) {
                                    nivelEdu = "Secundaria";
                                } else if ("BAC".equals(secc)) {
                                    nivelEdu = "Bachillerato";
                                } else {
                                    nivelEdu = secc;
                                }
                            } else if (rsCC.getString("CLS01") != null && !rsCC.getString("CLS01").trim().isEmpty()) {
                                nivelEdu = rsCC.getString("CLS01").trim();
                            }
                        }
                        rsCC.close();
                        psCC.close();

                        // Descripción de Ciclo Escolar (tescesc)
                        String descCiclo = "2025-2026";
                        PreparedStatement psCesc = con.prepareStatement("SELECT CDSC FROM tescesc WHERE CIA = ? AND CC = ? AND CESC = ?");
                        psCesc.setString(1, cia);
                        psCesc.setString(2, cc);
                        psCesc.setString(3, cesc);
                        ResultSet rsCesc = psCesc.executeQuery();
                        if (rsCesc.next() && rsCesc.getString("CDSC") != null) {
                            descCiclo = rsCesc.getString("CDSC").trim();
                        }
                        rsCesc.close();
                        psCesc.close();

                        // Conceptos del Recibo (tesralu + tescpto)
                        String sqlConceptos = "SELECT r.NCPTO, r.DCPTO, r.CUNIMN, r.IMPMN, r.IDSCMN, r.IBECMN, r.IRECMN, r.GRADO, c.CPROD "
                                + "FROM tesralu r "
                                + "LEFT JOIN tescpto c ON r.CIA = c.CIA AND r.CC = c.CC AND r.NCPTO = c.NCPTO "
                                + "WHERE r.CIA = ? AND r.NREC = ?";
                        PreparedStatement psCptos = con.prepareStatement(sqlConceptos);
                        psCptos.setString(1, cia);
                        psCptos.setString(2, nrec);
                        ResultSet rsCptos = psCptos.executeQuery();

                        java.util.List<Object[]> cptos = new java.util.ArrayList<>();
                        String gradoAlumno = "6P";
                        double totRecargo = 0.0;
                        while (rsCptos.next()) {
                            double ir = rsCptos.getDouble("IRECMN");
                            if (ir > 0) {
                                totRecargo += ir;
                            }
                            cptos.add(new Object[]{
                                rsCptos.getString("CPROD"),
                                rsCptos.getString("DCPTO"),
                                rsCptos.getDouble("CUNIMN"),
                                rsCptos.getDouble("IMPMN"),
                                rsCptos.getDouble("IDSCMN") + rsCptos.getDouble("IBECMN"),
                                rsCptos.getString("GRADO")
                            });
                        }
                        rsCptos.close();
                        psCptos.close();

                        // Generar archivo TXT independiente por cada recibo
                        File archivo = new File(felDir, "BridgeTxt_PAG_" + folio + "_" + nrec + "_" + sdfStamp.format(new java.util.Date()) + ".txt");

                        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(archivo), StandardCharsets.UTF_8))) {
                            // LÍNEA 1: RECEPTOR (re)
                            bw.write("re;" + rfcReceptor + ";" + nomReceptor + ";" + cpReceptor + ";;;" + regFisReceptor + ";" + usoCfdi + ";" + emailReceptor + ";");
                            bw.newLine();

                            // LÍNEA 2: FACTURA (fa) - SE CAMBIÓ 'nrec' POR 'folio' (NFAC)
                            bw.write("fa;PAG;MXN;1;" + metodoPagoPram + ";03;;" + cpEmisor + ";" + crutaEmisor + ";" + folio + ";" + regFisEmisor + ";");
                            bw.newLine();

                            // LÍNEAS 3 y 4: CONCEPTOS (cto) + COMPLEMENTO IEDU (ctoiedu)
                            for (Object[] cp : cptos) {
                                String cprod = cp[0] != null ? cp[0].toString().trim() : "86121500";
                                String dcpto = cp[1] != null ? cp[1].toString().trim() : "CONCEPTO ESCOLAR";
                                double valUni = (Double) cp[2];
                                double impMn = (Double) cp[3];
                                double desc = (Double) cp[4];
                                if (cp[5] != null) {
                                    gradoAlumno = cp[5].toString().trim();
                                }

                                bw.write("cto;1.00;Servicio;E48;" + cprod + ";;" + dcpto + ";" + df.format(valUni) + ";" + df.format(impMn) + ";" + (desc > 0 ? df.format(desc) : "") + ";01;");
                                bw.newLine();

                                bw.write("ctoiedu;" + nomComAlumno + ";" + curpAlumno + ";" + nivelEdu + ";" + cct + ";;");
                                bw.newLine();
                            }

                            // LÍNEA DE RECARGOS
                            if (totRecargo > 0) {
                                bw.write("cto;1.00;Servicio;E48;" + cprecVal + ";;RECARGOS;" + df.format(totRecargo) + ";" + df.format(totRecargo) + ";;01;");
                                bw.newLine();
                                bw.write("ctoiedu;" + nomComAlumno + ";" + curpAlumno + ";" + nivelEdu + ";" + cct + ";;");
                                bw.newLine();
                            }

                            // LÍNEA 5: ENCABEZADO ESCOLAR (ep)
                            bw.write("ep;Ciclo Escolar;" + descCiclo + "; Grado;" + gradoAlumno + ";Leyenda;.;");
                            bw.newLine();

                            // LÍNEA 6: FIN DE FACTURA (fafin)
                            bw.write("fafin");
                            bw.newLine();
                        }

                        // Actualización de estado en tesralu
                        PreparedStatement psUpdRec = con.prepareStatement("UPDATE tesralu SET FFAC = ?, NFAC = ?, TFAC = ? WHERE CIA = ? AND NREC = ?");
                        psUpdRec.setString(1, fecFactura);
                        psUpdRec.setInt(2, folio);
                        psUpdRec.setString(3, serie);
                        psUpdRec.setString(4, cia);
                        psUpdRec.setString(5, nrec);
                        psUpdRec.executeUpdate();
                        psUpdRec.close();

                        folio++;
                        foliosUsados++;
                    }

                    con.commit();
                    db.Cerrar();

                    txtNumFactura.setText(String.valueOf(folio));

                    String rango = foliosUsados == 1
                            ? String.valueOf(folio - 1)
                            : (String.valueOf(folio - foliosUsados) + " - " + String.valueOf(folio - 1));
                    JOptionPane.showMessageDialog(this, "Se generaron " + foliosUsados + " archivo(s) TXT de FEL en:\n" + felDir + "\n\nFacturas " + serie + " " + rango + " registradas con éxito.", "FEL Exitoso", JOptionPane.INFORMATION_MESSAGE);

                    btnFiltra.doClick(); // Recargar lista
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al procesar la factura FEL: " + ex.getMessage(), "Error FEL", JOptionPane.ERROR_MESSAGE);
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

        jPanel1 = new javax.swing.JPanel();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 750, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 430, Short.MAX_VALUE)
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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
