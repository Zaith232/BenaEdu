package com.mycompany.benaedu.views;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
public class RevisionAlumno {
 static void layout(Container c) { c.doLayout(); for (Component x:c.getComponents()) if(x instanceof Container cc) layout(cc); }
 static void render(JPanel p, String name) throws Exception { p.setSize(875,400); EstiloContenido.aplicar(p); layout(p); BufferedImage i=new BufferedImage(875,400,BufferedImage.TYPE_INT_RGB); Graphics2D g=i.createGraphics(); p.printAll(g); g.dispose(); ImageIO.write(i,"png",new File("target/"+name+".png")); }
 public static void main(String[] args) throws Exception {
 com.formdev.flatlaf.FlatLightLaf.setup();
        JPanel pnlFamiliares = new JPanel(null);
        JTabbedPane tbFamilia = new JTabbedPane(JTabbedPane.RIGHT);
        tbFamilia.setBounds(10, 10, 850, 380);
        JPanel pnlFamOuter = new JPanel(null);
        pnlFamOuter.setBorder(BorderFactory.createTitledBorder("Familia"));

        JLabel lblHeaderFam1 = new JLabel("Si el alumno no tiene asignada Familia Seleccionar una de la lista.", SwingUtilities.CENTER);
        lblHeaderFam1.setBounds(0, 20, 700, 20);
        JLabel lblHeaderFam2 = new JLabel("Si no existe la familia aqui podras crear una.", SwingUtilities.CENTER);
        lblHeaderFam2.setBounds(0, 40, 700, 20);
        JLabel lblHeaderFam3 = new JLabel("Dejar en blanco el numero de familia, llena el resto de la información", SwingUtilities.CENTER);
        lblHeaderFam3.setBounds(0, 60, 700, 20);
        JLabel lblHeaderFam4 = new JLabel("Presionar el Boton \"Agrega Familia\"", SwingUtilities.CENTER);
        lblHeaderFam4.setBounds(0, 80, 700, 20);

        pnlFamOuter.add(lblHeaderFam1);
        pnlFamOuter.add(lblHeaderFam2);
        pnlFamOuter.add(lblHeaderFam3);
        pnlFamOuter.add(lblHeaderFam4);
        pnlFamOuter.add(new JLabel("No Familia")).setBounds(20, 125, 100, 25);
        pnlFamOuter.add(new JComboBox<>(new String[]{""})).setBounds(130, 125, 110, 25);
        pnlFamOuter.add(new JLabel("Familia")).setBounds(20, 165, 100, 25);
        pnlFamOuter.add(new JTextField()).setBounds(130, 165, 570, 25);
        pnlFamOuter.add(new JLabel("Nombre Tutor")).setBounds(20, 205, 100, 25);
        JTextField txtNomTutor = new JTextField();
        txtNomTutor.setBounds(130, 205, 570, 25);
        pnlFamOuter.add(txtNomTutor);
        pnlFamOuter.add(new JLabel("Parentesco")).setBounds(20, 245, 100, 25);
        JTextField txtParentTutor = new JTextField();
        txtParentTutor.setBounds(130, 245, 80, 25);
        JButton btnParentTutor = new JButton("▼");
        btnParentTutor.setBounds(210, 245, 25, 25);
        pnlFamOuter.add(txtParentTutor);
        pnlFamOuter.add(btnParentTutor);
        JButton btnAgregaFamilia = new JButton("Agrega Familia");
        btnAgregaFamilia.setBounds(540, 305, 160, 32);
        btnAgregaFamilia.setEnabled(false);
        btnAgregaFamilia.setToolTipText("La creación de familias todavía no tiene guardado implementado.");
        pnlFamOuter.add(btnAgregaFamilia);

        JPanel pnlFamInner = new JPanel(null);
        pnlFamInner.setBorder(BorderFactory.createTitledBorder("Familiares"));
        pnlFamInner.add(new JLabel("Sec")).setBounds(20, 20, 30, 20);
        pnlFamInner.add(new JTextField()).setBounds(60, 20, 40, 20);
        pnlFamInner.add(new JLabel("Parentesco")).setBounds(120, 20, 80, 20);
        JTextField txtParent = new JTextField();
        txtParent.setBounds(200, 20, 60, 20);
        JButton btnParent = new JButton("▼");
        btnParent.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
        btnParent.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnParent.setBounds(260, 20, 20, 20);
        pnlFamInner.add(txtParent);
        pnlFamInner.add(btnParent);

        pnlFamInner.add(new JLabel("Nombre")).setBounds(20, 50, 80, 20);
        pnlFamInner.add(new JTextField()).setBounds(100, 50, 480, 20);
        pnlFamInner.add(new JLabel("Ocupación")).setBounds(20, 80, 80, 20);
        pnlFamInner.add(new JTextField()).setBounds(100, 80, 480, 20);
        pnlFamInner.add(new JLabel("Lugar Trabajo")).setBounds(20, 110, 90, 20);
        pnlFamInner.add(new JTextField()).setBounds(100, 110, 480, 20);

        pnlFamInner.add(new JLabel("Teléfono")).setBounds(20, 140, 80, 20);
        JTextField txtTipoTelFam = new JTextField();
        txtTipoTelFam.setBounds(100, 140, 50, 20);
        JButton btnTipoTelFam = new JButton("▼");
        btnTipoTelFam.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
        btnTipoTelFam.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnTipoTelFam.setBounds(150, 140, 20, 20);
        pnlFamInner.add(txtTipoTelFam);
        pnlFamInner.add(btnTipoTelFam);
        pnlFamInner.add(new JTextField()).setBounds(180, 140, 400, 20);

        JButton btnOkFam = new JButton("OK");
        btnOkFam.setBounds(590, 140, 50, 20);
        pnlFamInner.add(btnOkFam);
        tbFamilia.addTab("Familia", pnlFamOuter);
        tbFamilia.addTab("Familiares", pnlFamInner);
        pnlFamiliares.add(tbFamilia);

        // ==========================================
        // TAB 4: INF. FISCAL BANCARIA
        // ==========================================
        JPanel pnlFiscBanc = new JPanel(null);
        JTabbedPane tbLateral = new JTabbedPane(JTabbedPane.RIGHT);
        tbLateral.setBounds(10, 10, 850, 380);

        JPanel pnlSubFiscal = new JPanel(null);
        pnlSubFiscal.setBorder(BorderFactory.createEtchedBorder());
        pnlSubFiscal.add(new JLabel("Razón Social")).setBounds(20, 20, 80, 25);
        JTextField txtNomf = new JTextField();
        txtNomf.setBounds(110, 20, 480, 25);
        pnlSubFiscal.add(txtNomf);
        pnlSubFiscal.add(new JLabel("Calle")).setBounds(20, 50, 80, 25);
        JTextField txtCallef = new JTextField();
        txtCallef.setBounds(110, 50, 320, 25);
        pnlSubFiscal.add(txtCallef);
        pnlSubFiscal.add(new JLabel("No. Ext")).setBounds(440, 50, 50, 25);
        JTextField txtExtf = new JTextField();
        txtExtf.setBounds(490, 50, 100, 25);
        pnlSubFiscal.add(txtExtf);
        pnlSubFiscal.add(new JLabel("Colonia")).setBounds(20, 80, 80, 25);
        JTextField txtColf = new JTextField();
        txtColf.setBounds(110, 80, 320, 25);
        pnlSubFiscal.add(txtColf);
        pnlSubFiscal.add(new JLabel("No. Int")).setBounds(440, 80, 50, 25);
        JTextField txtIntf = new JTextField();
        txtIntf.setBounds(490, 80, 100, 25);
        pnlSubFiscal.add(txtIntf);
        pnlSubFiscal.add(new JLabel("Código Postal")).setBounds(410, 110, 90, 25);
        JTextField txtCpf = new JTextField();
        txtCpf.setBounds(500, 110, 90, 25);
        pnlSubFiscal.add(txtCpf);

        pnlSubFiscal.add(new JLabel("Población")).setBounds(20, 140, 80, 25);
        JTextField txtPobf = new JTextField();
        txtPobf.setBounds(110, 140, 50, 25);
        JButton btnPobf = new JButton("▼");
        btnPobf.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
        btnPobf.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnPobf.setBounds(160, 140, 20, 25);
        pnlSubFiscal.add(txtPobf);
        pnlSubFiscal.add(btnPobf);

        pnlSubFiscal.add(new JLabel("Estado")).setBounds(20, 170, 80, 25);
        JTextField txtEdof = new JTextField();
        txtEdof.setBounds(110, 170, 50, 25);
        JButton btnEdof = new JButton("▼");
        btnEdof.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
        btnEdof.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnEdof.setBounds(160, 170, 20, 25);
        pnlSubFiscal.add(txtEdof);
        pnlSubFiscal.add(btnEdof);

        pnlSubFiscal.add(new JLabel("País")).setBounds(20, 200, 80, 25);
        JTextField txtPaisf = new JTextField("MEX");
        txtPaisf.setBounds(110, 200, 50, 25);
        JButton btnPaisf = new JButton("▼");
        btnPaisf.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
        btnPaisf.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnPaisf.setBounds(160, 200, 20, 25);
        pnlSubFiscal.add(txtPaisf);
        pnlSubFiscal.add(btnPaisf);

        pnlSubFiscal.add(new JLabel("R.F.C.")).setBounds(20, 230, 80, 25);
        JTextField txtRfc = new JTextField();
        txtRfc.setBounds(110, 230, 150, 25);
        pnlSubFiscal.add(txtRfc);

        pnlSubFiscal.add(new JLabel("Tipo Persona")).setBounds(300, 230, 90, 25);
        JRadioButton rbFisica = new JRadioButton("Física", true);
        rbFisica.setBounds(390, 230, 60, 25);
        JRadioButton rbMoral = new JRadioButton("Moral");
        rbMoral.setBounds(460, 230, 60, 25);
        ButtonGroup bgTpOper = new ButtonGroup();
        bgTpOper.add(rbFisica);
        bgTpOper.add(rbMoral);
        pnlSubFiscal.add(rbFisica);
        pnlSubFiscal.add(rbMoral);

        pnlSubFiscal.add(new JLabel("Reg Fiscal")).setBounds(20, 260, 80, 25);
        JTextField txtRegFisc = new JTextField();
        txtRegFisc.setBounds(110, 260, 50, 25);
        JButton btnRegFisc = new JButton("▼");
        btnRegFisc.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
        btnRegFisc.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnRegFisc.setBounds(160, 260, 20, 25);
        pnlSubFiscal.add(txtRegFisc);
        pnlSubFiscal.add(btnRegFisc);

        pnlSubFiscal.add(new JLabel("Uso CFDI")).setBounds(20, 290, 80, 25);
        JTextField txtUsoCFDI = new JTextField();
        txtUsoCFDI.setBounds(110, 290, 50, 25);
        JButton btnUsoCFDI = new JButton("▼");
        btnUsoCFDI.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
        btnUsoCFDI.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnUsoCFDI.setBounds(160, 290, 20, 25);
        pnlSubFiscal.add(txtUsoCFDI);
        pnlSubFiscal.add(btnUsoCFDI);

        pnlSubFiscal.add(new JLabel("Teléfono")).setBounds(20, 320, 80, 25);
        JTextField txtTelFisc = new JTextField();
        txtTelFisc.setBounds(110, 320, 190, 25);
        pnlSubFiscal.add(txtTelFisc);
        pnlSubFiscal.add(new JLabel("Correo Electrónico")).setBounds(315, 320, 120, 25);
        JTextField txtCorreoFiscal = new JTextField();
        txtCorreoFiscal.setBounds(440, 320, 260, 25);
        txtCorreoFiscal.setEditable(false);
        txtCorreoFiscal.setToolTipText("El correo fiscal todavía no tiene guardado implementado.");
        pnlSubFiscal.add(txtCorreoFiscal);

        JPanel pnlSubBancaria = new JPanel(null);
        JPanel pnlDeposito = new JPanel(null);
        pnlDeposito.setBorder(BorderFactory.createTitledBorder("Cuenta de Depósito"));
        pnlDeposito.setBounds(15, 15, 720, 185);
        pnlDeposito.add(new JLabel("Banco")).setBounds(20, 25, 120, 25);
        pnlDeposito.add(new JComboBox<>(new String[]{""})).setBounds(150, 25, 160, 25);
        pnlDeposito.add(new JLabel("Cuenta Bancaria")).setBounds(20, 65, 120, 25);
        pnlDeposito.add(new JTextField()).setBounds(150, 65, 260, 25);
        pnlDeposito.add(new JLabel("Tipo Cuenta")).setBounds(20, 105, 120, 25);
        pnlDeposito.add(new JComboBox<>(new String[]{""})).setBounds(150, 105, 160, 25);
        pnlDeposito.add(new JLabel("Nombre Titular")).setBounds(20, 145, 120, 25);
        pnlDeposito.add(new JTextField()).setBounds(150, 145, 540, 25);
        pnlSubBancaria.add(pnlDeposito);

        JPanel pnlMetodoPago = new JPanel(null);
        pnlMetodoPago.setBorder(BorderFactory.createTitledBorder("Método de Pago"));
        pnlMetodoPago.setBounds(15, 210, 720, 145);
        pnlMetodoPago.add(new JLabel("Forma de pago")).setBounds(20, 25, 120, 25);
        pnlMetodoPago.add(new JComboBox<>(new String[]{""})).setBounds(150, 25, 160, 25);
        pnlMetodoPago.add(new JLabel("Banco")).setBounds(20, 65, 120, 25);
        pnlMetodoPago.add(new JComboBox<>(new String[]{""})).setBounds(150, 65, 160, 25);
        pnlMetodoPago.add(new JLabel("Cuenta de Pago")).setBounds(20, 105, 120, 25);
        pnlMetodoPago.add(new JTextField()).setBounds(150, 105, 260, 25);
        pnlSubBancaria.add(pnlMetodoPago);
        // Estos datos aún no forman parte del alta ni de la modificación de alumnos.
        for (JPanel grupo : new JPanel[]{pnlDeposito, pnlMetodoPago}) {
            for (java.awt.Component campo : grupo.getComponents()) {
                if (campo instanceof JTextField texto) texto.setEditable(false);
                if (campo instanceof JComboBox<?> combo) combo.setEnabled(false);
                if (campo instanceof javax.swing.JComponent componente && !(campo instanceof JLabel)) {
                    componente.setToolTipText("Los datos bancarios todavía no tienen guardado implementado.");
                }
            }
        }

        tbLateral.addTab("Fiscal", pnlSubFiscal);
        tbLateral.addTab("Bancaria", pnlSubBancaria);
        pnlFiscBanc.add(tbLateral);

        // ==========================================

 render(pnlFamiliares,"alumno-familia");
 tbFamilia.setSelectedIndex(1); render(pnlFamiliares,"alumno-familiares");
 render(pnlFiscBanc,"alumno-fiscal");
 tbLateral.setSelectedIndex(1); render(pnlFiscBanc,"alumno-bancaria");
 System.out.println("OK: cuatro vistas renderizadas sin conexiones a la base de datos.");
 }
}
