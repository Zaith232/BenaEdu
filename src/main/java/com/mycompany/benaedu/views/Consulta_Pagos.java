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
import javax.swing.JCheckBox;
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
public class Consulta_Pagos extends javax.swing.JPanel {

    /**
     * Creates new form Consulta_Pagos
     */
    public Consulta_Pagos() {
        initComponents();
         construirInterfazConsultaPagos();
    }
private void construirInterfazConsultaPagos() {
        this.removeAll();
        this.setLayout(null);
        this.setBackground(new java.awt.Color(255, 255, 255));

        // --- CLASE LOCAL BUSCADOR FLOTANTE ---
        class BuscadorFlotante {
            void configurar(JTextField txtClave, JTextField txtDesc, JButton boton, Object[][] datos, String[] columnas, int[] anchos, java.util.function.Consumer<Object[]> onSelect) {
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

        // --- CARGA DE DATOS DESDE LA BD PARA BUSCADORES ---
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

        Object[][] dCia       = cargarDatosMultiple.apply("SELECT CIA, NCIA FROM tmcias ORDER BY CIA", 2);
        Object[][] dCC        = cargarDatosMultiple.apply("SELECT CVE, DES1 FROM tgcc WHERE CVE IN ('12100','12200','12300','12400') ORDER BY CVE", 2);
        Object[][] dCiclo     = cargarDatosMultiple.apply("SELECT CESC, CDSC FROM tescesc ORDER BY CESC DESC", 2);
        Object[][] dMatricula = cargarDatosMultiple.apply("SELECT MAT, APATE, AMATE, NOMA FROM tesalum ORDER BY MAT", 4);

        // --- 1. PANEL DATOS DE SELECCIÓN ---
        JPanel pnlSel = new JPanel(null);
        pnlSel.setBorder(BorderFactory.createTitledBorder("Datos de selección"));
        pnlSel.setBounds(10, 10, 700, 90);

        // Compañía
        pnlSel.add(new JLabel("Compañía")).setBounds(15, 20, 70, 25);
        JComboBox<String> cmbCia = new JComboBox<>();
        for (Object[] r : dCia) {
            cmbCia.addItem((r[0] != null ? r[0] : "") + " - " + (r[1] != null ? r[1] : ""));
        }
        cmbCia.setBounds(85, 20, 150, 25);
        pnlSel.add(cmbCia);

        // C. Costos
        pnlSel.add(new JLabel("C. Costos")).setBounds(250, 20, 70, 25);
        JComboBox<String> cmbCC = new JComboBox<>();
        cmbCC.addItem("");
        for (Object[] r : dCC) {
            cmbCC.addItem((r[0] != null ? r[0] : "") + " - " + (r[1] != null ? r[1] : ""));
        }
        cmbCC.setEditable(true);
        cmbCC.setBounds(320, 20, 200, 25);
        pnlSel.add(cmbCC);

        // Ciclo Escolar
        pnlSel.add(new JLabel("Ciclo Escolar")).setBounds(535, 20, 80, 25);
        JComboBox<String> cmbCiclo = new JComboBox<>();
        cmbCiclo.addItem("");
        for (Object[] r : dCiclo) {
            cmbCiclo.addItem((r[0] != null ? r[0] : "") + " - " + (r[1] != null ? r[1] : ""));
        }
        cmbCiclo.setEditable(true);
        cmbCiclo.setBounds(615, 20, 75, 25);
        pnlSel.add(cmbCiclo);

        // Buscador Matrícula
        pnlSel.add(new JLabel("Matrícula")).setBounds(15, 55, 70, 25);
        JTextField txtMatricula = new JTextField(); txtMatricula.setBounds(85, 55, 80, 25);
        JButton btnMat = new JButton("▼"); btnMat.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
        btnMat.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnMat.setBounds(165, 55, 20, 25);
        buscador.configurar(txtMatricula, null, btnMat, dMatricula,
            new String[]{"Matrícula", "A. Paterno", "A. Materno", "Nombre"},
            new int[]{80, 120, 120, 150}, null);
        pnlSel.add(txtMatricula); pnlSel.add(btnMat);

        // Fecha Inicial con JDateChooser
        pnlSel.add(new JLabel("Fecha Inicial")).setBounds(200, 55, 80, 25);
        com.toedter.calendar.JDateChooser txtFecIni = new com.toedter.calendar.JDateChooser();
        txtFecIni.setDateFormatString("dd/MM/yyyy");
        txtFecIni.setBounds(280, 55, 110, 25);
        pnlSel.add(txtFecIni);

        // Fecha Final con JDateChooser
        pnlSel.add(new JLabel("Fecha Final")).setBounds(400, 55, 80, 25);
        com.toedter.calendar.JDateChooser txtFecFin = new com.toedter.calendar.JDateChooser();
        txtFecFin.setDateFormatString("dd/MM/yyyy");
        txtFecFin.setBounds(480, 55, 110, 25);
        pnlSel.add(txtFecFin);

        this.add(pnlSel);

        // --- 2. PANEL DERECHO (Filtro y Checkbox) ---
        JPanel pnlFiltro = new JPanel(null);
        pnlFiltro.setBorder(BorderFactory.createEtchedBorder());
        pnlFiltro.setBounds(720, 15, 200, 85);

        JCheckBox chkHoja = new JCheckBox("Hoja por Alumno");
        chkHoja.setBounds(10, 10, 150, 25);
        pnlFiltro.add(chkHoja);

        JButton btnFiltra = new JButton("Filtra Información");
        btnFiltra.setBounds(10, 45, 180, 30);
        pnlFiltro.add(btnFiltra);

        this.add(pnlFiltro);

        // --- 3. TABLA DE CONCEPTOS PAGADOS ---
        String[] columnas = {
            "Compañía", "C. Costos", "C. Escolar", "Recibo", "Tipo", "Fecha Rec", "Concepto", "Descripción", "Moneda",
            "Importe MN", "Factura", "Tipo Fac", "Fecha Fac", "Mot Cancelación", "Porc Beca", "Porc Dscto", "Porc Rec",
            "Efectivo", "Cheque", "Tarjeta", "Ficha Dep", "Matrícula", "Nombre", "Grado", "Sección", "Periodo", "Póliza", "TipoPol", "Fecha Pol"
        };

        DefaultTableModel modConceptos = new DefaultTableModel(new Object[][]{}, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tblConceptos = new JTable(modConceptos);
        tblConceptos.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblConceptos.getColumnModel().getColumn(7).setPreferredWidth(200);
        tblConceptos.getColumnModel().getColumn(22).setPreferredWidth(220);

        JPanel pnlTabla = new JPanel(null);
        pnlTabla.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Conceptos Pagados", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
        pnlTabla.setBounds(10, 110, 910, 430);

        JScrollPane scrollConceptos = new JScrollPane(tblConceptos);
        scrollConceptos.setBounds(10, 20, 890, 400);
        pnlTabla.add(scrollConceptos);

        this.add(pnlTabla);

        // --- 4. BOTONES INFERIORES ---
        JButton btnImprimir = new JButton("Imprimir");
        btnImprimir.setBounds(350, 550, 100, 35);
        JButton btnSalir = new JButton("Salir");
        btnSalir.setBounds(480, 550, 100, 35);

        this.add(btnImprimir);
        this.add(btnSalir);

        // --- 5. EVENTOS ---
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

        // Evento Filtrar Información
        btnFiltra.addActionListener(e -> {
            modConceptos.setRowCount(0);

            String cia = extraerCVE.apply((String) cmbCia.getSelectedItem());
            String cc = extraerCVE.apply((String) cmbCC.getSelectedItem());
            String ciclo = extraerCVE.apply((String) cmbCiclo.getSelectedItem());
            String matricula = txtMatricula.getText().trim();

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            String fIni = txtFecIni.getDate() != null ? sdf.format(txtFecIni.getDate()) : "";
            String fFin = txtFecFin.getDate() != null ? sdf.format(txtFecFin.getDate()) : "";

            try {
                ConDB db = new ConDB();
                Connection con = db.Conectar();
                if (con != null) {
                    StringBuilder sql = new StringBuilder("SELECT CIA, CC, CESC, NREC, TREC, FREC, NCPTO, DCPTO, CMON, IMPMN, NFAC, TFAC, FFAC, MCAN, PBEC, PDSC, PREC, IPAGMN, MAT, NOMALU, GRADO, SECC, PESC, RELPOL, FPAG FROM tesralu WHERE CIA = ?");
                    
                    if (!cc.isEmpty()) sql.append(" AND CC = ?");
                    if (!ciclo.isEmpty()) sql.append(" AND CESC = ?");
                    if (!matricula.isEmpty()) sql.append(" AND MAT = ?");
                    if (!fIni.isEmpty() && !fFin.isEmpty()) sql.append(" AND FREC BETWEEN ? AND ?");

                    sql.append(" ORDER BY FREC DESC, NREC DESC");

                    PreparedStatement ps = con.prepareStatement(sql.toString());
                    int p = 1;
                    ps.setString(p++, cia);
                    if (!cc.isEmpty()) ps.setString(p++, cc);
                    if (!ciclo.isEmpty()) ps.setString(p++, ciclo);
                    if (!matricula.isEmpty()) ps.setString(p++, matricula);
                    if (!fIni.isEmpty() && !fFin.isEmpty()) {
                        ps.setString(p++, fIni);
                        ps.setString(p++, fFin);
                    }

                    ResultSet rs = ps.executeQuery();
                    java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");

                    while (rs.next()) {
                        Object[] fila = new Object[29];
                        fila[0] = rs.getString("CIA");
                        fila[1] = rs.getString("CC");
                        fila[2] = rs.getString("CESC");
                        fila[3] = rs.getString("NREC");
                        fila[4] = rs.getString("TREC");
                        fila[5] = rs.getString("FREC");
                        fila[6] = rs.getString("NCPTO");
                        fila[7] = rs.getString("DCPTO");
                        fila[8] = rs.getString("CMON");
                        fila[9] = df.format(rs.getDouble("IMPMN"));
                        fila[10] = rs.getString("NFAC");
                        fila[11] = rs.getString("TFAC");
                        fila[12] = rs.getString("FFAC");
                        fila[13] = rs.getString("MCAN");
                        fila[14] = rs.getString("PBEC");
                        fila[15] = rs.getString("PDSC");
                        fila[16] = rs.getString("PREC");
                        
                        double ipag = rs.getDouble("IPAGMN");
                        String fmaPago = rs.getString("FPAG") != null ? rs.getString("FPAG") : "";

                        // Desglose de Forma de Pago
                        fila[17] = "EF".equalsIgnoreCase(fmaPago) ? df.format(ipag) : "0.00"; // Efectivo
                        fila[18] = "CH".equalsIgnoreCase(fmaPago) ? df.format(ipag) : "0.00"; // Cheque
                        fila[19] = ("TC".equalsIgnoreCase(fmaPago) || "TD".equalsIgnoreCase(fmaPago)) ? df.format(ipag) : "0.00"; // Tarjeta
                        fila[20] = ("DB".equalsIgnoreCase(fmaPago) || "TE".equalsIgnoreCase(fmaPago)) ? df.format(ipag) : "0.00"; // Ficha Dep/Transf

                        fila[21] = rs.getString("MAT");
                        fila[22] = rs.getString("NOMALU");
                        fila[23] = rs.getString("GRADO");
                        fila[24] = rs.getString("SECC");
                        fila[25] = rs.getString("PESC");
                        fila[26] = rs.getString("RELPOL");
                        fila[27] = "0";
                        fila[28] = "01/01/2001";
                        
                        modConceptos.addRow(fila);
                    }
                    rs.close(); ps.close(); db.Cerrar();

                    if (modConceptos.getRowCount() == 0) {
                        JOptionPane.showMessageDialog(this, "No se encontraron pagos con los filtros seleccionados.", "Información", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al consultar pagos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Evento Imprimir
        btnImprimir.addActionListener(e -> {
            if (modConceptos.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No hay pagos cargados para imprimir.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
                job.setJobName("Consulta de Pagos");

                job.setPrintable((g, pf, pageIndex) -> {
                    int filasPorPagina = 25;
                    int totalPaginas = (int) Math.ceil((double) modConceptos.getRowCount() / filasPorPagina);
                    if (totalPaginas == 0) totalPaginas = 1;

                    if (pageIndex >= totalPaginas) return java.awt.print.Printable.NO_SUCH_PAGE;

                    java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
                    g2d.translate(pf.getImageableX(), pf.getImageableY());

                    int y = 40;
                    g2d.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
                    g2d.drawString("UNIDAD ESCOLAR BENAVENTE, A.C.", 40, y); y += 20;
                    g2d.drawString("REPORTE DE CONSULTA DE PAGOS ESCOLARES", 40, y); y += 15;
                    g2d.drawLine(40, y, 530, y); y += 15;

                    g2d.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 8));
                    g2d.drawString("RECIBO", 40, y);
                    g2d.drawString("FECHA", 90, y);
                    g2d.drawString("MATRÍCULA", 150, y);
                    g2d.drawString("ALUMNO", 210, y);
                    g2d.drawString("CONCEPTO", 360, y);
                    g2d.drawString("IMPORTE MN", 470, y);
                    y += 5;
                    g2d.drawLine(40, y, 530, y); y += 12;

                    g2d.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 7));
                    int inicioRow = pageIndex * filasPorPagina;
                    int finRow = Math.min(inicioRow + filasPorPagina, modConceptos.getRowCount());

                    for (int r = inicioRow; r < finRow; r++) {
                        g2d.drawString(modConceptos.getValueAt(r, 3).toString(), 40, y);
                        g2d.drawString(modConceptos.getValueAt(r, 5).toString(), 90, y);
                        g2d.drawString(modConceptos.getValueAt(r, 21).toString(), 150, y);

                        String nom = modConceptos.getValueAt(r, 22) != null ? modConceptos.getValueAt(r, 22).toString() : "";
                        if (nom.length() > 22) nom = nom.substring(0, 19) + "...";
                        g2d.drawString(nom, 210, y);

                        String cpt = modConceptos.getValueAt(r, 7) != null ? modConceptos.getValueAt(r, 7).toString() : "";
                        if (cpt.length() > 20) cpt = cpt.substring(0, 17) + "...";
                        g2d.drawString(cpt, 360, y);

                        g2d.drawString(modConceptos.getValueAt(r, 9).toString(), 470, y);
                        y += 12;
                    }

                    g2d.drawString("Página " + (pageIndex + 1) + " de " + totalPaginas, 450, 750);
                    return java.awt.print.Printable.PAGE_EXISTS;
                });

                if (job.printDialog()) {
                    job.print();
                    JOptionPane.showMessageDialog(this, "Consulta de pagos enviada a la impresora.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
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
        tblCPagos = new javax.swing.JTable();
        btnAddCPagos = new javax.swing.JButton();
        btnEditCPagos = new javax.swing.JButton();
        btnDeleteCPagos = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        tblCPagos.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tblCPagos);

        btnAddCPagos.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAddCPagos.setForeground(new java.awt.Color(26, 61, 99));
        btnAddCPagos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/add.png"))); // NOI18N
        btnAddCPagos.setText("Añadir");
        btnAddCPagos.addActionListener(this::btnAddCPagosActionPerformed);

        btnEditCPagos.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEditCPagos.setForeground(new java.awt.Color(26, 61, 99));
        btnEditCPagos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edit.png"))); // NOI18N
        btnEditCPagos.setText("Editar");
        btnEditCPagos.setMaximumSize(new java.awt.Dimension(93, 31));
        btnEditCPagos.setMinimumSize(new java.awt.Dimension(93, 31));
        btnEditCPagos.addActionListener(this::btnEditCPagosActionPerformed);

        btnDeleteCPagos.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDeleteCPagos.setForeground(new java.awt.Color(26, 61, 99));
        btnDeleteCPagos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/delete.png"))); // NOI18N
        btnDeleteCPagos.setText("Eliminar");
        btnDeleteCPagos.addActionListener(this::btnDeleteCPagosActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAddCPagos)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditCPagos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteCPagos)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddCPagos, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditCPagos, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteCPagos, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void btnAddCPagosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddCPagosActionPerformed
    }//GEN-LAST:event_btnAddCPagosActionPerformed

    private void btnEditCPagosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditCPagosActionPerformed
    }//GEN-LAST:event_btnEditCPagosActionPerformed

    private void btnDeleteCPagosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteCPagosActionPerformed

    }//GEN-LAST:event_btnDeleteCPagosActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddCPagos;
    private javax.swing.JButton btnDeleteCPagos;
    private javax.swing.JButton btnEditCPagos;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblCPagos;
    // End of variables declaration//GEN-END:variables
}
