/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.benaedu.views;
import com.mycompany.benaedu.db.ConDB;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
public class Resumen_Conceptos_Pagados extends javax.swing.JPanel {
private DefaultTableModel modResumen;
    private JTable tblResumen;
    private JTextField[] arrTotales = new JTextField[6];

    private JComboBox<String> cmbCia;
    private JComboBox<String> cmbCC;
    private JComboBox<String> cmbCiclo;
    private JTextField txtCajero;
    private com.toedter.calendar.JDateChooser txtFecIni;
    private com.toedter.calendar.JDateChooser txtFecFin;
    private JCheckBox chkGrado;
    private JRadioButton rbOficial;
    private JRadioButton rbPart;
    private JRadioButton rbAmbos;
    /**
     * Creates new form Resumen_Conceptos_Pagados
     */
    public Resumen_Conceptos_Pagados() {
        initComponents();
        construirInterfazResumenConceptos();
    }
private void construirInterfazResumenConceptos() {
        this.removeAll();
        this.setLayout(null);
        this.setBackground(new java.awt.Color(255, 255, 255));

        // --- BUSCADOR FLOTANTE LOCAL ---
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

        // --- CARGA DE DATOS DESDE LA BD ---
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

        Object[][] dCia    = cargarDatosMultiple.apply("SELECT CIA, NCIA FROM tmcias ORDER BY CIA", 2);
        Object[][] dCC     = cargarDatosMultiple.apply("SELECT CVE, DES1 FROM tgcc WHERE CVE IN ('12100','12200','12300','12400') ORDER BY CVE", 2);
        Object[][] dCiclo  = cargarDatosMultiple.apply("SELECT CESC, CDSC FROM tescesc ORDER BY CESC DESC", 2);
        Object[][] dCajero = cargarDatosMultiple.apply("SELECT t.NEMP, e.NOME FROM tescaj t LEFT JOIN tgemp e ON t.NEMP = e.NEMP ORDER BY t.NEMP", 2);

        // --- 1. DATOS DE SELECCIÓN ---
        this.add(new JLabel("Compañía")).setBounds(20, 20, 70, 25);
        cmbCia = new JComboBox<>();
        for (Object[] r : dCia) cmbCia.addItem((r[0] != null ? r[0] : "") + " - " + (r[1] != null ? r[1] : ""));
        cmbCia.setBounds(90, 20, 180, 25);
        this.add(cmbCia);

        this.add(new JLabel("C. Costos")).setBounds(285, 20, 65, 25);
        cmbCC = new JComboBox<>();
        cmbCC.addItem(""); 
        for (Object[] r : dCC) cmbCC.addItem(r[0] != null ? r[0].toString() : "");
        cmbCC.setEditable(true);
        cmbCC.setBounds(350, 20, 100, 25);
        this.add(cmbCC);

        this.add(new JLabel("Ciclo Escolar")).setBounds(470, 20, 80, 25);
        cmbCiclo = new JComboBox<>();
        cmbCiclo.addItem("");
        for (Object[] r : dCiclo) cmbCiclo.addItem(r[0] != null ? r[0].toString() : "");
        cmbCiclo.setEditable(true);
        cmbCiclo.setBounds(550, 20, 90, 25);
        this.add(cmbCiclo);

        // Fila 2
        this.add(new JLabel("Cajero")).setBounds(20, 60, 60, 25);
        txtCajero = new JTextField(); txtCajero.setBounds(90, 60, 60, 25);
        JButton btnCajero = new JButton("▼"); btnCajero.setFont(new Font("SansSerif", Font.PLAIN, 10));
        btnCajero.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnCajero.setBounds(150, 60, 20, 25);
        buscador.configurar(txtCajero, null, btnCajero, dCajero, new String[]{"Clave", "Nombre"}, new int[]{60, 250});
        this.add(txtCajero); this.add(btnCajero);

        this.add(new JLabel("Fecha Inicial")).setBounds(200, 60, 80, 25);
        txtFecIni = new com.toedter.calendar.JDateChooser();
        txtFecIni.setDateFormatString("dd/MM/yyyy");
        txtFecIni.setDate(new java.util.Date());
        txtFecIni.setBounds(280, 60, 110, 25);
        this.add(txtFecIni);

        this.add(new JLabel("Fecha Final")).setBounds(410, 60, 70, 25);
        txtFecFin = new com.toedter.calendar.JDateChooser();
        txtFecFin.setDateFormatString("dd/MM/yyyy");
        txtFecFin.setDate(new java.util.Date());
        txtFecFin.setBounds(480, 60, 110, 25);
        this.add(txtFecFin);

        // --- 2. ORDEN ---
        JPanel pnlOrden = new JPanel(null);
        pnlOrden.setBackground(new java.awt.Color(255, 255, 255));
        pnlOrden.setBorder(BorderFactory.createTitledBorder("Orden"));
        pnlOrden.setBounds(660, 10, 100, 65);
        
        chkGrado = new JCheckBox("Por Grado");
        chkGrado.setBackground(new java.awt.Color(255, 255, 255));
        chkGrado.setBounds(10, 25, 80, 20);
        pnlOrden.add(chkGrado);
        this.add(pnlOrden);

        // --- 3. TIPO DE CUENTA ---
        JPanel pnlTipoCta = new JPanel(null);
        pnlTipoCta.setBackground(new java.awt.Color(255, 255, 255));
        pnlTipoCta.setBorder(BorderFactory.createTitledBorder("Tipo de Cuenta"));
        pnlTipoCta.setBounds(770, 10, 150, 95);

        rbOficial = new JRadioButton("Oficial", true); rbOficial.setBackground(new java.awt.Color(255, 255, 255)); rbOficial.setBounds(20, 20, 80, 20);
        rbPart = new JRadioButton("Particular"); rbPart.setBackground(new java.awt.Color(255, 255, 255)); rbPart.setBounds(20, 45, 100, 20);
        rbAmbos = new JRadioButton("Ambos"); rbAmbos.setBackground(new java.awt.Color(255, 255, 255)); rbAmbos.setBounds(20, 70, 80, 20);

        ButtonGroup bgTipo = new ButtonGroup();
        bgTipo.add(rbOficial); bgTipo.add(rbPart); bgTipo.add(rbAmbos);
        
        pnlTipoCta.add(rbOficial); pnlTipoCta.add(rbPart); pnlTipoCta.add(rbAmbos);
        this.add(pnlTipoCta);

        // --- BOTÓN FILTRAR ---
        JButton btnFiltra = new JButton("Filtrar información");
        btnFiltra.setBounds(770, 115, 150, 30);
        this.add(btnFiltra);

        // --- 4. TABLA DE RESUMEN DE CONCEPTOS PAGADOS ---
        modResumen = new DefaultTableModel(
            new Object[][]{}, 
            new String[]{"Grado", "Cve", "Concepto", "Cantidad", "Importe", "Becas", "Desc.", "Rec", "Pagado"}
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tblResumen = new JTable(modResumen);
        tblResumen.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
        
        tblResumen.getColumnModel().getColumn(0).setPreferredWidth(60);
        tblResumen.getColumnModel().getColumn(1).setPreferredWidth(60);
        tblResumen.getColumnModel().getColumn(2).setPreferredWidth(230);
        tblResumen.getColumnModel().getColumn(3).setPreferredWidth(80);
        tblResumen.getColumnModel().getColumn(4).setPreferredWidth(85);
        tblResumen.getColumnModel().getColumn(5).setPreferredWidth(85);
        tblResumen.getColumnModel().getColumn(6).setPreferredWidth(85);
        tblResumen.getColumnModel().getColumn(7).setPreferredWidth(85);
        tblResumen.getColumnModel().getColumn(8).setPreferredWidth(85);
        
        JPanel pnlTabla = new JPanel(null);
        pnlTabla.setBackground(new java.awt.Color(255, 255, 255));
        pnlTabla.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Resumen de Conceptos Pagados", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
        pnlTabla.setBounds(10, 150, 910, 310);
        
        JScrollPane scrollResumen = new JScrollPane(tblResumen);
        scrollResumen.setBounds(10, 20, 890, 280);
        pnlTabla.add(scrollResumen);
        this.add(pnlTabla);

        // --- 5. TOTALES INFERIORES ---
        JPanel pnlTotales = new JPanel(null);
        pnlTotales.setBackground(new java.awt.Color(255, 255, 255));
        pnlTotales.setBorder(BorderFactory.createEtchedBorder());
        pnlTotales.setBounds(10, 470, 910, 55);

        JLabel lblTot = new JLabel("Totales de Conceptos");
        lblTot.setBounds(180, 20, 140, 20);
        pnlTotales.add(lblTot);

        String[] lbls = {"Cantidad", "Importe", "Becas", "Desc", "Rec", "Pagado"};
        int startX = 350;
        int gap = 90;

        for (int i = 0; i < lbls.length; i++) {
            JLabel l = new JLabel(lbls[i], SwingUtilities.CENTER);
            l.setBounds(startX + (i * gap), 5, 80, 15);
            JTextField t = new JTextField(i == 0 ? "0" : "0.00");
            t.setBounds(startX + (i * gap), 20, 80, 25);
            t.setHorizontalAlignment(JTextField.RIGHT);
            t.setEditable(false);
            
            pnlTotales.add(l); 
            pnlTotales.add(t);
            arrTotales[i] = t;
        }
        this.add(pnlTotales);

        // --- 6. BOTONES INFERIORES ---
        JButton btnImprimir = new JButton("Imprimir");
        btnImprimir.setBounds(360, 535, 100, 35);
        JButton btnSalir = new JButton("Salir");
        btnSalir.setBounds(480, 535, 100, 35);

        this.add(btnImprimir);
        this.add(btnSalir);

        // --- 7. EVENTOS ---
        btnSalir.addActionListener(e -> {
            this.removeAll();
            this.revalidate();
            this.repaint();
        });

        java.util.function.Function<String, String> extraerCVE = texto -> {
            if (texto == null) return "";
            texto = texto.trim();
            if (texto.isEmpty()) return "";
            int idx = texto.indexOf(" - ");
            return idx > 0 ? texto.substring(0, idx).trim() : texto;
        };

        // Evento Filtrar Información con corrección de fechas (FPAG) y exclusión de cancelados (MCAN)
        btnFiltra.addActionListener(e -> {
            modResumen.setRowCount(0);

            String cia = extraerCVE.apply((String) cmbCia.getSelectedItem());
            String cc = extraerCVE.apply((String) cmbCC.getSelectedItem());
            String ciclo = extraerCVE.apply((String) cmbCiclo.getSelectedItem());
            String cajero = txtCajero.getText().trim();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String fIni = txtFecIni.getDate() != null ? sdf.format(txtFecIni.getDate()) : "";
            String fFin = txtFecFin.getDate() != null ? sdf.format(txtFecFin.getDate()) : "";

            String tipoCtaFiltro = "";
            if (rbOficial.isSelected()) tipoCtaFiltro = "O";
            else if (rbPart.isSelected()) tipoCtaFiltro = "P";

            try (Connection con = new ConDB().Conectar()) {
                if (con != null) {
                    boolean porGrado = chkGrado.isSelected();
                    StringBuilder sql = new StringBuilder(
                        "SELECT " + (porGrado ? "GRADO, " : "") +
                        "NCPTO, DCPTO, COUNT(*) AS CANTIDAD, " +
                        "SUM(IMPMN) AS TOTAL_IMP, " +
                        "SUM(IBECMN) AS TOTAL_BEC, " +
                        "SUM(IDSCMN) AS TOTAL_DSC, " +
                        "SUM(IRECMN) AS TOTAL_REC, " +
                        "SUM(IPAGMN) AS TOTAL_PAG " +
                        "FROM tesralu WHERE CIA = ? AND IPAGMN > 0 AND (MCAN IS NULL OR MCAN = '') "
                    );

                    if (!cc.isEmpty()) sql.append(" AND CC = ?");
                    if (!ciclo.isEmpty()) sql.append(" AND CESC = ?");
                    if (!cajero.isEmpty()) sql.append(" AND NCAJ = ?");
                    if (!tipoCtaFiltro.isEmpty()) sql.append(" AND TCONT = ?");
                    if (!fIni.isEmpty() && !fFin.isEmpty()) sql.append(" AND FPAG BETWEEN ? AND ?");

                    if (porGrado) {
                        sql.append(" GROUP BY GRADO, NCPTO, DCPTO ORDER BY GRADO, NCPTO");
                    } else {
                        sql.append(" GROUP BY NCPTO, DCPTO ORDER BY NCPTO");
                    }

                    PreparedStatement ps = con.prepareStatement(sql.toString());
                    int p = 1;
                    ps.setString(p++, cia);
                    if (!cc.isEmpty()) ps.setString(p++, cc);
                    if (!ciclo.isEmpty()) ps.setString(p++, ciclo);
                    if (!cajero.isEmpty()) ps.setString(p++, cajero);
                    if (!tipoCtaFiltro.isEmpty()) ps.setString(p++, tipoCtaFiltro);
                    if (!fIni.isEmpty() && !fFin.isEmpty()) {
                        ps.setString(p++, fIni);
                        ps.setString(p++, fFin);
                    }

                    ResultSet rs = ps.executeQuery();
                    DecimalFormat df = new DecimalFormat("#,##0.00");

                    int totCant = 0; 
                    double totImp = 0, totBec = 0, totDes = 0, totRec = 0, totPag = 0;

                    while (rs.next()) {
                        Object[] fila = new Object[9];
                        fila[0] = porGrado ? (rs.getString("GRADO") != null ? rs.getString("GRADO") : "") : "";
                        fila[1] = rs.getString("NCPTO");
                        fila[2] = rs.getString("DCPTO");

                        int cant = rs.getInt("CANTIDAD");
                        double imp = rs.getDouble("TOTAL_IMP");
                        double bec = rs.getDouble("TOTAL_BEC");
                        double dsc = rs.getDouble("TOTAL_DSC");
                        double rec = rs.getDouble("TOTAL_REC");
                        double pag = rs.getDouble("TOTAL_PAG");

                        fila[3] = cant;
                        fila[4] = df.format(imp);
                        fila[5] = df.format(bec);
                        fila[6] = df.format(dsc);
                        fila[7] = df.format(rec);
                        fila[8] = df.format(pag);

                        totCant += cant;
                        totImp += imp;
                        totBec += bec;
                        totDes += dsc;
                        totRec += rec;
                        totPag += pag;

                        modResumen.addRow(fila);
                    }

                    rs.close(); ps.close();

                    arrTotales[0].setText(String.valueOf(totCant));
                    arrTotales[1].setText(df.format(totImp));
                    arrTotales[2].setText(df.format(totBec));
                    arrTotales[3].setText(df.format(totDes));
                    arrTotales[4].setText(df.format(totRec));
                    arrTotales[5].setText(df.format(totPag));

                    if (modResumen.getRowCount() == 0) {
                        JOptionPane.showMessageDialog(this, "No se encontraron conceptos pagados con los criterios de búsqueda.", "Información", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al consultar resumen de conceptos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Evento Imprimir
        btnImprimir.addActionListener(e -> {
            if (tblResumen.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No hay datos para imprimir.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                PrinterJob job = PrinterJob.getPrinterJob();
                job.setJobName("Resumen de Conceptos Pagados");

                job.setPrintable((g, pf, pageIndex) -> {
                    int filasPorPagina = 28;
                    int totalPaginas = (int) Math.ceil((double) modResumen.getRowCount() / filasPorPagina);
                    if (totalPaginas == 0) totalPaginas = 1;

                    if (pageIndex >= totalPaginas) return Printable.NO_SUCH_PAGE;

                    Graphics2D g2d = (Graphics2D) g;
                    g2d.translate(pf.getImageableX(), pf.getImageableY());

                    int y = 40;
                    g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
                    g2d.drawString("UNIDAD ESCOLAR BENAVENTE, A.C.", 40, y); y += 20;
                    g2d.drawString("RESUMEN DE CONCEPTOS PAGADOS", 40, y); y += 15;
                    g2d.drawLine(40, y, 530, y); y += 15;

                    g2d.setFont(new Font("SansSerif", Font.BOLD, 8));
                    g2d.drawString("GRD", 40, y);
                    g2d.drawString("CVE", 70, y);
                    g2d.drawString("CONCEPTO", 100, y);
                    g2d.drawString("CANT", 260, y);
                    g2d.drawString("IMPORTE", 310, y);
                    g2d.drawString("BECAS", 380, y);
                    g2d.drawString("PAGADO", 460, y);
                    y += 5;
                    g2d.drawLine(40, y, 530, y); y += 12;

                    g2d.setFont(new Font("SansSerif", Font.PLAIN, 7));
                    int inicioRow = pageIndex * filasPorPagina;
                    int finRow = Math.min(inicioRow + filasPorPagina, modResumen.getRowCount());

                    for (int r = inicioRow; r < finRow; r++) {
                        g2d.drawString(modResumen.getValueAt(r, 0).toString(), 40, y);
                        g2d.drawString(modResumen.getValueAt(r, 1).toString(), 70, y);

                        String desc = modResumen.getValueAt(r, 2) != null ? modResumen.getValueAt(r, 2).toString() : "";
                        if (desc.length() > 24) desc = desc.substring(0, 21) + "...";
                        g2d.drawString(desc, 100, y);

                        g2d.drawString(modResumen.getValueAt(r, 3).toString(), 260, y);
                        g2d.drawString(modResumen.getValueAt(r, 4).toString(), 310, y);
                        g2d.drawString(modResumen.getValueAt(r, 5).toString(), 380, y);
                        g2d.drawString(modResumen.getValueAt(r, 8).toString(), 460, y);
                        y += 12;
                    }

                    if (pageIndex == totalPaginas - 1) {
                        y += 10;
                        g2d.drawLine(40, y, 530, y); y += 15;
                        g2d.setFont(new Font("SansSerif", Font.BOLD, 8));
                        g2d.drawString("CANTIDAD TOTAL: " + arrTotales[0].getText(), 40, y);
                        g2d.drawString("TOTAL IMPORTE: $" + arrTotales[1].getText(), 180, y);
                        g2d.drawString("TOTAL PAGADO: $" + arrTotales[5].getText(), 380, y);
                    }

                    g2d.drawString("Página " + (pageIndex + 1) + " de " + totalPaginas, 450, 750);
                    return Printable.PAGE_EXISTS;
                });

                if (job.printDialog()) {
                    job.print();
                    JOptionPane.showMessageDialog(this, "Resumen enviado a la impresora correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
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
        tblRCPagados = new javax.swing.JTable();
        btnAddRCPagados = new javax.swing.JButton();
        btnEditRCPagados = new javax.swing.JButton();
        btnDeleteRCPagados = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        tblRCPagados.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tblRCPagados);

        btnAddRCPagados.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAddRCPagados.setForeground(new java.awt.Color(26, 61, 99));
        btnAddRCPagados.setIcon(new javax.swing.ImageIcon(getClass().getResource("/add.png"))); // NOI18N
        btnAddRCPagados.setText("Añadir");
        btnAddRCPagados.addActionListener(this::btnAddRCPagadosActionPerformed);

        btnEditRCPagados.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEditRCPagados.setForeground(new java.awt.Color(26, 61, 99));
        btnEditRCPagados.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edit.png"))); // NOI18N
        btnEditRCPagados.setText("Editar");
        btnEditRCPagados.setMaximumSize(new java.awt.Dimension(93, 31));
        btnEditRCPagados.setMinimumSize(new java.awt.Dimension(93, 31));
        btnEditRCPagados.addActionListener(this::btnEditRCPagadosActionPerformed);

        btnDeleteRCPagados.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDeleteRCPagados.setForeground(new java.awt.Color(26, 61, 99));
        btnDeleteRCPagados.setIcon(new javax.swing.ImageIcon(getClass().getResource("/delete.png"))); // NOI18N
        btnDeleteRCPagados.setText("Eliminar");
        btnDeleteRCPagados.addActionListener(this::btnDeleteRCPagadosActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAddRCPagados)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditRCPagados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteRCPagados)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddRCPagados, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditRCPagados, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteRCPagados, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void btnAddRCPagadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddRCPagadosActionPerformed
    }//GEN-LAST:event_btnAddRCPagadosActionPerformed

    private void btnEditRCPagadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditRCPagadosActionPerformed
    }//GEN-LAST:event_btnEditRCPagadosActionPerformed

    private void btnDeleteRCPagadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteRCPagadosActionPerformed

    }//GEN-LAST:event_btnDeleteRCPagadosActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddRCPagados;
    private javax.swing.JButton btnDeleteRCPagados;
    private javax.swing.JButton btnEditRCPagados;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblRCPagados;
    // End of variables declaration//GEN-END:variables
}
