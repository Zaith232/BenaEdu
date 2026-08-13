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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
/**
 *
 * @author b17za
 */
public class Reeimpresion_Facturas extends javax.swing.JPanel {
private JComboBox<String> cmbCia;
    private JTextField txtDesde;
    private JTextField txtHasta;
    private JTextField txtTipo;
    private JCheckBox chkDetallada;
    private JCheckBox chkRegenera;
    /**
     * Creates new form Reeimpresion_Facturas
     */
    public Reeimpresion_Facturas() {
        initComponents();
        construirInterfazReeimpresion();
    }
private void construirInterfazReeimpresion() {
        this.removeAll();
        this.setLayout(new BorderLayout(10, 10));
        this.setBackground(Color.WHITE);

        // --- BUSCADOR FLOTANTE LOCAL ---
        class BuscadorFlotante {
            void configurar(JTextField txtClave, JTextField txtDesc, JButton boton, Object[][] datos, String[] cols, int[] anchos) {
                Runnable mostrarPopup = () -> {
                    JPopupMenu popup = new JPopupMenu();
                    popup.setFocusable(false);
                    DefaultTableModel mod = new DefaultTableModel(datos, cols) {
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
                        @Override public void mouseReleased(java.awt.event.MouseEvent me) {
                            int r = tabla.getSelectedRow();
                            if (r != -1) {
                                int modelRow = tabla.convertRowIndexToModel(r);
                                txtClave.setText(mod.getValueAt(modelRow, 0).toString());
                                if (txtDesc != null && mod.getColumnCount() >= 2) {
                                    txtDesc.setText(mod.getValueAt(modelRow, 1).toString());
                                }
                                popup.setVisible(false);
                            }
                        }
                    });
                    JScrollPane scroll = new JScrollPane(tabla);
                    scroll.setPreferredSize(new Dimension(320, 160));
                    popup.add(scroll);
                    popup.show(txtClave, 0, txtClave.getHeight());
                };
                boton.addActionListener(e -> mostrarPopup.run());
            }
        }
        BuscadorFlotante buscador = new BuscadorFlotante();

