/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.benaedu.views;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Font;
/**
 *
 * @author b17za
 */
public class Actualizacion_Saldo extends javax.swing.JPanel {

    /**
     * Creates new form Actualizacion_Saldo
     */
    public Actualizacion_Saldo() {
        initComponents();
        diseñarPanelActualizacion();
    }
private void diseñarPanelActualizacion() {
        // 1. Limpiamos el panel principal (pnlBg o jPanel1 según cómo lo llamó NetBeans)
        jPanel1.removeAll();
        jPanel1.setLayout(null);

        // 2. Creamos un contenedor centrado simulando la ventana flotante
        JPanel pnlVentana = new JPanel(null);
        pnlVentana.setBounds(50, 50, 550, 320); 
        pnlVentana.setBorder(BorderFactory.createEtchedBorder());

        // --- PANEL DE ADVERTENCIA ---
        JPanel pnlAdver = new JPanel(null);
        pnlAdver.setBorder(BorderFactory.createEtchedBorder());
        pnlAdver.setBounds(15, 15, 520, 100);

        // Signos de exclamación
        JLabel lblExclamacionIzq = new JLabel("!", SwingUtilities.CENTER);
        lblExclamacionIzq.setFont(new Font("Arial", Font.BOLD, 70));
        lblExclamacionIzq.setBounds(10, 10, 50, 80);
        pnlAdver.add(lblExclamacionIzq);

        JLabel lblExclamacionDer = new JLabel("!", SwingUtilities.CENTER);
        lblExclamacionDer.setFont(new Font("Arial", Font.BOLD, 70));
        lblExclamacionDer.setBounds(460, 10, 50, 80);
        pnlAdver.add(lblExclamacionDer);

        // Texto centrado multilínea usando HTML
        String textoAdvertencia = "<html><div style='text-align: center; font-family: Segoe UI; font-size: 13px; font-weight: bold;'>"
                + "Este proceso actualiza los saldos de las cuentas a<br>"
                + "partir de los movimientos contables.</div></html>";
        
        JLabel lblTexto = new JLabel(textoAdvertencia, SwingUtilities.CENTER);
        lblTexto.setBounds(70, 15, 380, 70);
        pnlAdver.add(lblTexto);

        pnlVentana.add(pnlAdver);

        // --- PANEL DE SELECCIÓN Y PROGRESO ---
        JPanel pnlSel = new JPanel(null);
        pnlSel.setBorder(BorderFactory.createEtchedBorder());
        pnlSel.setBounds(15, 125, 520, 100);

        // Combos superiores
        pnlSel.add(new JLabel("Compañía")).setBounds(20, 15, 60, 25);
        pnlSel.add(new JComboBox<>(new String[]{""})).setBounds(80, 15, 70, 25);

        pnlSel.add(new JLabel("Año")).setBounds(170, 15, 30, 25);
        pnlSel.add(new JComboBox<>(new String[]{""})).setBounds(200, 15, 70, 25);

        pnlSel.add(new JLabel("Tipo Contab.")).setBounds(290, 15, 80, 25);
        // Agregamos las opciones que mostraste en tu captura desplegada
        pnlSel.add(new JComboBox<>(new String[]{"MN - Moneda Nacional", "PL - Presupuesto MN", "USG - Contabilidad en Dolares"})).setBounds(370, 15, 130, 25);

        // Registros y barra
        pnlSel.add(new JLabel("Registros Actualizados")).setBounds(80, 55, 130, 25);
        JTextField txtRegistros = new JTextField();
        txtRegistros.setEditable(false);
        txtRegistros.setHorizontalAlignment(JTextField.RIGHT);
        txtRegistros.setBounds(210, 55, 100, 25);
        pnlSel.add(txtRegistros);

        // Simulamos el recuadro gris debajo como una barra de progreso
        JProgressBar barraProgreso = new JProgressBar();
        barraProgreso.setBounds(80, 85, 230, 8);
        pnlSel.add(barraProgreso);

        pnlVentana.add(pnlSel);

        // --- BOTONES ---
        JButton btnAceptar = new JButton("Aceptar");
        btnAceptar.setBounds(150, 245, 110, 45);
        
        JButton btnSalir = new JButton("Salir");
        btnSalir.setBounds(290, 245, 110, 45);

        pnlVentana.add(btnAceptar);
        pnlVentana.add(btnSalir);

        // --- EVENTOS ---
        btnAceptar.addActionListener(e -> {
            int resp = JOptionPane.showConfirmDialog(jPanel1, 
                "¿Desea iniciar la actualización de saldos?", 
                "Confirmación de Proceso", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.QUESTION_MESSAGE);
                
            if(resp == JOptionPane.YES_OPTION){
                barraProgreso.setIndeterminate(true); // Hace que la barra se mueva simulando carga
                JOptionPane.showMessageDialog(jPanel1, "Actualización completada exitosamente.");
                barraProgreso.setIndeterminate(false);
                barraProgreso.setValue(100);
                txtRegistros.setText("1,452"); // Simulación de registros procesados
            }
        });

        btnSalir.addActionListener(e -> {
             JOptionPane.showMessageDialog(jPanel1, "Saliendo del módulo de Actualización de Saldos...");
             // Aquí va tu código para cerrar la pestaña
        });

        // 3. Agregamos el diseño al panel de fondo y refrescamos
        jPanel1.add(pnlVentana);
        jPanel1.revalidate();
        jPanel1.repaint();
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

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 750, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 430, Short.MAX_VALUE)
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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
