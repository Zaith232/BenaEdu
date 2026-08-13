/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.benaedu.views;

import com.mycompany.benaedu.Dashboard;
import com.mycompany.benaedu.db.ConDB;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
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
public class Cobranza_Escolar extends javax.swing.JPanel {
private String usuarioLogueado = "Admin";
    private double totalCobrado = 0.0;
    private String tipoReciboGenerado = "RO";
    private String seccionAlumno = "SEC";
    private String descripcionSeccion = "SECUNDARIA";
    /**
     * Creates new form Cobranza_Escolar
     */
    
    // Estructuras de soporte para la impresión de los comprobantes
    private static class ConceptoImpresion {
        String ncpto;
        String dcpto;
        double importe;
        double pagadoPrev;
        String fven;
        double desc;
        double pBeca;
        double pRec;
        double pago;
        double saldo;
    }
    
    private static class PagoImpresion {
        String formaPago;
        String banco;
        String referencia;
        String fecha;
        double importe;
    }

    private final List<ConceptoImpresion> listaConceptosCobrados = new ArrayList<>();
    private final List<PagoImpresion> listaPagosCobrados = new ArrayList<>();
    public Cobranza_Escolar(String usuarioLogueado) {
        if (usuarioLogueado != null && !usuarioLogueado.trim().isEmpty()) {
            this.usuarioLogueado = usuarioLogueado.trim();
        }
        initComponents();
        construirInterfazCobranza();
    }

    public Cobranza_Escolar() {
        initComponents();
        construirInterfazCobranza();
    }

    private String obtenerUsuarioActivo() {
        if (this.usuarioLogueado != null && !this.usuarioLogueado.equals("Admin")) {
            return this.usuarioLogueado;
        }
        java.awt.Window parentWindow = SwingUtilities.getWindowAncestor(this);
        if (parentWindow instanceof Dashboard dash) {
            return dash.getUsuarioCodigo();
        }
        return this.usuarioLogueado;
    }

