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
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
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
public class Estado_Resultados extends javax.swing.JPanel {

    /**
     * Creates new form Estado_Resultados
     */
    public Estado_Resultados() {
        initComponents();
         construirInterfazEstadoResultados();
    }
private void construirInterfazEstadoResultados() {
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

        Object[][] dContab = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'TCONT' ORDER BY CVE", 2);

        // --- PANEL DE FILTROS SUPERIOR ---
        JPanel pnlSel = new JPanel(null);
        pnlSel.setBorder(BorderFactory.createEtchedBorder());
        pnlSel.setBounds(10, 10, 915, 140);

        pnlSel.add(new JLabel("Compañía")).setBounds(20, 15, 80, 25);
        JComboBox<String> cmbCia = new JComboBox<>(new String[]{"12"});
        cmbCia.setBounds(100, 15, 60, 25);
        JLabel lblCiaDesc = new JLabel("UNIDAD ESCOLAR BENAVENTE, A.C.");
        lblCiaDesc.setBounds(170, 15, 250, 25);

        try {
            ConDB db = new ConDB();
            Connection con = db.Conectar();
            if (con != null) {
                ResultSet rsCia = con.prepareStatement("SELECT CIA, NCIA FROM tmcias ORDER BY CIA").executeQuery();
                if(rsCia.next()) {
                    cmbCia.removeAllItems();
                    cmbCia.addItem(rsCia.getString("CIA"));
                    lblCiaDesc.setText(rsCia.getString("NCIA"));
                }
                rsCia.close(); db.Cerrar();
            }
        } catch (Exception ex) {}

        pnlSel.add(cmbCia); pnlSel.add(lblCiaDesc);

        pnlSel.add(new JLabel("Año")).setBounds(20, 50, 50, 25);
        JTextField txtAno = new JTextField("2026"); txtAno.setBounds(100, 50, 60, 25);
        pnlSel.add(txtAno);

        pnlSel.add(new JLabel("Período")).setBounds(180, 50, 50, 25);
        JTextField txtPeriodo = new JTextField("6"); txtPeriodo.setBounds(230, 50, 50, 25);
        pnlSel.add(txtPeriodo);

        pnlSel.add(new JLabel("Tipo Contab.")).setBounds(300, 50, 80, 25);
        JTextField txtContab = new JTextField("MN"); txtContab.setBounds(380, 50, 50, 25);
        JButton btnContab = new JButton("▼"); btnContab.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnContab.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnContab.setBounds(430, 50, 20, 25);
        buscador.configurar(txtContab, null, btnContab, dContab, new String[]{"Clave", "Descripción"}, new int[]{60, 200});
        pnlSel.add(txtContab); pnlSel.add(btnContab);

        // Nivel RadioButtons
        pnlSel.add(new JLabel("Nivel")).setBounds(20, 90, 50, 25);
        ButtonGroup bgNivel = new ButtonGroup();
        JRadioButton[] rbsNivel = new JRadioButton[10];
        int xPosNivel = 70;
        for (int i = 1; i <= 9; i++) {
            rbsNivel[i] = new JRadioButton(String.valueOf(i));
            if (i == 9) rbsNivel[i].setSelected(true);
            rbsNivel[i].setBounds(xPosNivel, 90, 40, 25);
            bgNivel.add(rbsNivel[i]);
            pnlSel.add(rbsNivel[i]);
            xPosNivel += 45;
        }

        JCheckBox chkDetalleCC = new JCheckBox("Ver Detalle por Centro de Costo");
        chkDetalleCC.setBounds(500, 15, 220, 25);
        pnlSel.add(chkDetalleCC);

        JCheckBox chkCeros = new JCheckBox("Incluir Cuentas en Ceros");
        chkCeros.setBounds(500, 45, 200, 25);
        pnlSel.add(chkCeros);

        JButton btnFiltra = new JButton("Filtrar Información");
        btnFiltra.setBounds(730, 90, 165, 35);
        pnlSel.add(btnFiltra);

        this.add(pnlSel);

        // --- TABLA DE ESTADO DE RESULTADOS ---
        DefaultTableModel modResultados = new DefaultTableModel(
            new Object[][]{}, 
            new String[]{"Clave / Rubro", "Descripción de Cuenta / Agrupador", "Importe Mes", "% Mes", "Acumulado Año", "% Acum"}
        ) { @Override public boolean isCellEditable(int r, int c) { return false; } };

        JTable tblResultados = new JTable(modResultados);
        tblResultados.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblResultados.getColumnModel().getColumn(0).setPreferredWidth(120);
        tblResultados.getColumnModel().getColumn(1).setPreferredWidth(360);
        tblResultados.getColumnModel().getColumn(2).setPreferredWidth(110);
        tblResultados.getColumnModel().getColumn(3).setPreferredWidth(70);
        tblResultados.getColumnModel().getColumn(4).setPreferredWidth(120);
        tblResultados.getColumnModel().getColumn(5).setPreferredWidth(70);

        JPanel pnlTabla = new JPanel(null);
        pnlTabla.setBorder(BorderFactory.createEtchedBorder());
        pnlTabla.setBounds(10, 160, 915, 390);

        JScrollPane scrollResultados = new JScrollPane(tblResultados);
        scrollResultados.setBounds(10, 10, 895, 370);
        pnlTabla.add(scrollResultados);

        this.add(pnlTabla);

        // --- BOTONES INFERIORES ---
        JButton btnExportar = new JButton("Exportar CSV");
        btnExportar.setBounds(340, 560, 110, 35);
        JButton btnImprimir = new JButton("Imprimir");
        btnImprimir.setBounds(460, 560, 110, 35);

        this.add(btnExportar);
        this.add(btnImprimir);

        // --- LÓGICA DE CONSULTA Y CÁLCULO ---
        btnFiltra.addActionListener(e -> {
            modResultados.setRowCount(0);

            String cia = cmbCia.getSelectedItem() != null ? cmbCia.getSelectedItem().toString() : "12";
            String ano = txtAno.getText().trim();
            String periodoStr = txtPeriodo.getText().trim();
            String contab = txtContab.getText().trim();
            boolean inclCeros = chkCeros.isSelected();

            int periodo = 1;
            try { periodo = Integer.parseInt(periodoStr); } catch(Exception ex) {}

            int nivelSeleccionado = 9;
            for (int i = 1; i <= 9; i++) {
                if (rbsNivel[i].isSelected()) { nivelSeleccionado = i; break; }
            }

            try {
                ConDB db = new ConDB();
                Connection con = db.Conectar();
                if (con != null) {
                    String colMesActual = String.format("MN%02d", periodo);

                    StringBuilder sumAcumulado = new StringBuilder();
                    for (int m = 1; m <= periodo; m++) {
                        sumAcumulado.append(m == 1 ? "" : " + ").append(String.format("s.MN%02d", m));
                    }

                    // 1. Obtener los Rubros de tgrub para agrupar el Estado de Resultados
                    String sqlRubros = "SELECT RUB, DRUB, COBJI, COBJF FROM tgrub ORDER BY RUB ASC";
                    PreparedStatement psRub = con.prepareStatement(sqlRubros);
                    ResultSet rsRub = psRub.executeQuery();

                    java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");
                    java.text.DecimalFormat dfPorc = new java.text.DecimalFormat("0.00%");

                    double totalVentasMes = 0.0;
                    double totalVentasAcum = 0.0;

                    // Primero obtenemos las ventas base para calcular porcentajes relacionales
                    String sqlVentasBase = "SELECT SUM(s." + colMesActual + ") AS MES_VENTAS, SUM(" + sumAcumulado + ") AS ACUM_VENTAS " +
                                           "FROM tsctas s INNER JOIN tmctas c ON s.CCTA = c.CCTA " +
                                           "WHERE s.CIA = ? AND s.ANO = ? AND c.CCTA LIKE '4%'";
                    PreparedStatement psVentas = con.prepareStatement(sqlVentasBase);
                    psVentas.setString(1, cia);
                    psVentas.setString(2, ano);
                    ResultSet rsVentas = psVentas.executeQuery();
                    if (rsVentas.next()) {
                        totalVentasMes = Math.abs(rsVentas.getDouble("MES_VENTAS"));
                        totalVentasAcum = Math.abs(rsVentas.getDouble("ACUM_VENTAS"));
                    }
                    rsVentas.close(); psVentas.close();

                    boolean hayDatos = false;

                    while (rsRub.next()) {
                        String rubroKey = rsRub.getString("RUB");
                        String rubroDesc = rsRub.getString("DRUB");
                        String ctaIni = rsRub.getString("COBJI");
                        String ctaFin = rsRub.getString("COBJF");

                        // Fila Encabezado de Rubro
                        modResultados.addRow(new Object[]{"--- " + rubroKey + " ---", rubroDesc.toUpperCase(), "", "", "", ""});

                        StringBuilder sqlCtas = new StringBuilder(
                            "SELECT s.CCTA, c.CDES, c.NATCTA, s." + colMesActual + " AS MES_ACTUAL, (" + sumAcumulado + ") AS ACUM_ANUAL " +
                            "FROM tsctas s " +
                            "INNER JOIN tmctas c ON s.CCTA = c.CCTA " +
                            "WHERE s.CIA = ? AND s.ANO = ? AND c.CNIV <= ? "
                        );

                        if (ctaIni != null && !ctaIni.isEmpty() && ctaFin != null && !ctaFin.isEmpty()) {
                            sqlCtas.append(" AND s.CCTA BETWEEN ? AND ? ");
                        } else if (ctaIni != null && !ctaIni.isEmpty()) {
                            sqlCtas.append(" AND s.CCTA >= ? ");
                        }

                        if (!contab.isEmpty()) sqlCtas.append(" AND s.TCONT = ? ");
                        sqlCtas.append(" ORDER BY s.CCTA ASC");

                        PreparedStatement psDet = con.prepareStatement(sqlCtas.toString());
                        int pIdx = 1;
                        psDet.setString(pIdx++, cia);
                        psDet.setString(pIdx++, ano);
                        psDet.setInt(pIdx++, nivelSeleccionado);

                        if (ctaIni != null && !ctaIni.isEmpty() && ctaFin != null && !ctaFin.isEmpty()) {
                            psDet.setString(pIdx++, ctaIni);
                            psDet.setString(pIdx++, ctaFin);
                        } else if (ctaIni != null && !ctaIni.isEmpty()) {
                            psDet.setString(pIdx++, ctaIni);
                        }

                        if (!contab.isEmpty()) psDet.setString(pIdx++, contab);

                        ResultSet rsDet = psDet.executeQuery();

                        double subtotalRubroMes = 0.0;
                        double subtotalRubroAcum = 0.0;

                        while (rsDet.next()) {
                            double mActual = rsDet.getDouble("MES_ACTUAL");
                            double mAcum = rsDet.getDouble("ACUM_ANUAL");
                            String nat = rsDet.getString("NATCTA");

                            // Ajuste de signo según la naturaleza contable
                            if ("A".equalsIgnoreCase(nat) || "C".equalsIgnoreCase(nat)) {
                                mActual = -mActual;
                                mAcum = -mAcum;
                            }

                            if (!inclCeros && mActual == 0 && mAcum == 0) continue;

                            double porcMes = totalVentasMes > 0 ? (mActual / totalVentasMes) : 0.0;
                            double porcAcum = totalVentasAcum > 0 ? (mAcum / totalVentasAcum) : 0.0;

                            subtotalRubroMes += mActual;
                            subtotalRubroAcum += mAcum;
                            hayDatos = true;

                            modResultados.addRow(new Object[]{
                                rsDet.getString("CCTA"),
                                "  " + rsDet.getString("CDES"),
                                df.format(mActual),
                                dfPorc.format(porcMes),
                                df.format(mAcum),
                                dfPorc.format(porcAcum)
                            });
                        }
                        rsDet.close(); psDet.close();

                        // Subtotal de Rubro
                        double subPorcMes = totalVentasMes > 0 ? (subtotalRubroMes / totalVentasMes) : 0.0;
                        double subPorcAcum = totalVentasAcum > 0 ? (subtotalRubroAcum / totalVentasAcum) : 0.0;

                        modResultados.addRow(new Object[]{
                            "TOTAL " + rubroKey,
                            "SUBTOTAL " + rubroDesc,
                            df.format(subtotalRubroMes),
                            dfPorc.format(subPorcMes),
                            df.format(subtotalRubroAcum),
                            dfPorc.format(subPorcAcum)
                        });
                        modResultados.addRow(new Object[]{"", "", "", "", "", ""}); // Renglón de separación
                    }

                    rsRub.close(); psRub.close(); db.Cerrar();

                    if (!hayDatos) {
                        JOptionPane.showMessageDialog(this, "No se encontraron saldos contables para generar el Estado de Resultados.", "Información", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al generar Estado de Resultados: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Evento Exportar a CSV
        btnExportar.addActionListener(e -> {
            if (modResultados.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No hay información para exportar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("Estado_de_Resultados.csv"));
            int sel = chooser.showSaveDialog(this);

            if (sel == JFileChooser.APPROVE_OPTION) {
                try (FileWriter writer = new FileWriter(chooser.getSelectedFile())) {
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

                    JOptionPane.showMessageDialog(this, "Estado de Resultados exportado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al exportar reporte: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Evento Imprimir
        btnImprimir.addActionListener(e -> {
            if (modResultados.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No hay datos para imprimir.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
                job.setJobName("Estado de Resultados");

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
                    g2d.drawString("ESTADO DE RESULTADOS - AÑO " + txtAno.getText() + " / PERÍODO " + txtPeriodo.getText(), 40, y); y += 15;
                    g2d.drawLine(40, y, 530, y); y += 15;

                    g2d.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 8));
                    g2d.drawString("CLAVE", 40, y);
                    g2d.drawString("DESCRIPCIÓN DE CUENTA / RUBRO", 120, y);
                    g2d.drawString("MES", 350, y);
                    g2d.drawString("% MES", 410, y);
                    g2d.drawString("ACUMULADO", 450, y);
                    g2d.drawString("% ACUM", 510, y);
                    y += 5;
                    g2d.drawLine(40, y, 530, y); y += 12;

                    g2d.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 7));
                    int inicioRow = pageIndex * filasPorPagina;
                    int finRow = Math.min(inicioRow + filasPorPagina, modResultados.getRowCount());

                    for (int r = inicioRow; r < finRow; r++) {
                        String colClave = modResultados.getValueAt(r, 0) != null ? modResultados.getValueAt(r, 0).toString() : "";
                        String colDesc = modResultados.getValueAt(r, 1) != null ? modResultados.getValueAt(r, 1).toString() : "";

                        if (colDesc.length() > 38) colDesc = colDesc.substring(0, 35) + "...";

                        g2d.drawString(colClave, 40, y);
                        g2d.drawString(colDesc, 120, y);
                        g2d.drawString(modResultados.getValueAt(r, 2) != null ? modResultados.getValueAt(r, 2).toString() : "", 350, y);
                        g2d.drawString(modResultados.getValueAt(r, 3) != null ? modResultados.getValueAt(r, 3).toString() : "", 410, y);
                        g2d.drawString(modResultados.getValueAt(r, 4) != null ? modResultados.getValueAt(r, 4).toString() : "", 450, y);
                        g2d.drawString(modResultados.getValueAt(r, 5) != null ? modResultados.getValueAt(r, 5).toString() : "", 510, y);
                        y += 12;
                    }

                    g2d.drawString("Página " + (pageIndex + 1) + " de " + totalPaginas, 450, 750);
                    return java.awt.print.Printable.PAGE_EXISTS;
                });

                if (job.printDialog()) {
                    job.print();
                    JOptionPane.showMessageDialog(this, "Estado de Resultados enviado a la impresora.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al imprimir reporte: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        tblEResultados = new javax.swing.JTable();
        btnAddEResultados = new javax.swing.JButton();
        btnEditEResultados = new javax.swing.JButton();
        btnDeleteEResultados = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        tblEResultados.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tblEResultados);

        btnAddEResultados.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAddEResultados.setForeground(new java.awt.Color(26, 61, 99));
        btnAddEResultados.setIcon(new javax.swing.ImageIcon(getClass().getResource("/add.png"))); // NOI18N
        btnAddEResultados.setText("Añadir");
        btnAddEResultados.addActionListener(this::btnAddEResultadosActionPerformed);

        btnEditEResultados.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEditEResultados.setForeground(new java.awt.Color(26, 61, 99));
        btnEditEResultados.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edit.png"))); // NOI18N
        btnEditEResultados.setText("Editar");
        btnEditEResultados.setMaximumSize(new java.awt.Dimension(93, 31));
        btnEditEResultados.setMinimumSize(new java.awt.Dimension(93, 31));
        btnEditEResultados.addActionListener(this::btnEditEResultadosActionPerformed);

        btnDeleteEResultados.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDeleteEResultados.setForeground(new java.awt.Color(26, 61, 99));
        btnDeleteEResultados.setIcon(new javax.swing.ImageIcon(getClass().getResource("/delete.png"))); // NOI18N
        btnDeleteEResultados.setText("Eliminar");
        btnDeleteEResultados.addActionListener(this::btnDeleteEResultadosActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAddEResultados)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditEResultados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteEResultados)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddEResultados, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditEResultados, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteEResultados, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void btnAddEResultadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddEResultadosActionPerformed
    }//GEN-LAST:event_btnAddEResultadosActionPerformed

    private void btnEditEResultadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditEResultadosActionPerformed
        
    }//GEN-LAST:event_btnEditEResultadosActionPerformed

    private void btnDeleteEResultadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteEResultadosActionPerformed
    }//GEN-LAST:event_btnDeleteEResultadosActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddEResultados;
    private javax.swing.JButton btnDeleteEResultados;
    private javax.swing.JButton btnEditEResultados;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblEResultados;
    // End of variables declaration//GEN-END:variables
}
