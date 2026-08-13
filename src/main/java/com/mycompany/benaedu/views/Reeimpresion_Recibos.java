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
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
public class Reeimpresion_Recibos extends javax.swing.JPanel {
// Estructuras de datos para agrupar conceptos por recibo
    private static class ConceptoItem {
        String descripcion;
        double importe;

        ConceptoItem(String descripcion, double importe) {
            this.descripcion = descripcion;
            this.importe = importe;
        }
    }

    private static class ReciboData {
        String numRecibo;
        String fecha;
        String matricula;
        String alumno;
        String formaPago;
        List<ConceptoItem> conceptos = new ArrayList<>();

        double getTotal() {
            double total = 0.0;
            for (ConceptoItem c : conceptos) {
                total += c.importe;
            }
            return total;
        }
    }
    /**
     * Creates new form Reeimpresion_Recibos
     */
    public Reeimpresion_Recibos() {
        initComponents();
        construirInterfazReimpresion();
    }
private void construirInterfazReimpresion() {
        this.removeAll();
        this.setLayout(null);
        this.setBackground(new java.awt.Color(255, 255, 255));

        // --- CARGA DE COMPAÑÍAS DESDE LA BD ---
        JComboBox<String> cmbCia = new JComboBox<>();
        try {
            ConDB db = new ConDB();
            Connection con = db.Conectar();
            if (con != null) {
                ResultSet rs = con.prepareStatement("SELECT CIA, NCIA FROM tmcias ORDER BY CIA").executeQuery();
                while (rs.next()) {
                    cmbCia.addItem(rs.getString("CIA") + " - " + rs.getString("NCIA"));
                }
                rs.close(); 
                db.Cerrar();
            }
        } catch (Exception ex) {
            cmbCia.addItem("12 - UNIDAD ESCOLAR BENAVENTE");
        }

        // --- PESTAÑA PRINCIPAL DE REIMPRESIÓN ---
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setBounds(20, 20, 520, 180);

        JPanel pnlImpresion = new JPanel(null);
        pnlImpresion.setBackground(new java.awt.Color(255, 255, 255));

        pnlImpresion.add(new JLabel("Compañía")).setBounds(30, 25, 90, 25);
        cmbCia.setBounds(130, 25, 340, 25);
        pnlImpresion.add(cmbCia);

        JLabel lblDesde = new JLabel("Desde Número");
        lblDesde.setBounds(30, 65, 90, 25);
        JTextField txtDesdeRecibo = new JTextField();
        txtDesdeRecibo.setBounds(130, 65, 90, 25);

        JLabel lblA = new JLabel("A");
        lblA.setBounds(235, 65, 20, 25);
        JTextField txtHastaRecibo = new JTextField();
        txtHastaRecibo.setBounds(255, 65, 90, 25);

        pnlImpresion.add(lblDesde);
        pnlImpresion.add(txtDesdeRecibo);
        pnlImpresion.add(lblA);
        pnlImpresion.add(txtHastaRecibo);

        pestanas.addTab("Impresión de Recibos", pnlImpresion);
        this.add(pestanas);

        // --- CARGA AUTOMÁTICA DE RANGO DE RECIBOS EXISTENTES ---
        Runnable consultarRangoRecibos = () -> {
            String ciaSel = cmbCia.getSelectedItem() != null ? cmbCia.getSelectedItem().toString() : "";
            String cia = ciaSel.contains(" - ") ? ciaSel.split(" - ")[0].trim() : ciaSel;
            
            try {
                ConDB db = new ConDB();
                Connection con = db.Conectar();
                if (con != null) {
                    PreparedStatement psR = con.prepareStatement("SELECT MIN(CAST(NREC AS UNSIGNED)) AS MINR, MAX(CAST(NREC AS UNSIGNED)) AS MAXR FROM tesralu WHERE CIA = ?");
                    psR.setString(1, cia);
                    ResultSet rsR = psR.executeQuery();
                    if (rsR.next() && rsR.getString("MINR") != null) {
                        txtDesdeRecibo.setText(rsR.getString("MINR"));
                        txtHastaRecibo.setText(rsR.getString("MAXR"));
                    } else {
                        txtDesdeRecibo.setText("1");
                        txtHastaRecibo.setText("1");
                    }
                    rsR.close(); psR.close(); db.Cerrar();
                }
            } catch (Exception ex) {
                txtDesdeRecibo.setText("1");
                txtHastaRecibo.setText("1");
            }
        };

        cmbCia.addActionListener(e -> consultarRangoRecibos.run());
        consultarRangoRecibos.run(); // Ejecutar al abrir

        // --- BOTONES INFERIORES ---
        javax.swing.JButton btnImprimir = new javax.swing.JButton("Imprimir");
        btnImprimir.setBounds(150, 220, 120, 38);
        btnImprimir.setFont(new Font("Segoe UI", Font.BOLD, 12));

        javax.swing.JButton btnSalir = new javax.swing.JButton("Salir");
        btnSalir.setBounds(290, 220, 120, 38);
        btnSalir.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        this.add(btnImprimir);
        this.add(btnSalir);

        // --- EVENTO SALIR ---
        btnSalir.addActionListener(e -> {
            this.removeAll();
            this.revalidate();
            this.repaint();
        });

        // --- LÓGICA DE REIMPRESIÓN E IMPRESIÓN DIRECTA ---
        btnImprimir.addActionListener(e -> {
            String ciaCompleta = cmbCia.getSelectedItem() != null ? cmbCia.getSelectedItem().toString() : "";
            String cia = ciaCompleta.contains(" - ") ? ciaCompleta.split(" - ")[0].trim() : ciaCompleta;

            String numDesdeStr = txtDesdeRecibo.getText().trim();
            String numHastaStr = txtHastaRecibo.getText().trim();

            if (numDesdeStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe ingresar al menos un número de recibo inicial.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (numHastaStr.isEmpty()) numHastaStr = numDesdeStr;

            int desdeNum = 0, hastaNum = 0;
            try {
                desdeNum = Integer.parseInt(numDesdeStr);
                hastaNum = Integer.parseInt(numHastaStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "El rango de folios debe ser numérico.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                ConDB db = new ConDB();
                Connection con = db.Conectar();

                if (con != null) {
                    String sql = "SELECT r.NREC, r.FREC, r.MAT, r.NOMALU, r.DCPTO, r.IPAGMN, " +
                                 "p.FMAPAG " +
                                 "FROM tesralu r " +
                                 "LEFT JOIN tespalu p ON p.CIA = r.CIA AND p.CESC = r.CESC AND p.MAT = r.MAT AND p.NREC = r.NREC AND p.TREC = r.TREC " +
                                 "WHERE r.CIA = ? AND CAST(r.NREC AS UNSIGNED) BETWEEN ? AND ? " +
                                 "ORDER BY CAST(r.NREC AS UNSIGNED) ASC";

                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, cia);
                    ps.setInt(2, desdeNum);
                    ps.setInt(3, hastaNum);
                    ResultSet rs = ps.executeQuery();

                    Map<String, ReciboData> mapRecibos = new LinkedHashMap<>();

                    while (rs.next()) {
                        String nrec = rs.getString("NREC");
                        ReciboData rData = mapRecibos.get(nrec);

                        if (rData == null) {
                            rData = new ReciboData();
                            rData.numRecibo = nrec;
                            rData.fecha = rs.getString("FREC") != null ? rs.getString("FREC") : "";
                            rData.matricula = rs.getString("MAT") != null ? rs.getString("MAT") : "";
                            rData.alumno = rs.getString("NOMALU") != null ? rs.getString("NOMALU") : "";
                            rData.formaPago = rs.getString("FMAPAG") != null ? rs.getString("FMAPAG") : "EF";
                            mapRecibos.put(nrec, rData);
                        }

                        String dcpto = rs.getString("DCPTO") != null ? rs.getString("DCPTO") : "";
                        double ipag = rs.getDouble("IPAGMN");
                        rData.conceptos.add(new ConceptoItem(dcpto, ipag));
                    }

                    rs.close(); ps.close(); db.Cerrar();

                    if (mapRecibos.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "No se encontraron recibos para la compañía " + cia + " en ese rango de folios.", "Información", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    List<ReciboData> listaRecibos = new ArrayList<>(mapRecibos.values());

                    PrinterJob job = PrinterJob.getPrinterJob();
                    job.setJobName("Reimpresión de Recibos");

                    job.setPrintable((g, pf, pageIndex) -> {
                        if (pageIndex >= listaRecibos.size()) return Printable.NO_SUCH_PAGE;

                        Graphics2D g2d = (Graphics2D) g;
                        g2d.translate(pf.getImageableX(), pf.getImageableY());

                        ReciboData rec = listaRecibos.get(pageIndex);

                        int y = 30;
                        g2d.setFont(new Font("Monospaced", Font.BOLD, 12));
                        g2d.drawString("UNIDAD ESCOLAR BENAVENTE, A.C.", 40, y); y += 20;
                        
                        g2d.setFont(new Font("Monospaced", Font.PLAIN, 10));
                        g2d.drawString("REIMPRESIÓN DE RECIBO DE PAGO: " + rec.numRecibo, 40, y); y += 15;
                        g2d.drawString("FECHA: " + rec.fecha + " | FORMA PAGO: " + rec.formaPago, 40, y); y += 15;
                        g2d.drawString("MATRÍCULA: " + rec.matricula + " | ALUMNO: " + rec.alumno, 40, y); y += 15;
                        g2d.drawString("--------------------------------------------------", 40, y); y += 15;

                        g2d.setFont(new Font("Monospaced", Font.BOLD, 9));
                        g2d.drawString("DESCRIPCIÓN CONCEPTO", 40, y);
                        g2d.drawString("IMPORTE", 400, y); y += 10;
                        g2d.drawString("--------------------------------------------------", 40, y); y += 15;

                        g2d.setFont(new Font("Monospaced", Font.PLAIN, 9));
                        DecimalFormat df = new DecimalFormat("#,##0.00");

                        for (ConceptoItem cItem : rec.conceptos) {
                            String desc = cItem.descripcion;
                            if (desc.length() > 40) desc = desc.substring(0, 37) + "...";
                            g2d.drawString(desc, 40, y);
                            g2d.drawString("$" + df.format(cItem.importe), 400, y);
                            y += 14;
                        }

                        y += 5;
                        g2d.drawString("--------------------------------------------------", 40, y); y += 15;
                        g2d.setFont(new Font("Monospaced", Font.BOLD, 11));
                        g2d.drawString("TOTAL PAGADO: $" + df.format(rec.getTotal()), 280, y);

                        return Printable.PAGE_EXISTS;
                    });

                    if (job.printDialog()) {
                        job.print();
                        JOptionPane.showMessageDialog(this, "Se reimprimieron " + listaRecibos.size() + " recibos correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al procesar la reimpresión: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        tblRRecibos = new javax.swing.JTable();
        btnAddRRecibos = new javax.swing.JButton();
        btnEditRRecibos = new javax.swing.JButton();
        btnDeleteRRecibos = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        tblRRecibos.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tblRRecibos);

        btnAddRRecibos.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAddRRecibos.setForeground(new java.awt.Color(26, 61, 99));
        btnAddRRecibos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/add.png"))); // NOI18N
        btnAddRRecibos.setText("Añadir");
        btnAddRRecibos.addActionListener(this::btnAddRRecibosActionPerformed);

        btnEditRRecibos.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEditRRecibos.setForeground(new java.awt.Color(26, 61, 99));
        btnEditRRecibos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edit.png"))); // NOI18N
        btnEditRRecibos.setText("Editar");
        btnEditRRecibos.setMaximumSize(new java.awt.Dimension(93, 31));
        btnEditRRecibos.setMinimumSize(new java.awt.Dimension(93, 31));
        btnEditRRecibos.addActionListener(this::btnEditRRecibosActionPerformed);

        btnDeleteRRecibos.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDeleteRRecibos.setForeground(new java.awt.Color(26, 61, 99));
        btnDeleteRRecibos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/delete.png"))); // NOI18N
        btnDeleteRRecibos.setText("Eliminar");
        btnDeleteRRecibos.addActionListener(this::btnDeleteRRecibosActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAddRRecibos)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditRRecibos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteRRecibos)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddRRecibos, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditRRecibos, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteRRecibos, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void btnAddRRecibosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddRRecibosActionPerformed
    
    }//GEN-LAST:event_btnAddRRecibosActionPerformed

    private void btnEditRRecibosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditRRecibosActionPerformed
       
    }//GEN-LAST:event_btnEditRRecibosActionPerformed

    private void btnDeleteRRecibosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteRRecibosActionPerformed

    }//GEN-LAST:event_btnDeleteRRecibosActionPerformed
   

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddRRecibos;
    private javax.swing.JButton btnDeleteRRecibos;
    private javax.swing.JButton btnEditRRecibos;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblRRecibos;
    // End of variables declaration//GEN-END:variables
}
