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
public class Detalle_Conceptos_Pagos extends javax.swing.JPanel {

    /**
     * Creates new form Detalle_Conceptos_Pagos
     */
    public Detalle_Conceptos_Pagos() {
        initComponents();
        construirInterfazDetalleConceptos();
    }
private void construirInterfazDetalleConceptos() {
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

        Object[][] dCiclo  = cargarDatosMultiple.apply("SELECT CESC, CDSC FROM tescesc ORDER BY CESC DESC", 2);
        Object[][] dCajero = cargarDatosMultiple.apply("SELECT NEMP, NOME FROM tgemp WHERE CAJ = 'S' ORDER BY NEMP", 2);
        Object[][] dCpto   = cargarDatosMultiple.apply("SELECT DISTINCT NCPTO, DCPTO FROM tescpto ORDER BY NCPTO", 2);
        Object[][] dTipo   = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'TCPT' ORDER BY CVE", 2);

        // --- 1. SECCIÓN SUPERIOR DE FILTROS ---
        this.add(new JLabel("Compañía")).setBounds(20, 20, 70, 25);
        JComboBox<String> cmbCia = new JComboBox<>(); cmbCia.setBounds(90, 20, 60, 25);
        this.add(cmbCia);

        this.add(new JLabel("Centro Costos")).setBounds(170, 20, 90, 25);
        JComboBox<String> cmbCC = new JComboBox<>(); cmbCC.setBounds(260, 20, 80, 25);
        this.add(cmbCC);

        try {
            ConDB db = new ConDB();
            Connection con = db.Conectar();
            if (con != null) {
                ResultSet rsCia = con.prepareStatement("SELECT CIA FROM tmcias ORDER BY CIA").executeQuery();
                while(rsCia.next()) cmbCia.addItem(rsCia.getString("CIA"));
                rsCia.close();

                ResultSet rsCC = con.prepareStatement("SELECT CVE FROM tgcc WHERE CVE IN ('12100', '12200', '12300', '12400') ORDER BY CVE").executeQuery();
                while(rsCC.next()) cmbCC.addItem(rsCC.getString("CVE"));
                rsCC.close(); db.Cerrar();
            }
        } catch (Exception ex) { cmbCia.addItem("12"); cmbCC.addItem("12100"); }

        this.add(new JLabel("Ciclo Escolar")).setBounds(360, 20, 80, 25);
        JTextField txtCiclo = new JTextField("2526"); txtCiclo.setBounds(445, 20, 60, 25);
        JButton btnCiclo = new JButton("▼"); btnCiclo.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnCiclo.setMargin(new java.awt.Insets(0,0,0,0)); btnCiclo.setBounds(505, 20, 20, 25);
        buscador.configurar(txtCiclo, null, btnCiclo, dCiclo, new String[]{"Clave", "Descripción"}, new int[]{60, 150});
        this.add(txtCiclo); this.add(btnCiclo);

        // Cajero
        this.add(new JLabel("Cajero")).setBounds(20, 55, 70, 25);
        JTextField txtCajero = new JTextField(); txtCajero.setBounds(90, 55, 40, 25);
        JButton btnCajero = new JButton("▼"); btnCajero.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnCajero.setMargin(new java.awt.Insets(0,0,0,0)); btnCajero.setBounds(130, 55, 20, 25);
        buscador.configurar(txtCajero, null, btnCajero, dCajero, new String[]{"Clave", "Nombre"}, new int[]{60, 200});
        this.add(txtCajero); this.add(btnCajero);

        // Fechas
        this.add(new JLabel("Fecha Inicial")).setBounds(170, 55, 90, 25);
        com.toedter.calendar.JDateChooser txtFecIni = new com.toedter.calendar.JDateChooser();
        txtFecIni.setDateFormatString("dd/MM/yyyy"); txtFecIni.setBounds(260, 55, 110, 25);
        this.add(txtFecIni);

        this.add(new JLabel("Fecha Final")).setBounds(390, 55, 80, 25);
        com.toedter.calendar.JDateChooser txtFecFin = new com.toedter.calendar.JDateChooser();
        txtFecFin.setDateFormatString("dd/MM/yyyy"); txtFecFin.setBounds(465, 55, 110, 25);
        this.add(txtFecFin);

        // Concepto
        this.add(new JLabel("Concepto")).setBounds(20, 90, 70, 25);
        JTextField txtConcepto = new JTextField(); txtConcepto.setBounds(90, 90, 60, 25);
        JButton btnConcepto = new JButton("▼"); btnConcepto.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnConcepto.setMargin(new java.awt.Insets(0,0,0,0)); btnConcepto.setBounds(150, 90, 20, 25);
        buscador.configurar(txtConcepto, null, btnConcepto, dCpto, new String[]{"Clave", "Descripción"}, new int[]{70, 250});
        this.add(txtConcepto); this.add(btnConcepto);

        // Tipo Concepto
        this.add(new JLabel("Tipo Concepto")).setBounds(180, 90, 90, 25);
        JTextField txtTipo = new JTextField(); txtTipo.setBounds(270, 90, 40, 25);
        JButton btnTipo = new JButton("▼"); btnTipo.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnTipo.setMargin(new java.awt.Insets(0,0,0,0)); btnTipo.setBounds(310, 90, 20, 25);
        buscador.configurar(txtTipo, null, btnTipo, dTipo, new String[]{"Clave", "Descripción"}, new int[]{60, 200});
        this.add(txtTipo); this.add(btnTipo);

        // Formato
        this.add(new JLabel("Formato")).setBounds(20, 125, 70, 25);
        JComboBox<String> cmbFormato = new JComboBox<>(new String[]{"01 DETALLE DE CONCEPTOS PAGADOS"});
        cmbFormato.setBounds(90, 125, 435, 25);
        this.add(cmbFormato);

        // --- 2. TIPO DE CUENTA (Panel Derecho) ---
        JPanel pnlTipoCta = new JPanel(null);
        pnlTipoCta.setBorder(BorderFactory.createTitledBorder("Tipo de Cuenta"));
        pnlTipoCta.setBounds(730, 10, 180, 95);

        JRadioButton rbOficial = new JRadioButton("Oficial", true); rbOficial.setBounds(20, 20, 80, 20);
        JRadioButton rbPart = new JRadioButton("Particular"); rbPart.setBounds(20, 40, 100, 20);
        JRadioButton rbAmbos = new JRadioButton("Ambos"); rbAmbos.setBounds(20, 60, 80, 20);

        ButtonGroup bgTipo = new ButtonGroup();
        bgTipo.add(rbOficial); bgTipo.add(rbPart); bgTipo.add(rbAmbos);

        pnlTipoCta.add(rbOficial); pnlTipoCta.add(rbPart); pnlTipoCta.add(rbAmbos);
        this.add(pnlTipoCta);

        JButton btnFiltra = new JButton("Filtrar información");
        btnFiltra.setBounds(730, 115, 180, 30);
        this.add(btnFiltra);

        // --- 3. TABLA DETALLE DE CONCEPTOS ---
        DefaultTableModel modDetalle = new DefaultTableModel(
            new Object[][]{}, 
            new String[]{"Tipo", "Cve", "Concepto", "Matrícula", "Nombre del Alumno", "Grado", "Grupo", "Cantidad", "Importe"}
        ) { @Override public boolean isCellEditable(int row, int column) { return false; } };

        JTable tblDetalle = new JTable(modDetalle);
        tblDetalle.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
        tblDetalle.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblDetalle.getColumnModel().getColumn(1).setPreferredWidth(50);
        tblDetalle.getColumnModel().getColumn(2).setPreferredWidth(180);
        tblDetalle.getColumnModel().getColumn(3).setPreferredWidth(80);
        tblDetalle.getColumnModel().getColumn(4).setPreferredWidth(230);
        tblDetalle.getColumnModel().getColumn(5).setPreferredWidth(50);
        tblDetalle.getColumnModel().getColumn(6).setPreferredWidth(50);
        tblDetalle.getColumnModel().getColumn(7).setPreferredWidth(60);
        tblDetalle.getColumnModel().getColumn(8).setPreferredWidth(90); 
        
        JPanel pnlTabla = new JPanel(null);
        pnlTabla.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Detalle de Conceptos Pagados", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
        pnlTabla.setBounds(10, 160, 900, 290);
        
        JScrollPane scrollDetalle = new JScrollPane(tblDetalle);
        scrollDetalle.setBounds(10, 20, 880, 260);
        pnlTabla.add(scrollDetalle);
        this.add(pnlTabla);

        // --- 4. TOTALES INFERIORES ---
        JPanel pnlTotales = new JPanel(null);
        pnlTotales.setBorder(BorderFactory.createEtchedBorder());
        pnlTotales.setBounds(10, 460, 900, 45);

        JLabel lblTotales = new JLabel("Totales de Concepto"); lblTotales.setBounds(620, 10, 130, 25);
        JTextField txtTotCant = new JTextField("0"); txtTotCant.setHorizontalAlignment(JTextField.RIGHT); txtTotCant.setEditable(false); txtTotCant.setBounds(750, 10, 50, 25);
        JTextField txtTotImp = new JTextField("0.00"); txtTotImp.setHorizontalAlignment(JTextField.RIGHT); txtTotImp.setEditable(false); txtTotImp.setBounds(810, 10, 80, 25);

        pnlTotales.add(lblTotales); pnlTotales.add(txtTotCant); pnlTotales.add(txtTotImp);
        this.add(pnlTotales);

        // --- 5. BOTONES INFERIORES ---
        JButton btnImprimir = new JButton("Imprimir"); btnImprimir.setBounds(350, 520, 100, 35);
        JButton btnSalir = new JButton("Salir"); btnSalir.setBounds(470, 520, 100, 35);
        this.add(btnImprimir); this.add(btnSalir);

        // --- 6. EVENTOS DE LA VISTA ---

        btnSalir.addActionListener(e -> {
            this.removeAll();
            this.revalidate();
            this.repaint();
        });

        // Evento de Consulta a tesralu
        btnFiltra.addActionListener(e -> {
            modDetalle.setRowCount(0);

            String cia = cmbCia.getSelectedItem() != null ? cmbCia.getSelectedItem().toString() : "";
            String cc = cmbCC.getSelectedItem() != null ? cmbCC.getSelectedItem().toString() : "";
            String ciclo = txtCiclo.getText().trim();
            String cajero = txtCajero.getText().trim();
            String concepto = txtConcepto.getText().trim();

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            String fIni = txtFecIni.getDate() != null ? sdf.format(txtFecIni.getDate()) : "";
            String fFin = txtFecFin.getDate() != null ? sdf.format(txtFecFin.getDate()) : "";

            try {
                ConDB db = new ConDB();
                Connection con = db.Conectar();
                if (con != null) {
                    StringBuilder sql = new StringBuilder(
                        "SELECT COALESCE(TALU, 'O') AS TIPO, NCPTO, DCPTO, MAT, NOMALU, GRADO, GRUPO, " +
                        "COALESCE(CANT, 1) AS CANTIDAD, IMPMN " +
                        "FROM tesralu WHERE CIA = ? "
                    );

                    if (!cc.isEmpty()) sql.append(" AND CC = ?");
                    if (!ciclo.isEmpty()) sql.append(" AND CESC = ?");
                    if (!cajero.isEmpty()) sql.append(" AND NCAJ = ?");
                    if (!concepto.isEmpty()) sql.append(" AND NCPTO = ?");
                    if (!fIni.isEmpty() && !fFin.isEmpty()) sql.append(" AND FREC BETWEEN ? AND ?");

                    // Filtro de Tipo de Cuenta (Oficial / Particular)
                    if (rbOficial.isSelected()) sql.append(" AND (TALU = 'O' OR TALU IS NULL)");
                    else if (rbPart.isSelected()) sql.append(" AND TALU = 'P'");

                    sql.append(" ORDER BY NCPTO ASC, FREC ASC");

                    PreparedStatement ps = con.prepareStatement(sql.toString());
                    int p = 1;
                    ps.setString(p++, cia);
                    if (!cc.isEmpty()) ps.setString(p++, cc);
                    if (!ciclo.isEmpty()) ps.setString(p++, ciclo);
                    if (!cajero.isEmpty()) ps.setString(p++, cajero);
                    if (!concepto.isEmpty()) ps.setString(p++, concepto);
                    if (!fIni.isEmpty() && !fFin.isEmpty()) {
                        ps.setString(p++, fIni);
                        ps.setString(p++, fFin);
                    }

                    ResultSet rs = ps.executeQuery();
                    java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");

                    int sumaCant = 0;
                    double sumaImp = 0.0;

                    while (rs.next()) {
                        int cant = rs.getInt("CANTIDAD");
                        double imp = rs.getDouble("IMPMN");

                        Object[] fila = new Object[9];
                        fila[0] = rs.getString("TIPO");
                        fila[1] = rs.getString("NCPTO");
                        fila[2] = rs.getString("DCPTO");
                        fila[3] = rs.getString("MAT");
                        fila[4] = rs.getString("NOMALU");
                        fila[5] = rs.getString("GRADO");
                        fila[6] = rs.getString("GRUPO");
                        fila[7] = cant;
                        fila[8] = df.format(imp);

                        sumaCant += cant;
                        sumaImp += imp;

                        modDetalle.addRow(fila);
                    }

                    rs.close(); ps.close(); db.Cerrar();

                    txtTotCant.setText(String.valueOf(sumaCant));
                    txtTotImp.setText(df.format(sumaImp));

                    if (modDetalle.getRowCount() == 0) {
                        JOptionPane.showMessageDialog(this, "No se encontraron conceptos pagados con esos filtros.", "Información", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al consultar detalle de pagos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Evento Imprimir
        btnImprimir.addActionListener(e -> {
            if (modDetalle.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No hay registros para imprimir.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
                job.setJobName("Detalle de Conceptos Pagados");

                job.setPrintable((g, pf, pageIndex) -> {
                    int filasPorPagina = 28;
                    int totalPaginas = (int) Math.ceil((double) modDetalle.getRowCount() / filasPorPagina);
                    if (totalPaginas == 0) totalPaginas = 1;

                    if (pageIndex >= totalPaginas) return java.awt.print.Printable.NO_SUCH_PAGE;

                    java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
                    g2d.translate(pf.getImageableX(), pf.getImageableY());

                    int y = 40;
                    g2d.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
                    g2d.drawString("UNIDAD ESCOLAR BENAVENTE, A.C.", 40, y); y += 20;
                    g2d.drawString("DETALLE DE CONCEPTOS PAGADOS", 40, y); y += 15;
                    g2d.drawLine(40, y, 530, y); y += 15;

                    g2d.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 8));
                    g2d.drawString("TPO", 40, y);
                    g2d.drawString("CONCEPTO", 70, y);
                    g2d.drawString("MATRÍCULA", 210, y);
                    g2d.drawString("ALUMNO", 280, y);
                    g2d.drawString("CANT", 440, y);
                    g2d.drawString("IMPORTE", 480, y);
                    y += 5;
                    g2d.drawLine(40, y, 530, y); y += 12;

                    g2d.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 7));
                    int inicioRow = pageIndex * filasPorPagina;
                    int finRow = Math.min(inicioRow + filasPorPagina, modDetalle.getRowCount());

                    for (int r = inicioRow; r < finRow; r++) {
                        g2d.drawString(modDetalle.getValueAt(r, 0).toString(), 40, y);

                        String cpt = modDetalle.getValueAt(r, 2) != null ? modDetalle.getValueAt(r, 2).toString() : "";
                        if (cpt.length() > 22) cpt = cpt.substring(0, 19) + "...";
                        g2d.drawString(cpt, 70, y);

                        g2d.drawString(modDetalle.getValueAt(r, 3).toString(), 210, y);

                        String nom = modDetalle.getValueAt(r, 4) != null ? modDetalle.getValueAt(r, 4).toString() : "";
                        if (nom.length() > 24) nom = nom.substring(0, 21) + "...";
                        g2d.drawString(nom, 280, y);

                        g2d.drawString(modDetalle.getValueAt(r, 7).toString(), 440, y);
                        g2d.drawString(modDetalle.getValueAt(r, 8).toString(), 480, y);
                        y += 12;
                    }

                    if (pageIndex == totalPaginas - 1) {
                        y += 10;
                        g2d.drawLine(40, y, 530, y); y += 15;
                        g2d.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 9));
                        g2d.drawString("CANTIDAD TOTAL: " + txtTotCant.getText(), 40, y);
                        g2d.drawString("IMPORTE TOTAL: $" + txtTotImp.getText(), 380, y);
                    }

                    g2d.drawString("Página " + (pageIndex + 1) + " de " + totalPaginas, 450, 750);
                    return java.awt.print.Printable.PAGE_EXISTS;
                });

                if (job.printDialog()) {
                    job.print();
                    JOptionPane.showMessageDialog(this, "Reporte enviado a la impresora.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
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
        tblDCPAgos = new javax.swing.JTable();
        btnAddDCPagos = new javax.swing.JButton();
        btnEditDCPagados = new javax.swing.JButton();
        btnDeleteDCPagados = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        tblDCPAgos.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tblDCPAgos);

        btnAddDCPagos.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAddDCPagos.setForeground(new java.awt.Color(26, 61, 99));
        btnAddDCPagos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/add.png"))); // NOI18N
        btnAddDCPagos.setText("Añadir");
        btnAddDCPagos.addActionListener(this::btnAddDCPagosActionPerformed);

        btnEditDCPagados.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEditDCPagados.setForeground(new java.awt.Color(26, 61, 99));
        btnEditDCPagados.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edit.png"))); // NOI18N
        btnEditDCPagados.setText("Editar");
        btnEditDCPagados.setMaximumSize(new java.awt.Dimension(93, 31));
        btnEditDCPagados.setMinimumSize(new java.awt.Dimension(93, 31));
        btnEditDCPagados.addActionListener(this::btnEditDCPagadosActionPerformed);

        btnDeleteDCPagados.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDeleteDCPagados.setForeground(new java.awt.Color(26, 61, 99));
        btnDeleteDCPagados.setIcon(new javax.swing.ImageIcon(getClass().getResource("/delete.png"))); // NOI18N
        btnDeleteDCPagados.setText("Eliminar");
        btnDeleteDCPagados.addActionListener(this::btnDeleteDCPagadosActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAddDCPagos)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditDCPagados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteDCPagados)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddDCPagos, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditDCPagados, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteDCPagados, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void btnAddDCPagosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddDCPagosActionPerformed
  
    }//GEN-LAST:event_btnAddDCPagosActionPerformed

    private void btnEditDCPagadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditDCPagadosActionPerformed
    }//GEN-LAST:event_btnEditDCPagadosActionPerformed

    private void btnDeleteDCPagadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteDCPagadosActionPerformed
    }//GEN-LAST:event_btnDeleteDCPagadosActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddDCPagos;
    private javax.swing.JButton btnDeleteDCPagados;
    private javax.swing.JButton btnEditDCPagados;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblDCPAgos;
    // End of variables declaration//GEN-END:variables
}