        // 1. Carga de catálogo de Tipos de Documento
        Object[][] dFact;
        try (Connection con = new ConDB().Conectar(); 
             PreparedStatement ps = con.prepareStatement("SELECT CVE, DES FROM tmclas WHERE TBL = 'FACT' AND CVE IN ('FE','FP','NC','VC') ORDER BY CVE")) {
            ResultSet rs = ps.executeQuery();
            List<Object[]> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(new Object[]{rs.getString(1), rs.getString(2)});
            }
            dFact = lista.toArray(new Object[0][0]);
            rs.close();
        } catch (Exception e) {
            dFact = new Object[][]{{"FE", "FACTURA ELECTRÓNICA"}, {"NC", "NOTA DE CRÉDITO"}};
        }

        // 2. Carga de lista de Facturas para los buscadores de folios
        Object[][] dFacturas;
        try (Connection con = new ConDB().Conectar();
             PreparedStatement ps = con.prepareStatement("SELECT NFAC, CTE, FFAC FROM tgfcte ORDER BY CAST(NFAC AS UNSIGNED) DESC LIMIT 200")) {
            ResultSet rs = ps.executeQuery();
            List<Object[]> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(new Object[]{rs.getString("NFAC"), rs.getString("CTE"), rs.getString("FFAC")});
            }
            dFacturas = lista.toArray(new Object[0][0]);
            rs.close();
        } catch (Exception e) {
            dFacturas = new Object[0][0];
        }

        JTabbedPane pestanas = new JTabbedPane();
        JPanel pnlImpresion = new JPanel(null);
        pnlImpresion.setBackground(Color.WHITE);

        // --- Selección de Compañía ---
        pnlImpresion.add(new JLabel("Compañía:")).setBounds(30, 20, 100, 25);
        cmbCia = new JComboBox<>();
        cmbCia.setBounds(140, 20, 310, 25);

        try (Connection con = new ConDB().Conectar();
             PreparedStatement psCia = con.prepareStatement("SELECT CIA, NCIA FROM tmcias ORDER BY CIA")) {
            ResultSet rsCia = psCia.executeQuery();
            while (rsCia.next()) {
                cmbCia.addItem(rsCia.getString("CIA") + " - " + rsCia.getString("NCIA"));
            }
            rsCia.close();
        } catch (Exception ex) {
            cmbCia.addItem("12 - UNIDAD ESCOLAR BENAVENTE");
        }
        pnlImpresion.add(cmbCia);

        // --- Rango de Facturas (con buscadores flotantes) ---
        pnlImpresion.add(new JLabel("Desde Número:")).setBounds(30, 60, 100, 25);
        txtDesde = new JTextField("1");
        txtDesde.setBounds(140, 60, 80, 25);
        JButton btnDesde = new JButton("▼");
        btnDesde.setFont(new Font("SansSerif", Font.PLAIN, 10));
        btnDesde.setBounds(220, 60, 22, 25);
        buscador.configurar(txtDesde, null, btnDesde, dFacturas, new String[]{"Folio", "Cliente", "Fecha"}, new int[]{60, 140, 90});
        
        pnlImpresion.add(txtDesde);
        pnlImpresion.add(btnDesde);

        pnlImpresion.add(new JLabel("A:")).setBounds(260, 60, 20, 25);
        txtHasta = new JTextField("1");
        txtHasta.setBounds(280, 60, 80, 25);
        JButton btnHasta = new JButton("▼");
        btnHasta.setFont(new Font("SansSerif", Font.PLAIN, 10));
        btnHasta.setBounds(360, 60, 22, 25);
        buscador.configurar(txtHasta, null, btnHasta, dFacturas, new String[]{"Folio", "Cliente", "Fecha"}, new int[]{60, 140, 90});

        pnlImpresion.add(txtHasta);
        pnlImpresion.add(btnHasta);

        // --- Tipo de Documento ---
        pnlImpresion.add(new JLabel("Tipo Doc.:")).setBounds(30, 100, 100, 25);
        txtTipo = new JTextField("FE");
        txtTipo.setBounds(140, 100, 60, 25);
        JButton btnTipo = new JButton("▼");
        btnTipo.setFont(new Font("SansSerif", Font.PLAIN, 10));
        btnTipo.setBounds(200, 100, 22, 25);
        buscador.configurar(txtTipo, null, btnTipo, dFact, new String[]{"Clave", "Descripción"}, new int[]{50, 180});

        pnlImpresion.add(txtTipo);
        pnlImpresion.add(btnTipo);

        // --- Parámetros de Impresión ---
        JPanel pnlInfoFac = new JPanel(null);
        pnlInfoFac.setBackground(Color.WHITE);
        pnlInfoFac.setBorder(BorderFactory.createTitledBorder("Información de Factura"));
        pnlInfoFac.setBounds(30, 140, 480, 60);

        chkDetallada = new JCheckBox("Factura Detallada", true);
        chkDetallada.setBackground(Color.WHITE);
        chkDetallada.setBounds(20, 20, 180, 25);

        chkRegenera = new JCheckBox("Regenera Factura");
        chkRegenera.setBackground(Color.WHITE);
        chkRegenera.setBounds(220, 20, 180, 25);

        pnlInfoFac.add(chkDetallada);
        pnlInfoFac.add(chkRegenera);
        pnlImpresion.add(pnlInfoFac);

        pestanas.addTab("Impresión de Factura", pnlImpresion);

        // Botones de Control
        JPanel pnlBotones = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 15, 10));
        pnlBotones.setBackground(Color.WHITE);

        JButton btnImprimir = new JButton("Imprimir");
        btnImprimir.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnImprimir.setPreferredSize(new Dimension(110, 35));

        JButton btnSalir = new JButton("Salir");
        btnSalir.setPreferredSize(new Dimension(110, 35));

        pnlBotones.add(btnImprimir);
        pnlBotones.add(btnSalir);

        this.add(pestanas, BorderLayout.CENTER);
        this.add(pnlBotones, BorderLayout.SOUTH);

        // Eventos
        btnSalir.addActionListener(e -> {
            this.removeAll();
            this.revalidate();
            this.repaint();
        });

        btnImprimir.addActionListener(e -> ejecutarReeimpresion());

        this.revalidate();
        this.repaint();
    }

    private void ejecutarReeimpresion() {
        String ciaCompleta = cmbCia.getSelectedItem() != null ? cmbCia.getSelectedItem().toString() : "";
        String cia = ciaCompleta.contains(" - ") ? ciaCompleta.split(" - ")[0].trim() : ciaCompleta;
        String tipoDoc = txtTipo.getText().trim();
        String desdeStr = txtDesde.getText().trim();
        String hastaStr = txtHasta.getText().trim();

        if (tipoDoc.isEmpty() || desdeStr.isEmpty() || hastaStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Especifique el Tipo de Documento y el rango de números.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int desdeNum, hastaNum;
        try {
            desdeNum = Integer.parseInt(desdeStr);
            hastaNum = Integer.parseInt(hastaStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El rango de facturas debe ser numérico.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            ConDB db = new ConDB();
            Connection con = db.Conectar();
            if (con == null) return;

            String sql = "SELECT CFAC, NFAC, FFAC, CTE, DESPAR, MIMP, IVAR, FOLFIS " +
                         "FROM tgfcte " +
                         "WHERE CIA = ? AND TFAC = ? AND CAST(NFAC AS UNSIGNED) BETWEEN ? AND ? " +
                         "ORDER BY CAST(NFAC AS UNSIGNED) ASC";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, cia);
            ps.setString(2, tipoDoc);
            ps.setInt(3, desdeNum);
            ps.setInt(4, hastaNum);

            ResultSet rs = ps.executeQuery();
            List<Object[]> listaFacturas = new ArrayList<>();

            while (rs.next()) {
                Object[] f = new Object[8];
                f[0] = rs.getString("CFAC");
                f[1] = rs.getString("NFAC");
                f[2] = rs.getString("FFAC");
                f[3] = rs.getString("CTE");
                f[4] = rs.getString("DESPAR");
                f[5] = rs.getDouble("MIMP");
                f[6] = rs.getDouble("IVAR");
                f[7] = rs.getString("FOLFIS");
                listaFacturas.add(f);
            }

            rs.close(); ps.close(); db.Cerrar();

            if (listaFacturas.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se encontraron facturas en el rango especificado.", "Sin Resultados", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName("Reimpresión de Facturas (" + desdeStr + " - " + hastaStr + ")");

            final boolean esDetallada = chkDetallada.isSelected();

            job.setPrintable((g, pf, pageIndex) -> {
                if (pageIndex >= listaFacturas.size()) return Printable.NO_SUCH_PAGE;

                Graphics2D g2d = (Graphics2D) g;
                g2d.translate(pf.getImageableX(), pf.getImageableY());

                Object[] fac = listaFacturas.get(pageIndex);
                String cfac = fac[0].toString();
                String nfac = fac[1].toString();
                String ffac = fac[2] != null ? fac[2].toString() : "";
                String cte = fac[3] != null ? fac[3].toString() : "";
                double mimp = (double) fac[5];
                double ivar = (double) fac[6];
                String folfis = fac[7] != null ? fac[7].toString() : "N/A";

                int y = 40;
                g2d.setFont(new Font("Monospaced", Font.BOLD, 12));
                g2d.drawString("UNIDAD ESCOLAR BENAVENTE, A.C.", 40, y); y += 20;
                g2d.drawString("FACTURA ELECTRÓNICA (" + tipoDoc + "): " + nfac, 40, y); y += 15;
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 9));
                g2d.drawString("FECHA EMISIÓN: " + ffac + " | FOLIO FISCAL: " + folfis, 40, y); y += 15;
                g2d.drawString("RECEPTOR (CLIENTE / MATRÍCULA): " + cte, 40, y); y += 15;
                g2d.drawLine(40, y, 520, y); y += 15;

                if (esDetallada) {
                    g2d.setFont(new Font("Monospaced", Font.BOLD, 8));
                    g2d.drawString("SEC", 40, y);
                    g2d.drawString("CONCEPTO", 80, y);
                    g2d.drawString("DESCRIPCIÓN", 160, y);
                    y += 10;
                    g2d.drawLine(40, y, 520, y); y += 12;

                    g2d.setFont(new Font("Monospaced", Font.PLAIN, 8));
                    try (Connection conDet = new ConDB().Conectar()) {
                        if (conDet != null) {
                            PreparedStatement psDet = conDet.prepareStatement("SELECT NLIN, CODCON, CONCEP FROM tgfcted WHERE CFAC = ? ORDER BY NLIN ASC");
                            psDet.setString(1, cfac);
                            ResultSet rsDet = psDet.executeQuery();
                            while (rsDet.next()) {
                                g2d.drawString(rsDet.getString("NLIN"), 40, y);
                                g2d.drawString(rsDet.getString("CODCON"), 80, y);
                                String d = rsDet.getString("CONCEP");
                                if (d.length() > 40) d = d.substring(0, 37) + "...";
                                g2d.drawString(d, 160, y);
                                y += 12;
                            }
                            rsDet.close(); psDet.close();
                        }
                    } catch (Exception ignore) {}
                } else {
                    g2d.drawString("CONCEPTO GENERAL: " + fac[4], 40, y); y += 20;
                }

                y += 15;
                g2d.drawLine(40, y, 520, y); y += 15;
                DecimalFormat df = new DecimalFormat("#,##0.00");
                g2d.drawString("SUBTOTAL: $" + df.format(mimp - ivar), 350, y); y += 12;
                g2d.drawString("IVA: $" + df.format(ivar), 350, y); y += 12;
                g2d.setFont(new Font("Monospaced", Font.BOLD, 10));
                g2d.drawString("TOTAL: $" + df.format(mimp), 350, y);

                return Printable.PAGE_EXISTS;
            });

            if (job.printDialog()) {
                job.print();
                JOptionPane.showMessageDialog(this, "Se reimprimieron " + listaFacturas.size() + " facturas correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error durante la reimpresión: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
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
        tblRFacturas = new javax.swing.JTable();
        btnAddRFacturas = new javax.swing.JButton();
        btnEditRFacturas = new javax.swing.JButton();
        btnDeleteRFacturas = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        tblRFacturas.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tblRFacturas);

        btnAddRFacturas.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAddRFacturas.setForeground(new java.awt.Color(26, 61, 99));
        btnAddRFacturas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/add.png"))); // NOI18N
        btnAddRFacturas.setText("Añadir");
        btnAddRFacturas.addActionListener(this::btnAddRFacturasActionPerformed);

        btnEditRFacturas.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEditRFacturas.setForeground(new java.awt.Color(26, 61, 99));
        btnEditRFacturas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edit.png"))); // NOI18N
        btnEditRFacturas.setText("Editar");
        btnEditRFacturas.setMaximumSize(new java.awt.Dimension(93, 31));
        btnEditRFacturas.setMinimumSize(new java.awt.Dimension(93, 31));
        btnEditRFacturas.addActionListener(this::btnEditRFacturasActionPerformed);

        btnDeleteRFacturas.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDeleteRFacturas.setForeground(new java.awt.Color(26, 61, 99));
        btnDeleteRFacturas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/delete.png"))); // NOI18N
        btnDeleteRFacturas.setText("Eliminar");
        btnDeleteRFacturas.addActionListener(this::btnDeleteRFacturasActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAddRFacturas)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditRFacturas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteRFacturas)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddRFacturas, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditRFacturas, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteRFacturas, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void btnAddRFacturasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddRFacturasActionPerformed
    }//GEN-LAST:event_btnAddRFacturasActionPerformed

    private void btnEditRFacturasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditRFacturasActionPerformed
    }//GEN-LAST:event_btnEditRFacturasActionPerformed

    private void btnDeleteRFacturasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteRFacturasActionPerformed
    }//GEN-LAST:event_btnDeleteRFacturasActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddRFacturas;
    private javax.swing.JButton btnDeleteRFacturas;
    private javax.swing.JButton btnEditRFacturas;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblRFacturas;
    // End of variables declaration//GEN-END:variables
}
