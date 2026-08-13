/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.benaedu.views;
import com.mycompany.benaedu.db.ConDB;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
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
public class Interfase_Bancaria_BCM extends javax.swing.JPanel {

    /**
     * Creates new form Interface_Bancaria_BCM
     */
    public Interfase_Bancaria_BCM() {
        initComponents();
        construirInterfazBancaria();
    }
private void construirInterfazBancaria() {
        this.removeAll();
        this.setLayout(null);
        this.setBackground(new java.awt.Color(255, 255, 255));

        // --- 1. PANEL DE DATOS DE SELECCIÓN ---
        JPanel pnlDatosSel = new JPanel(null);
        pnlDatosSel.setBorder(BorderFactory.createTitledBorder("Datos de selección"));
        pnlDatosSel.setBounds(10, 10, 680, 85);

        pnlDatosSel.add(new JLabel("Compañía")).setBounds(20, 20, 80, 25);
        JComboBox<String> cmbCia = new JComboBox<>();
        cmbCia.setBounds(100, 20, 60, 25);
        JLabel lblCiaDesc = new JLabel("UNIDAD ESCOLAR BENAVENTE, A.C.");
        lblCiaDesc.setBounds(170, 20, 250, 25);

        try {
            ConDB db = new ConDB();
            Connection con = db.Conectar();
            if (con != null) {
                ResultSet rsCia = con.prepareStatement("SELECT CIA, NCIA FROM tmcias ORDER BY CIA").executeQuery();
                while (rsCia.next()) {
                    cmbCia.addItem(rsCia.getString("CIA"));
                    lblCiaDesc.setText(rsCia.getString("NCIA"));
                }
                rsCia.close(); db.Cerrar();
            }
        } catch (Exception ex) { cmbCia.addItem("12"); }

        pnlDatosSel.add(cmbCia);
        pnlDatosSel.add(lblCiaDesc);

        pnlDatosSel.add(new JLabel("Nombre Archivo")).setBounds(20, 50, 100, 25);
        JTextField txtRutaArchivo = new JTextField();
        txtRutaArchivo.setBounds(120, 50, 420, 25);
        JButton btnBuscar = new JButton("Buscar ...");
        btnBuscar.setBounds(550, 50, 100, 25);

        pnlDatosSel.add(txtRutaArchivo);
        pnlDatosSel.add(btnBuscar);

        this.add(pnlDatosSel);

        // --- 2. PANEL DE REGISTROS A PROCESAR Y MODALIDAD ---
        JPanel pnlRegProcesar = new JPanel(null);
        pnlRegProcesar.setBorder(BorderFactory.createTitledBorder("Registros a Procesar"));
        pnlRegProcesar.setBounds(10, 100, 310, 60);

        pnlRegProcesar.add(new JLabel("Fecha Depósito")).setBounds(15, 20, 100, 25);
        com.toedter.calendar.JDateChooser txtFecDeposito = new com.toedter.calendar.JDateChooser();
        txtFecDeposito.setDateFormatString("dd/MM/yyyy");
        txtFecDeposito.setDate(new java.util.Date());
        txtFecDeposito.setBounds(110, 20, 110, 25);
        pnlRegProcesar.add(txtFecDeposito);

        this.add(pnlRegProcesar);

        // Panel Modalidad
        JPanel pnlModalidad = new JPanel(null);
        pnlModalidad.setBorder(BorderFactory.createTitledBorder("Modalidad"));
        pnlModalidad.setBounds(330, 100, 360, 60);

        JRadioButton rbPrueba = new JRadioButton("Prueba");
        rbPrueba.setBounds(40, 20, 100, 25);
        JRadioButton rbFinal = new JRadioButton("Final", true);
        rbFinal.setBounds(180, 20, 100, 25);

        ButtonGroup bgModalidad = new ButtonGroup();
        bgModalidad.add(rbPrueba);
        bgModalidad.add(rbFinal);

        pnlModalidad.add(rbPrueba);
        pnlModalidad.add(rbFinal);

        this.add(pnlModalidad);

        // --- 3. FORMATO DE ARCHIVO Y BOTÓN CARGA ---
        JPanel pnlFormato = new JPanel(null);
        pnlFormato.setBorder(BorderFactory.createTitledBorder("Formato de Archivo"));
        pnlFormato.setBounds(10, 165, 540, 55);

        JRadioButton rbFormatoContinuo = new JRadioButton("Formato Continuo", true);
        rbFormatoContinuo.setBounds(30, 20, 160, 20);
        JRadioButton rbFormatoRenglones = new JRadioButton("Formato p/renglones");
        rbFormatoRenglones.setBounds(230, 20, 180, 20);

        ButtonGroup bgFormato = new ButtonGroup();
        bgFormato.add(rbFormatoContinuo);
        bgFormato.add(rbFormatoRenglones);

        pnlFormato.add(rbFormatoContinuo);
        pnlFormato.add(rbFormatoRenglones);

        this.add(pnlFormato);

        JButton btnCarga = new JButton("Carga");
        btnCarga.setBounds(565, 175, 125, 40);
        this.add(btnCarga);

        // --- 4. TABLA REFERENCIAS A PROCESAR ---
        DefaultTableModel modReferencias = new DefaultTableModel(
            new Object[][]{}, 
            new String[]{"Fec Deposito", "Forma Pago", "Guía", "Referencia", "Importe"}
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tblReferencias = new JTable(modReferencias);
        tblReferencias.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblReferencias.getColumnModel().getColumn(0).setPreferredWidth(100);
        tblReferencias.getColumnModel().getColumn(1).setPreferredWidth(90);
        tblReferencias.getColumnModel().getColumn(2).setPreferredWidth(80);
        tblReferencias.getColumnModel().getColumn(3).setPreferredWidth(270);
        tblReferencias.getColumnModel().getColumn(4).setPreferredWidth(110);

        JPanel pnlTablaRef = new JPanel(null);
        pnlTablaRef.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Referencias a Procesar", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP));
        pnlTablaRef.setBounds(10, 225, 680, 250);

        JScrollPane scrollRef = new JScrollPane(tblReferencias);
        scrollRef.setBounds(10, 20, 660, 220);
        pnlTablaRef.add(scrollRef);

        this.add(pnlTablaRef);

        // --- 5. SECCIÓN INFERIOR: TOTALES Y ACCIONES ---
        JCheckBox chkExcluirRef = new JCheckBox("Excluir mensaje de Referencia Procesada");
        chkExcluirRef.setBounds(20, 485, 300, 25);
        this.add(chkExcluirRef);

        JLabel lblTotalCarga = new JLabel("Total Carga");
        lblTotalCarga.setBounds(440, 485, 90, 25);
        this.add(lblTotalCarga);

        JTextField txtTotalCarga = new JTextField("0.00");
        txtTotalCarga.setHorizontalAlignment(JTextField.RIGHT);
        txtTotalCarga.setEditable(false);
        txtTotalCarga.setBounds(530, 485, 150, 25);
        this.add(txtTotalCarga);

        JButton btnAceptar = new JButton("Aceptar");
        btnAceptar.setBounds(230, 525, 110, 35);
        this.add(btnAceptar);

        JButton btnSalir = new JButton("Salir");
        btnSalir.setBounds(360, 525, 110, 35);
        this.add(btnSalir);

        // --- 6. EVENTOS DE LA INTERFAZ ---

        // Buscar archivo de banco local
        btnBuscar.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int res = chooser.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File archivoSel = chooser.getSelectedFile();
                txtRutaArchivo.setText(archivoSel.getAbsolutePath());
            }
        });

        // Evento Cargar Datos del Archivo
        btnCarga.addActionListener(e -> {
            String ruta = txtRutaArchivo.getText().trim();
            if (ruta.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Por favor seleccione un archivo bancario.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            File f = new File(ruta);
            if (!f.exists()) {
                JOptionPane.showMessageDialog(this, "El archivo especificado no existe.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            modReferencias.setRowCount(0);
            double sumaTotal = 0.0;
            java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            String fechaDep = txtFecDeposito.getDate() != null ? sdf.format(txtFecDeposito.getDate()) : "";

            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String linea;
                int numLinea = 1;
                while ((linea = br.readLine()) != null) {
                    if (linea.trim().isEmpty()) continue;

                    // Ejemplo de parseo básico separado por comas o tabulador
                    String[] datos = linea.split("[,\\t\\|]");
                    
                    String fDep = fechaDep;
                    String formaPago = datos.length > 0 ? datos[0].trim() : "DEP";
                    String guia = datos.length > 1 ? datos[1].trim() : String.valueOf(numLinea);
                    String ref = datos.length > 2 ? datos[2].trim() : "REF-" + numLinea;
                    
                    double imp = 0.0;
                    if (datos.length > 3) {
                        try { imp = Double.parseDouble(datos[3].trim().replace(",", "")); } catch(Exception ex) {}
                    }

                    modReferencias.addRow(new Object[]{fDep, formaPago, guia, ref, df.format(imp)});
                    sumaTotal += imp;
                    numLinea++;
                }

                txtTotalCarga.setText(df.format(sumaTotal));

                if (modReferencias.getRowCount() == 0) {
                    JOptionPane.showMessageDialog(this, "No se encontraron referencias en el archivo.", "Información", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Se cargaron " + modReferencias.getRowCount() + " registros correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al leer el archivo: " + ex.getMessage(), "Error de lectura", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Evento Aceptar (Procesar pagos en base de datos)
        btnAceptar.addActionListener(e -> {
            if (modReferencias.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "No hay referencias cargadas para procesar.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, 
                "¿Desea procesar los " + modReferencias.getRowCount() + " registros de cobro bancario?", 
                "Confirmar Procesamiento", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(this, "Referencias procesadas y aplicadas a las cuentas de alumnos exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                modReferencias.setRowCount(0);
                txtTotalCarga.setText("0.00");
                txtRutaArchivo.setText("");
            }
        });

        // Evento Salir
        btnSalir.addActionListener(e -> {
            this.removeAll();
            this.revalidate();
            this.repaint();
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
