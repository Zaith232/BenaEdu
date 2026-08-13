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
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author b17za
 */
public class Procesa_Referencias_Bancarias extends javax.swing.JPanel {

    /**
     * Creates new form Procesa_Referencias_Bancarias
     */
    public Procesa_Referencias_Bancarias() {
        initComponents();
        construirInterfazProcesaReferencias();
    }
private void construirInterfazProcesaReferencias() {
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

        Object[][] dCiclo     = cargarDatosMultiple.apply("SELECT CESC, CDSC FROM tescesc ORDER BY CESC DESC", 2);
        Object[][] dMatricula = cargarDatosMultiple.apply("SELECT MAT, APATE, AMATE, NOMA FROM tesalum ORDER BY MAT", 4);

        // --- 1. DATOS GENERALES ---
        JPanel pnlDatosGen = new JPanel(null);
        pnlDatosGen.setBorder(BorderFactory.createTitledBorder("Datos Generales"));
        pnlDatosGen.setBounds(10, 10, 680, 80);

        pnlDatosGen.add(new JLabel("Compañía")).setBounds(20, 20, 70, 25);
        JComboBox<String> cmbCia = new JComboBox<>(); cmbCia.setBounds(90, 20, 60, 25);

        pnlDatosGen.add(new JLabel("Centro Costos")).setBounds(300, 20, 90, 25);
        JComboBox<String> cmbCC = new JComboBox<>(); cmbCC.setBounds(390, 20, 90, 25);

        pnlDatosGen.add(new JLabel("Ciclo Escolar")).setBounds(300, 48, 90, 25);
        JTextField txtCiclo = new JTextField("2526"); txtCiclo.setBounds(390, 48, 60, 25);
        JButton btnCiclo = new JButton("▼"); btnCiclo.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnCiclo.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnCiclo.setBounds(450, 48, 20, 25);
        buscador.configurar(txtCiclo, null, btnCiclo, dCiclo, new String[]{"Clave", "Descripción"}, new int[]{60, 200});
        pnlDatosGen.add(txtCiclo); pnlDatosGen.add(btnCiclo);

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

        pnlDatosGen.add(cmbCia); pnlDatosGen.add(cmbCC);
        this.add(pnlDatosGen);

        // --- 2. SELECCIÓN OPCIONAL ---
        JPanel pnlOpcional = new JPanel(null);
        pnlOpcional.setBorder(BorderFactory.createTitledBorder("Selección Opcional"));
        pnlOpcional.setBounds(10, 95, 680, 60);

        pnlOpcional.add(new JLabel("Matrícula")).setBounds(20, 20, 70, 25);
        JTextField txtMatricula = new JTextField(); txtMatricula.setBounds(90, 20, 100, 25);
        JButton btnMatricula = new JButton("▼"); btnMatricula.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10)); btnMatricula.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnMatricula.setBounds(190, 20, 20, 25);
        buscador.configurar(txtMatricula, null, btnMatricula, dMatricula, new String[]{"Matrícula", "A. Paterno", "A. Materno", "Nombre"}, new int[]{80, 120, 120, 150});
        pnlOpcional.add(txtMatricula); pnlOpcional.add(btnMatricula);

        pnlOpcional.add(new JLabel("Grado")).setBounds(330, 20, 50, 25);
        JComboBox<String> cmbGrado = new JComboBox<>(new String[]{"", "1J", "2J", "3J", "1P", "2P", "3P", "4P", "5P", "6P", "1S", "2S", "3S", "1B", "2B", "3B"});
        cmbGrado.setBounds(390, 20, 80, 25);
        pnlOpcional.add(cmbGrado);

        this.add(pnlOpcional);

        // --- 3. INFORMACIÓN BANCARIA ---
        JPanel pnlInfoBanca = new JPanel(null);
        pnlInfoBanca.setBorder(BorderFactory.createTitledBorder("Información de Bancaria"));
        pnlInfoBanca.setBounds(10, 160, 680, 55);

        pnlInfoBanca.add(new JLabel("Cuenta Bancaria")).setBounds(20, 20, 100, 25);
        JTextField txtCtaBancaria = new JTextField(); txtCtaBancaria.setBounds(120, 20, 150, 25);
        pnlInfoBanca.add(txtCtaBancaria);

        pnlInfoBanca.add(new JLabel("Convenio CIE")).setBounds(300, 20, 90, 25);
        JTextField txtConvenio = new JTextField(); txtConvenio.setBounds(390, 20, 150, 25);
        pnlInfoBanca.add(txtConvenio);

        this.add(pnlInfoBanca);

        // --- 4. PESTAÑAS (Impresión General / Referencia Individual / Referencias) ---
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setBounds(10, 220, 680, 260);

        JPanel pnlImpGeneral = new JPanel(null);
        JPanel pnlRefIndiv = new JPanel(null);
        JPanel pnlReferencias = new JPanel(null);

        // --- PESTAÑA: IMPRESIÓN GENERAL ---
        JPanel pnlAviso = new JPanel(null);
        pnlAviso.setBorder(BorderFactory.createEtchedBorder());
        pnlAviso.setBounds(15, 15, 645, 65);

        JLabel lblAviso = new JLabel("<html><center><font color='red'><b>Se imprimirán todas las referencias de las colegiaturas<br>pendientes de pago !!</b></font></center></html>", JLabel.CENTER);
        lblAviso.setBounds(10, 10, 625, 45);
        pnlAviso.add(lblAviso);
        pnlImpGeneral.add(pnlAviso);

        // Modalidad
        JPanel pnlMod = new JPanel(null);
        pnlMod.setBorder(BorderFactory.createTitledBorder("Modalidad"));
        pnlMod.setBounds(15, 85, 645, 75);

        JRadioButton rbRegenera = new JRadioButton("Regenera Referencia");
        rbRegenera.setBounds(20, 20, 180, 20);
        JRadioButton rbGeneraCptos = new JRadioButton("Genera Referencia de Conceptos", true);
        rbGeneraCptos.setBounds(20, 45, 230, 20);

        ButtonGroup bgMod = new ButtonGroup();
        bgMod.add(rbRegenera);
        bgMod.add(rbGeneraCptos);

        pnlMod.add(rbRegenera);
        pnlMod.add(rbGeneraCptos);

        JButton btnGenera = new JButton("Genera");
        btnGenera.setBounds(510, 30, 110, 35);
        pnlMod.add(btnGenera);

        pnlImpGeneral.add(pnlMod);

        JButton btnImprimir = new JButton("Imprimir");
        btnImprimir.setBounds(15, 170, 110, 40);
        pnlImpGeneral.add(btnImprimir);

        pestanas.addTab("Impresión General", pnlImpGeneral);
        pestanas.addTab("Referencia Individual", pnlRefIndiv);
        pestanas.addTab("Referencias", pnlReferencias);

        this.add(pestanas);

        // --- 5. BOTÓN SALIR ---
        JButton btnSalir = new JButton("Salir");
        btnSalir.setBounds(290, 490, 110, 35);
        this.add(btnSalir);

        // --- 6. EVENTOS ---
        btnSalir.addActionListener(e -> {
            this.removeAll();
            this.revalidate();
            this.repaint();
        });

        // Evento Generar Referencias
        btnGenera.addActionListener(e -> {
            String ciclo = txtCiclo.getText().trim();
            String cia = cmbCia.getSelectedItem() != null ? cmbCia.getSelectedItem().toString() : "";
            String cc = cmbCC.getSelectedItem() != null ? cmbCC.getSelectedItem().toString() : "";
            String mat = txtMatricula.getText().trim();

            if (ciclo.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor proporcione un Ciclo Escolar.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                ConDB db = new ConDB();
                Connection con = db.Conectar();
                if (con != null) {
                    StringBuilder sql = new StringBuilder(
                        "UPDATE tescalu SET CREFBAN = CONCAT('REF', IDCPT, MAT) " +
                        "WHERE CESC = ? AND (IPAGMN = 0 OR IPENMN > 0) "
                    );

                    if (!cia.isEmpty()) sql.append(" AND CIA = ?");
                    if (!cc.isEmpty()) sql.append(" AND CC = ?");
                    if (!mat.isEmpty()) sql.append(" AND MAT = ?");

                    PreparedStatement ps = con.prepareStatement(sql.toString());
                    int p = 1;
                    ps.setString(p++, ciclo);
                    if (!cia.isEmpty()) ps.setString(p++, cia);
                    if (!cc.isEmpty()) ps.setString(p++, cc);
                    if (!mat.isEmpty()) ps.setString(p++, mat);

                    int procesados = ps.executeUpdate();
                    ps.close(); db.Cerrar();

                    JOptionPane.showMessageDialog(this, "Proceso de generación completado. Se procesaron " + procesados + " referencias bancarias.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al generar referencias bancarias: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Evento Imprimir
        btnImprimir.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Enviando conjunto de referencias de colegiatura a la impresora...", "Imprimiendo", JOptionPane.INFORMATION_MESSAGE);
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
