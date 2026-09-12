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
import javax.swing.JButton;
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
public class Alumnos_Becados extends javax.swing.JPanel {

    /**
     * Creates new form Alumnos_Becados
     */
    public Alumnos_Becados() {
        initComponents();
        construirInterfazAlumnosBecados();
    }
private void construirInterfazAlumnosBecados() {
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

      // Carga de catálogos para buscadores
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

        Object[][] dCiclo = cargarDatosMultiple.apply(
                "SELECT CESC, MAX(CDSC) FROM tescesc GROUP BY CESC "
                + "ORDER BY MAX(CASE WHEN CURDATE() BETWEEN FINI AND FFIN THEN 1 ELSE 0 END) DESC, CESC DESC", 2);
        Object[][] dBeca  = cargarDatosMultiple.apply("SELECT CBECA, DBECA FROM tesbege ORDER BY CBECA", 2);

        // --- 1. DATOS DE SELECCIÓN ---
        JPanel pnlSel = new JPanel(null);
        pnlSel.setBorder(BorderFactory.createTitledBorder("Datos de Selección - UNIDAD ESCOLAR BENAVENTE"));
        pnlSel.setBounds(10, 10, 830, 95);

        // Compañía
        pnlSel.add(new JLabel("Compañía")).setBounds(15, 20, 70, 25);
        JComboBox<String> cmbCia = new JComboBox<>(); cmbCia.setBounds(85, 20, 60, 25);
        
        // Centro Costos
        pnlSel.add(new JLabel("Centro Costos")).setBounds(155, 20, 90, 25);
        JComboBox<String> cmbCC = new JComboBox<>(); cmbCC.setBounds(245, 20, 80, 25);

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

        pnlSel.add(cmbCia); pnlSel.add(cmbCC);

        // Ciclo Escolar
        pnlSel.add(new JLabel("Ciclo Escolar")).setBounds(335, 20, 80, 25);
        String cicloInicial = dCiclo.length > 0 && dCiclo[0][0] != null ? dCiclo[0][0].toString() : "";
        JTextField txtCiclo = new JTextField(cicloInicial); txtCiclo.setBounds(415, 20, 60, 25);
        JButton btnCiclo = new JButton("▼"); btnCiclo.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnCiclo.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnCiclo.setBounds(475, 20, 20, 25);
        buscador.configurar(txtCiclo, null, btnCiclo, dCiclo, new String[]{"Clave", "Descripción"}, new int[]{60, 200});
        pnlSel.add(txtCiclo); pnlSel.add(btnCiclo);

        // Grado
        pnlSel.add(new JLabel("Grado")).setBounds(505, 20, 45, 25);
        JComboBox<String> cmbGrado = new JComboBox<>(new String[]{"", "1J", "2J", "3J", "1P", "2P", "3P", "4P", "5P", "6P", "1S", "2S", "3S", "1B", "2B", "3B"});
        cmbGrado.setBounds(550, 20, 60, 25);
        pnlSel.add(cmbGrado);

        // Beca
        pnlSel.add(new JLabel("Beca")).setBounds(15, 55, 40, 25);
        JTextField txtBeca = new JTextField(); txtBeca.setBounds(85, 55, 80, 25);
        JButton btnBeca = new JButton("▼"); btnBeca.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnBeca.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnBeca.setBounds(165, 55, 20, 25);
        buscador.configurar(txtBeca, null, btnBeca, dBeca, new String[]{"Clave", "Descripción"}, new int[]{80, 220});
        pnlSel.add(txtBeca); pnlSel.add(btnBeca);

        // Botón Filtra Información
        JButton btnFiltra = new JButton("Filtra Información");
        btnFiltra.setBounds(650, 50, 160, 30);
        pnlSel.add(btnFiltra);

        this.add(pnlSel);

        // --- 2. TABLA DE ALUMNOS BECADOS ---
        // Columnas en el orden exacto especificado
        DefaultTableModel modBecados = new DefaultTableModel(
            new Object[][]{}, 
            new String[]{
                "Cia", "C. Costos", "Sección", "C. Esc.", "Grado", 
                "Turno", "Grupo", "Matrícula", "Nombre Alumno", 
                "Tipo", "Beca", "Imp. Concepto", "Imp. Beca", "Imp. a Pagar"
            }
        ) { @Override public boolean isCellEditable(int r, int c) { return false; } };

        JTable tblBecados = new JTable(modBecados);
        tblBecados.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Tamaños sugeridos
        tblBecados.getColumnModel().getColumn(0).setPreferredWidth(40);  // Cia
        tblBecados.getColumnModel().getColumn(1).setPreferredWidth(65);  // C. Costos
        tblBecados.getColumnModel().getColumn(2).setPreferredWidth(55);  // Sección
        tblBecados.getColumnModel().getColumn(3).setPreferredWidth(55);  // C. Esc.
        tblBecados.getColumnModel().getColumn(4).setPreferredWidth(45);  // Grado
        tblBecados.getColumnModel().getColumn(5).setPreferredWidth(45);  // Turno
        tblBecados.getColumnModel().getColumn(6).setPreferredWidth(45);  // Grupo
        tblBecados.getColumnModel().getColumn(7).setPreferredWidth(80);  // Matrícula
        tblBecados.getColumnModel().getColumn(8).setPreferredWidth(210); // Nombre Alumno
        tblBecados.getColumnModel().getColumn(9).setPreferredWidth(40);  // Tipo
        tblBecados.getColumnModel().getColumn(10).setPreferredWidth(70); // Beca
        tblBecados.getColumnModel().getColumn(11).setPreferredWidth(90); // Imp. Concepto
        tblBecados.getColumnModel().getColumn(12).setPreferredWidth(90); // Imp. Beca
        tblBecados.getColumnModel().getColumn(13).setPreferredWidth(90); // Imp. a Pagar

        JPanel pnlTabla = new JPanel(null);
        pnlTabla.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Alumnos Becados", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
        pnlTabla.setBounds(10, 110, 830, 270);

        JScrollPane scrollTabla = new JScrollPane(tblBecados);
        scrollTabla.setBounds(10, 20, 810, 240);
        pnlTabla.add(scrollTabla);

        this.add(pnlTabla);

        // --- 3. PANEL DE TOTALES ---
        JPanel pnlTotales = new JPanel(null);
        pnlTotales.setBounds(10, 385, 830, 45);

        JLabel lblLblTotales = new JLabel("Totales");
        lblLblTotales.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
        lblLblTotales.setBounds(10, 10, 60, 25);
        pnlTotales.add(lblLblTotales);

        pnlTotales.add(new JLabel("Alumnos")).setBounds(80, 10, 60, 25);
        JTextField txtTotAlumnos = new JTextField("0");
        txtTotAlumnos.setHorizontalAlignment(JTextField.RIGHT); txtTotAlumnos.setEditable(false);
        txtTotAlumnos.setBounds(140, 10, 60, 25);
        pnlTotales.add(txtTotAlumnos);

        pnlTotales.add(new JLabel("Imp. Concepto")).setBounds(220, 10, 90, 25);
        JTextField txtTotConcepto = new JTextField("0.00");
        txtTotConcepto.setHorizontalAlignment(JTextField.RIGHT); txtTotConcepto.setEditable(false);
        txtTotConcepto.setBounds(310, 10, 90, 25);
        pnlTotales.add(txtTotConcepto);

        pnlTotales.add(new JLabel("Imp. Beca")).setBounds(420, 10, 70, 25);
        JTextField txtTotBeca = new JTextField("0.00");
        txtTotBeca.setHorizontalAlignment(JTextField.RIGHT); txtTotBeca.setEditable(false);
        txtTotBeca.setBounds(490, 10, 90, 25);
        pnlTotales.add(txtTotBeca);

        pnlTotales.add(new JLabel("Imp. a Pagar")).setBounds(600, 10, 80, 25);
        JTextField txtTotPagar = new JTextField("0.00");
        txtTotPagar.setHorizontalAlignment(JTextField.RIGHT); txtTotPagar.setEditable(false);
        txtTotPagar.setBounds(680, 10, 100, 25);
        pnlTotales.add(txtTotPagar);

        this.add(pnlTotales);

        // --- 4. BOTONES INFERIORES ---
        JButton btnImprimir = new JButton("Imprimir");
        btnImprimir.setBounds(310, 440, 100, 35);
        JButton btnSalir = new JButton("Salir");
        btnSalir.setBounds(430, 440, 100, 35);

        this.add(btnImprimir);
        this.add(btnSalir);

        // --- 5. EVENTOS ---
        btnSalir.addActionListener(e -> {
            this.removeAll();
            this.revalidate();
            this.repaint();
        });

        // Evento Filtrar Información
        btnFiltra.addActionListener(e -> {
            modBecados.setRowCount(0);
            txtTotAlumnos.setText("0");
            txtTotConcepto.setText("0.00");
            txtTotBeca.setText("0.00");
            txtTotPagar.setText("0.00");

            String cia = cmbCia.getSelectedItem() != null ? cmbCia.getSelectedItem().toString() : "";
            String cc = cmbCC.getSelectedItem() != null ? cmbCC.getSelectedItem().toString() : "";
            String ciclo = txtCiclo.getText().trim();
            String gradoSel = cmbGrado.getSelectedItem() != null ? cmbGrado.getSelectedItem().toString() : "";
            String becaFiltro = txtBeca.getText().trim();

            if (ciclo.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor introduzca un Ciclo Escolar.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                ConDB db = new ConDB();
                Connection con = db.Conectar();
                if (con != null) {
                    StringBuilder sql = new StringBuilder(
                        "WITH inscripciones AS (" +
                        " SELECT x.*, ROW_NUMBER() OVER (PARTITION BY x.CIA, x.CC, x.CESC, x.MAT " +
                        " ORDER BY x.FEAC DESC, x.HOAC DESC) AS FILA " +
                        " FROM tesaxce x WHERE x.CESC = ? AND TRIM(COALESCE(x.CBECA, '')) <> '' " +
                        " AND COALESCE(NULLIF(TRIM(x.SITALU), ''), 'CUR') = 'CUR'" +
                        "), alumnos AS (" +
                        " SELECT a.*, ROW_NUMBER() OVER (PARTITION BY a.MAT ORDER BY a.FEAC DESC, a.HOAC DESC) AS FILA " +
                        " FROM tesalum a" +
                        "), cargos AS (" +
                        " SELECT c.CIA, c.CC, c.CESC, c.MAT, SUM(COALESCE(c.IMPMN, 0)) AS IMP_CONCEPTO, " +
                        " SUM(COALESCE(c.IBECMN, 0)) AS IMP_BECA, " +
                        " SUM(COALESCE(c.IMPTMN, COALESCE(c.IMPMN, 0) - COALESCE(c.IDSCMN, 0) " +
                        " + COALESCE(c.IRECMN, 0) - COALESCE(c.IBECMN, 0))) AS IMP_PAGAR " +
                        " FROM tescalu c WHERE COALESCE(c.MCAN, '') = '' " +
                        " GROUP BY c.CIA, c.CC, c.CESC, c.MAT" +
                        ") SELECT x.CIA, x.CC, x.SECC, x.CESC, x.GRADO, x.TURNO, x.GRUPO, x.MAT, " +
                        "COALESCE(NULLIF(a.NOMCOM, ''), CONCAT_WS(' ', a.APATE, a.AMATE, a.NOMA)) AS NOMBRE, " +
                        "COALESCE(NULLIF(x.TBECA, ''), 'B') AS TPO, x.CBECA, " +
                        "COALESCE(c.IMP_CONCEPTO, 0) AS IMP_CONCEPTO, COALESCE(c.IMP_BECA, 0) AS IMP_BECA, " +
                        "COALESCE(c.IMP_PAGAR, 0) AS IMP_PAGAR " +
                        "FROM inscripciones x INNER JOIN alumnos a ON a.MAT = x.MAT AND a.FILA = 1 " +
                        "LEFT JOIN cargos c ON c.CIA = x.CIA AND c.CC = x.CC AND c.CESC = x.CESC AND c.MAT = x.MAT " +
                        "WHERE x.FILA = 1 "
                    );

                    if (!cia.isEmpty()) sql.append(" AND x.CIA = ?");
                    if (!cc.isEmpty()) sql.append(" AND x.CC = ?");
                    if (!gradoSel.isEmpty()) sql.append(" AND x.GRADO = ?");
                    if (!becaFiltro.isEmpty()) sql.append(" AND x.CBECA = ?");

                    sql.append(" ORDER BY x.GRADO, x.GRUPO, a.APATE, a.AMATE, x.MAT");

                    PreparedStatement ps = con.prepareStatement(sql.toString());
                    int p = 1;
                    ps.setString(p++, ciclo);
                    if (!cia.isEmpty()) ps.setString(p++, cia);
                    if (!cc.isEmpty()) ps.setString(p++, cc);
                    if (!gradoSel.isEmpty()) ps.setString(p++, gradoSel);
                    if (!becaFiltro.isEmpty()) ps.setString(p++, becaFiltro);

                    ResultSet rs = ps.executeQuery();
                    java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");

                    double sumConcepto = 0.0, sumBeca = 0.0, sumPagar = 0.0;
                    int cantAlumnos = 0;

                    while (rs.next()) {
                        double impConc = rs.getDouble("IMP_CONCEPTO");
                        double impBeca = rs.getDouble("IMP_BECA");
                        double impPagar = rs.getDouble("IMP_PAGAR");

                        Object[] fila = new Object[14];
                        fila[0] = rs.getString("CIA");
                        fila[1] = rs.getString("CC");
                        fila[2] = rs.getString("SECC");
                        fila[3] = rs.getString("CESC");
                        fila[4] = rs.getString("GRADO");
                        fila[5] = rs.getString("TURNO");
                        fila[6] = rs.getString("GRUPO");
                        fila[7] = rs.getString("MAT");
                        fila[8] = rs.getString("NOMBRE");
                        fila[9] = rs.getString("TPO");
                        fila[10] = rs.getString("CBECA");
                        fila[11] = df.format(impConc);
                        fila[12] = df.format(impBeca);
                        fila[13] = df.format(impPagar);

                        sumConcepto += impConc;
                        sumBeca += impBeca;
                        sumPagar += impPagar;
                        cantAlumnos++;

                        modBecados.addRow(fila);
                    }

                    rs.close(); ps.close(); db.Cerrar();

                    txtTotAlumnos.setText(String.valueOf(cantAlumnos));
                    txtTotConcepto.setText(df.format(sumConcepto));
                    txtTotBeca.setText(df.format(sumBeca));
                    txtTotPagar.setText(df.format(sumPagar));

                    if (modBecados.getRowCount() == 0) {
                        JOptionPane.showMessageDialog(this, "No se encontraron alumnos becados con los criterios indicados.", "Información", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al consultar alumnos becados: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Evento Imprimir
        btnImprimir.addActionListener(e -> {
            if (modBecados.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No hay datos para imprimir.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
                job.setJobName("Reporte de Alumnos Becados");

                job.setPrintable((g, pf, pageIndex) -> {
                    int filasPorPagina = 28;
                    int totalPaginas = (int) Math.ceil((double) modBecados.getRowCount() / filasPorPagina);
                    if (totalPaginas == 0) totalPaginas = 1;

                    if (pageIndex >= totalPaginas) return java.awt.print.Printable.NO_SUCH_PAGE;

                    java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
                    g2d.translate(pf.getImageableX(), pf.getImageableY());

                    int y = 40;
                    g2d.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
                    g2d.drawString("UNIDAD ESCOLAR BENAVENTE, A.C.", 40, y); y += 20;
                    g2d.drawString("REPORTE DE ALUMNOS BECADOS - CICLO: " + txtCiclo.getText(), 40, y); y += 15;
                    g2d.drawLine(40, y, 530, y); y += 15;

                    g2d.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 8));
                    g2d.drawString("GRD/GPO", 40, y);
                    g2d.drawString("MATRÍCULA", 90, y);
                    g2d.drawString("NOMBRE DEL ALUMNO", 160, y);
                    g2d.drawString("BECA", 350, y);
                    g2d.drawString("IMP CONCEPTO", 410, y);
                    g2d.drawString("IMP BECA", 480, y);
                    y += 5;
                    g2d.drawLine(40, y, 530, y); y += 12;

                    g2d.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 7));
                    int inicioRow = pageIndex * filasPorPagina;
                    int finRow = Math.min(inicioRow + filasPorPagina, modBecados.getRowCount());

                    for (int r = inicioRow; r < finRow; r++) {
                        String grGpo = modBecados.getValueAt(r, 4).toString() + " " + modBecados.getValueAt(r, 6).toString();
                        g2d.drawString(grGpo, 40, y);
                        g2d.drawString(modBecados.getValueAt(r, 7).toString(), 90, y);

                        String nom = modBecados.getValueAt(r, 8).toString();
                        if (nom.length() > 30) nom = nom.substring(0, 27) + "...";
                        g2d.drawString(nom, 160, y);

                        g2d.drawString(modBecados.getValueAt(r, 10).toString(), 350, y);
                        g2d.drawString(modBecados.getValueAt(r, 11).toString(), 410, y);
                        g2d.drawString(modBecados.getValueAt(r, 12).toString(), 480, y);
                        y += 12;
                    }

                    if (pageIndex == totalPaginas - 1) {
                        y += 10;
                        g2d.drawLine(40, y, 530, y); y += 15;
                        g2d.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 9));
                        g2d.drawString("TOTAL ALUMNOS: " + txtTotAlumnos.getText(), 40, y);
                        g2d.drawString("TOTAL BECA: $" + txtTotBeca.getText(), 280, y);
                        g2d.drawString("TOTAL A PAGAR: $" + txtTotPagar.getText(), 420, y);
                    }

                    g2d.drawString("Página " + (pageIndex + 1) + " de " + totalPaginas, 450, 750);
                    return java.awt.print.Printable.PAGE_EXISTS;
                });

                if (job.printDialog()) {
                    job.print();
                    JOptionPane.showMessageDialog(this, "Reporte de alumnos becados enviado a la impresora.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
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
