/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.benaedu.views;
import com.mycompany.benaedu.db.ConDB;
import java.awt.Graphics2D;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
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
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
/**
 * Consulta e imprime los cargos, pagos y saldos de los alumnos.
 *
 * @author b17za
 */
public class Estado_Cuenta_Detallado extends javax.swing.JPanel {
    private String usuarioLogueado = "Admin";

    /**
     * Crea el estado de cuenta para el usuario autenticado.
     *
     * @param usuarioLogueado alias del usuario que inició sesión
     */
    public Estado_Cuenta_Detallado(String usuarioLogueado) {
        if (usuarioLogueado != null && !usuarioLogueado.trim().isEmpty()) {
            this.usuarioLogueado = usuarioLogueado.trim();
        }
        initComponents();
        construirInterfazEstadoCuenta();
    }
    /**
     * Crea el estado de cuenta usando el usuario predeterminado.
     */
    public Estado_Cuenta_Detallado() {
        initComponents();
        construirInterfazEstadoCuenta();
    }

    /**
     * Construye los filtros, la tabla de movimientos y las acciones del reporte.
     */
    private void construirInterfazEstadoCuenta() {
        this.removeAll();
        this.setLayout(null);
        this.setBackground(new java.awt.Color(238, 238, 238)); // Fondo clásico Gris/Sistema

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

        Object[][] dMatricula = cargarDatosMultiple.apply(
                "SELECT MAT,COALESCE(NULLIF(MAX(NOMCOM),'')," +
                "CONCAT_WS(' ',MAX(APATE),MAX(AMATE),MAX(NOMA))) AS NOMBRE " +
                "FROM tesalum GROUP BY MAT ORDER BY MAT", 2);

        // ==========================================
        // 1. PANEL SUPERIOR DE FILTROS
        // ==========================================
        JPanel pnlTopFiltros = new JPanel(null);
        pnlTopFiltros.setBorder(BorderFactory.createEtchedBorder());
        pnlTopFiltros.setBounds(10, 10, 620, 125);

        pnlTopFiltros.add(new JLabel("Compañía")).setBounds(15, 15, 70, 25);
        JComboBox<String> cmbCia = new JComboBox<>();
        cmbCia.setBounds(85, 15, 110, 25);
        pnlTopFiltros.add(cmbCia);

        pnlTopFiltros.add(new JLabel("Ctro de Costo")).setBounds(210, 15, 80, 25);
        JComboBox<String> cmbCC = new JComboBox<>();
        cmbCC.setBounds(295, 15, 90, 25);
        pnlTopFiltros.add(cmbCC);

        pnlTopFiltros.add(new JLabel("Ciclo Escolar")).setBounds(400, 15, 80, 25);
        JComboBox<String> cmbCiclo = new JComboBox<>();
        cmbCiclo.setBounds(485, 15, 90, 25);
        pnlTopFiltros.add(cmbCiclo);

        pnlTopFiltros.add(new JLabel("Grado")).setBounds(15, 50, 70, 25);
        JComboBox<String> cmbGrado = new JComboBox<>();
        cmbGrado.setBounds(85, 50, 110, 25);
        pnlTopFiltros.add(cmbGrado);

        pnlTopFiltros.add(new JLabel("Matricula")).setBounds(15, 85, 70, 25);
        JTextField txtMat = new JTextField(); txtMat.setBounds(85, 85, 85, 25);
        JButton btnMat = new JButton("▼"); btnMat.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnMat.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnMat.setBounds(170, 85, 25, 25);
        buscador.configurar(txtMat, null, btnMat, dMatricula, new String[]{"Matricula", "Nombre Alumno"}, new int[]{80, 220});
        pnlTopFiltros.add(txtMat); pnlTopFiltros.add(btnMat);

        // Llenar Cia, CC y Ciclo desde BD
        try {
            ConDB db = new ConDB(); Connection con = db.Conectar();
            if (con != null) {
                ResultSet rsCia = con.prepareStatement("SELECT CIA FROM tmcias ORDER BY CIA").executeQuery();
                while(rsCia.next()) cmbCia.addItem(rsCia.getString("CIA"));
                rsCia.close();

                cmbCC.addItem("");
                ResultSet rsCC = con.prepareStatement(
                        "SELECT DISTINCT CVE FROM tgcc WHERE COALESCE(CVE,'')<>'' ORDER BY CVE")
                        .executeQuery();
                while (rsCC.next()) cmbCC.addItem(rsCC.getString("CVE"));
                rsCC.close();

                cmbGrado.addItem("");
                ResultSet rsGrado = con.prepareStatement(
                        "SELECT DISTINCT CGRAD FROM tesgrad WHERE COALESCE(CGRAD,'')<>'' ORDER BY CGRAD")
                        .executeQuery();
                while (rsGrado.next()) cmbGrado.addItem(rsGrado.getString("CGRAD"));
                rsGrado.close();

                ResultSet rsCesc = con.prepareStatement("SELECT DISTINCT CESC FROM tescesc ORDER BY CESC DESC").executeQuery();
                cmbCiclo.addItem("");
                while(rsCesc.next()) cmbCiclo.addItem(rsCesc.getString("CESC"));
                if (cmbCiclo.getItemCount() > 1) cmbCiclo.setSelectedIndex(1);
                rsCesc.close(); db.Cerrar();
            }
        } catch(Exception ex) {}

        this.add(pnlTopFiltros);

        // --- Panel Incluir ---
        JPanel pnlIncluir = new JPanel(null);
        pnlIncluir.setBorder(BorderFactory.createTitledBorder("Incluir"));
        pnlIncluir.setBounds(640, 10, 135, 125);
        JCheckBox chkMensaje3 = new JCheckBox("Mensaje (3)");
        chkMensaje3.setBounds(10, 30, 115, 25);
        pnlIncluir.add(chkMensaje3);
        this.add(pnlIncluir);

        // --- Panel Tipo de Cuenta ---
        JPanel pnlTipoCuenta = new JPanel(null);
        pnlTipoCuenta.setBorder(BorderFactory.createTitledBorder("Tipo de Cuenta"));
        pnlTipoCuenta.setBounds(785, 10, 135, 125);

        JRadioButton rbOficial = new JRadioButton("Oficial", true); rbOficial.setBounds(10, 20, 110, 20);
        JRadioButton rbParticular = new JRadioButton("Particular"); rbParticular.setBounds(10, 45, 110, 20);
        JRadioButton rbAmbos = new JRadioButton("Ambos"); rbAmbos.setBounds(10, 70, 110, 20);
        ButtonGroup bgTipoCuenta = new ButtonGroup();
        bgTipoCuenta.add(rbOficial); bgTipoCuenta.add(rbParticular); bgTipoCuenta.add(rbAmbos);

        pnlTipoCuenta.add(rbOficial); pnlTipoCuenta.add(rbParticular); pnlTipoCuenta.add(rbAmbos);
        this.add(pnlTipoCuenta);

        // --- Botón Filtrar Información ---
        JButton btnFiltrar = new JButton("Filtrar información");
        btnFiltrar.setBounds(785, 140, 135, 30);
        this.add(btnFiltrar);

        // ==========================================
        // 2. TABLA DETALLE DE CONCEPTOS
        // ==========================================
        DefaultTableModel modDetalle = new DefaultTableModel(
            new Object[][]{},
            new String[]{
                "Matricula", "Concepto", "Descripcion", "Grad", "Gpo", 
                "A Pagar", "Fec Venc", "Imp Pagado", "Fec Pago", "Recibo", "Saldo", "% Beca"
            }
        ) { @Override public boolean isCellEditable(int r, int c) { return false; } };

        JTable tblDetalle = new JTable(modDetalle);
        tblDetalle.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblDetalle.setAutoCreateRowSorter(true);

        tblDetalle.getColumnModel().getColumn(0).setPreferredWidth(75);  // Matricula
        tblDetalle.getColumnModel().getColumn(1).setPreferredWidth(65);  // Concepto
        tblDetalle.getColumnModel().getColumn(2).setPreferredWidth(180); // Descripcion
        tblDetalle.getColumnModel().getColumn(3).setPreferredWidth(45);  // Grad
        tblDetalle.getColumnModel().getColumn(4).setPreferredWidth(45);  // Gpo
        tblDetalle.getColumnModel().getColumn(5).setPreferredWidth(85);  // A Pagar
        tblDetalle.getColumnModel().getColumn(6).setPreferredWidth(80);  // Fec Venc
        tblDetalle.getColumnModel().getColumn(7).setPreferredWidth(85);  // Imp Pagado
        tblDetalle.getColumnModel().getColumn(8).setPreferredWidth(80);  // Fec Pago
        tblDetalle.getColumnModel().getColumn(9).setPreferredWidth(85);  // Ref Ban
        tblDetalle.getColumnModel().getColumn(10).setPreferredWidth(85); // Saldo
        tblDetalle.getColumnModel().getColumn(11).setPreferredWidth(55); // % Beca

        JPanel pnlTabla = new JPanel(null);
        pnlTabla.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Detalle de Conceptos", TitledBorder.CENTER, TitledBorder.TOP
        ));
        pnlTabla.setBounds(10, 175, 910, 240);

        JScrollPane scrollTabla = new JScrollPane(tblDetalle);
        scrollTabla.setBounds(10, 20, 890, 210);
        pnlTabla.add(scrollTabla);

        this.add(pnlTabla);

        // ==========================================
        // 3. PANEL DE TOTALES
        // ==========================================
        JPanel pnlTotales = new JPanel(null);
        pnlTotales.setBounds(10, 420, 910, 55);

        pnlTotales.add(new JLabel("Total A Pagar")).setBounds(425, 5, 90, 20);
        JTextField txtTotAPagar = new JTextField("0.00");
        txtTotAPagar.setHorizontalAlignment(JTextField.RIGHT); txtTotAPagar.setEditable(false);
        txtTotAPagar.setBounds(425, 25, 95, 25);
        pnlTotales.add(txtTotAPagar);

        pnlTotales.add(new JLabel("Total Pagado")).setBounds(585, 5, 90, 20);
        JTextField txtTotPagado = new JTextField("0.00");
        txtTotPagado.setHorizontalAlignment(JTextField.RIGHT); txtTotPagado.setEditable(false);
        txtTotPagado.setBounds(585, 25, 95, 25);
        pnlTotales.add(txtTotPagado);

        pnlTotales.add(new JLabel("Total Saldo")).setBounds(800, 5, 90, 20);
        JTextField txtTotSaldo = new JTextField("0.00");
        txtTotSaldo.setHorizontalAlignment(JTextField.RIGHT); txtTotSaldo.setEditable(false);
        txtTotSaldo.setBounds(800, 25, 95, 25);
        pnlTotales.add(txtTotSaldo);

        this.add(pnlTotales);

        // ==========================================
        // 4. BOTONES INFERIORES
        // ==========================================
        JButton btnImprimir = new JButton("Imprimir");
        btnImprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edit.png"))); // Icono sugerido
        btnImprimir.setBounds(330, 480, 100, 45);

        JButton btnSalir = new JButton("Salir");
        btnSalir.setBounds(550, 480, 100, 45);

        this.add(btnImprimir);
        this.add(btnSalir);

        // ==========================================
        // 5. LÓGICA Y CONSULTA A BASE DE DATOS
        // ==========================================
        btnSalir.addActionListener(e -> {
            this.removeAll();
            this.revalidate();
            this.repaint();
        });

        btnFiltrar.addActionListener(e -> {
            modDetalle.setRowCount(0);
            txtTotAPagar.setText("0.00");
            txtTotPagado.setText("0.00");
            txtTotSaldo.setText("0.00");

            String cia = cmbCia.getSelectedItem() != null ? cmbCia.getSelectedItem().toString() : "12";
            String cc = cmbCC.getSelectedItem() != null ? cmbCC.getSelectedItem().toString() : "";
            String ciclo = cmbCiclo.getSelectedItem() != null ? cmbCiclo.getSelectedItem().toString() : "";
            String grado = cmbGrado.getSelectedItem() != null ? cmbGrado.getSelectedItem().toString() : "";
            String matricula = txtMat.getText().trim();

            if (ciclo.isEmpty() && matricula.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Selecciona un ciclo escolar o una matrícula para consultar.",
                        "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection con = ConDB.getConnection()) {
                    StringBuilder sql = new StringBuilder(
                        "SELECT c.MAT, c.NCPTO, c.DCPTO, c.GRADO, c.GRUPO, " +
                        "c.IMPTMN AS A_PAGAR, c.FVEN, c.IPAGMN AS IMP_PAGADO, " +
                        "r.FPAG AS FEC_PAGO, r.NREC AS RECIBO, c.IPENMN AS SALDO, c.PBEC " +
                        "FROM tescalu c " +
                        "LEFT JOIN (SELECT p.CIA,p.CC,p.CESC,p.MAT,p.IDCPT,p.FPAG,p.NREC FROM (" +
                        "SELECT r.CIA,r.CC,r.CESC,r.MAT,r.IDCPT,r.FPAG,r.FREC,r.NREC," +
                        "ROW_NUMBER() OVER (PARTITION BY r.CIA,r.CC,r.CESC,r.MAT,r.IDCPT " +
                        "ORDER BY r.FPAG DESC,r.FREC DESC,r.NREC DESC) AS FILA FROM tesralu r " +
                        "WHERE COALESCE(r.MCAN,'')='' AND r.CIA=? "
                    );

                    if (!cc.isEmpty()) sql.append(" AND r.CC=? ");
                    if (!ciclo.isEmpty()) sql.append(" AND r.CESC=? ");
                    if (!matricula.isEmpty()) sql.append(" AND r.MAT=? ");

                    sql.append(
                        ") p WHERE p.FILA=1) r " +
                        "ON c.CIA=r.CIA AND c.CC=r.CC AND c.CESC=r.CESC " +
                        "AND c.MAT=r.MAT AND c.IDCPT=r.IDCPT " +
                        "WHERE c.CIA=? AND COALESCE(c.MCAN,'')='' "
                    );

                    if (!cc.isEmpty()) sql.append(" AND c.CC = ? ");
                    if (!ciclo.isEmpty()) sql.append(" AND c.CESC = ? ");
                    if (!grado.isEmpty()) sql.append(" AND c.GRADO = ? ");
                    if (!matricula.isEmpty()) sql.append(" AND c.MAT = ? ");
                    if (rbOficial.isSelected()) sql.append(" AND COALESCE(c.TCONT,'O')='O' ");
                    if (rbParticular.isSelected()) sql.append(" AND c.TCONT='P' ");

                    sql.append(" ORDER BY c.MAT,c.FVEN,c.IDCPT");

                    java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");
                    double sumAPagar = 0.0;
                    double sumPagado = 0.0;
                    double sumSaldo = 0.0;

                    try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
                        int p = 1;
                        ps.setString(p++, cia);
                        if (!cc.isEmpty()) ps.setString(p++, cc);
                        if (!ciclo.isEmpty()) ps.setString(p++, ciclo);
                        if (!matricula.isEmpty()) ps.setString(p++, matricula);

                        ps.setString(p++, cia);
                        if (!cc.isEmpty()) ps.setString(p++, cc);
                        if (!ciclo.isEmpty()) ps.setString(p++, ciclo);
                        if (!grado.isEmpty()) ps.setString(p++, grado);
                        if (!matricula.isEmpty()) ps.setString(p++, matricula);

                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                double aPagar = rs.getDouble("A_PAGAR");
                                double pagado = rs.getDouble("IMP_PAGADO");
                                double saldo = rs.getDouble("SALDO");

                                modDetalle.addRow(new Object[]{
                                    rs.getString("MAT"), rs.getString("NCPTO"), rs.getString("DCPTO"),
                                    rs.getString("GRADO"), rs.getString("GRUPO"), df.format(aPagar),
                                    rs.getString("FVEN"), df.format(pagado),
                                    rs.getString("FEC_PAGO") != null ? rs.getString("FEC_PAGO") : "",
                                    rs.getString("RECIBO") != null ? rs.getString("RECIBO") : "",
                                    df.format(saldo),
                                    rs.getString("PBEC") != null ? rs.getString("PBEC") + "%" : "0%"
                                });

                                sumAPagar += aPagar;
                                sumPagado += pagado;
                                sumSaldo += saldo;
                            }
                        }
                    }

                    txtTotAPagar.setText(df.format(sumAPagar));
                    txtTotPagado.setText(df.format(sumPagado));
                    txtTotSaldo.setText(df.format(sumSaldo));

                    if (modDetalle.getRowCount() == 0) {
                        JOptionPane.showMessageDialog(this, "No se encontraron registros con los criterios especificados.", "Información", JOptionPane.INFORMATION_MESSAGE);
                    }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al consultar estado de cuenta: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Evento Imprimir
        btnImprimir.addActionListener(e -> {
            if (modDetalle.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No hay movimientos para imprimir.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                PrinterJob job = PrinterJob.getPrinterJob();
                job.setJobName("Estado de Cuenta Detallado");

                job.setPrintable((g, pf, pageIndex) -> {
                    int filasPorPagina = 25;
                    int totalPaginas = (int) Math.ceil((double) modDetalle.getRowCount() / filasPorPagina);
                    if (totalPaginas == 0) totalPaginas = 1;

                    if (pageIndex >= totalPaginas) return Printable.NO_SUCH_PAGE;

                    Graphics2D g2d = (Graphics2D) g;
                    g2d.translate(pf.getImageableX(), pf.getImageableY());

                    int y = 40;
                    g2d.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
                    g2d.drawString("UNIDAD ESCOLAR BENAVENTE, A.C.", 40, y); y += 20;
                    g2d.drawString("ESTADO DE CUENTA DETALLADO", 40, y); y += 15;
                    g2d.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 8));
                    g2d.drawString("Ciclo: " + cmbCiclo.getSelectedItem()
                            + " | Usuario: " + usuarioLogueado, 40, y); y += 12;
                    g2d.drawLine(40, y, 530, y); y += 15;

                    g2d.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 8));
                    g2d.drawString("MATRÍCULA", 40, y);
                    g2d.drawString("CONCEPTO", 100, y);
                    g2d.drawString("A PAGAR", 260, y);
                    g2d.drawString("PAGADO", 330, y);
                    g2d.drawString("FEC PAGO", 400, y);
                    g2d.drawString("SALDO", 470, y);
                    y += 5;
                    g2d.drawLine(40, y, 530, y); y += 12;

                    g2d.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 7));
                    int inicioRow = pageIndex * filasPorPagina;
                    int finRow = Math.min(inicioRow + filasPorPagina, modDetalle.getRowCount());

                    for (int r = inicioRow; r < finRow; r++) {
                        g2d.drawString(modDetalle.getValueAt(r, 0).toString(), 40, y);
                        
                        String desc = modDetalle.getValueAt(r, 2).toString();
                        if (desc.length() > 28) desc = desc.substring(0, 25) + "...";
                        g2d.drawString(desc, 100, y);

                        g2d.drawString(modDetalle.getValueAt(r, 5).toString(), 260, y);
                        g2d.drawString(modDetalle.getValueAt(r, 7).toString(), 330, y);
                        g2d.drawString(modDetalle.getValueAt(r, 8).toString(), 400, y);
                        g2d.drawString(modDetalle.getValueAt(r, 10).toString(), 470, y);
                        y += 12;
                    }

                    if (pageIndex == totalPaginas - 1) {
                        y += 10;
                        g2d.drawLine(40, y, 530, y); y += 15;
                        g2d.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 8));
                        g2d.drawString("TOTAL A PAGAR: $" + txtTotAPagar.getText(), 40, y);
                        g2d.drawString("TOTAL PAGADO: $" + txtTotPagado.getText(), 220, y);
                        g2d.drawString("TOTAL SALDO: $" + txtTotSaldo.getText(), 400, y);
                    }

                    g2d.drawString("Página " + (pageIndex + 1) + " de " + totalPaginas, 450, 750);
                    return Printable.PAGE_EXISTS;
                });

                if (job.printDialog()) {
                    job.print();
                    JOptionPane.showMessageDialog(this, "Estado de cuenta enviado a la impresora.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
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

        jScrollPane1 = new javax.swing.JScrollPane();
        tblCPagos = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblECDetallado = new javax.swing.JTable();
        btnAddECDetallado = new javax.swing.JButton();
        btnEditECDetallado = new javax.swing.JButton();
        btnDeleteECDetallado = new javax.swing.JButton();

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

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        tblECDetallado.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(tblECDetallado);

        btnAddECDetallado.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAddECDetallado.setForeground(new java.awt.Color(26, 61, 99));
        btnAddECDetallado.setIcon(new javax.swing.ImageIcon(getClass().getResource("/add.png"))); // NOI18N
        btnAddECDetallado.setText("Añadir");
        btnAddECDetallado.addActionListener(this::btnAddECDetalladoActionPerformed);

        btnEditECDetallado.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEditECDetallado.setForeground(new java.awt.Color(26, 61, 99));
        btnEditECDetallado.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edit.png"))); // NOI18N
        btnEditECDetallado.setText("Editar");
        btnEditECDetallado.setMaximumSize(new java.awt.Dimension(93, 31));
        btnEditECDetallado.setMinimumSize(new java.awt.Dimension(93, 31));
        btnEditECDetallado.addActionListener(this::btnEditECDetalladoActionPerformed);

        btnDeleteECDetallado.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDeleteECDetallado.setForeground(new java.awt.Color(26, 61, 99));
        btnDeleteECDetallado.setIcon(new javax.swing.ImageIcon(getClass().getResource("/delete.png"))); // NOI18N
        btnDeleteECDetallado.setText("Eliminar");
        btnDeleteECDetallado.addActionListener(this::btnDeleteECDetalladoActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAddECDetallado)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditECDetallado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteECDetallado)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddECDetallado, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditECDetallado, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteECDetallado, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void btnAddECDetalladoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddECDetalladoActionPerformed
    }//GEN-LAST:event_btnAddECDetalladoActionPerformed

    private void btnEditECDetalladoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditECDetalladoActionPerformed
    }//GEN-LAST:event_btnEditECDetalladoActionPerformed

    private void btnDeleteECDetalladoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteECDetalladoActionPerformed
    }//GEN-LAST:event_btnDeleteECDetalladoActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddECDetallado;
    private javax.swing.JButton btnDeleteECDetallado;
    private javax.swing.JButton btnEditECDetallado;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable tblCPagos;
    private javax.swing.JTable tblECDetallado;
    // End of variables declaration//GEN-END:variables
}
