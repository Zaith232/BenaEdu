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
public class Balanza_Comprobacion extends javax.swing.JPanel {

    /**
     * Creates new form Balanza_Comprobacion
     */
    public Balanza_Comprobacion() {
        initComponents();
        construirInterfazBalanza();
    }
private void construirInterfazBalanza() {
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

        Object[][] dCta    = cargarDatosMultiple.apply("SELECT CCTA, CDES FROM tmctas ORDER BY CCTA", 2);
        Object[][] dMoneda = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'TMON' ORDER BY CVE", 2);
        Object[][] dContab = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'TCONT' ORDER BY CVE", 2);

        // --- PANEL DE FILTROS ---
        JPanel pnlSel = new JPanel(null);
        pnlSel.setBorder(BorderFactory.createEtchedBorder());
        pnlSel.setBounds(10, 10, 915, 150);

        pnlSel.add(new JLabel("Compañía")).setBounds(20, 15, 80, 25);
        JComboBox<String> cmbCia = new JComboBox<>(new String[]{"12"});
        cmbCia.setBounds(100, 15, 60, 25);
        JLabel lblCiaDesc = new JLabel("UNIDAD ESCOLAR BENAVENTE, A.C.");
        lblCiaDesc.setBounds(170, 15, 250, 25);

        pnlSel.add(cmbCia);
        pnlSel.add(lblCiaDesc);

        pnlSel.add(new JLabel("De la cuenta")).setBounds(20, 50, 80, 25);
        JTextField txtCtaIni = new JTextField(); txtCtaIni.setBounds(100, 50, 120, 25);
        JButton btnCtaIni = new JButton("▼"); btnCtaIni.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnCtaIni.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnCtaIni.setBounds(220, 50, 20, 25);
        buscador.configurar(txtCtaIni, null, btnCtaIni, dCta, new String[]{"Clave", "Descripción"}, new int[]{110, 250});
        pnlSel.add(txtCtaIni); pnlSel.add(btnCtaIni);

        pnlSel.add(new JLabel("A la cuenta")).setBounds(20, 85, 80, 25);
        JTextField txtCtaFin = new JTextField(); txtCtaFin.setBounds(100, 85, 120, 25);
        JButton btnCtaFin = new JButton("▼"); btnCtaFin.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnCtaFin.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnCtaFin.setBounds(220, 85, 20, 25);
        buscador.configurar(txtCtaFin, null, btnCtaFin, dCta, new String[]{"Clave", "Descripción"}, new int[]{110, 250});
        pnlSel.add(txtCtaFin); pnlSel.add(btnCtaFin);

        pnlSel.add(new JLabel("Nivel")).setBounds(20, 120, 50, 25);
        ButtonGroup bgNivel = new ButtonGroup();
        JRadioButton[] rbsNivel = new JRadioButton[10];
        int xPosNivel = 70;
        for (int i = 1; i <= 9; i++) {
            rbsNivel[i] = new JRadioButton(String.valueOf(i));
            if(i == 9) rbsNivel[i].setSelected(true);
            rbsNivel[i].setBounds(xPosNivel, 120, 40, 25);
            bgNivel.add(rbsNivel[i]);
            pnlSel.add(rbsNivel[i]);
            xPosNivel += 45;
        }

        JCheckBox chkTodosCC = new JCheckBox("Todos los Centros de Costos");
        chkTodosCC.setBounds(500, 15, 200, 25);
        pnlSel.add(chkTodosCC);

        pnlSel.add(new JLabel("Tipo Moneda")).setBounds(540, 50, 80, 25);
        JTextField txtMoneda = new JTextField("MXP"); txtMoneda.setBounds(620, 50, 50, 25);
        JButton btnMoneda = new JButton("▼"); btnMoneda.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnMoneda.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnMoneda.setBounds(670, 50, 20, 25);
        buscador.configurar(txtMoneda, null, btnMoneda, dMoneda, new String[]{"Clave", "Descripción"}, new int[]{60, 150});
        pnlSel.add(txtMoneda); pnlSel.add(btnMoneda);

        pnlSel.add(new JLabel("Tipo Contab.")).setBounds(540, 85, 80, 25);
        JTextField txtContab = new JTextField("MN"); txtContab.setBounds(620, 85, 50, 25);
        JButton btnContab = new JButton("▼"); btnContab.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnContab.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnContab.setBounds(670, 85, 20, 25);
        buscador.configurar(txtContab, null, btnContab, dContab, new String[]{"Clave", "Descripción"}, new int[]{60, 200});
        pnlSel.add(txtContab); pnlSel.add(btnContab);

        JPanel pnlDerecho = new JPanel(null);
        pnlDerecho.setBorder(BorderFactory.createEtchedBorder());
        pnlDerecho.setBounds(715, 10, 185, 130);

        pnlDerecho.add(new JLabel("Año")).setBounds(10, 10, 40, 25);
        JTextField txtAno = new JTextField("2026"); txtAno.setBounds(60, 10, 80, 25);
        pnlDerecho.add(txtAno);

        pnlDerecho.add(new JLabel("Período")).setBounds(10, 40, 50, 25);
        JTextField txtPeriodo = new JTextField("5"); txtPeriodo.setBounds(60, 40, 80, 25);
        pnlDerecho.add(txtPeriodo);

        JCheckBox chkMostrarCargos = new JCheckBox("Mostrar Cargos/Creditos", true);
        chkMostrarCargos.setBounds(10, 70, 170, 20);
        JCheckBox chkIncluirCeros = new JCheckBox("Incluir Saldos en Ceros");
        chkIncluirCeros.setBounds(10, 95, 170, 20);

        pnlDerecho.add(chkMostrarCargos);
        pnlDerecho.add(chkIncluirCeros);
        pnlSel.add(pnlDerecho);

        this.add(pnlSel);

        JButton btnFiltra = new JButton("Filtrar Información");
        btnFiltra.setBounds(735, 165, 185, 30);
        this.add(btnFiltra);

        // --- TABLA DE RESULTADOS ---
        DefaultTableModel modBalanza = new DefaultTableModel(
            new Object[][]{}, 
            new String[]{"Cuenta", "Descripción", "Saldo Inicial", "Cargos", "Créditos", "Saldo Final"}
        ) { @Override public boolean isCellEditable(int r, int c) { return false; } };

        JTable tblBalanza = new JTable(modBalanza);
        tblBalanza.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblBalanza.getColumnModel().getColumn(0).setPreferredWidth(120);
        tblBalanza.getColumnModel().getColumn(1).setPreferredWidth(250);
        tblBalanza.getColumnModel().getColumn(2).setPreferredWidth(110);
        tblBalanza.getColumnModel().getColumn(3).setPreferredWidth(110);
        tblBalanza.getColumnModel().getColumn(4).setPreferredWidth(110);
        tblBalanza.getColumnModel().getColumn(5).setPreferredWidth(110);

        JPanel pnlTabla = new JPanel(null);
        pnlTabla.setBorder(BorderFactory.createEtchedBorder());
        pnlTabla.setBounds(10, 200, 915, 350);

        JScrollPane scrollBalanza = new JScrollPane(tblBalanza);
        scrollBalanza.setBounds(10, 10, 895, 330);
        pnlTabla.add(scrollBalanza);

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
            modBalanza.setRowCount(0);

            String cia = cmbCia.getSelectedItem() != null ? cmbCia.getSelectedItem().toString() : "12";
            String ctaIni = txtCtaIni.getText().trim();
            String ctaFin = txtCtaFin.getText().trim();
            String moneda = txtMoneda.getText().trim();
            String contab = txtContab.getText().trim();
            String ano = txtAno.getText().trim();
            String periodoStr = txtPeriodo.getText().trim();

            int periodo = 1;
            try { periodo = Integer.parseInt(periodoStr); } catch(Exception ex) {}

            int nivelSeleccionado = 9;
            for (int i = 1; i <= 9; i++) {
                if (rbsNivel[i].isSelected()) { nivelSeleccionado = i; break; }
            }

            boolean incluirCeros = chkIncluirCeros.isSelected();

            try {
                ConDB db = new ConDB();
                Connection con = db.Conectar();
                if (con != null) {
                    String colMes = String.format("MN%02d", periodo);

                    StringBuilder sumMesesAnt = new StringBuilder();
                    for (int m = 1; m < periodo; m++) {
                        sumMesesAnt.append(String.format(" + s.MN%02d", m));
                    }

                    StringBuilder sql = new StringBuilder(
                        "SELECT s.CCTA, c.CDES, c.CNIV, c.NATCTA, s.SINI " + sumMesesAnt.toString() + " AS SALDO_INI, " +
                        "s." + colMes + " AS MOV_MES " +
                        "FROM tsctas s " +
                        "INNER JOIN tmctas c ON s.CCTA = c.CCTA " +
                        "WHERE s.CIA = ? AND c.CNIV <= ? "
                    );

                    if (!ctaIni.isEmpty() && !ctaFin.isEmpty()) sql.append(" AND s.CCTA BETWEEN ? AND ? ");
                    else if (!ctaIni.isEmpty()) sql.append(" AND s.CCTA >= ? ");

                    if (!ano.isEmpty()) sql.append(" AND s.ANO = ? ");
                    if (!moneda.isEmpty()) sql.append(" AND s.CMON = ? ");
                    if (!contab.isEmpty()) sql.append(" AND s.TCONT = ? ");

                    sql.append(" ORDER BY s.CCTA ASC");

                    PreparedStatement ps = con.prepareStatement(sql.toString());
                    int pIdx = 1;
                    ps.setString(pIdx++, cia);
                    ps.setInt(pIdx++, nivelSeleccionado);

                    if (!ctaIni.isEmpty() && !ctaFin.isEmpty()) {
                        ps.setString(pIdx++, ctaIni);
                        ps.setString(pIdx++, ctaFin);
                    } else if (!ctaIni.isEmpty()) {
                        ps.setString(pIdx++, ctaIni);
                    }

                    if (!ano.isEmpty()) ps.setString(pIdx++, ano);
                    if (!moneda.isEmpty()) ps.setString(pIdx++, moneda);
                    if (!contab.isEmpty()) ps.setString(pIdx++, contab);

                    ResultSet rs = ps.executeQuery();
                    java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");

                    double sumIni = 0, sumCargos = 0, sumCreditos = 0, sumFin = 0;

                    while (rs.next()) {
                        double sIni = rs.getDouble("SALDO_INI");
                        double mov = rs.getDouble("MOV_MES");

                        double cargo = mov >= 0 ? mov : 0.0;
                        double credito = mov < 0 ? Math.abs(mov) : 0.0;
                        double sFin = sIni + cargo - credito;

                        if (!incluirCeros && sIni == 0 && cargo == 0 && credito == 0 && sFin == 0) {
                            continue;
                        }

                        Object[] fila = new Object[6];
                        fila[0] = rs.getString("CCTA");
                        fila[1] = rs.getString("CDES");
                        fila[2] = df.format(sIni);
                        fila[3] = df.format(cargo);
                        fila[4] = df.format(credito);
                        fila[5] = df.format(sFin);

                        sumIni += sIni;
                        sumCargos += cargo;
                        sumCreditos += credito;
                        sumFin += sFin;

                        modBalanza.addRow(fila);
                    }

                    rs.close(); ps.close(); db.Cerrar();

                    if (modBalanza.getRowCount() == 0) {
                        JOptionPane.showMessageDialog(this, "No se encontraron saldos de balanza para los filtros seleccionados.", "Información", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al calcular Balanza de Comprobación: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnExportar.addActionListener(e -> {
            if (modBalanza.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No hay datos para exportar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("Balanza_Comprobacion.csv"));
            int sel = chooser.showSaveDialog(this);

            if (sel == JFileChooser.APPROVE_OPTION) {
                try (FileWriter writer = new FileWriter(chooser.getSelectedFile())) {
                    for (int i = 0; i < modBalanza.getColumnCount(); i++) {
                        writer.write(modBalanza.getColumnName(i) + (i == modBalanza.getColumnCount() - 1 ? "" : ","));
                    }
                    writer.write("\n");

                    for (int r = 0; r < modBalanza.getRowCount(); r++) {
                        for (int c = 0; c < modBalanza.getColumnCount(); c++) {
                            Object val = modBalanza.getValueAt(r, c);
                            writer.write((val != null ? val.toString().replace(",", "") : "") + (c == modBalanza.getColumnCount() - 1 ? "" : ","));
                        }
                        writer.write("\n");
                    }

                    JOptionPane.showMessageDialog(this, "Balanza de Comprobación exportada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al exportar archivo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnImprimir.addActionListener(e -> {
            if (modBalanza.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No hay datos para imprimir.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
                job.setJobName("Balanza de Comprobación");

                job.setPrintable((g, pf, pageIndex) -> {
                    int filasPorPagina = 28;
                    int totalPaginas = (int) Math.ceil((double) modBalanza.getRowCount() / filasPorPagina);
                    if (totalPaginas == 0) totalPaginas = 1;

                    if (pageIndex >= totalPaginas) return java.awt.print.Printable.NO_SUCH_PAGE;

                    java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
                    g2d.translate(pf.getImageableX(), pf.getImageableY());

                    int y = 40;
                    g2d.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
                    g2d.drawString("UNIDAD ESCOLAR BENAVENTE, A.C.", 40, y); y += 20;
                    g2d.drawString("BALANZA DE COMPROBACIÓN - PERÍODO: " + txtPeriodo.getText() + "/" + txtAno.getText(), 40, y); y += 15;
                    g2d.drawLine(40, y, 530, y); y += 15;

                    g2d.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 8));
                    g2d.drawString("CUENTA", 40, y);
                    g2d.drawString("DESCRIPCIÓN", 120, y);
                    g2d.drawString("SALDO INICIAL", 280, y);
                    g2d.drawString("CARGOS", 360, y);
                    g2d.drawString("CRÉDITOS", 420, y);
                    g2d.drawString("SALDO FINAL", 480, y);
                    y += 5;
                    g2d.drawLine(40, y, 530, y); y += 12;

                    g2d.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 7));
                    int inicioRow = pageIndex * filasPorPagina;
                    int finRow = Math.min(inicioRow + filasPorPagina, modBalanza.getRowCount());

                    for (int r = inicioRow; r < finRow; r++) {
                        g2d.drawString(modBalanza.getValueAt(r, 0).toString(), 40, y);

                        String desc = modBalanza.getValueAt(r, 1).toString();
                        if (desc.length() > 25) desc = desc.substring(0, 22) + "...";
                        g2d.drawString(desc, 120, y);

                        g2d.drawString(modBalanza.getValueAt(r, 2).toString(), 280, y);
                        g2d.drawString(modBalanza.getValueAt(r, 3).toString(), 360, y);
                        g2d.drawString(modBalanza.getValueAt(r, 4).toString(), 420, y);
                        g2d.drawString(modBalanza.getValueAt(r, 5).toString(), 480, y);
                        y += 12;
                    }

                    g2d.drawString("Página " + (pageIndex + 1) + " de " + totalPaginas, 450, 750);
                    return java.awt.print.Printable.PAGE_EXISTS;
                });

                if (job.printDialog()) {
                    job.print();
                    JOptionPane.showMessageDialog(this, "Balanza de Comprobación enviada a la impresora.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al imprimir balanza: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        tblBComprobacion = new javax.swing.JTable();
        btnAddBC = new javax.swing.JButton();
        btnEditBC = new javax.swing.JButton();
        btnDeleteBC = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        tblBComprobacion.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tblBComprobacion);

        btnAddBC.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAddBC.setForeground(new java.awt.Color(26, 61, 99));
        btnAddBC.setIcon(new javax.swing.ImageIcon(getClass().getResource("/add.png"))); // NOI18N
        btnAddBC.setText("Añadir");
        btnAddBC.addActionListener(this::btnAddBCActionPerformed);

        btnEditBC.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEditBC.setForeground(new java.awt.Color(26, 61, 99));
        btnEditBC.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edit.png"))); // NOI18N
        btnEditBC.setText("Editar");
        btnEditBC.setMaximumSize(new java.awt.Dimension(93, 31));
        btnEditBC.setMinimumSize(new java.awt.Dimension(93, 31));
        btnEditBC.addActionListener(this::btnEditBCActionPerformed);

        btnDeleteBC.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDeleteBC.setForeground(new java.awt.Color(26, 61, 99));
        btnDeleteBC.setIcon(new javax.swing.ImageIcon(getClass().getResource("/delete.png"))); // NOI18N
        btnDeleteBC.setText("Eliminar");
        btnDeleteBC.addActionListener(this::btnDeleteBCActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAddBC)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditBC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteBC)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddBC, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditBC, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteBC, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void btnAddBCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddBCActionPerformed
   
    }//GEN-LAST:event_btnAddBCActionPerformed

    private void btnEditBCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditBCActionPerformed

    }//GEN-LAST:event_btnEditBCActionPerformed

    private void btnDeleteBCActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteBCActionPerformed
    
    }//GEN-LAST:event_btnDeleteBCActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddBC;
    private javax.swing.JButton btnDeleteBC;
    private javax.swing.JButton btnEditBC;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblBComprobacion;
    // End of variables declaration//GEN-END:variables
}
