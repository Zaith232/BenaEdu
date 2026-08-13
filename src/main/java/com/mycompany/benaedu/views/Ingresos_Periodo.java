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
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileWriter;
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
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
 /*
 * @author b17za
 */
public class Ingresos_Periodo extends javax.swing.JPanel {
    private JComboBox<String> cmbCia;
    private JTextField txtCC;
    private JTextField txtCiclo;
    private JTextField txtCajero;
    private JTextField txtFormaPago;
    private JTextField txtReferencia;
    private com.toedter.calendar.JDateChooser txtFecIni;
    private com.toedter.calendar.JDateChooser txtFecFin;

    private JRadioButton rbOficial;
    private JRadioButton rbPart;
    private JRadioButton rbDetalle;
    private JRadioButton rbResumen;

    private JTable tblIngresos;
    private DefaultTableModel modIngresos;

    private JTextField txtTotEfectivo;
    private JTextField txtTotCheques;
    private JTextField txtTotDep;
    private JTextField txtTotTarjetas;
    private JTextField txtTotTElectronica;
    private JTextField txtTotGeneral;

    /**
     * Creates new form Ingresos_Periodo
     */
    public Ingresos_Periodo() {
        initComponents();
        construirInterfazIngresosPeriodo();
    }
private void construirInterfazIngresosPeriodo() {
        this.removeAll();
        this.setLayout(new BorderLayout(10, 10));
        this.setBackground(Color.WHITE);

        // --- CLASE LOCAL BUSCADOR FLOTANTE ---
        class BuscadorFlotante {
            void configurar(JTextField txtClave, JTextField txtDesc, JButton boton, Object[][] datos, String[] columnas, int[] anchos) {
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
                                popup.setVisible(false);
                            }
                        }
                    });

                    int widthTotal = 0; for (int w : anchos) widthTotal += w;
                    JScrollPane scroll = new JScrollPane(tabla);
                    scroll.setPreferredSize(new Dimension(widthTotal + 20, 140));
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

        // Carga de arreglos de catálogos desde la base de datos
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
            } catch (Exception ignore) {}
            return lista.toArray(new Object[0][0]);
        };

        Object[][] dCia       = cargarDatosMultiple.apply("SELECT CIA, NCIA FROM tmcias ORDER BY CIA", 2);
        Object[][] dCC        = cargarDatosMultiple.apply("SELECT CVE, DES1 FROM tgcc WHERE CVE IN ('12100','12200','12300','12400') ORDER BY CVE", 2);
        Object[][] dCiclo     = cargarDatosMultiple.apply("SELECT CESC, CDSC FROM tescesc ORDER BY CESC DESC", 2);
        Object[][] dCajero    = cargarDatosMultiple.apply("SELECT NEMP, NOME FROM tgemp WHERE CAJ = 'S' ORDER BY NEMP", 2);
        Object[][] dFormaPago = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'IPAG' ORDER BY CVE", 2);

        // --- PANEL NORTE (SELECCIÓN Y FILTROS) ---
        JPanel pnlNorte = new JPanel(null);
        pnlNorte.setPreferredSize(new Dimension(940, 105));
        pnlNorte.setBackground(Color.WHITE);

        JPanel pnlSel = new JPanel(null);
        pnlSel.setBackground(Color.WHITE);
        pnlSel.setBorder(BorderFactory.createTitledBorder("Datos de selección"));
        pnlSel.setBounds(10, 5, 700, 95);

        pnlSel.add(new JLabel("Compañía")).setBounds(15, 20, 70, 25);
        cmbCia = new JComboBox<>();
        for (Object[] r : dCia) cmbCia.addItem(r[0] != null ? r[0].toString() : "");
        cmbCia.setBounds(90, 20, 70, 25);
        pnlSel.add(cmbCia);

        pnlSel.add(new JLabel("C. Costos")).setBounds(175, 20, 70, 25);
        txtCC = new JTextField("12100"); txtCC.setBounds(245, 20, 55, 25);
        JButton btnCC = new JButton("▼"); btnCC.setFont(new Font("SansSerif", Font.PLAIN, 10)); btnCC.setMargin(new java.awt.Insets(0,0,0,0)); btnCC.setBounds(300, 20, 20, 25);
        buscador.configurar(txtCC, null, btnCC, dCC, new String[]{"Clave", "Descripción"}, new int[]{60, 250});
        pnlSel.add(txtCC); pnlSel.add(btnCC);

        pnlSel.add(new JLabel("Ciclo Escolar")).setBounds(335, 20, 80, 25);
        txtCiclo = new JTextField("2526"); txtCiclo.setBounds(420, 20, 55, 25);
        JButton btnCiclo = new JButton("▼"); btnCiclo.setFont(new Font("SansSerif", Font.PLAIN, 10)); btnCiclo.setMargin(new java.awt.Insets(0,0,0,0)); btnCiclo.setBounds(475, 20, 20, 25);
        buscador.configurar(txtCiclo, null, btnCiclo, dCiclo, new String[]{"Clave", "Descripción"}, new int[]{60, 150});
        pnlSel.add(txtCiclo); pnlSel.add(btnCiclo);

        pnlSel.add(new JLabel("Cajero")).setBounds(515, 20, 50, 25);
        txtCajero = new JTextField(); txtCajero.setBounds(565, 20, 55, 25);
        JButton btnCajero = new JButton("▼"); btnCajero.setFont(new Font("SansSerif", Font.PLAIN, 10)); btnCajero.setMargin(new java.awt.Insets(0,0,0,0)); btnCajero.setBounds(620, 20, 20, 25);
        buscador.configurar(txtCajero, null, btnCajero, dCajero, new String[]{"Clave", "Nombre"}, new int[]{60, 200});
        pnlSel.add(txtCajero); pnlSel.add(btnCajero);

        pnlSel.add(new JLabel("Forma pago")).setBounds(15, 55, 70, 25);
        txtFormaPago = new JTextField(); txtFormaPago.setBounds(90, 55, 45, 25);
        JButton btnFormaPago = new JButton("▼"); btnFormaPago.setFont(new Font("SansSerif", Font.PLAIN, 10)); btnFormaPago.setMargin(new java.awt.Insets(0,0,0,0)); btnFormaPago.setBounds(135, 55, 20, 25);
        buscador.configurar(txtFormaPago, null, btnFormaPago, dFormaPago, new String[]{"Clave", "Descripción"}, new int[]{50, 200});
        pnlSel.add(txtFormaPago); pnlSel.add(btnFormaPago);

        pnlSel.add(new JLabel("Fecha Inicial")).setBounds(175, 55, 80, 25);
        txtFecIni = new com.toedter.calendar.JDateChooser();
        txtFecIni.setDateFormatString("dd/MM/yyyy"); txtFecIni.setDate(new Date());
        txtFecIni.setBounds(245, 55, 105, 25);
        pnlSel.add(txtFecIni);

        pnlSel.add(new JLabel("Fecha Final")).setBounds(360, 55, 70, 25);
        txtFecFin = new com.toedter.calendar.JDateChooser();
        txtFecFin.setDateFormatString("dd/MM/yyyy"); txtFecFin.setDate(new Date());
        txtFecFin.setBounds(420, 55, 105, 25);
        pnlSel.add(txtFecFin);

        pnlSel.add(new JLabel("Referencia")).setBounds(535, 55, 65, 25);
        txtReferencia = new JTextField(); txtReferencia.setBounds(600, 55, 85, 25);
        pnlSel.add(txtReferencia);

        pnlNorte.add(pnlSel);

        // Panel Opción / Filtro
        JPanel pnlOpciones = new JPanel(null);
        pnlOpciones.setBackground(Color.WHITE);
        pnlOpciones.setBorder(BorderFactory.createTitledBorder("Tipo de Cuenta"));
        pnlOpciones.setBounds(720, 5, 210, 95);

        rbOficial = new JRadioButton("Oficial", true); rbOficial.setBackground(Color.WHITE); rbOficial.setBounds(15, 18, 75, 20);
        rbPart = new JRadioButton("Particular"); rbPart.setBackground(Color.WHITE); rbPart.setBounds(100, 18, 90, 20);
        ButtonGroup bgCta = new ButtonGroup(); bgCta.add(rbOficial); bgCta.add(rbPart);

        rbDetalle = new JRadioButton("Detalle", true); rbDetalle.setBackground(Color.WHITE); rbDetalle.setBounds(15, 40, 75, 20);
        rbResumen = new JRadioButton("Resumen"); rbResumen.setBackground(Color.WHITE); rbResumen.setBounds(100, 40, 90, 20);
        ButtonGroup bgRep = new ButtonGroup(); bgRep.add(rbDetalle); bgRep.add(rbResumen);

        JButton btnFiltra = new JButton("Filtra Información");
        btnFiltra.setBounds(10, 63, 190, 25);

        pnlOpciones.add(rbOficial); pnlOpciones.add(rbPart);
        pnlOpciones.add(rbDetalle); pnlOpciones.add(rbResumen);
        pnlOpciones.add(btnFiltra);
        pnlNorte.add(pnlOpciones);

        this.add(pnlNorte, BorderLayout.NORTH);

        // --- TABLA DE INGRESOS (CENTRO) ---
        modIngresos = new DefaultTableModel(
            new Object[][]{}, 
            new String[]{"Compañía", "C. Costos", "Ciclo", "Matrícula", "Nombre", "Grado", "Num Recibo", "Tipo", "Fec Recibo", "Fma Pago", "Banco", "Cta Pago", "Referencia", "Fec Pol", "Importe"}
        ) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        tblIngresos = new JTable(modIngresos);
        tblIngresos.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
        tblIngresos.getColumnModel().getColumn(4).setPreferredWidth(210);
        tblIngresos.getColumnModel().getColumn(8).setPreferredWidth(80);
        tblIngresos.getColumnModel().getColumn(12).setPreferredWidth(100);
        tblIngresos.getColumnModel().getColumn(14).setPreferredWidth(90);

        JScrollPane scrollIngresos = new JScrollPane(tblIngresos);
        scrollIngresos.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Ingresos por Periodo", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));

        this.add(scrollIngresos, BorderLayout.CENTER);

        // --- PANEL SUR (TOTALES Y ACCIONES) ---
        JPanel pnlSur = new JPanel(new BorderLayout(5, 5));
        pnlSur.setBackground(Color.WHITE);

        JPanel pnlTotales = new JPanel(null);
        pnlTotales.setPreferredSize(new Dimension(940, 65));
        pnlTotales.setBackground(Color.WHITE);
        pnlTotales.setBorder(BorderFactory.createTitledBorder("Totales"));

        txtTotEfectivo = new JTextField("0.00");
        txtTotCheques = new JTextField("0.00");
        txtTotDep = new JTextField("0.00");
        txtTotTarjetas = new JTextField("0.00");
        txtTotTElectronica = new JTextField("0.00");
        txtTotGeneral = new JTextField("0.00");

        JTextField[] txtsTot = {txtTotEfectivo, txtTotCheques, txtTotDep, txtTotTarjetas, txtTotTElectronica, txtTotGeneral};
        String[] lbls = {"Efectivo", "Cheques", "Dep. Bancario", "Tarjetas", "T. Electronica", "Total"};

        int startX = 140;
        for (int i = 0; i < lbls.length; i++) {
            JLabel l = new JLabel(lbls[i], SwingConstants.CENTER);
            l.setBounds(startX, 15, 110, 18);
            
            txtsTot[i].setBounds(startX, 33, 110, 24);
            txtsTot[i].setHorizontalAlignment(JTextField.RIGHT);
            txtsTot[i].setEditable(false);
            
            pnlTotales.add(l); pnlTotales.add(txtsTot[i]);
            startX += 125;
        }
        pnlSur.add(pnlTotales, BorderLayout.NORTH);

        JPanel pnlBotones = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 15, 10));
        pnlBotones.setBackground(Color.WHITE);

        JButton btnExportar = new JButton("Exportar CSV"); btnExportar.setPreferredSize(new Dimension(120, 35));
        JButton btnImprimir = new JButton("Imprimir"); btnImprimir.setPreferredSize(new Dimension(110, 35));
        JButton btnSalir = new JButton("Salir"); btnSalir.setPreferredSize(new Dimension(110, 35));

        pnlBotones.add(btnExportar); pnlBotones.add(btnImprimir); pnlBotones.add(btnSalir);
        pnlSur.add(pnlBotones, BorderLayout.SOUTH);

        this.add(pnlSur, BorderLayout.SOUTH);

        // --- EVENTOS DE LA VISTA ---
        btnSalir.addActionListener(e -> {
            this.removeAll();
            this.revalidate();
            this.repaint();
        });

        btnFiltra.addActionListener(e -> {
            modIngresos.setRowCount(0);

            String cia = cmbCia.getSelectedItem() != null ? cmbCia.getSelectedItem().toString() : "12";
            String cc = txtCC.getText().trim();
            String ciclo = txtCiclo.getText().trim();
            String cajero = txtCajero.getText().trim();
            String fmaPagoFiltro = txtFormaPago.getText().trim();
            String refFiltro = txtReferencia.getText().trim();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String fIni = txtFecIni.getDate() != null ? sdf.format(txtFecIni.getDate()) : "";
            String fFin = txtFecFin.getDate() != null ? sdf.format(txtFecFin.getDate()) : "";

            try (Connection con = new ConDB().Conectar()) {
                if (con != null) {
                    StringBuilder sql = new StringBuilder(
                        "SELECT p.CIA, p.CC, p.CESC, p.MAT, COALESCE(a.NOMCOM, r.NOMALU) AS NOMBRE, r.GRADO, " +
                        "p.NREC, p.TREC, p.FPAG AS FEC_RECIBO, p.FMAPAG, p.BCOPAG, p.CTAPAG, p.REFPAG, " +
                        "pol.FPOL AS FEC_POLIZA, p.IMPMN " +
                        "FROM tespalu p " +
                        "LEFT JOIN tesralu r ON p.CIA = r.CIA AND p.CC = r.CC AND p.MAT = r.MAT AND p.NREC = r.NREC AND p.TREC = r.TREC AND p.CESC = r.CESC " +
                        "LEFT JOIN tesalum a ON p.MAT = a.MAT " +
                        "LEFT JOIN tgpol pol ON p.CIA = pol.CIA AND p.RELPOL = pol.NPOL " +
                        "WHERE p.CIA = ? "
                    );

                    if (!cc.isEmpty()) sql.append(" AND p.CC = ?");
                    if (!ciclo.isEmpty()) sql.append(" AND p.CESC = ?");
                    if (!cajero.isEmpty()) sql.append(" AND p.NCAJ = ?");
                    if (!fmaPagoFiltro.isEmpty()) sql.append(" AND p.FMAPAG = ?");
                    if (!refFiltro.isEmpty()) sql.append(" AND p.REFPAG LIKE ?");
                    if (!fIni.isEmpty() && !fFin.isEmpty()) sql.append(" AND p.FPAG BETWEEN ? AND ?");

                    sql.append(" ORDER BY p.FPAG DESC, p.NREC DESC");

                    PreparedStatement ps = con.prepareStatement(sql.toString());
                    int idx = 1;
                    ps.setString(idx++, cia);
                    if (!cc.isEmpty()) ps.setString(idx++, cc);
                    if (!ciclo.isEmpty()) ps.setString(idx++, ciclo);
                    if (!cajero.isEmpty()) ps.setString(idx++, cajero);
                    if (!fmaPagoFiltro.isEmpty()) ps.setString(idx++, fmaPagoFiltro);
                    if (!refFiltro.isEmpty()) ps.setString(idx++, "%" + refFiltro + "%");
                    if (!fIni.isEmpty() && !fFin.isEmpty()) {
                        ps.setString(idx++, fIni);
                        ps.setString(idx++, fFin);
                    }

                    ResultSet rs = ps.executeQuery();
                    DecimalFormat df = new DecimalFormat("#,##0.00");

                    double sumEf = 0, sumCh = 0, sumDep = 0, sumTar = 0, sumTE = 0, sumGen = 0;

                    while (rs.next()) {
                        double imp = rs.getDouble("IMPMN");
                        String fma = rs.getString("FMAPAG") != null ? rs.getString("FMAPAG").trim().toUpperCase() : "";

                        Object[] fila = new Object[15];
                        fila[0] = rs.getString("CIA");
                        fila[1] = rs.getString("CC");
                        fila[2] = rs.getString("CESC");
                        fila[3] = rs.getString("MAT");
                        fila[4] = rs.getString("NOMBRE");
                        fila[5] = rs.getString("GRADO");
                        fila[6] = rs.getString("NREC");
                        fila[7] = rs.getString("TREC");
                        fila[8] = rs.getString("FEC_RECIBO");
                        fila[9] = fma;
                        fila[10] = rs.getString("BCOPAG");
                        fila[11] = rs.getString("CTAPAG");
                        fila[12] = rs.getString("REFPAG");
                        fila[13] = rs.getString("FEC_POLIZA") != null ? rs.getString("FEC_POLIZA") : "";
                        fila[14] = df.format(imp);

                        if ("EF".equals(fma) || "E".equals(fma)) sumEf += imp;
                        else if ("CH".equals(fma) || "C".equals(fma)) sumCh += imp;
                        else if ("DB".equals(fma) || "DP".equals(fma) || "D".equals(fma)) sumDep += imp;
                        else if ("TC".equals(fma) || "TD".equals(fma) || "T".equals(fma)) sumTar += imp;
                        else if ("TE".equals(fma) || "TR".equals(fma)) sumTE += imp;
                        else sumEf += imp;

                        sumGen += imp;
                        modIngresos.addRow(fila);
                    }

                    rs.close(); ps.close();

                    txtTotEfectivo.setText(df.format(sumEf));
                    txtTotCheques.setText(df.format(sumCh));
                    txtTotDep.setText(df.format(sumDep));
                    txtTotTarjetas.setText(df.format(sumTar));
                    txtTotTElectronica.setText(df.format(sumTE));
                    txtTotGeneral.setText(df.format(sumGen));

                    if (modIngresos.getRowCount() == 0) {
                        JOptionPane.showMessageDialog(this, "No se encontraron ingresos con los criterios especificados.", "Información", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al consultar ingresos por período: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnExportar.addActionListener(e -> {
            if (modIngresos.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No hay ingresos cargados para exportar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("Ingresos_Por_Periodo.csv"));
            int sel = chooser.showSaveDialog(this);

            if (sel == JFileChooser.APPROVE_OPTION) {
                try (FileWriter writer = new FileWriter(chooser.getSelectedFile())) {
                    for (int i = 0; i < modIngresos.getColumnCount(); i++) {
                        writer.write(modIngresos.getColumnName(i) + (i == modIngresos.getColumnCount() - 1 ? "" : ","));
                    }
                    writer.write("\n");

                    for (int r = 0; r < modIngresos.getRowCount(); r++) {
                        for (int c = 0; c < modIngresos.getColumnCount(); c++) {
                            Object val = modIngresos.getValueAt(r, c);
                            writer.write((val != null ? val.toString().replace(",", "") : "") + (c == modIngresos.getColumnCount() - 1 ? "" : ","));
                        }
                        writer.write("\n");
                    }

                    JOptionPane.showMessageDialog(this, "Reporte exportado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al exportar reporte: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnImprimir.addActionListener(e -> {
            if (modIngresos.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No hay datos para imprimir.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                PrinterJob job = PrinterJob.getPrinterJob();
                job.setJobName("Reporte de Ingresos por Período");

                job.setPrintable((g, pf, pageIndex) -> {
                    int filasPorPagina = 28;
                    int totalPaginas = (int) Math.ceil((double) modIngresos.getRowCount() / filasPorPagina);
                    if (totalPaginas == 0) totalPaginas = 1;

                    if (pageIndex >= totalPaginas) return Printable.NO_SUCH_PAGE;

                    Graphics2D g2d = (Graphics2D) g;
                    g2d.translate(pf.getImageableX(), pf.getImageableY());

                    int y = 40;
                    g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
                    g2d.drawString("UNIDAD ESCOLAR BENAVENTE, A.C.", 40, y); y += 20;
                    g2d.drawString("REPORTE DE INGRESOS POR PERÍODO", 40, y); y += 15;
                    g2d.drawLine(40, y, 530, y); y += 15;

                    g2d.setFont(new Font("SansSerif", Font.BOLD, 8));
                    g2d.drawString("RECIBO", 40, y);
                    g2d.drawString("FECHA", 90, y);
                    g2d.drawString("MATRÍCULA", 140, y);
                    g2d.drawString("ALUMNO", 200, y);
                    g2d.drawString("FORMA PAGO", 360, y);
                    g2d.drawString("IMPORTE", 470, y);
                    y += 5;
                    g2d.drawLine(40, y, 530, y); y += 12;

                    g2d.setFont(new Font("SansSerif", Font.PLAIN, 7));
                    int inicioRow = pageIndex * filasPorPagina;
                    int finRow = Math.min(inicioRow + filasPorPagina, modIngresos.getRowCount());

                    for (int r = inicioRow; r < finRow; r++) {
                        g2d.drawString(modIngresos.getValueAt(r, 6).toString(), 40, y);
                        g2d.drawString(modIngresos.getValueAt(r, 8).toString(), 90, y);
                        g2d.drawString(modIngresos.getValueAt(r, 3).toString(), 140, y);

                        String nom = modIngresos.getValueAt(r, 4) != null ? modIngresos.getValueAt(r, 4).toString() : "";
                        if (nom.length() > 24) nom = nom.substring(0, 21) + "...";
                        g2d.drawString(nom, 200, y);

                        g2d.drawString(modIngresos.getValueAt(r, 9).toString(), 360, y);
                        g2d.drawString(modIngresos.getValueAt(r, 14).toString(), 470, y);
                        y += 12;
                    }

                    if (pageIndex == totalPaginas - 1) {
                        y += 10;
                        g2d.drawLine(40, y, 530, y); y += 15;
                        g2d.setFont(new Font("SansSerif", Font.BOLD, 8));
                        g2d.drawString("EFECTIVO: $" + txtTotEfectivo.getText(), 40, y);
                        g2d.drawString("CHEQUES: $" + txtTotCheques.getText(), 150, y);
                        g2d.drawString("TARJETAS: $" + txtTotTarjetas.getText(), 260, y);
                        g2d.drawString("TOTAL: $" + txtTotGeneral.getText(), 420, y);
                    }

                    g2d.drawString("Página " + (pageIndex + 1) + " de " + totalPaginas, 450, 750);
                    return Printable.PAGE_EXISTS;
                });

                if (job.printDialog()) {
                    job.print();
                    JOptionPane.showMessageDialog(this, "Reporte de ingresos enviado a la impresora.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al imprimir: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        tblIPeriodo = new javax.swing.JTable();
        btnAddIPeriodo = new javax.swing.JButton();
        btnEditIPeriodo = new javax.swing.JButton();
        btnDeleteIPeriodo = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        tblIPeriodo.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tblIPeriodo);

        btnAddIPeriodo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAddIPeriodo.setForeground(new java.awt.Color(26, 61, 99));
        btnAddIPeriodo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/add.png"))); // NOI18N
        btnAddIPeriodo.setText("Añadir");
        btnAddIPeriodo.addActionListener(this::btnAddIPeriodoActionPerformed);

        btnEditIPeriodo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEditIPeriodo.setForeground(new java.awt.Color(26, 61, 99));
        btnEditIPeriodo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edit.png"))); // NOI18N
        btnEditIPeriodo.setText("Editar");
        btnEditIPeriodo.setMaximumSize(new java.awt.Dimension(93, 31));
        btnEditIPeriodo.setMinimumSize(new java.awt.Dimension(93, 31));
        btnEditIPeriodo.addActionListener(this::btnEditIPeriodoActionPerformed);

        btnDeleteIPeriodo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDeleteIPeriodo.setForeground(new java.awt.Color(26, 61, 99));
        btnDeleteIPeriodo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/delete.png"))); // NOI18N
        btnDeleteIPeriodo.setText("Eliminar");
        btnDeleteIPeriodo.addActionListener(this::btnDeleteIPeriodoActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAddIPeriodo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditIPeriodo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteIPeriodo)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddIPeriodo, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditIPeriodo, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteIPeriodo, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void btnAddIPeriodoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddIPeriodoActionPerformed
    }//GEN-LAST:event_btnAddIPeriodoActionPerformed

    private void btnEditIPeriodoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditIPeriodoActionPerformed
    }//GEN-LAST:event_btnEditIPeriodoActionPerformed

    private void btnDeleteIPeriodoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteIPeriodoActionPerformed
    }//GEN-LAST:event_btnDeleteIPeriodoActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddIPeriodo;
    private javax.swing.JButton btnDeleteIPeriodo;
    private javax.swing.JButton btnEditIPeriodo;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblIPeriodo;
    // End of variables declaration//GEN-END:variables
}
