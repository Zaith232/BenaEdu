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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author b17za
 */
public class Resultado_Grupo extends javax.swing.JPanel {
private JComboBox<String> cmbCia;
    private JTextField txtAno, txtMes, txtCuentaSel;
    private JComboBox<String> cmbReal, cmbPpto;
    private JComboBox<String>[] cmbAgrupar = new JComboBox[5];
    private JRadioButton[] rbsNivel = new JRadioButton[10];
    private DefaultTableModel modResultados;
    private JTable tblResultados;
    /**
     * Creates new form Resultado_Grupo
     */
    public Resultado_Grupo() {
        initComponents();
        construirInterfazEstadoResultadosGrupo();
    }
    
    private void construirInterfazEstadoResultadosGrupo() {
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

        Object[][] dCtas = cargarDatosMultiple.apply("SELECT CCTA, CDES FROM tmctas WHERE TCTA IN ('I','G') ORDER BY CCTA", 2);

        // --- 1. DATOS DE SELECCIÓN (Panel Superior) ---
        JPanel pnlSel = new JPanel(null);
        pnlSel.setBorder(BorderFactory.createEtchedBorder());
        pnlSel.setBounds(10, 10, 915, 150);

        // Compañía
        pnlSel.add(new JLabel("Compañía")).setBounds(20, 15, 70, 25);
        cmbCia = new JComboBox<>();
        cmbCia.setBounds(90, 15, 60, 25);
        JLabel lblCiaDesc = new JLabel("UNIDAD ESCOLAR BENAVENTE, A.C.");
        lblCiaDesc.setBounds(160, 15, 250, 25);

        try {
            ConDB db = new ConDB();
            Connection con = db.Conectar();
            if (con != null) {
                ResultSet rsCia = con.prepareStatement("SELECT CIA, NCIA FROM tmcias ORDER BY CIA").executeQuery();
                while(rsCia.next()) {
                    cmbCia.addItem(rsCia.getString("CIA"));
                    lblCiaDesc.setText(rsCia.getString("NCIA"));
                }
                rsCia.close(); db.Cerrar();
            }
        } catch (Exception ex) { cmbCia.addItem("12"); }

        pnlSel.add(cmbCia); pnlSel.add(lblCiaDesc);

        // Año y Mes
        pnlSel.add(new JLabel("Año")).setBounds(20, 45, 50, 25);
        txtAno = new JTextField("2026"); txtAno.setBounds(90, 45, 60, 25);
        pnlSel.add(txtAno);

        pnlSel.add(new JLabel("Mes")).setBounds(20, 75, 50, 25);
        txtMes = new JTextField("5"); txtMes.setBounds(90, 75, 60, 25);
        pnlSel.add(txtMes);

        // Niveles (1 al 9)
        pnlSel.add(new JLabel("Nivel")).setBounds(170, 45, 40, 25);
        ButtonGroup bgNivel = new ButtonGroup();
        int xPosNivel = 210;
        for (int i = 1; i <= 9; i++) {
            rbsNivel[i] = new JRadioButton(String.valueOf(i));
            if (i == 3) rbsNivel[i].setSelected(true);
            rbsNivel[i].setBounds(xPosNivel, 45, 40, 25);
            bgNivel.add(rbsNivel[i]);
            pnlSel.add(rbsNivel[i]);
            xPosNivel += 45;
        }

        // Llenar Combos para "Agrupar" desde tmctas.CODAGR y tgrub.RUB
        java.util.List<String> listaAgrupadores = new java.util.ArrayList<>();
        listaAgrupadores.add("");
        try {
            ConDB db = new ConDB();
            Connection con = db.Conectar();
            if (con != null) {
                ResultSet rsCod = con.prepareStatement("SELECT DISTINCT CODAGR FROM tmctas WHERE CODAGR IS NOT NULL AND CODAGR != '' ORDER BY CODAGR").executeQuery();
                while(rsCod.next()) listaAgrupadores.add(rsCod.getString("CODAGR"));
                rsCod.close();

                ResultSet rsRub = con.prepareStatement("SELECT RUB FROM tgrub ORDER BY RUB").executeQuery();
                while(rsRub.next()) {
                    String rub = rsRub.getString("RUB");
                    if(!listaAgrupadores.contains(rub)) listaAgrupadores.add(rub);
                }
                rsRub.close(); db.Cerrar();
            }
        } catch(Exception ex) {}

        pnlSel.add(new JLabel("Agrupar")).setBounds(20, 105, 60, 25);
        int xPosAgrupar = 90;
        for (int i = 0; i < 5; i++) {
            cmbAgrupar[i] = new JComboBox<>(listaAgrupadores.toArray(new String[0]));
            cmbAgrupar[i].setBounds(xPosAgrupar, 105, 95, 25);
            pnlSel.add(cmbAgrupar[i]);
            xPosAgrupar += 105;
        }

        // Panel Lateral Derecho (Tipo Contabilidad desde tmclas)
        JPanel pnlTipoCont = new JPanel(null);
        pnlTipoCont.setBorder(BorderFactory.createTitledBorder("Tipo de Contabilidad"));
        pnlTipoCont.setBounds(690, 10, 205, 60);
        
        pnlTipoCont.add(new JLabel("Real")).setBounds(20, 15, 40, 20);
        pnlTipoCont.add(new JLabel("Ppto.")).setBounds(110, 15, 40, 20);
        
        cmbReal = new JComboBox<>(new String[]{"MN"});
        cmbReal.setBounds(10, 32, 80, 22);
        cmbPpto = new JComboBox<>(new String[]{"PL"});
        cmbPpto.setBounds(105, 32, 80, 22);

        pnlTipoCont.add(cmbReal); pnlTipoCont.add(cmbPpto);
        pnlSel.add(pnlTipoCont);

        // Botón Selecciona (Abre catálogo de cuentas tmctas)
        txtCuentaSel = new JTextField(); txtCuentaSel.setBounds(0,0,0,0);
        JButton btnSelecciona = new JButton("Selecciona Cuentas");
        btnSelecciona.setBounds(690, 75, 205, 25);
        buscador.configurar(txtCuentaSel, null, btnSelecciona, dCtas, new String[]{"Cuenta", "Descripción"}, new int[]{100, 200});
        pnlSel.add(btnSelecciona);

        JButton btnFiltra = new JButton("Filtrar Información");
        btnFiltra.setBounds(690, 105, 205, 25);
        pnlSel.add(btnFiltra);

        this.add(pnlSel);

        // --- 2. TABLA DE RESULTADOS ---
        modResultados = new DefaultTableModel(
            new Object[][]{}, 
            new String[]{"Agrupador / Cuenta", "Descripción", "Real Mes", "Ppto Mes", "Dif. Mes", "Real Acum. Año", "Ppto Acum. Año", "Dif. Acum."}
        ) { @Override public boolean isCellEditable(int r, int c) { return false; } };

        tblResultados = new JTable(modResultados);
        tblResultados.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 
        tblResultados.getColumnModel().getColumn(0).setPreferredWidth(120);
        tblResultados.getColumnModel().getColumn(1).setPreferredWidth(230);
        tblResultados.getColumnModel().getColumn(2).setPreferredWidth(95);
        tblResultados.getColumnModel().getColumn(3).setPreferredWidth(95);
        tblResultados.getColumnModel().getColumn(4).setPreferredWidth(95);
        tblResultados.getColumnModel().getColumn(5).setPreferredWidth(110);
        tblResultados.getColumnModel().getColumn(6).setPreferredWidth(110);
        tblResultados.getColumnModel().getColumn(7).setPreferredWidth(110);

        JPanel pnlTabla = new JPanel(null);
        pnlTabla.setBorder(BorderFactory.createEtchedBorder());
        pnlTabla.setBounds(10, 170, 915, 390);
        
        JScrollPane scrollResultados = new JScrollPane(tblResultados);
        scrollResultados.setBounds(10, 10, 895, 370);
        pnlTabla.add(scrollResultados);
        
        this.add(pnlTabla);

        // --- 3. BOTONES INFERIORES ---
        JButton btnExportar = new JButton("Exportar CSV");
        btnExportar.setBounds(330, 565, 120, 35);
        JButton btnImprimir = new JButton("Imprimir");
        btnImprimir.setBounds(460, 565, 110, 35);
        JButton btnSalir = new JButton("Salir");
        btnSalir.setBounds(580, 565, 110, 35);

        this.add(btnExportar);
        this.add(btnImprimir);
        this.add(btnSalir);

        // --- 4. EVENTOS Y LÓGICA DE NEGOCIO ---
        btnSalir.addActionListener(e -> {
            this.removeAll();
            this.revalidate();
            this.repaint();
        });

        // Evento Filtrar Información conectado a tsctas + tmctas + tgrub
        btnFiltra.addActionListener(e -> {
            modResultados.setRowCount(0);

            String cia = cmbCia.getSelectedItem() != null ? cmbCia.getSelectedItem().toString() : "12";
            String ano = txtAno.getText().trim();
            String mesStr = txtMes.getText().trim();
            String ctaFiltro = txtCuentaSel.getText().trim();

            int mes = 1;
            try { mes = Integer.parseInt(mesStr); } catch(Exception ex) {}

            int nivelSeleccionado = 3;
            for (int i = 1; i <= 9; i++) {
                if (rbsNivel[i].isSelected()) { nivelSeleccionado = i; break; }
            }

            try {
                ConDB db = new ConDB();
                Connection con = db.Conectar();
                if (con != null) {
                    String colMes = String.format("MN%02d", mes);

                    StringBuilder sumMeses = new StringBuilder();
                    for (int m = 1; m <= mes; m++) {
                        sumMeses.append(m == 1 ? "" : " + ").append(String.format("s.%s", String.format("MN%02d", m)));
                    }

                    // Consulta uniendo tsctas y tmctas agrupando por CODAGR
                    StringBuilder sqlReal = new StringBuilder(
                        "SELECT COALESCE(t.CODAGR, 'SIN_AGRUPAR') AS AGRUPADOR, t.CCTA, t.CDES, t.NATCTA, " +
                        "SUM(s." + colMes + ") AS MES_REAL, SUM(" + sumMeses + ") AS ACUM_REAL " +
                        "FROM tmctas t " +
                        "INNER JOIN tsctas s ON t.CCTA = s.CCTA AND s.CIA = t.CIA " +
                        "WHERE s.CIA = ? AND s.ANO = ? AND s.TCONT = 'MN' AND t.CNIV <= ? "
                    );

                    if (!ctaFiltro.isEmpty()) sqlReal.append(" AND t.CCTA = ?");
                    sqlReal.append(" GROUP BY COALESCE(t.CODAGR, 'SIN_AGRUPAR'), t.CCTA, t.CDES, t.NATCTA ORDER BY AGRUPADOR, t.CCTA");

                    PreparedStatement psReal = con.prepareStatement(sqlReal.toString());
                    psReal.setString(1, cia);
                    psReal.setString(2, ano);
                    psReal.setInt(3, nivelSeleccionado);
                    if (!ctaFiltro.isEmpty()) psReal.setString(4, ctaFiltro);

                    ResultSet rsReal = psReal.executeQuery();

                    java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");
                    boolean hayRegistros = false;

                    while (rsReal.next()) {
                        String agrupador = rsReal.getString("AGRUPADOR");
                        String ccta = rsReal.getString("CCTA");
                        String cdes = rsReal.getString("CDES");
                        double mesReal = rsReal.getDouble("MES_REAL");
                        double acumReal = rsReal.getDouble("ACUM_REAL");

                        // Obtener equivalente de Presupuesto (TCONT = 'PL')
                        double mesPpto = 0.0;
                        double acumPpto = 0.0;

                        String sqlPpto = "SELECT SUM(" + colMes + ") AS MES_PPTO, SUM(" + sumMeses + ") AS ACUM_PPTO " +
                                         "FROM tsctas s WHERE s.CIA = ? AND s.ANO = ? AND s.CCTA = ? AND s.TCONT = 'PL'";
                        PreparedStatement psPpto = con.prepareStatement(sqlPpto);
                        psPpto.setString(1, cia);
                        psPpto.setString(2, ano);
                        psPpto.setString(3, ccta);
                        ResultSet rsPpto = psPpto.executeQuery();
                        if (rsPpto.next()) {
                            mesPpto = rsPpto.getDouble("MES_PPTO");
                            acumPpto = rsPpto.getDouble("ACUM_PPTO");
                        }
                        rsPpto.close(); psPpto.close();

                        double difMes = mesReal - mesPpto;
                        double difAcum = acumReal - acumPpto;

                        modResultados.addRow(new Object[]{
                            agrupador + " / " + ccta,
                            cdes,
                            df.format(mesReal),
                            df.format(mesPpto),
                            df.format(difMes),
                            df.format(acumReal),
                            df.format(acumPpto),
                            df.format(difAcum)
                        });

                        hayRegistros = true;
                    }

                    rsReal.close(); psReal.close(); db.Cerrar();

                    if (!hayRegistros) {
                        JOptionPane.showMessageDialog(this, "No se encontraron saldos para el Estado de Resultados por Grupo.", "Información", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al consultar estado de resultados por grupo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Evento Exportar CSV
        btnExportar.addActionListener(e -> {
            if (modResultados.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No hay datos para exportar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
            chooser.setSelectedFile(new java.io.File("Estado_Resultados_Grupo.csv"));
            if (chooser.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION) {
                try (java.io.FileWriter writer = new java.io.FileWriter(chooser.getSelectedFile())) {
                    for (int i = 0; i < modResultados.getColumnCount(); i++) {
                        writer.write(modResultados.getColumnName(i) + (i == modResultados.getColumnCount() - 1 ? "" : ","));
                    }
                    writer.write("\n");

                    for (int r = 0; r < modResultados.getRowCount(); r++) {
                        for (int c = 0; c < modResultados.getColumnCount(); c++) {
                            Object val = modResultados.getValueAt(r, c);
                            writer.write((val != null ? val.toString().replace(",", "") : "") + (c == modResultados.getColumnCount() - 1 ? "" : ","));
                        }
                        writer.write("\n");
                    }
                    JOptionPane.showMessageDialog(this, "Reporte exportado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al exportar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Evento Imprimir
        btnImprimir.addActionListener(e -> {
            if (modResultados.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No hay información para imprimir.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
                job.setJobName("Estado de Resultados por Grupo");

                job.setPrintable((g, pf, pageIndex) -> {
                    int filasPorPagina = 28;
                    int totalPaginas = (int) Math.ceil((double) modResultados.getRowCount() / filasPorPagina);
                    if (totalPaginas == 0) totalPaginas = 1;

                    if (pageIndex >= totalPaginas) return java.awt.print.Printable.NO_SUCH_PAGE;

                    java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
                    g2d.translate(pf.getImageableX(), pf.getImageableY());

                    int y = 40;
                    g2d.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
                    g2d.drawString("UNIDAD ESCOLAR BENAVENTE, A.C.", 40, y); y += 20;
                    g2d.drawString("ESTADO DE RESULTADOS POR GRUPO - AÑO " + txtAno.getText() + " / MES " + txtMes.getText(), 40, y); y += 15;
                    g2d.drawLine(40, y, 530, y); y += 15;

                    g2d.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 8));
                    g2d.drawString("AGRUPADOR / CUENTA", 40, y);
                    g2d.drawString("DESCRIPCIÓN", 160, y);
                    g2d.drawString("REAL MES", 330, y);
                    g2d.drawString("PPTO MES", 400, y);
                    g2d.drawString("DIF MES", 470, y);
                    y += 5;
                    g2d.drawLine(40, y, 530, y); y += 12;

                    g2d.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 7));
                    int inicioRow = pageIndex * filasPorPagina;
                    int finRow = Math.min(inicioRow + filasPorPagina, modResultados.getRowCount());

                    for (int r = inicioRow; r < finRow; r++) {
                        g2d.drawString(modResultados.getValueAt(r, 0).toString(), 40, y);

                        String desc = modResultados.getValueAt(r, 1) != null ? modResultados.getValueAt(r, 1).toString() : "";
                        if (desc.length() > 25) desc = desc.substring(0, 22) + "...";
                        g2d.drawString(desc, 160, y);

                        g2d.drawString(modResultados.getValueAt(r, 2).toString(), 330, y);
                        g2d.drawString(modResultados.getValueAt(r, 3).toString(), 400, y);
                        g2d.drawString(modResultados.getValueAt(r, 4).toString(), 470, y);
                        y += 12;
                    }

                    g2d.drawString("Página " + (pageIndex + 1) + " de " + totalPaginas, 450, 750);
                    return java.awt.print.Printable.PAGE_EXISTS;
                });

                if (job.printDialog()) {
                    job.print();
                    JOptionPane.showMessageDialog(this, "Estado de resultados por grupo enviado a la impresora.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
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
        tblRGrupo = new javax.swing.JTable();
        btnAddRGrupo = new javax.swing.JButton();
        btnEditRGrupo = new javax.swing.JButton();
        btnDeleteRGrupo = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        tblRGrupo.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tblRGrupo);

        btnAddRGrupo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAddRGrupo.setForeground(new java.awt.Color(26, 61, 99));
        btnAddRGrupo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/add.png"))); // NOI18N
        btnAddRGrupo.setText("Añadir");
        btnAddRGrupo.addActionListener(this::btnAddRGrupoActionPerformed);

        btnEditRGrupo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEditRGrupo.setForeground(new java.awt.Color(26, 61, 99));
        btnEditRGrupo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edit.png"))); // NOI18N
        btnEditRGrupo.setText("Editar");
        btnEditRGrupo.setMaximumSize(new java.awt.Dimension(93, 31));
        btnEditRGrupo.setMinimumSize(new java.awt.Dimension(93, 31));
        btnEditRGrupo.addActionListener(this::btnEditRGrupoActionPerformed);

        btnDeleteRGrupo.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDeleteRGrupo.setForeground(new java.awt.Color(26, 61, 99));
        btnDeleteRGrupo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/delete.png"))); // NOI18N
        btnDeleteRGrupo.setText("Eliminar");
        btnDeleteRGrupo.addActionListener(this::btnDeleteRGrupoActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAddRGrupo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditRGrupo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteRGrupo)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddRGrupo, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditRGrupo, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteRGrupo, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void btnAddRGrupoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddRGrupoActionPerformed
    }//GEN-LAST:event_btnAddRGrupoActionPerformed

    private void btnEditRGrupoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditRGrupoActionPerformed
    
    }//GEN-LAST:event_btnEditRGrupoActionPerformed

    private void btnDeleteRGrupoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteRGrupoActionPerformed
    }//GEN-LAST:event_btnDeleteRGrupoActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddRGrupo;
    private javax.swing.JButton btnDeleteRGrupo;
    private javax.swing.JButton btnEditRGrupo;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblRGrupo;
    // End of variables declaration//GEN-END:variables
}
