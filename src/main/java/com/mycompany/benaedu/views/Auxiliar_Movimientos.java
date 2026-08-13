/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.benaedu.views;
import com.mycompany.benaedu.db.ConDB;
import java.awt.Window;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
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
public class Auxiliar_Movimientos extends javax.swing.JPanel {

    /**
     * Creates new form Auxiliar_Movimientos
     */
    public Auxiliar_Movimientos() {
        initComponents();
        construirInterfazAuxiliar();
    }
private void construirInterfazAuxiliar() {
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

        Object[][] dCta     = cargarDatosMultiple.apply("SELECT CCTA, CDES FROM tmctas ORDER BY CCTA", 2);
        Object[][] dMoneda  = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'TMON' ORDER BY CVE", 2);
        Object[][] dContab  = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'TCONT' ORDER BY CVE", 2);
        Object[][] dMov     = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'TPOL' ORDER BY CVE", 2);
        Object[][] dTssc    = cargarDatosMultiple.apply("SELECT CVE, REL FROM tmclas WHERE TBL = 'TSSC' ORDER BY CVE", 2);

        // --- PANEL DE FILTROS ---
        JPanel pnlSel = new JPanel(null);
        pnlSel.setBorder(BorderFactory.createEtchedBorder());
        pnlSel.setBounds(10, 10, 915, 140);

        pnlSel.add(new JLabel("Compañía")).setBounds(15, 15, 80, 25);
        JComboBox<String> cmbCia = new JComboBox<>(new String[]{"12"});
        cmbCia.setBounds(105, 15, 60, 25);
        pnlSel.add(cmbCia);
        pnlSel.add(new JLabel("UNIDAD ESCOLAR BENAVENTE, A.C.")).setBounds(175, 15, 250, 25);

        pnlSel.add(new JLabel("Cuenta Inicial")).setBounds(15, 45, 80, 25);
        JTextField txtCtaIni = new JTextField(); txtCtaIni.setBounds(105, 45, 120, 25);
        JButton btnCtaIni = new JButton("▼"); btnCtaIni.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnCtaIni.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnCtaIni.setBounds(225, 45, 20, 25);
        buscador.configurar(txtCtaIni, null, btnCtaIni, dCta, new String[]{"Clave", "Descripción"}, new int[]{110, 250});
        pnlSel.add(txtCtaIni); pnlSel.add(btnCtaIni);

        pnlSel.add(new JLabel("Cuenta Final")).setBounds(15, 75, 80, 25);
        JTextField txtCtaFin = new JTextField(); txtCtaFin.setBounds(105, 75, 120, 25);
        JButton btnCtaFin = new JButton("▼"); btnCtaFin.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnCtaFin.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnCtaFin.setBounds(225, 75, 20, 25);
        buscador.configurar(txtCtaFin, null, btnCtaFin, dCta, new String[]{"Clave", "Descripción"}, new int[]{110, 250});
        pnlSel.add(txtCtaFin); pnlSel.add(btnCtaFin);

        pnlSel.add(new JLabel("Fecha Inicial")).setBounds(15, 105, 80, 25);
        com.toedter.calendar.JDateChooser txtFecIni = new com.toedter.calendar.JDateChooser();
        txtFecIni.setDateFormatString("dd/MM/yyyy"); txtFecIni.setBounds(105, 105, 110, 25);
        pnlSel.add(txtFecIni);

        pnlSel.add(new JLabel("Fecha Final")).setBounds(225, 105, 70, 25);
        com.toedter.calendar.JDateChooser txtFecFin = new com.toedter.calendar.JDateChooser();
        txtFecFin.setDateFormatString("dd/MM/yyyy"); txtFecFin.setBounds(295, 105, 110, 25);
        pnlSel.add(txtFecFin);

        JCheckBox chkTodosCC = new JCheckBox("Todos los Centros de Costos");
        chkTodosCC.setBounds(460, 15, 200, 25);
        pnlSel.add(chkTodosCC);

        pnlSel.add(new JLabel("Tipo de Moneda")).setBounds(460, 45, 100, 25);
        JTextField txtMoneda = new JTextField(); txtMoneda.setBounds(570, 45, 60, 25);
        JButton btnMoneda = new JButton("▼"); btnMoneda.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnMoneda.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnMoneda.setBounds(630, 45, 20, 25);
        buscador.configurar(txtMoneda, null, btnMoneda, dMoneda, new String[]{"Clave", "Descripción"}, new int[]{60, 150});
        pnlSel.add(txtMoneda); pnlSel.add(btnMoneda);

        pnlSel.add(new JLabel("Tipo Contab.")).setBounds(460, 75, 100, 25);
        JTextField txtContab = new JTextField(); txtContab.setBounds(570, 75, 60, 25);
        JButton btnContab = new JButton("▼"); btnContab.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnContab.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnContab.setBounds(630, 75, 20, 25);
        buscador.configurar(txtContab, null, btnContab, dContab, new String[]{"Clave", "Descripción"}, new int[]{60, 200});
        pnlSel.add(txtContab); pnlSel.add(btnContab);

        pnlSel.add(new JLabel("Tipo de Movimiento")).setBounds(460, 105, 110, 25);
        JTextField txtMov = new JTextField(); txtMov.setBounds(570, 105, 60, 25);
        JButton btnMov = new JButton("▼"); btnMov.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnMov.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnMov.setBounds(630, 105, 20, 25);
        buscador.configurar(txtMov, null, btnMov, dMov, new String[]{"Clave", "Descripción"}, new int[]{60, 200});
        pnlSel.add(txtMov); pnlSel.add(btnMov);

        JPanel pnlSub = new JPanel(null);
        pnlSub.setBorder(BorderFactory.createTitledBorder("Sub-Subcuenta"));
        pnlSub.setBounds(675, 10, 225, 85);
        
        pnlSub.add(new JLabel("Tipo")).setBounds(15, 20, 40, 25);
        JTextField txtSubTipo = new JTextField(); txtSubTipo.setBounds(60, 20, 60, 25);
        JButton btnSubTipo = new JButton("▼"); btnSubTipo.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnSubTipo.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnSubTipo.setBounds(120, 20, 20, 25);
        buscador.configurar(txtSubTipo, null, btnSubTipo, dTssc, new String[]{"Clave", "Descripción"}, new int[]{60, 120});
        pnlSub.add(txtSubTipo); pnlSub.add(btnSubTipo);
        
        pnlSub.add(new JLabel("Número")).setBounds(15, 50, 50, 25);
        JTextField txtSubNum = new JTextField(); txtSubNum.setBounds(60, 50, 80, 25);
        JButton btnSubNum = new JButton("▼"); btnSubNum.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnSubNum.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnSubNum.setBounds(140, 50, 20, 25);
        pnlSub.add(txtSubNum); pnlSub.add(btnSubNum);
        pnlSel.add(pnlSub);

        JButton btnFiltra = new JButton("Filtrar Información");
        btnFiltra.setBounds(675, 100, 225, 30);
        pnlSel.add(btnFiltra);

        this.add(pnlSel);

        // --- TABLA AUXILIAR ---
        DefaultTableModel modAuxiliar = new DefaultTableModel(
            new Object[][]{}, 
            new String[]{"Cuenta", "Fecha", "Tipo", "Póliza", "Concepto", "Referencia", "Cargo", "Abono", "Saldo"}
        ) { @Override public boolean isCellEditable(int r, int c) { return false; } };

        JTable tblAuxiliar = new JTable(modAuxiliar);
        tblAuxiliar.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
        tblAuxiliar.getColumnModel().getColumn(0).setPreferredWidth(120);
        tblAuxiliar.getColumnModel().getColumn(4).setPreferredWidth(220);
        
        JPanel pnlTabla = new JPanel(null);
        pnlTabla.setBorder(BorderFactory.createEtchedBorder());
        pnlTabla.setBounds(10, 160, 915, 390);
        
        JScrollPane scrollAuxiliar = new JScrollPane(tblAuxiliar);
        scrollAuxiliar.setBounds(10, 10, 895, 370);
        pnlTabla.add(scrollAuxiliar);
        this.add(pnlTabla);

        // --- BOTONES INFERIORES ---
        JButton btnExportar = new JButton("Exporta");
        btnExportar.setBounds(360, 560, 100, 35);
        JButton btnImprimir = new JButton("Imprimir");
        btnImprimir.setBounds(480, 560, 100, 35);

        this.add(btnExportar);
        this.add(btnImprimir);

        // --- EVENTOS ---
        btnFiltra.addActionListener(e -> {
            modAuxiliar.setRowCount(0);

            String cia = cmbCia.getSelectedItem() != null ? cmbCia.getSelectedItem().toString() : "12";
            String ctaIni = txtCtaIni.getText().trim();
            String ctaFin = txtCtaFin.getText().trim();
            String moneda = txtMoneda.getText().trim();
            String contab = txtContab.getText().trim();
            String tMov = txtMov.getText().trim();

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            String fIni = txtFecIni.getDate() != null ? sdf.format(txtFecIni.getDate()) : "";
            String fFin = txtFecFin.getDate() != null ? sdf.format(txtFecFin.getDate()) : "";

            try {
                ConDB db = new ConDB();
                Connection con = db.Conectar();
                if (con != null) {
                    StringBuilder sql = new StringBuilder(
                        "SELECT d.CCTA, d.FPOL, d.TPOL, d.NPOL, d.CONC, d.CPAR, d.MTO, d.TCONT, d.CMON " +
                        "FROM tdpol d " +
                        "INNER JOIN tgpol g ON d.CIA = g.CIA AND d.TPOL = g.TPOL AND d.NPOL = g.NPOL AND d.FPOL = g.FPOL " +
                        "WHERE d.CIA = ? "
                    );

                    if (!ctaIni.isEmpty() && !ctaFin.isEmpty()) sql.append(" AND d.CCTA BETWEEN ? AND ? ");
                    else if (!ctaIni.isEmpty()) sql.append(" AND d.CCTA >= ? ");

                    if (!fIni.isEmpty() && !fFin.isEmpty()) sql.append(" AND d.FPOL BETWEEN ? AND ? ");
                    if (!moneda.isEmpty()) sql.append(" AND d.CMON = ? ");
                    if (!contab.isEmpty()) sql.append(" AND d.TCONT = ? ");
                    if (!tMov.isEmpty()) sql.append(" AND d.TPOL = ? ");

                    sql.append(" ORDER BY d.CCTA ASC, d.FPOL ASC, d.NPOL ASC");

                    PreparedStatement ps = con.prepareStatement(sql.toString());
                    int pIdx = 1;
                    ps.setString(pIdx++, cia);

                    if (!ctaIni.isEmpty() && !ctaFin.isEmpty()) {
                        ps.setString(pIdx++, ctaIni);
                        ps.setString(pIdx++, ctaFin);
                    } else if (!ctaIni.isEmpty()) {
                        ps.setString(pIdx++, ctaIni);
                    }

                    if (!fIni.isEmpty() && !fFin.isEmpty()) {
                        ps.setString(pIdx++, fIni);
                        ps.setString(pIdx++, fFin);
                    }
                    if (!moneda.isEmpty()) ps.setString(pIdx++, moneda);
                    if (!contab.isEmpty()) ps.setString(pIdx++, contab);
                    if (!tMov.isEmpty()) ps.setString(pIdx++, tMov);

                    ResultSet rs = ps.executeQuery();
                    java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");

                    double saldoAcumulado = 0.0;
                    String ctaActual = "";

                    while (rs.next()) {
                        String cta = rs.getString("CCTA");
                        if (!cta.equals(ctaActual)) {
                            ctaActual = cta;
                            saldoAcumulado = 0.0;
                        }

                        double mto = rs.getDouble("MTO");
                        String cpar = rs.getString("CPAR") != null ? rs.getString("CPAR") : "";

                        double cargo = 0.0;
                        double abono = 0.0;

                        if ("1".equals(cpar) || mto >= 0) {
                            cargo = Math.abs(mto);
                            saldoAcumulado += cargo;
                        } else {
                            abono = Math.abs(mto);
                            saldoAcumulado -= abono;
                        }

                        Object[] fila = new Object[9];
                        fila[0] = cta;
                        fila[1] = rs.getString("FPOL");
                        fila[2] = rs.getString("TPOL");
                        fila[3] = rs.getString("NPOL");
                        fila[4] = rs.getString("CONC");
                        fila[5] = rs.getString("NPOL");
                        fila[6] = df.format(cargo);
                        fila[7] = df.format(abono);
                        fila[8] = df.format(saldoAcumulado);

                        modAuxiliar.addRow(fila);
                    }

                    rs.close(); ps.close(); db.Cerrar();

                    if (modAuxiliar.getRowCount() == 0) {
                        JOptionPane.showMessageDialog(this, "No se encontraron movimientos con los filtros seleccionados.", "Información", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al consultar auxiliar de movimientos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnExportar.addActionListener(e -> {
            if (modAuxiliar.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No hay datos para exportar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("Auxiliar_Movimientos.csv"));
            int sel = chooser.showSaveDialog(this);

            if (sel == JFileChooser.APPROVE_OPTION) {
                try (FileWriter writer = new FileWriter(chooser.getSelectedFile())) {
                    for (int i = 0; i < modAuxiliar.getColumnCount(); i++) {
                        writer.write(modAuxiliar.getColumnName(i) + (i == modAuxiliar.getColumnCount() - 1 ? "" : ","));
                    }
                    writer.write("\n");

                    for (int r = 0; r < modAuxiliar.getRowCount(); r++) {
                        for (int c = 0; c < modAuxiliar.getColumnCount(); c++) {
                            Object val = modAuxiliar.getValueAt(r, c);
                            writer.write((val != null ? val.toString().replace(",", "") : "") + (c == modAuxiliar.getColumnCount() - 1 ? "" : ","));
                        }
                        writer.write("\n");
                    }

                    JOptionPane.showMessageDialog(this, "Archivo exportado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al exportar archivo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnImprimir.addActionListener(e -> {
            if (modAuxiliar.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No hay datos para imprimir.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
                job.setJobName("Auxiliar de Movimientos");

                job.setPrintable((g, pf, pageIndex) -> {
                    int filasPorPagina = 25;
                    int totalPaginas = (int) Math.ceil((double) modAuxiliar.getRowCount() / filasPorPagina);
                    if (totalPaginas == 0) totalPaginas = 1;

                    if (pageIndex >= totalPaginas) return java.awt.print.Printable.NO_SUCH_PAGE;

                    java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
                    g2d.translate(pf.getImageableX(), pf.getImageableY());

                    int y = 40;
                    g2d.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
                    g2d.drawString("UNIDAD ESCOLAR BENAVENTE, A.C.", 40, y); y += 20;
                    g2d.drawString("AUXILIAR DE MOVIMIENTOS CONTABLES", 40, y); y += 15;
                    g2d.drawLine(40, y, 530, y); y += 15;

                    g2d.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 8));
                    g2d.drawString("CUENTA", 40, y);
                    g2d.drawString("FECHA", 110, y);
                    g2d.drawString("TIPO", 160, y);
                    g2d.drawString("PÓLIZA", 190, y);
                    g2d.drawString("CONCEPTO", 230, y);
                    g2d.drawString("CARGO", 390, y);
                    g2d.drawString("ABONO", 440, y);
                    g2d.drawString("SALDO", 490, y);
                    y += 5;
                    g2d.drawLine(40, y, 530, y); y += 12;

                    g2d.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 7));
                    int inicioRow = pageIndex * filasPorPagina;
                    int finRow = Math.min(inicioRow + filasPorPagina, modAuxiliar.getRowCount());

                    for (int r = inicioRow; r < finRow; r++) {
                        g2d.drawString(modAuxiliar.getValueAt(r, 0).toString(), 40, y);
                        g2d.drawString(modAuxiliar.getValueAt(r, 1).toString(), 110, y);
                        g2d.drawString(modAuxiliar.getValueAt(r, 2).toString(), 160, y);
                        g2d.drawString(modAuxiliar.getValueAt(r, 3).toString(), 190, y);

                        String conc = modAuxiliar.getValueAt(r, 4).toString();
                        if (conc.length() > 25) conc = conc.substring(0, 22) + "...";
                        g2d.drawString(conc, 230, y);

                        g2d.drawString(modAuxiliar.getValueAt(r, 6).toString(), 390, y);
                        g2d.drawString(modAuxiliar.getValueAt(r, 7).toString(), 440, y);
                        g2d.drawString(modAuxiliar.getValueAt(r, 8).toString(), 490, y);
                        y += 12;
                    }

                    g2d.drawString("Página " + (pageIndex + 1) + " de " + totalPaginas, 450, 750);
                    return java.awt.print.Printable.PAGE_EXISTS;
                });

                if (job.printDialog()) {
                    job.print();
                    JOptionPane.showMessageDialog(this, "Auxiliar enviado a la impresora.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
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
        tblAMovimientos = new javax.swing.JTable();
        btnAddAMovimientos = new javax.swing.JButton();
        btnEditAMovimientos = new javax.swing.JButton();
        btnDeleteAMovimientos = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.addContainerListener(new java.awt.event.ContainerAdapter() {
            public void componentRemoved(java.awt.event.ContainerEvent evt) {
                jPanel1ComponentRemoved(evt);
            }
        });

        tblAMovimientos.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tblAMovimientos);

        btnAddAMovimientos.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAddAMovimientos.setForeground(new java.awt.Color(26, 61, 99));
        btnAddAMovimientos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/add.png"))); // NOI18N
        btnAddAMovimientos.setText("Añadir");
        btnAddAMovimientos.addActionListener(this::btnAddAMovimientosActionPerformed);

        btnEditAMovimientos.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEditAMovimientos.setForeground(new java.awt.Color(26, 61, 99));
        btnEditAMovimientos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edit.png"))); // NOI18N
        btnEditAMovimientos.setText("Editar");
        btnEditAMovimientos.setMaximumSize(new java.awt.Dimension(93, 31));
        btnEditAMovimientos.setMinimumSize(new java.awt.Dimension(93, 31));
        btnEditAMovimientos.addActionListener(this::btnEditAMovimientosActionPerformed);

        btnDeleteAMovimientos.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDeleteAMovimientos.setForeground(new java.awt.Color(26, 61, 99));
        btnDeleteAMovimientos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/delete.png"))); // NOI18N
        btnDeleteAMovimientos.setText("Eliminar");
        btnDeleteAMovimientos.addActionListener(this::btnDeleteAMovimientosActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAddAMovimientos)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditAMovimientos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteAMovimientos)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddAMovimientos, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditAMovimientos, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteAMovimientos, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void jPanel1ComponentRemoved(java.awt.event.ContainerEvent evt) {//GEN-FIRST:event_jPanel1ComponentRemoved
        // TODO add your handling code here:
    }//GEN-LAST:event_jPanel1ComponentRemoved

    private void btnAddAMovimientosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddAMovimientosActionPerformed
  
    }//GEN-LAST:event_btnAddAMovimientosActionPerformed

    private void btnEditAMovimientosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditAMovimientosActionPerformed
    }//GEN-LAST:event_btnEditAMovimientosActionPerformed

    private void btnDeleteAMovimientosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteAMovimientosActionPerformed
    }//GEN-LAST:event_btnDeleteAMovimientosActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddAMovimientos;
    private javax.swing.JButton btnDeleteAMovimientos;
    private javax.swing.JButton btnEditAMovimientos;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblAMovimientos;
    // End of variables declaration//GEN-END:variables
}