    private void construirInterfazCobranza() {
        this.removeAll();
        this.setLayout(null);
        this.setBackground(new Color(255, 255, 255));

        // --- BUSCADOR FLOTANTE ---
        class BuscadorFlotante {
            void configurar(JTextField txtClave, JTextField txtDesc, JButton boton, Object[][] datos, String[] columnas, int[] anchos, java.util.function.Consumer<Object[]> onSelect) {
                Runnable mostrarPopup = () -> {
                    JPopupMenu popup = new JPopupMenu();
                    popup.setFocusable(false);
                    DefaultTableModel mod = new DefaultTableModel(datos, columnas) {
                        @Override public boolean isCellEditable(int r, int c) { return false; }
                    };
                    JTable tabla = new JTable(mod);
                    tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    for (int i = 0; i < anchos.length; i++) {
                        tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
                    }

                    TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(mod);
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
                                    for (int i = 0; i < mod.getColumnCount(); i++) {
                                        rowData[i] = mod.getValueAt(modelRow, i);
                                    }
                                    onSelect.accept(rowData);
                                }
                                popup.setVisible(false);
                            }
                        }
                    });

                    int widthTotal = 0;
                    for (int w : anchos) widthTotal += w;
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
                        if (c == 27 || c == 10 || c == 38 || c == 40 || c == 37 || c == 39 || c == 9) return;
                        mostrarPopup.run();
                    }
                });
            }
        }
        BuscadorFlotante buscador = new BuscadorFlotante();

        java.util.function.BiFunction<String, Integer, Object[][]> cargarDatosMultiple = (query, numCols) -> {
            List<Object[]> lista = new ArrayList<>();
            try (Connection con = new ConDB().Conectar()) {
                if (con != null) {
                    PreparedStatement ps = con.prepareStatement(query);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        Object[] row = new Object[numCols];
                        for (int i = 0; i < numCols; i++) row[i] = rs.getString(i + 1);
                        lista.add(row);
                    }
                    rs.close(); ps.close();
                }
            } catch (Exception ignored) {}
            return lista.toArray(new Object[0][0]);
        };

        Object[][] dCC = cargarDatosMultiple.apply("SELECT CVE, DES1 FROM tgcc WHERE CVE IN ('12100','12200','12300','12400') ORDER BY CVE", 2);
        Object[][] dMatricula = cargarDatosMultiple.apply("SELECT MAT, APATE, AMATE, NOMA FROM tesalum ORDER BY MAT", 4);
        Object[][] dCiclo = cargarDatosMultiple.apply("SELECT CESC, CDSC FROM tescesc ORDER BY CESC DESC", 2);
        Object[][] dMoneda = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'TMON' ORDER BY CVE", 2);
        Object[][] dFormaPago = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'IPAG' ORDER BY CVE", 2);
        Object[][] dBanco = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'BCOS' ORDER BY CVE", 2);

        // --- 1. SECCIÓN SUPERIOR (Datos del Alumno) ---
        JPanel pnlTop = new JPanel(null);
        pnlTop.setBackground(Color.WHITE);
        pnlTop.setBorder(BorderFactory.createEtchedBorder());
        pnlTop.setBounds(10, 10, 885, 90);

        pnlTop.add(new JLabel("Compañía")).setBounds(15, 15, 70, 25);
        JComboBox<String> cmbCia = new JComboBox<>(new String[]{"12"});
        cmbCia.setBounds(85, 15, 60, 25);
        pnlTop.add(cmbCia);

        pnlTop.add(new JLabel("C. Costos")).setBounds(160, 15, 70, 25);
        JComboBox<String> cmbCC = new JComboBox<>();
        for (Object[] r : dCC) cmbCC.addItem(r[0] != null ? r[0].toString() : "");
        cmbCC.setBounds(230, 15, 80, 25);
        pnlTop.add(cmbCC);

        pnlTop.add(new JLabel("Ciclo Escolar")).setBounds(330, 15, 80, 25);
        JTextField txtCiclo = new JTextField("2526");
        txtCiclo.setBounds(415, 15, 60, 25);
        JButton btnCiclo = new JButton("▼");
        btnCiclo.setFont(new Font("SansSerif", Font.PLAIN, 10));
        btnCiclo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnCiclo.setBounds(475, 15, 20, 25);
        buscador.configurar(txtCiclo, null, btnCiclo, dCiclo, new String[]{"Clave", "Descripción"}, new int[]{60, 150}, null);
        pnlTop.add(txtCiclo);
        pnlTop.add(btnCiclo);

        pnlTop.add(new JLabel("Fecha Recibo")).setBounds(510, 15, 80, 25);
        com.toedter.calendar.JDateChooser txtFecRecibo = new com.toedter.calendar.JDateChooser();
        txtFecRecibo.setDateFormatString("dd/MM/yyyy");
        txtFecRecibo.setDate(new Date());
        txtFecRecibo.setBounds(595, 15, 110, 25);
        pnlTop.add(txtFecRecibo);

        pnlTop.add(new JLabel("Grado")).setBounds(330, 45, 50, 25);
        JTextField txtGrado = new JTextField();
        txtGrado.setBounds(385, 45, 50, 25);
        txtGrado.setEditable(false);
        pnlTop.add(txtGrado);

        pnlTop.add(new JLabel("Grupo")).setBounds(510, 45, 50, 25);
        JTextField txtGrupo = new JTextField();
        txtGrupo.setBounds(565, 45, 50, 25);
        txtGrupo.setEditable(false);
        pnlTop.add(txtGrupo);

        // Beca
        JRadioButton rbBecaNo = new JRadioButton("No", true);
        rbBecaNo.setBounds(10, 20, 50, 25);
        rbBecaNo.setEnabled(false);
        JRadioButton rbBecaSi = new JRadioButton("Beca");
        rbBecaSi.setBounds(60, 20, 60, 25);
        rbBecaSi.setEnabled(false);
        JRadioButton rbBecaConv = new JRadioButton("Convenio");
        rbBecaConv.setBounds(120, 20, 75, 25);
        rbBecaConv.setEnabled(false);
        ButtonGroup bgBeca = new ButtonGroup();
        bgBeca.add(rbBecaNo);
        bgBeca.add(rbBecaSi);
        bgBeca.add(rbBecaConv);

        JTextField txtCodBeca = new JTextField();
        txtCodBeca.setBounds(10, 20, 175, 25);
        txtCodBeca.setEditable(false);

        // Modelos de Conceptos
        DefaultTableModel modAdeudos = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Concepto", "Descripción", "Importe", "Pagado", "Saldo", "ID", "F Venc", "Ciclo", "Tpo Cont"}
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblAdeudos = new JTable(modAdeudos);

        DefaultTableModel modAPagar = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Concepto", "Descripción", "Importe", "Pagado", "Saldo", "ID", "F Venc", "Ciclo", "Tpo Cont"}
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tblAPagar = new JTable(modAPagar);

        JTextField txtTotalAdeudo = new JTextField("0.00");
        txtTotalAdeudo.setBounds(740, 300, 100, 25);
        txtTotalAdeudo.setEditable(false);
        txtTotalAdeudo.setHorizontalAlignment(JTextField.RIGHT);

        JTextField txtTotalPagarTab1 = new JTextField("0.00");
        txtTotalPagarTab1.setBounds(740, 300, 100, 25);
        txtTotalPagarTab1.setEditable(false);
        txtTotalPagarTab1.setHorizontalAlignment(JTextField.RIGHT);

        JTextField txtTotalPagarTab2 = new JTextField("0.00");
        txtTotalPagarTab2.setBounds(740, 20, 100, 25);
        txtTotalPagarTab2.setEditable(false);
        txtTotalPagarTab2.setHorizontalAlignment(JTextField.RIGHT);

        // Tabla de Instrumentos de Pago
        DefaultTableModel modInstr = new DefaultTableModel(new Object[][]{}, new String[]{"IPago", "Descripción", "CBanco", "Banco", "CtaPag", "Referencia", "Fecha", "Importe"});
        JTable tblInstr = new JTable(modInstr);

        JTextField txtTotalInstr = new JTextField("0.00");
        txtTotalInstr.setBounds(550, 300, 100, 25);
        txtTotalInstr.setEditable(false);
        txtTotalInstr.setHorizontalAlignment(JTextField.RIGHT);

        JTextField txtSaldoInstr = new JTextField("0.00");
        txtSaldoInstr.setBounds(550, 330, 100, 25);
        txtSaldoInstr.setEditable(false);
        txtSaldoInstr.setHorizontalAlignment(JTextField.RIGHT);

        // Buscador de Alumno / Matrícula
        pnlTop.add(new JLabel("Matrícula")).setBounds(15, 45, 70, 25);
        JTextField txtMat = new JTextField();
        txtMat.setBounds(85, 45, 90, 25);
        JButton btnMat = new JButton("▼");
        btnMat.setFont(new Font("SansSerif", Font.PLAIN, 10));
        btnMat.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnMat.setBounds(175, 45, 20, 25);
        JTextField txtNombreAlum = new JTextField();
        txtNombreAlum.setBounds(200, 45, 120, 25);
        txtNombreAlum.setEditable(false);
        txtNombreAlum.setBackground(new Color(240, 240, 240));

        java.util.function.Consumer<Object[]> onMatSelect = (rowData) -> {
            String matriculaSel = rowData[0].toString();
            String paterno = rowData[1] != null ? rowData[1].toString() : "";
            String materno = rowData[2] != null ? rowData[2].toString() : "";
            String nombre = rowData[3] != null ? rowData[3].toString() : "";
            txtNombreAlum.setText((paterno + " " + materno + " " + nombre).trim());

            totalCobrado = 0.0;

            try (Connection con = new ConDB().Conectar()) {
                if (con != null) {
                    String sqlAxce = "SELECT a.GRADO, a.GRUPO, a.CBECA, a.TBECA, a.CC, a.CESC, a.SECC, g.DES1 AS DESC_CC " +
                                     "FROM tesaxce a " +
                                     "LEFT JOIN tgcc g ON a.CC = g.CVE " +
                                     "WHERE a.MAT = ? ORDER BY a.CESC DESC LIMIT 1";
                    PreparedStatement ps = con.prepareStatement(sqlAxce);
                    ps.setString(1, matriculaSel);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        txtGrado.setText(rs.getString("GRADO"));
                        txtGrupo.setText(rs.getString("GRUPO"));
                        seccionAlumno = rs.getString("SECC") != null ? rs.getString("SECC") : "SEC";
                        descripcionSeccion = rs.getString("DESC_CC") != null ? rs.getString("DESC_CC") : "SECUNDARIA";

                        if (rs.getString("CC") != null) cmbCC.setSelectedItem(rs.getString("CC"));
                        if (rs.getString("CESC") != null) txtCiclo.setText(rs.getString("CESC"));

                        String cBeca = rs.getString("CBECA");
                        String tBeca = rs.getString("TBECA");
                        if (cBeca != null && !cBeca.trim().isEmpty()) {
                            txtCodBeca.setText(cBeca);
                            if ("C".equalsIgnoreCase(tBeca)) rbBecaConv.setSelected(true);
                            else rbBecaSi.setSelected(true);
                        } else {
                            txtCodBeca.setText("");
                            rbBecaNo.setSelected(true);
                        }
                    }
                    rs.close(); ps.close();

                    modAdeudos.setRowCount(0);
                    modAPagar.setRowCount(0);
                    modInstr.setRowCount(0);

                    String sqlCalu = "SELECT NCPTO, DCPTO, IMPTMN, IPAGMN, IPENMN, IDCPT, FVEN, CESC, TCONT " +
                                     "FROM tescalu WHERE MAT = ? AND IPENMN > 0 ORDER BY FVEN ASC";
                    PreparedStatement psCalu = con.prepareStatement(sqlCalu);
                    psCalu.setString(1, matriculaSel);
                    ResultSet rsCalu = psCalu.executeQuery();

                    double sumaTotalAdeudo = 0.0;
                    DecimalFormat df = new DecimalFormat("#,##0.00");

                    while (rsCalu.next()) {
                        Object[] fila = new Object[9];
                        fila[0] = rsCalu.getString("NCPTO");
                        fila[1] = rsCalu.getString("DCPTO");
                        fila[2] = df.format(rsCalu.getDouble("IMPTMN"));
                        fila[3] = df.format(rsCalu.getDouble("IPAGMN"));
                        double saldo = rsCalu.getDouble("IPENMN");
                        fila[4] = df.format(saldo);
                        fila[5] = rsCalu.getString("IDCPT");
                        fila[6] = rsCalu.getString("FVEN");
                        fila[7] = rsCalu.getString("CESC");
                        fila[8] = rsCalu.getString("TCONT");

                        sumaTotalAdeudo += saldo;
                        modAdeudos.addRow(fila);
                    }
                    txtTotalAdeudo.setText(df.format(sumaTotalAdeudo));
                    rsCalu.close(); psCalu.close();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al cargar adeudos: " + ex.getMessage());
            }
        };

        buscador.configurar(txtMat, null, btnMat, dMatricula, new String[]{"Matrícula", "Paterno", "Materno", "Nombre"}, new int[]{80, 120, 120, 150}, onMatSelect);
        pnlTop.add(txtMat);
        pnlTop.add(btnMat);
        pnlTop.add(txtNombreAlum);

        this.add(pnlTop);

        // --- 2. PESTAÑAS PRINCIPALES ---
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setBounds(10, 110, 885, 500);

        // Interacción Doble Clic para Pasar a Pagar
        tblAdeudos.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = tblAdeudos.getSelectedRow();
                    if (row != -1) {
                        Object[] fila = new Object[tblAdeudos.getColumnCount()];
                        for (int i = 0; i < fila.length; i++) fila[i] = tblAdeudos.getValueAt(row, i);
                        modAPagar.addRow(fila);
                        modAdeudos.removeRow(row);

                        double total = 0;
                        DecimalFormat df = new DecimalFormat("#,##0.00");
                        for (int i = 0; i < modAPagar.getRowCount(); i++) {
                            total += Double.parseDouble(modAPagar.getValueAt(i, 4).toString().replace(",", ""));
                        }
                        txtTotalPagarTab1.setText(df.format(total));
                        txtTotalPagarTab2.setText(df.format(total));
                    }
                }
            }
        });

        // ------------------------------------------
        // TAB 1: REGISTRO CONCEPTOS
        // ------------------------------------------
        JPanel pnlConceptos = new JPanel(null);
        pnlConceptos.setBackground(Color.WHITE);

        JTabbedPane subTabAdeudos = new JTabbedPane();
        subTabAdeudos.setBounds(10, 10, 860, 440);

        JPanel pnlAdeudos = new JPanel(null);
        pnlAdeudos.setBackground(Color.WHITE);
        pnlAdeudos.setBorder(BorderFactory.createTitledBorder("Adeudos Pendientes (Doble clic para pagar)"));
        JScrollPane scrollAdeudos = new JScrollPane(tblAdeudos);
        scrollAdeudos.setBounds(15, 25, 825, 330);
        pnlAdeudos.add(scrollAdeudos);
        pnlAdeudos.add(new JLabel("Total Adeudo:")).setBounds(650, 365, 80, 25);
        pnlAdeudos.add(txtTotalAdeudo);

        JPanel pnlAPagar = new JPanel(null);
        pnlAPagar.setBackground(Color.WHITE);
        pnlAPagar.setBorder(BorderFactory.createTitledBorder("Conceptos a Liquidar en este Recibo"));
        JScrollPane scrollAPagar = new JScrollPane(tblAPagar);
        scrollAPagar.setBounds(15, 25, 825, 330);
        pnlAPagar.add(scrollAPagar);
        pnlAPagar.add(new JLabel("Total a Pagar:")).setBounds(650, 365, 80, 25);
        pnlAPagar.add(txtTotalPagarTab1);

        subTabAdeudos.addTab("Adeudos", pnlAdeudos);
        subTabAdeudos.addTab("A Pagar", pnlAPagar);
        pnlConceptos.add(subTabAdeudos);

        // ------------------------------------------
        // TAB 2: REGISTRO DE PAGO
        // ------------------------------------------
        JPanel pnlPagos = new JPanel(null);
        pnlPagos.setBackground(Color.WHITE);

        JPanel pnlTotalPagar = new JPanel(null);
        pnlTotalPagar.setBackground(Color.WHITE);
        pnlTotalPagar.setBorder(BorderFactory.createTitledBorder("Total a Liquidar"));
        pnlTotalPagar.setBounds(10, 10, 860, 60);
        pnlTotalPagar.add(txtTotalPagarTab2);
        pnlPagos.add(pnlTotalPagar);

        JPanel pnlRegPagos = new JPanel(null);
        pnlRegPagos.setBackground(Color.WHITE);
        pnlRegPagos.setBorder(BorderFactory.createTitledBorder("Instrumentos de Pago"));
        pnlRegPagos.setBounds(10, 80, 860, 380);

        pnlRegPagos.add(new JLabel("Forma Pago")).setBounds(15, 20, 90, 20);
        JTextField txtFpago = new JTextField("EF");
        txtFpago.setBounds(15, 40, 35, 25);
        JButton btnFpago = new JButton("▼");
        btnFpago.setFont(new Font("SansSerif", Font.PLAIN, 10));
        btnFpago.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnFpago.setBounds(50, 40, 20, 25);

        pnlRegPagos.add(new JLabel("Descripción")).setBounds(115, 20, 120, 20);
        JTextField txtFpagoDesc = new JTextField("01-EFECTIVO");
        txtFpagoDesc.setBounds(80, 40, 150, 25);
        txtFpagoDesc.setEditable(false);
        txtFpagoDesc.setBackground(new Color(240, 240, 240));
        buscador.configurar(txtFpago, txtFpagoDesc, btnFpago, dFormaPago, new String[]{"Clave", "Descripción"}, new int[]{60, 200}, null);

        pnlRegPagos.add(txtFpago); pnlRegPagos.add(btnFpago); pnlRegPagos.add(txtFpagoDesc);

        pnlRegPagos.add(new JLabel("Banco")).setBounds(240, 20, 60, 20);
        JTextField txtBanco = new JTextField();
        txtBanco.setBounds(240, 40, 35, 25);
        JButton btnBanco = new JButton("▼");
        btnBanco.setFont(new Font("SansSerif", Font.PLAIN, 10));
        btnBanco.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnBanco.setBounds(275, 40, 20, 25);
        buscador.configurar(txtBanco, null, btnBanco, dBanco, new String[]{"Clave", "Descripción"}, new int[]{60, 200}, null);
        pnlRegPagos.add(txtBanco); pnlRegPagos.add(btnBanco);

        pnlRegPagos.add(new JLabel("Cta Pago")).setBounds(305, 20, 70, 20);
        JTextField txtCtaPago = new JTextField();
        txtCtaPago.setBounds(305, 40, 80, 25);
        pnlRegPagos.add(txtCtaPago);

        pnlRegPagos.add(new JLabel("Referencia")).setBounds(395, 20, 80, 20);
        JTextField txtReferencia = new JTextField();
        txtReferencia.setBounds(395, 40, 100, 25);
        pnlRegPagos.add(txtReferencia);

        pnlRegPagos.add(new JLabel("Fecha")).setBounds(505, 20, 80, 20);
        com.toedter.calendar.JDateChooser txtFecPago = new com.toedter.calendar.JDateChooser();
        txtFecPago.setDateFormatString("dd/MM/yyyy");
        txtFecPago.setDate(new Date());
        txtFecPago.setBounds(505, 40, 110, 25);
        pnlRegPagos.add(txtFecPago);

        pnlRegPagos.add(new JLabel("Importe")).setBounds(625, 20, 60, 20);
        JTextField txtImportePago = new JTextField("0.00");
        txtImportePago.setBounds(625, 40, 80, 25);
        txtImportePago.setHorizontalAlignment(JTextField.RIGHT);
        pnlRegPagos.add(txtImportePago);

        JButton btnOkPago = new JButton("OK");
        btnOkPago.setBounds(715, 40, 55, 25);
        pnlRegPagos.add(btnOkPago);

        JScrollPane scrollInstr = new JScrollPane(tblInstr);
        scrollInstr.setBounds(15, 80, 825, 210);
        pnlRegPagos.add(scrollInstr);

        pnlRegPagos.add(new JLabel("Total Cubierto:")).setBounds(460, 300, 90, 25);
        pnlRegPagos.add(txtTotalInstr);
        pnlRegPagos.add(new JLabel("Saldo Pendiente:")).setBounds(460, 330, 90, 25);
        pnlRegPagos.add(txtSaldoInstr);

        JButton btnAceptarCobro = new JButton("Aceptar y Cobrar");
        btnAceptarCobro.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAceptarCobro.setBounds(680, 300, 150, 55);
        pnlRegPagos.add(btnAceptarCobro);

        pestanas.addChangeListener(e -> {
            if (pestanas.getSelectedIndex() == 1) {
                double totalAPagarVal = 0.0;
                try {
                    totalAPagarVal = Double.parseDouble(txtTotalPagarTab2.getText().replace(",", ""));
                } catch (Exception ignored) {}

                double totalReg = 0.0;
                for (int i = 0; i < modInstr.getRowCount(); i++) {
                    totalReg += Double.parseDouble(modInstr.getValueAt(i, 7).toString().replace(",", ""));
                }
                double saldoVal = totalAPagarVal - totalReg;
                DecimalFormat df = new DecimalFormat("#,##0.00");

                txtTotalInstr.setText(df.format(totalReg));
                txtSaldoInstr.setText(df.format(saldoVal));
                txtImportePago.setText(df.format(Math.max(0, saldoVal)));
            }
        });

        btnOkPago.addActionListener(e -> {
            try {
                double imp = Double.parseDouble(txtImportePago.getText().trim().replace(",", ""));
                if (imp <= 0) {
                    JOptionPane.showMessageDialog(this, "Ingrese un importe válido.");
                    return;
                }
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                String fechaStr = txtFecPago.getDate() != null ? sdf.format(txtFecPago.getDate()) : "";

                modInstr.addRow(new Object[]{
                    txtFpago.getText().trim(),
                    txtFpagoDesc.getText().trim(),
                    txtBanco.getText().trim(),
                    txtBanco.getText().trim().isEmpty() ? "" : "SANTANDER",
                    txtCtaPago.getText().trim(),
                    txtReferencia.getText().trim(),
                    fechaStr,
                    String.format("%.2f", imp)
                });

                double totalReg = 0;
                for (int i = 0; i < modInstr.getRowCount(); i++) {
                    totalReg += Double.parseDouble(modInstr.getValueAt(i, 7).toString().replace(",", ""));
                }
                DecimalFormat df = new DecimalFormat("#,##0.00");
                txtTotalInstr.setText(df.format(totalReg));

                double totalAPagarVal = Double.parseDouble(txtTotalPagarTab2.getText().trim().replace(",", ""));
                double saldoVal = totalAPagarVal - totalReg;
                txtSaldoInstr.setText(df.format(saldoVal));
                txtImportePago.setText(df.format(Math.max(0, saldoVal)));

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error en el formato del importe.");
            }
        });

        pnlPagos.add(pnlRegPagos);

        // ------------------------------------------
        // TAB 3: IMPRESIÓN DE COMPROBANTE (RO / RP)
        // ------------------------------------------
        JPanel pnlImpresion = new JPanel(null);
        pnlImpresion.setBackground(Color.WHITE);

        JPanel pnlRecibo = new JPanel(null);
        pnlRecibo.setBackground(Color.WHITE);
        pnlRecibo.setBorder(BorderFactory.createTitledBorder("Emisión y Reimpresión de Recibo"));
        pnlRecibo.setBounds(10, 20, 860, 80);

        JTextField txtNumReciboGen = new JTextField();
        txtNumReciboGen.setEditable(false);
        txtNumReciboGen.setFont(new Font("Segoe UI", Font.BOLD, 13));

        pnlRecibo.add(new JLabel("Número de Recibo:")).setBounds(30, 30, 120, 25);
        txtNumReciboGen.setBounds(150, 30, 100, 25);
        pnlRecibo.add(txtNumReciboGen);

        JButton btnImpRecibo = new JButton("Imprimir Recibo (Físico / PDF)");
        btnImpRecibo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnImpRecibo.setBounds(270, 27, 240, 32);
        pnlRecibo.add(btnImpRecibo);

        JButton btnLimpiarCobranza = new JButton("Nuevo Cobro");
        btnLimpiarCobranza.setBounds(530, 27, 130, 32);
        pnlRecibo.add(btnLimpiarCobranza);

        pnlImpresion.add(pnlRecibo);

        btnLimpiarCobranza.addActionListener(e -> {
            txtMat.setText("");
            txtNombreAlum.setText("");
            txtGrado.setText("");
            txtGrupo.setText("");
            txtCodBeca.setText("");
            rbBecaNo.setSelected(true);
            modAdeudos.setRowCount(0);
            modAPagar.setRowCount(0);
            modInstr.setRowCount(0);
            listaConceptosCobrados.clear();
            listaPagosCobrados.clear();
            totalCobrado = 0.0;
            txtTotalAdeudo.setText("0.00");
            txtTotalPagarTab1.setText("0.00");
            txtTotalPagarTab2.setText("0.00");
            txtTotalInstr.setText("0.00");
            txtSaldoInstr.setText("0.00");
            txtImportePago.setText("0.00");
            pestanas.setSelectedIndex(0);
        });

        // =========================================================================
        // MOTOR DE IMPRESIÓN CON FORMATOS EXACTOS RO Y RP (SEGÚN LOS PDFS)
        // =========================================================================
        btnImpRecibo.addActionListener(e -> {
            String numRecibo = txtNumReciboGen.getText().trim();
            if (numRecibo.isEmpty() || totalCobrado <= 0) {
                JOptionPane.showMessageDialog(this, "No hay cobros aplicados para imprimir.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName("Recibo " + numRecibo + " " + tipoReciboGenerado);

            SimpleDateFormat sdfFecha = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm:ss");
            String fechaHoy = sdfFecha.format(new Date());
            String horaHoy = sdfHora.format(new Date());

            String matricula = txtMat.getText().trim();
            String alumno = txtNombreAlum.getText().trim();
            String cia = cmbCia.getSelectedItem().toString();
            String cc = cmbCC.getSelectedItem().toString();
            String ciclo = txtCiclo.getText().trim();
            String grado = txtGrado.getText().trim();
            String grupo = txtGrupo.getText().trim();

            job.setPrintable((g, pf, pageIndex) -> {
                if (pageIndex > 0) return Printable.NO_SUCH_PAGE;

                Graphics2D g2d = (Graphics2D) g;
                g2d.translate(pf.getImageableX(), pf.getImageableY());
                DecimalFormat df = new DecimalFormat("#,##0.00");

                int y = 35;

                // -------------------------------------------------------------
                // ENCABEZADO: DIFERENCIACIÓN SEGÚN TIPO DE RECIBO (RO vs RP)
                // -------------------------------------------------------------
                if ("RO".equalsIgnoreCase(tipoReciboGenerado)) {
                    // FORMATO OFICIAL PREIMPRESO (Colegiaturas e Inscripciones)
                    g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
                    g2d.drawString("UNIDAD ESCOLAR BENAVENTE, A.C.", 35, y);
                    g2d.setFont(new Font("SansSerif", Font.BOLD, 9));
                    g2d.drawString("RECIBO " + numRecibo + " RO", 430, y);
                    y += 12;

                    g2d.setFont(new Font("SansSerif", Font.PLAIN, 7));
                    g2d.drawString("JARDIN DE NIÑOS  -  PRIMARIA  -  SECUNDARIA  -  BACHILLERATO", 35, y);
                    g2d.drawString("Fecha Recibo: " + fechaHoy, 430, y);
                    y += 10;

                    g2d.drawString("R.F.C. UEB-820625-896 | Prolongación Av. Lerdo de Tejada #3613", 35, y);
                    g2d.drawString("TEHUACAN, PUEBLA A " + fechaHoy + " " + horaHoy, 330, y);
                    y += 10;
                    g2d.drawString("C.P. 75760, Tehuacán, Puebla  Tel: (238) 382-1630", 35, y);
                } else {
                    // FORMATO PARTICULAR (Constancias, Entrenamientos, Cuotas)
                    g2d.setFont(new Font("SansSerif", Font.BOLD, 11));
                    g2d.drawString("UNIDAD ESCOLAR BENAVENTE (" + descripcionSeccion.toUpperCase() + ")", 35, y);
                    g2d.setFont(new Font("SansSerif", Font.PLAIN, 8));
                    g2d.drawString("Fecha: " + fechaHoy, 450, y);
                    y += 12;

                    g2d.drawString("PROLONGACION AV. LERDO DE TEJADA #3613 Col. FRACC. REFORMA Pob: TEHUACAN Edo: PUE", 35, y);
                    g2d.drawString("Hora: " + horaHoy, 450, y);
                    y += 10;

                    g2d.drawString("C.P. 75760  R.F.C. UEB-820625-899", 35, y);
                    g2d.setFont(new Font("SansSerif", Font.BOLD, 8));
                    g2d.drawString("Num. Recibo " + numRecibo + " RP", 430, y);
                }

                y += 14;
                g2d.drawLine(35, y, 540, y);
                y += 12;

                // DATOS DEL ALUMNO
                g2d.setFont(new Font("SansSerif", Font.PLAIN, 8));
                g2d.drawString("Matrícula: " + matricula, 35, y);
                g2d.drawString("Nombre: " + alumno, 180, y);
                y += 11;
                g2d.drawString("Cia: " + cia + "   C. Costo: " + cc + "   Sección: " + seccionAlumno + "   Ciclo: " + ciclo + "   Grado: " + grado + "   Grupo: " + grupo, 35, y);
                y += 8;
                g2d.drawLine(35, y, 540, y);
                y += 12;

                // TABLA DE CONCEPTOS
                g2d.setFont(new Font("SansSerif", Font.BOLD, 7));
                g2d.drawString("Concepto / Descripción", 35, y);
                g2d.drawString("Importe", 230, y);
                g2d.drawString("Pagado", 275, y);
                g2d.drawString("FVenc", 315, y);
                g2d.drawString("Desc", 360, y);
                g2d.drawString("% Beca", 395, y);
                g2d.drawString("% Rec", 435, y);
                g2d.drawString("Pago", 475, y);
                g2d.drawString("Saldo", 515, y);
                y += 4;
                g2d.drawLine(35, y, 540, y);
                y += 11;

                g2d.setFont(new Font("SansSerif", Font.PLAIN, 7));
                for (ConceptoImpresion ci : listaConceptosCobrados) {
                    String cptoTxt = ci.ncpto + " " + ci.dcpto;
                    if (cptoTxt.length() > 38) cptoTxt = cptoTxt.substring(0, 36) + "..";

                    g2d.drawString(cptoTxt, 35, y);
                    g2d.drawString(df.format(ci.importe), 230, y);
                    g2d.drawString(df.format(ci.pagadoPrev), 275, y);
                    g2d.drawString(ci.fven, 315, y);
                    g2d.drawString(df.format(ci.desc), 360, y);
                    g2d.drawString(df.format(ci.pBeca), 395, y);
                    g2d.drawString(df.format(ci.pRec), 435, y);
                    g2d.drawString(df.format(ci.pago), 475, y);
                    g2d.drawString(df.format(ci.saldo), 515, y);
                    y += 11;
                }

                y += 5;
                g2d.drawLine(35, y, 540, y);
                y += 12;

                // IMPORTE CON LETRA Y TOTAL
                g2d.setFont(new Font("SansSerif", Font.BOLD, 8));
                g2d.drawString("***** " + convertirNumeroALetras(totalCobrado) + " *****", 35, y);
                g2d.drawString("Total: $" + df.format(totalCobrado), 460, y);
                y += 14;
                g2d.drawLine(35, y, 540, y);
                y += 12;

                // FORMAS DE PAGO DESGLOSADAS
                g2d.setFont(new Font("SansSerif", Font.BOLD, 7));
                g2d.drawString("FORMAS DE PAGO:", 35, y);
                g2d.drawString("BANCO", 180, y);
                g2d.drawString("REFERENCIA", 270, y);
                g2d.drawString("FECHA", 380, y);
                g2d.drawString("IMPORTE", 475, y);
                y += 10;

                g2d.setFont(new Font("SansSerif", Font.PLAIN, 7));
                for (PagoImpresion pi : listaPagosCobrados) {
                    g2d.drawString(pi.formaPago, 35, y);
                    g2d.drawString(pi.banco, 180, y);
                    g2d.drawString(pi.referencia, 270, y);
                    g2d.drawString(pi.fecha, 380, y);
                    g2d.drawString("$" + df.format(pi.importe), 475, y);
                    y += 10;
                }

                return Printable.PAGE_EXISTS;
            });

            if (job.printDialog()) {
                try {
                    job.print();
                    JOptionPane.showMessageDialog(this, "Recibo enviado a la impresora con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception exP) {
                    JOptionPane.showMessageDialog(this, "Error al imprimir: " + exP.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // ==========================================
        // EVENTO ACEPTAR COBRO (TRANSACCIONAL)
        // ==========================================
        btnAceptarCobro.addActionListener(e -> {
            Connection con = null;
            try {
                double saldoRestante = Double.parseDouble(txtSaldoInstr.getText().trim().replace(",", ""));
                if (Math.abs(saldoRestante) > 0.01) {
                    JOptionPane.showMessageDialog(this, "El importe registrado no cubre el saldo total a pagar.", "Atención", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (modAPagar.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(this, "No hay conceptos seleccionados para pagar.", "Atención", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (modInstr.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(this, "Debe registrar al menos un método de pago.", "Atención", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String matricula = txtMat.getText().trim();
                String cia = cmbCia.getSelectedItem().toString();
                String cc = cmbCC.getSelectedItem().toString();
                String ciclo = txtCiclo.getText().trim();
                String grado = txtGrado.getText().trim();
                String grupo = txtGrupo.getText().trim();
                String usuarioSesionActiva = obtenerUsuarioActivo();

                SimpleDateFormat sdfSql = new SimpleDateFormat("yyyy-MM-dd");
                String fecReciboStr = txtFecRecibo.getDate() != null ? sdfSql.format(txtFecRecibo.getDate()) : sdfSql.format(new Date());

                con = new ConDB().Conectar();
                if (con == null) return;
                con.setAutoCommit(false);

                // 1. Obtener Cajero (NCAJ)
                int ncajVal = 80;
                PreparedStatement psCaj = con.prepareStatement("SELECT NEMP FROM tescaj WHERE USER = ? AND CIA = ? AND ECAJ = 'A' ORDER BY CAST(NEMP AS UNSIGNED) LIMIT 1");
                psCaj.setString(1, usuarioSesionActiva);
                psCaj.setString(2, cia);
                ResultSet rsCaj = psCaj.executeQuery();
                if (rsCaj.next()) ncajVal = rsCaj.getInt("NEMP");
                rsCaj.close(); psCaj.close();

                // 2. Determinar si es Recibo Oficial (RO) o Particular (RP)
                tipoReciboGenerado = "RO";
                for (int i = 0; i < modAPagar.getRowCount(); i++) {
                    String tcontFila = modAPagar.getValueAt(i, 8) != null ? modAPagar.getValueAt(i, 8).toString() : "O";
                    if ("P".equalsIgnoreCase(tcontFila)) {
                        tipoReciboGenerado = "RP";
                        break;
                    }
                }

                // 3. Generar Folio NREC
                int numReciboVal = 1001;
                PreparedStatement psNrec = con.prepareStatement("SELECT COALESCE(MAX(CAST(NREC AS UNSIGNED)), 0) + 1 AS SIG_REC FROM tesralu WHERE CIA = ?");
                psNrec.setString(1, cia);
                ResultSet rsNrec = psNrec.executeQuery();
                if (rsNrec.next()) numReciboVal = rsNrec.getInt("SIG_REC");
                rsNrec.close(); psNrec.close();

                String nrecStr = String.valueOf(numReciboVal);

                // 4. Inserción de conceptos en tesralu y actualización en tescalu
                String sqlInsRalu = "INSERT INTO tesralu (CIA, CC, SECC, PESC, TALU, CESC, GRADO, GRUPO, MAT, NOMALU, NREC, TREC, FREC, " +
                                    "NFAC, TFAC, FFAC, IDCPT, NCPTO, TCPTO, DCPTO, TCONT, CMON, TCAMB, CUNIMN, CANT, IMPMN, " +
                                    "TDSC, PDSC, IDSCMN, PREC, IRECMN, TBECA, CBECA, PBEC, IBECMN, IMPTMN, IPENMN, FVEN, FCON, " +
                                    "IPAGMN, FPAG, CUNIME, IMPME, IPAGME, NCAJ, RELPOL, RELPOC, MCAN, USER, FEAC, HOAC) " +
                                    "VALUES (?, ?, ?, 'P', ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, '', NULL, ?, ?, ?, ?, ?, 'MXP', 1, ?, ?, ?, " +
                                    "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?, ?, ?, 0, 0, 0, ?, 0, 0, '', ?, CURDATE(), DATE_FORMAT(NOW(), '%r'))";

                PreparedStatement psRalu = con.prepareStatement(sqlInsRalu);
                PreparedStatement psCalu = con.prepareStatement("UPDATE tescalu SET IPAGMN = IMPTMN, IPENMN = 0 WHERE MAT = ? AND IDCPT = ?");

                listaConceptosCobrados.clear();

                for (int i = 0; i < modAPagar.getRowCount(); i++) {
                    String idCpt = modAPagar.getValueAt(i, 5).toString();
                    String cicloFila = modAPagar.getValueAt(i, 7) != null ? modAPagar.getValueAt(i, 7).toString() : ciclo;

                    PreparedStatement psReadCpt = con.prepareStatement(
                        "SELECT SECC, TALU, NCPTO, TCPTO, DCPTO, TCONT, CUNIMN, CANT, IMPMN, TDSC, PDSC, IDSCMN, PREC, IRECMN, TBECA, CBECA, PBEC, IBECMN, IMPTMN, FVEN, FCON " +
                        "FROM tescalu WHERE MAT = ? AND IDCPT = ?"
                    );
                    psReadCpt.setString(1, matricula);
                    psReadCpt.setString(2, idCpt);
                    ResultSet rsCpt = psReadCpt.executeQuery();

                    if (rsCpt.next()) {
                        double impBruto = rsCpt.getDouble("IMPMN");
                        double impNeto = rsCpt.getDouble("IMPTMN");
                        double cantVal = rsCpt.getDouble("CANT") > 0 ? rsCpt.getDouble("CANT") : 1.0;

                        psRalu.setString(1, cia);
                        psRalu.setString(2, cc);
                        psRalu.setString(3, rsCpt.getString("SECC") != null ? rsCpt.getString("SECC") : seccionAlumno);
                        psRalu.setString(4, rsCpt.getString("TALU") != null ? rsCpt.getString("TALU") : "O");
                        psRalu.setString(5, cicloFila);
                        psRalu.setString(6, grado);
                        psRalu.setString(7, grupo);
                        psRalu.setString(8, matricula);
                        psRalu.setString(9, txtNombreAlum.getText().trim());
                        psRalu.setString(10, nrecStr);
                        psRalu.setString(11, tipoReciboGenerado);
                        psRalu.setString(12, fecReciboStr);
                        psRalu.setString(13, idCpt);
                        psRalu.setString(14, rsCpt.getString("NCPTO"));
                        psRalu.setString(15, rsCpt.getString("TCPTO"));
                        psRalu.setString(16, rsCpt.getString("DCPTO"));
                        psRalu.setString(17, rsCpt.getString("TCONT") != null ? rsCpt.getString("TCONT") : "O");
                        psRalu.setDouble(18, rsCpt.getDouble("CUNIMN"));
                        psRalu.setDouble(19, cantVal);
                        psRalu.setDouble(20, impBruto);
                        psRalu.setString(21, rsCpt.getString("TDSC") != null ? rsCpt.getString("TDSC") : "");
                        psRalu.setDouble(22, rsCpt.getDouble("PDSC"));
                        psRalu.setDouble(23, rsCpt.getDouble("IDSCMN"));
                        psRalu.setDouble(24, rsCpt.getDouble("PREC"));
                        psRalu.setDouble(25, rsCpt.getDouble("IRECMN"));
                        psRalu.setString(26, rsCpt.getString("TBECA") != null ? rsCpt.getString("TBECA") : "");
                        psRalu.setString(27, rsCpt.getString("CBECA") != null ? rsCpt.getString("CBECA") : "");
                        psRalu.setDouble(28, rsCpt.getDouble("PBEC"));
                        psRalu.setDouble(29, rsCpt.getDouble("IBECMN"));
                        psRalu.setDouble(30, impNeto);
                        psRalu.setString(31, rsCpt.getString("FVEN"));

                        String fcon = rsCpt.getString("FCON");
                        if (fcon == null || fcon.trim().isEmpty() || fcon.equals("0000-00-00")) {
                            psRalu.setNull(32, java.sql.Types.DATE);
                        } else {
                            psRalu.setDate(32, java.sql.Date.valueOf(fcon));
                        }

                        psRalu.setDouble(33, impNeto);
                        psRalu.setString(34, fecReciboStr);
                        psRalu.setInt(35, ncajVal);
                        psRalu.setString(36, usuarioSesionActiva);
                        psRalu.executeUpdate();

                        // Guardar para el generador de impresión
                        ConceptoImpresion ci = new ConceptoImpresion();
                        ci.ncpto = rsCpt.getString("NCPTO");
                        ci.dcpto = rsCpt.getString("DCPTO");
                        ci.importe = impBruto;
                        ci.pagadoPrev = 0.0;
                        ci.fven = rsCpt.getDate("FVEN") != null ? new SimpleDateFormat("dd/MM/yyyy").format(rsCpt.getDate("FVEN")) : "";
                        ci.desc = rsCpt.getDouble("IDSCMN");
                        ci.pBeca = rsCpt.getDouble("PBEC");
                        ci.pRec = rsCpt.getDouble("PREC");
                        ci.pago = impNeto;
                        ci.saldo = 0.0;
                        listaConceptosCobrados.add(ci);
                    }
                    rsCpt.close(); psReadCpt.close();

                    psCalu.setString(1, matricula);
                    psCalu.setString(2, idCpt);
                    psCalu.addBatch();
                }

                psCalu.executeBatch();
                psCalu.close();
                psRalu.close();

                // 5. Inserción de formas de pago en tespalu
                String sqlInsPalu = "INSERT INTO tespalu (CIA, CC, SECC, CESC, PESC, MAT, TALU, GRADO, NREC, TREC, FREC, " +
                                    "IDPAG, FDEP, FCON, CMON, TCAMB, IMPMN, IMPME, CCTA, FMAPAG, BCOPAG, CTAPAG, REFPAG, " +
                                    "FPAG, SPAG, RELPOL, RELPOC, MCAN, RELFMA, NCAJ, USER, FEAC, HOAC) " +
                                    "VALUES (?, ?, ?, ?, 'P', ?, 'O', ?, ?, ?, ?, 0, NULL, NULL, 'MXP', 1, ?, 0, '', ?, ?, ?, ?, ?, '', 0, 0, '', '', ?, ?, CURDATE(), DATE_FORMAT(NOW(), '%r'))";

                PreparedStatement psPalu = con.prepareStatement(sqlInsPalu);
                listaPagosCobrados.clear();

                for (int j = 0; j < modInstr.getRowCount(); j++) {
                    String fmaPag = modInstr.getValueAt(j, 0).toString();
                    String descFma = modInstr.getValueAt(j, 1).toString();
                    String bcoPag = modInstr.getValueAt(j, 2).toString();
                    String ctaPag = modInstr.getValueAt(j, 4).toString();
                    String refPag = modInstr.getValueAt(j, 5).toString();
                    String fPagStr = modInstr.getValueAt(j, 6).toString();
                    double impInst = Double.parseDouble(modInstr.getValueAt(j, 7).toString().replace(",", ""));

                    Date dFp = new SimpleDateFormat("dd/MM/yyyy").parse(fPagStr);
                    String fPagSql = sdfSql.format(dFp);

                    psPalu.setString(1, cia);
                    psPalu.setString(2, cc);
                    psPalu.setString(3, seccionAlumno);
                    psPalu.setString(4, ciclo);
                    psPalu.setString(5, matricula);
                    psPalu.setString(6, grado);
                    psPalu.setString(7, nrecStr);
                    psPalu.setString(8, tipoReciboGenerado);
                    psPalu.setString(9, fecReciboStr);
                    psPalu.setDouble(10, impInst);
                    psPalu.setString(11, fmaPag);
                    psPalu.setString(12, bcoPag);
                    psPalu.setString(13, ctaPag);
                    psPalu.setString(14, refPag);
                    psPalu.setString(15, fPagSql);
                    psPalu.setInt(16, ncajVal);
                    psPalu.setString(17, usuarioSesionActiva);
                    psPalu.addBatch();

                    PagoImpresion pi = new PagoImpresion();
                    pi.formaPago = descFma;
                    pi.banco = bcoPag.isEmpty() ? "" : "SANTANDER";
                    pi.referencia = refPag;
                    pi.fecha = fPagStr;
                    pi.importe = impInst;
                    listaPagosCobrados.add(pi);
                }

                psPalu.executeBatch();
                psPalu.close();

                con.commit();
                con.setAutoCommit(true);
                con.close();

                totalCobrado = Double.parseDouble(txtTotalPagarTab2.getText().trim().replace(",", ""));
                txtNumReciboGen.setText(nrecStr);

                JOptionPane.showMessageDialog(this, "¡Cobro registrado con éxito!\nRecibo generado: " + nrecStr + " " + tipoReciboGenerado, "Éxito", JOptionPane.INFORMATION_MESSAGE);
                pestanas.setSelectedIndex(2);

            } catch (Exception ex) {
                if (con != null) {
                    try {
                        con.rollback();
                        con.setAutoCommit(true);
                        con.close();
                    } catch (Exception ignored) {}
                }
                JOptionPane.showMessageDialog(this, "Error al procesar el cobro: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        pestanas.addTab("Registro Conceptos", pnlConceptos);
        pestanas.addTab("Registro de Pago", pnlPagos);
        pestanas.addTab("Impresión de Recibo/Factura", pnlImpresion);

        this.add(pestanas);
        this.revalidate();
        this.repaint();
    }

    // =========================================================================
    // CONVERTIDOR DE NÚMEROS A LETRAS PARA COMPROBANTES FISCALES Y RECIBOS
    // =========================================================================
    private String convertirNumeroALetras(double cantidad) {
        long entero = (long) cantidad;
        int centavos = (int) Math.round((cantidad - entero) * 100);
        return (convertirEnteroALetras(entero) + " PESOS " + String.format("%02d", centavos) + "/100 MXN").toUpperCase();
    }

    private String convertirEnteroALetras(long n) {
        if (n == 0) return "CERO";
        if (n == 100) return "CIEN";
        if (n < 1000) {
            String[] centenas = {"", "CIENTO", "DOSCIENTOS", "TRESCIENTOS", "CUATROCIENTOS", "QUINIENTOS", "SEISCIENTOS", "SETECIENTOS", "OCHOCIENTOS", "NOVECIENTOS"};
            String[] decenas = {"", "DIEZ", "VEINTE", "TREINTA", "CUARENTA", "CINCUENTA", "SESENTA", "SETENTA", "OCHENTA", "NOVENTA"};
            String[] unidades = {"", "UN", "DOS", "TRES", "CUATRO", "CINCO", "SEIS", "SIETE", "OCHO", "NUEVE", "DIEZ", "ONCE", "DOCE", "TRECE", "CATORCE", "QUINCE", "DIECISEIS", "DIECISIETE", "DIECIOCHO", "DIECINUEVE", "VEINTE", "VEINTIUN", "VEINTIDOS", "VEINTITRES", "VEINTICUATRO", "VEINTICINCO", "VEINTISEIS", "VEINTISIETE", "VEINTIOCHO", "VEINTINUEVE"};

            int c = (int) (n / 100);
            int d = (int) (n % 100);

            if (d < 30) {
                return (centenas[c] + " " + unidades[d]).trim();
            } else {
                int dec = d / 10;
                int uni = d % 10;
                return (centenas[c] + " " + decenas[dec] + (uni > 0 ? " Y " + unidades[uni] : "")).trim();
            }
        }
        if (n < 1000000) {
            long miles = n / 1000;
            long resto = n % 1000;
            String strMiles = (miles == 1) ? "UN MIL" : convertirEnteroALetras(miles) + " MIL";
            return (strMiles + " " + (resto > 0 ? convertirEnteroALetras(resto) : "")).trim();
        }
        return String.valueOf(n);
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
        tblCOEscolar = new javax.swing.JTable();
        btnAddCEscolar = new javax.swing.JButton();
        btnEditCEscolar = new javax.swing.JButton();
        btnDeleteCEscolar = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        tblCOEscolar.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tblCOEscolar);

        btnAddCEscolar.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAddCEscolar.setForeground(new java.awt.Color(26, 61, 99));
        btnAddCEscolar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/add.png"))); // NOI18N
        btnAddCEscolar.setText("Añadir");
        btnAddCEscolar.addActionListener(this::btnAddCEscolarActionPerformed);

        btnEditCEscolar.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEditCEscolar.setForeground(new java.awt.Color(26, 61, 99));
        btnEditCEscolar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edit.png"))); // NOI18N
        btnEditCEscolar.setText("Editar");
        btnEditCEscolar.setMaximumSize(new java.awt.Dimension(93, 31));
        btnEditCEscolar.setMinimumSize(new java.awt.Dimension(93, 31));
        btnEditCEscolar.addActionListener(this::btnEditCEscolarActionPerformed);

        btnDeleteCEscolar.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDeleteCEscolar.setForeground(new java.awt.Color(26, 61, 99));
        btnDeleteCEscolar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/delete.png"))); // NOI18N
        btnDeleteCEscolar.setText("Eliminar");
        btnDeleteCEscolar.addActionListener(this::btnDeleteCEscolarActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAddCEscolar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditCEscolar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteCEscolar)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddCEscolar, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditCEscolar, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteCEscolar, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void btnAddCEscolarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddCEscolarActionPerformed

    }//GEN-LAST:event_btnAddCEscolarActionPerformed

    private void btnEditCEscolarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditCEscolarActionPerformed

    }//GEN-LAST:event_btnEditCEscolarActionPerformed

    private void btnDeleteCEscolarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteCEscolarActionPerformed

    }//GEN-LAST:event_btnDeleteCEscolarActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddCEscolar;
    private javax.swing.JButton btnDeleteCEscolar;
    private javax.swing.JButton btnEditCEscolar;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblCOEscolar;
    // End of variables declaration//GEN-END:variables
}
