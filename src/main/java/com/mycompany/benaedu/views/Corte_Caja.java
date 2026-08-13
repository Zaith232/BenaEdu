/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.benaedu.views;
import com.mycompany.benaedu.Dashboard;
import com.mycompany.benaedu.db.ConDB;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
/**
 *
 * @author b17za
 */
public class Corte_Caja extends javax.swing.JPanel {
private String usuarioLogueado = "Admin";
    private DefaultTableModel modDetalle;
    private JTable tblDetalleCorte;

    private JTextField txtCia;
    private JTextField txtCC;
    private JTextField txtCiclo;
    private JTextField txtCajero;
    private com.toedter.calendar.JDateChooser txtFecIni;
    private com.toedter.calendar.JDateChooser txtFecFin;

    private JRadioButton rbOficial;
    private JRadioButton rbPart;
    private JRadioButton rbNo;
    private JRadioButton rbSi;
    private JTextField txtCiaInf;
    private com.toedter.calendar.JDateChooser txtFecCont;

    private JTextField txtTotRecibos;
    private JTextField txtTotEfectivo;
    private JTextField txtTotCheques;
    private JTextField txtTotDep;
    private JTextField txtTotTarjetas;
    private JTextField txtTotTransf;
    private JTextField txtTotGlobal;

    // Totales calculados para impresión
    private double totImpBaseGlobal = 0.0;
    private double totBecaGlobal = 0.0;
    private double totDescGlobal = 0.0;
    private double totRecargoGlobal = 0.0;
    private double totPagadoGlobal = 0.0;
    private String cajeroNombreReporte = "";
    private String seccionNombreReporte = "PRIMARIA";

 // Estructura de datos para el reporte
    private static class FilaCorte {
        String numRec;
        String tipoRec;
        String matricula;
        String alumno;
        String grado;
        String grupo;
        String ncpto;
        String dcpto;
        String fven;
        double impte;
        double pbec;
        double ibec;
        double pdsc;
        double idsc;
        double prec;
        double irec;
        double ipag;
    }

    private List<FilaCorte> listaReporte = new ArrayList<>();
    /**
     * Creates new form Corte_Caja
     */
    public Corte_Caja(String usuarioLogueado) {
        if (usuarioLogueado != null && !usuarioLogueado.trim().isEmpty()) {
            this.usuarioLogueado = usuarioLogueado.trim();
        }
        initComponents();
        construirInterfazCorteCaja();
    }
    
    public Corte_Caja() {
        initComponents();
         construirInterfazCorteCaja();
      
    }
 private String obtenerUsuarioActivo() {
        if (this.usuarioLogueado != null && !this.usuarioLogueado.equals("Admin")) {
            return this.usuarioLogueado;
        }
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        if (parentWindow instanceof Dashboard dash) {
            return dash.getUsuarioCodigo();
        }
        return this.usuarioLogueado;
    }

   private void construirInterfazCorteCaja() {
        this.removeAll();
        this.setLayout(null);
        this.setBackground(new java.awt.Color(255, 255, 255));

        // --- BUSCADOR FLOTANTE LOCAL ---
        class BuscadorFlotante {
            void configurar(JTextField txtClave, JTextField txtDesc, JButton boton, Object[][] datos) {
                String[] columnas = {"Clave", "Descripción"};
                Runnable mostrarPopup = () -> {
                    JPopupMenu popup = new JPopupMenu();
                    popup.setFocusable(false);
                    DefaultTableModel mod = new DefaultTableModel(datos, columnas) {
                        @Override public boolean isCellEditable(int r, int c) { return false; }
                    };
                    JTable tabla = new JTable(mod);
                    tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                    tabla.getColumnModel().getColumn(0).setPreferredWidth(80);
                    tabla.getColumnModel().getColumn(1).setPreferredWidth(250);

                    TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(mod);
                    tabla.setRowSorter(sorter);

                    tabla.addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mouseReleased(java.awt.event.MouseEvent me) {
                            int viewRow = tabla.getSelectedRow();
                            if (viewRow != -1) {
                                int modelRow = tabla.convertRowIndexToModel(viewRow);
                                txtClave.setText(mod.getValueAt(modelRow, 0).toString());
                                if (txtDesc != null) {
                                    txtDesc.setText(mod.getValueAt(modelRow, 1).toString());
                                }
                                popup.setVisible(false);
                            }
                        }
                    });
                    JScrollPane scroll = new JScrollPane(tabla);
                    scroll.setPreferredSize(new java.awt.Dimension(340, 150));
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
            List<Object[]> lista = new ArrayList<>();
            try (Connection con = new ConDB().Conectar()) {
                if (con != null) {
                    PreparedStatement ps = con.prepareStatement(query);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        Object[] row = new Object[numCols];
                        for (int i = 0; i < numCols; i++) row[i] = rs.getString(i + 1);
                        lista.add(row);
                    }
                    rs.close(); ps.close();
                }
            } catch (Exception ignored) {}
            return lista.toArray(new Object[0][0]);
        };

        Object[][] dCia    = cargarDatosMultiple.apply("SELECT CIA, NCIA FROM tmcias ORDER BY CIA", 2);
        Object[][] dCajero = cargarDatosMultiple.apply("SELECT t.NEMP, e.NOME FROM tescaj t LEFT JOIN tgemp e ON t.NEMP = e.NEMP ORDER BY t.NEMP", 2);
        Object[][] dCC     = cargarDatosMultiple.apply("SELECT CVE, DES1 FROM tgcc WHERE CVE IN ('12100', '12200', '12300', '12400') ORDER BY CVE", 2);
        Object[][] dCiclo  = cargarDatosMultiple.apply("SELECT CESC, CDSC FROM tescesc ORDER BY CESC DESC", 2);

        // --- 1. DATOS DE SELECCIÓN ---
        JPanel pnlSeleccion = new JPanel(null);
        pnlSeleccion.setBackground(new java.awt.Color(255, 255, 255));
        pnlSeleccion.setBorder(BorderFactory.createTitledBorder("Datos de selección"));
        pnlSeleccion.setBounds(10, 10, 630, 90);

        // Compañía
        pnlSeleccion.add(new JLabel("Compañía")).setBounds(15, 20, 70, 25);
        txtCia = new JTextField("12"); txtCia.setBounds(80, 20, 40, 25);
        JButton btnCia = new JButton("▼"); btnCia.setFont(new Font("SansSerif", Font.PLAIN, 10)); btnCia.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnCia.setBounds(120, 20, 20, 25);
        buscador.configurar(txtCia, null, btnCia, dCia);
        pnlSeleccion.add(txtCia); pnlSeleccion.add(btnCia);

        // Cajero
        pnlSeleccion.add(new JLabel("Cajero")).setBounds(15, 55, 70, 25);
        txtCajero = new JTextField(); txtCajero.setBounds(80, 55, 40, 25);
        JButton btnCajero = new JButton("▼"); btnCajero.setFont(new Font("SansSerif", Font.PLAIN, 10)); btnCajero.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnCajero.setBounds(120, 55, 20, 25);
        buscador.configurar(txtCajero, null, btnCajero, dCajero);
        pnlSeleccion.add(txtCajero); pnlSeleccion.add(btnCajero);

        // Centro de Costos
        pnlSeleccion.add(new JLabel("C. Costos")).setBounds(220, 20, 70, 25);
        txtCC = new JTextField("12100"); txtCC.setBounds(290, 20, 50, 25);
        JButton btnCC = new JButton("▼"); btnCC.setFont(new Font("SansSerif", Font.PLAIN, 10)); btnCC.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnCC.setBounds(340, 20, 20, 25);
        buscador.configurar(txtCC, null, btnCC, dCC);
        pnlSeleccion.add(txtCC); pnlSeleccion.add(btnCC);

        // Fecha Inicial
        pnlSeleccion.add(new JLabel("Fecha Inicial")).setBounds(220, 55, 80, 25);
        txtFecIni = new com.toedter.calendar.JDateChooser();
        txtFecIni.setDateFormatString("dd/MM/yyyy");
        txtFecIni.setDate(new java.util.Date());
        txtFecIni.setBounds(290, 55, 100, 25);
        pnlSeleccion.add(txtFecIni);

        // Ciclo Escolar
        pnlSeleccion.add(new JLabel("Ciclo Escolar")).setBounds(400, 20, 80, 25);
        txtCiclo = new JTextField("2526"); txtCiclo.setBounds(480, 20, 50, 25);
        JButton btnCiclo = new JButton("▼"); btnCiclo.setFont(new Font("SansSerif", Font.PLAIN, 10)); btnCiclo.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnCiclo.setBounds(530, 20, 20, 25);
        buscador.configurar(txtCiclo, null, btnCiclo, dCiclo);
        pnlSeleccion.add(txtCiclo); pnlSeleccion.add(btnCiclo);

        // Fecha Final
        pnlSeleccion.add(new JLabel("Fecha Final")).setBounds(400, 55, 80, 25);
        txtFecFin = new com.toedter.calendar.JDateChooser();
        txtFecFin.setDateFormatString("dd/MM/yyyy");
        txtFecFin.setDate(new java.util.Date());
        txtFecFin.setBounds(480, 55, 100, 25);
        pnlSeleccion.add(txtFecFin);

        this.add(pnlSeleccion);

        // --- 2. TIPO DE CUENTA ---
        JPanel pnlTipoCta = new JPanel(null);
        pnlTipoCta.setBackground(new java.awt.Color(255, 255, 255));
        pnlTipoCta.setBorder(BorderFactory.createTitledBorder("Tipo de Cuenta"));
        pnlTipoCta.setBounds(650, 10, 240, 50);
        rbOficial = new JRadioButton("Oficial", true); rbOficial.setBackground(Color.WHITE); rbOficial.setBounds(20, 20, 80, 20);
        rbPart = new JRadioButton("Particular"); rbPart.setBackground(Color.WHITE); rbPart.setBounds(110, 20, 100, 20);
        ButtonGroup bgTipoCta = new ButtonGroup(); bgTipoCta.add(rbOficial); bgTipoCta.add(rbPart);
        pnlTipoCta.add(rbOficial); pnlTipoCta.add(rbPart);
        this.add(pnlTipoCta);

        // --- 3. CONTABILIZAR CORTE ---
        JPanel pnlContabilizar = new JPanel(null);
        pnlContabilizar.setBackground(new java.awt.Color(255, 255, 255));
        pnlContabilizar.setBorder(BorderFactory.createTitledBorder("Contabilizar Corte"));
        pnlContabilizar.setBounds(650, 65, 240, 50);
        rbNo = new JRadioButton("No", true); rbNo.setBackground(Color.WHITE); rbNo.setBounds(20, 20, 60, 20);
        rbSi = new JRadioButton("Si"); rbSi.setBackground(Color.WHITE); rbSi.setBounds(110, 20, 60, 20);
        ButtonGroup bgContab = new ButtonGroup(); bgContab.add(rbNo); bgContab.add(rbSi);
        pnlContabilizar.add(rbNo); pnlContabilizar.add(rbSi);
        this.add(pnlContabilizar);

        // --- 4. INFORMACIÓN CONTABLE ---
        JPanel pnlInfoCont = new JPanel(null);
        pnlInfoCont.setBackground(new java.awt.Color(255, 255, 255));
        pnlInfoCont.setBorder(BorderFactory.createTitledBorder("Información Contable"));
        pnlInfoCont.setBounds(10, 105, 630, 60);

        pnlInfoCont.add(new JLabel("Compañía")).setBounds(15, 20, 70, 25);
        txtCiaInf = new JTextField("12"); txtCiaInf.setBounds(80, 20, 40, 25);
        JButton btnCiaInf = new JButton("▼"); btnCiaInf.setFont(new Font("SansSerif", Font.PLAIN, 10)); btnCiaInf.setMargin(new java.awt.Insets(0, 0, 0, 0)); btnCiaInf.setBounds(120, 20, 20, 25);
        buscador.configurar(txtCiaInf, null, btnCiaInf, dCia);
        pnlInfoCont.add(txtCiaInf); pnlInfoCont.add(btnCiaInf);

        pnlInfoCont.add(new JLabel("Fecha Contable")).setBounds(220, 20, 100, 25);
        txtFecCont = new com.toedter.calendar.JDateChooser();
        txtFecCont.setDateFormatString("dd/MM/yyyy"); txtFecCont.setDate(new java.util.Date());
        txtFecCont.setBounds(320, 20, 100, 25);
        pnlInfoCont.add(txtFecCont);

        this.add(pnlInfoCont);

        // --- BOTÓN FILTRA INFORMACIÓN ---
        JButton btnFiltra = new JButton("Filtra Información");
        btnFiltra.setBounds(650, 125, 240, 35);
        this.add(btnFiltra);

        // --- 5. TABLA CORTE DE CAJA ---
        modDetalle = new DefaultTableModel(
            new Object[][]{}, 
            new String[]{"No. Recibo", "Tipo", "Matrícula", "Nombre del Alumno", "Concepto", "Fec Venc", "Importe", "% Beca", "Imp Beca", "Recargo", "Total Pagado"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tblDetalleCorte = new JTable(modDetalle);
        tblDetalleCorte.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tblDetalleCorte.getColumnModel().getColumn(0).setPreferredWidth(75);
        tblDetalleCorte.getColumnModel().getColumn(1).setPreferredWidth(45);
        tblDetalleCorte.getColumnModel().getColumn(2).setPreferredWidth(85);
        tblDetalleCorte.getColumnModel().getColumn(3).setPreferredWidth(210);
        tblDetalleCorte.getColumnModel().getColumn(4).setPreferredWidth(170);
        tblDetalleCorte.getColumnModel().getColumn(5).setPreferredWidth(80);
        tblDetalleCorte.getColumnModel().getColumn(6).setPreferredWidth(75);
        tblDetalleCorte.getColumnModel().getColumn(7).setPreferredWidth(60);
        tblDetalleCorte.getColumnModel().getColumn(8).setPreferredWidth(75);
        tblDetalleCorte.getColumnModel().getColumn(9).setPreferredWidth(70);
        tblDetalleCorte.getColumnModel().getColumn(10).setPreferredWidth(85);

        JScrollPane scrollDetalle = new JScrollPane(tblDetalleCorte);
        scrollDetalle.setBounds(10, 175, 880, 290);
        scrollDetalle.setBorder(BorderFactory.createTitledBorder("Detalle de Movimientos"));
        this.add(scrollDetalle);

        // --- 6. TOTALES ---
        JPanel pnlTotales = new JPanel(null);
        pnlTotales.setBackground(new java.awt.Color(255, 255, 255));
        pnlTotales.setBorder(BorderFactory.createTitledBorder("Totales por Forma de Pago"));
        pnlTotales.setBounds(10, 470, 880, 70);

        txtTotRecibos   = new JTextField("0"); txtTotRecibos.setHorizontalAlignment(JTextField.RIGHT); txtTotRecibos.setEditable(false);
        txtTotEfectivo = new JTextField("0.00"); txtTotEfectivo.setHorizontalAlignment(JTextField.RIGHT); txtTotEfectivo.setEditable(false);
        txtTotCheques  = new JTextField("0.00"); txtTotCheques.setHorizontalAlignment(JTextField.RIGHT); txtTotCheques.setEditable(false);
        txtTotDep      = new JTextField("0.00"); txtTotDep.setHorizontalAlignment(JTextField.RIGHT); txtTotDep.setEditable(false);
        txtTotTarjetas = new JTextField("0.00"); txtTotTarjetas.setHorizontalAlignment(JTextField.RIGHT); txtTotTarjetas.setEditable(false);
        txtTotTransf   = new JTextField("0.00"); txtTotTransf.setHorizontalAlignment(JTextField.RIGHT); txtTotTransf.setEditable(false);
        txtTotGlobal   = new JTextField("0.00"); txtTotGlobal.setHorizontalAlignment(JTextField.RIGHT); txtTotGlobal.setEditable(false);

        String[] lbls = {"Num. Recibos", "Efectivo", "Cheques", "Dep. Bancario", "Tarjetas", "T. Electronica", "Total Cobrado"};
        JTextField[] fields = {txtTotRecibos, txtTotEfectivo, txtTotCheques, txtTotDep, txtTotTarjetas, txtTotTransf, txtTotGlobal};

        int xPos = 20;
        for (int i = 0; i < lbls.length; i++) {
            JLabel l = new JLabel(lbls[i], SwingUtilities.CENTER);
            l.setBounds(xPos, 15, 110, 20);
            fields[i].setBounds(xPos, 35, 110, 25);
            pnlTotales.add(l); 
            pnlTotales.add(fields[i]);
            xPos += 120;
        }
        this.add(pnlTotales);

        // --- 7. BOTONES INFERIORES ---
        JButton btnImprimir = new JButton("Imprimir");
        btnImprimir.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnImprimir.setBounds(330, 550, 110, 35);

        JButton btnSalir = new JButton("Salir");
        btnSalir.setBounds(460, 550, 110, 35);

        this.add(btnImprimir);
        this.add(btnSalir);

        btnSalir.addActionListener(e -> {
            this.removeAll();
            this.revalidate();
            this.repaint();
        });

        // --- 8. LÓGICA DE FILTRADO, POBLADO DE pasocortecaja Y CÁLCULOS ---
        btnFiltra.addActionListener(e -> {
            modDetalle.setRowCount(0);
            listaReporte.clear();

            String cia = txtCia.getText().trim();
            String cc = txtCC.getText().trim();
            String ciclo = txtCiclo.getText().trim();
            String cajeroEmp = txtCajero.getText().trim();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String fIni = txtFecIni.getDate() != null ? sdf.format(txtFecIni.getDate()) : "";
            String fFin = txtFecFin.getDate() != null ? sdf.format(txtFecFin.getDate()) : "";

            try (Connection con = new ConDB().Conectar()) {
                if (con != null) {
                    con.setAutoCommit(false);

                    // A) Obtener nombre de la sección / centro de costo
                    if (!cc.isEmpty()) {
                        PreparedStatement psCC = con.prepareStatement("SELECT DES1 FROM tgcc WHERE CVE = ? LIMIT 1");
                        psCC.setString(1, cc);
                        ResultSet rsCC = psCC.executeQuery();
                        if (rsCC.next()) {
                            seccionNombreReporte = rsCC.getString("DES1");
                        }
                        rsCC.close(); psCC.close();
                    }

                    // B) Resolver nombre de cajero
                    cajeroNombreReporte = "TODOS";
                    if (!cajeroEmp.isEmpty()) {
                        PreparedStatement psUser = con.prepareStatement(
                            "SELECT e.NOME, t.USER FROM tescaj t LEFT JOIN tgemp e ON t.NEMP = e.NEMP WHERE t.NEMP = ? LIMIT 1"
                        );
                        psUser.setString(1, cajeroEmp);
                        ResultSet rsUser = psUser.executeQuery();
                        if (rsUser.next()) {
                            String nomEmp = rsUser.getString("NOME");
                            cajeroNombreReporte = (nomEmp != null && !nomEmp.isEmpty()) ? nomEmp : rsUser.getString("USER");
                        }
                        rsUser.close(); psUser.close();
                    }

                    // 1. Limpiar pasocortecaja
                    PreparedStatement psClean = con.prepareStatement("DELETE FROM pasocortecaja");
                    psClean.executeUpdate();
                    psClean.close();

                    // 2. Poblar pasocortecaja desde tesralu con todas las columnas numéricas
                    StringBuilder sqlPopulate = new StringBuilder(
                        "INSERT INTO pasocortecaja (CIA, CC, SECC, CESC, MAT, NOMALU, GRADO, GRUPO, NREC, TREC, FREC, NCPTO, TCPTO, DCPTO, IMPMN, PBEC, IBECMN, PDSC, IDSCMN, PREC, IRECMN, IPAGMN, FVEN, NCAJ) " +
                        "SELECT CIA, CC, SECC, CESC, MAT, NOMALU, GRADO, GRUPO, NREC, TREC, FREC, NCPTO, TCPTO, DCPTO, IMPMN, PBEC, IBECMN, PDSC, IDSCMN, PREC, IRECMN, IPAGMN, FVEN, NCAJ " +
                        "FROM tesralu WHERE (MCAN IS NULL OR MCAN = '') AND IPAGMN > 0 "
                    );

                    if (!cia.isEmpty()) sqlPopulate.append(" AND CIA = ? ");
                    if (!cc.isEmpty()) sqlPopulate.append(" AND CC = ? ");
                    if (!ciclo.isEmpty()) sqlPopulate.append(" AND CESC = ? ");
                    if (!cajeroEmp.isEmpty()) sqlPopulate.append(" AND NCAJ = ? ");
                    if (!fIni.isEmpty() && !fFin.isEmpty()) sqlPopulate.append(" AND FPAG BETWEEN ? AND ? ");

                    PreparedStatement psPopulate = con.prepareStatement(sqlPopulate.toString());
                    int pIdx = 1;
                    if (!cia.isEmpty()) psPopulate.setString(pIdx++, cia);
                    if (!cc.isEmpty()) psPopulate.setString(pIdx++, cc);
                    if (!ciclo.isEmpty()) psPopulate.setString(pIdx++, ciclo);
                    if (!cajeroEmp.isEmpty()) psPopulate.setString(pIdx++, cajeroEmp);
                    if (!fIni.isEmpty() && !fFin.isEmpty()) {
                        psPopulate.setString(pIdx++, fIni);
                        psPopulate.setString(pIdx++, fFin);
                    }

                    psPopulate.executeUpdate();
                    psPopulate.close();
                    con.commit();

                    // 3. Consultar pasocortecaja para la tabla y estructura del reporte
                    PreparedStatement psRead = con.prepareStatement(
                        "SELECT NREC, TREC, MAT, NOMALU, GRADO, GRUPO, NCPTO, DCPTO, FVEN, IMPMN, PBEC, IBECMN, PDSC, IDSCMN, PREC, IRECMN, IPAGMN " +
                        "FROM pasocortecaja ORDER BY NREC ASC, NCPTO ASC"
                    );
                    ResultSet rs = psRead.executeQuery();
                    DecimalFormat df = new DecimalFormat("#,##0.00");

                    totImpBaseGlobal = 0.0;
                    totBecaGlobal = 0.0;
                    totDescGlobal = 0.0;
                    totRecargoGlobal = 0.0;
                    totPagadoGlobal = 0.0;

                    while (rs.next()) {
                        FilaCorte f = new FilaCorte();
                        f.numRec = rs.getString("NREC");
                        f.tipoRec = rs.getString("TREC");
                        f.matricula = rs.getString("MAT");
                        f.alumno = rs.getString("NOMALU");
                        f.grado = rs.getString("GRADO");
                        f.grupo = rs.getString("GRUPO");
                        f.ncpto = rs.getString("NCPTO");
                        f.dcpto = rs.getString("DCPTO");
                        f.fven = rs.getDate("FVEN") != null ? new SimpleDateFormat("dd/MM/yyyy").format(rs.getDate("FVEN")) : "";
                        f.impte = rs.getDouble("IMPMN");
                        f.pbec = rs.getDouble("PBEC");
                        f.ibec = rs.getDouble("IBECMN");
                        f.pdsc = rs.getDouble("PDSC");
                        f.idsc = rs.getDouble("IDSCMN");
                        f.prec = rs.getDouble("PREC");
                        f.irec = rs.getDouble("IRECMN");
                        f.ipag = rs.getDouble("IPAGMN");

                        listaReporte.add(f);

                        totImpBaseGlobal += f.impte;
                        totBecaGlobal += f.ibec;
                        totDescGlobal += f.idsc;
                        totRecargoGlobal += f.irec;
                        totPagadoGlobal += f.ipag;

                        modDetalle.addRow(new Object[]{
                            f.numRec, f.tipoRec, f.matricula, f.alumno, f.dcpto, f.fven,
                            df.format(f.impte), df.format(f.pbec), df.format(f.ibec),
                            df.format(f.irec), df.format(f.ipag)
                        });
                    }
                    rs.close(); psRead.close();

                    // 4. CALCULAR FORMAS DE PAGO CON JOIN EXACTO A pasocortecaja
                    String sqlPagos = "SELECT p.FMAPAG, SUM(p.IMPMN) AS TOTAL_FORMA " +
                                     "FROM tespalu p " +
                                     "INNER JOIN (SELECT DISTINCT CIA, NREC, MAT FROM pasocortecaja) pc " +
                                     "ON p.CIA = pc.CIA AND p.NREC = pc.NREC AND p.MAT = pc.MAT " +
                                     "WHERE (p.MCAN IS NULL OR p.MCAN = '') " +
                                     "GROUP BY p.FMAPAG";

                    PreparedStatement psPagos = con.prepareStatement(sqlPagos);
                    ResultSet rsPagos = psPagos.executeQuery();

                    double totEf = 0, totCh = 0, totDp = 0, totTc = 0, totTe = 0;
                    boolean huboPagos = false;

                    while (rsPagos.next()) {
                        huboPagos = true;
                        String fma = rsPagos.getString("FMAPAG") != null ? rsPagos.getString("FMAPAG").trim().toUpperCase() : "";
                        double montoFma = rsPagos.getDouble("TOTAL_FORMA");

                        if ("EF".equals(fma) || "E".equals(fma)) totEf += montoFma;
                        else if ("CH".equals(fma) || "C".equals(fma)) totCh += montoFma;
                        else if ("DB".equals(fma) || "DP".equals(fma) || "D".equals(fma)) totDp += montoFma;
                        else if ("TC".equals(fma) || "TD".equals(fma) || "T".equals(fma)) totTc += montoFma;
                        else if ("TE".equals(fma) || "TR".equals(fma)) totTe += montoFma;
                        else totEf += montoFma;
                    }
                    rsPagos.close(); psPagos.close();

                    // Respaldo: Si no se desglosó tespalu, el total pagado se asigna a efectivo
                    if (!huboPagos && totPagadoGlobal > 0) {
                        totEf = totPagadoGlobal;
                    }

                    // Conteo de recibos únicos
                    PreparedStatement psCountRec = con.prepareStatement("SELECT COUNT(DISTINCT NREC) FROM pasocortecaja");
                    ResultSet rsC = psCountRec.executeQuery();
                    int cantRecibosUnicos = 0;
                    if (rsC.next()) cantRecibosUnicos = rsC.getInt(1);
                    rsC.close(); psCountRec.close();

                    // 5. Asignar Totales a la UI
                    txtTotRecibos.setText(String.valueOf(cantRecibosUnicos));
                    txtTotEfectivo.setText(df.format(totEf));
                    txtTotCheques.setText(df.format(totCh));
                    txtTotDep.setText(df.format(totDp));
                    txtTotTarjetas.setText(df.format(totTc));
                    txtTotTransf.setText(df.format(totTe));
                    txtTotGlobal.setText(df.format(totPagadoGlobal));

                    if (modDetalle.getRowCount() == 0) {
                        JOptionPane.showMessageDialog(this, "No se encontraron movimientos para generar el corte.", "Información", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al generar corte de caja: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // --- 9. IMPRESIÓN IDÉNTICA AL REPORTE PDF (CORTE DE CAJA.pdf) ---
        btnImprimir.addActionListener(e -> {
            if (listaReporte.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay datos filtrados para imprimir.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                PrinterJob job = PrinterJob.getPrinterJob();
                job.setJobName("Corte de Caja - " + cajeroNombreReporte);

                SimpleDateFormat sdfDia = new SimpleDateFormat("dd/MM/yyyy");
                String fIniTxt = txtFecIni.getDate() != null ? sdfDia.format(txtFecIni.getDate()) : "";
                String fFinTxt = txtFecFin.getDate() != null ? sdfDia.format(txtFecFin.getDate()) : "";
                String fechaHoy = sdfDia.format(new Date());
                String horaHoy = new SimpleDateFormat("HH:mm:ss").format(new Date());
                String cicloTxt = txtCiclo.getText().trim();

                job.setPrintable((g, pf, pageIndex) -> {
                    int filasPorPagina = 18;
                    int totalPaginas = (int) Math.ceil((double) listaReporte.size() / filasPorPagina);
                    if (totalPaginas == 0) totalPaginas = 1;

                    if (pageIndex >= totalPaginas) return Printable.NO_SUCH_PAGE;

                    Graphics2D g2d = (Graphics2D) g;
                    g2d.translate(pf.getImageableX(), pf.getImageableY());

                    int y = 35;
                    // ENCABEZADO
                    g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
                    g2d.drawString("UNIDAD ESCOLAR BENAVENTE, A.C.", 35, y);
                    g2d.setFont(new Font("SansSerif", Font.PLAIN, 8));
                    g2d.drawString("Fecha: " + fechaHoy, 450, y);
                    y += 14;

                    g2d.setFont(new Font("SansSerif", Font.BOLD, 10));
                    g2d.drawString("Corte de Caja", 35, y);
                    g2d.setFont(new Font("SansSerif", Font.PLAIN, 8));
                    g2d.drawString("Hora: " + horaHoy, 450, y);
                    y += 12;

                    g2d.setFont(new Font("SansSerif", Font.PLAIN, 8));
                    g2d.drawString("Del " + fIniTxt + " al " + fFinTxt, 35, y);
                    g2d.drawString("Página: " + (pageIndex + 1), 450, y);
                    y += 12;

                    g2d.drawString("Cajero: " + cajeroNombreReporte, 35, y);
                    y += 14;
                    g2d.setFont(new Font("SansSerif", Font.BOLD, 9));
                    g2d.drawString(seccionNombreReporte.toUpperCase() + "  |  Ciclo: " + cicloTxt, 35, y);
                    y += 10;
                    g2d.drawLine(35, y, 540, y);
                    y += 12;

                    // CABECERA DE TABLA
                    g2d.setFont(new Font("SansSerif", Font.BOLD, 7));
                    g2d.drawString("No", 35, y);
                    g2d.drawString("Tipo", 60, y);
                    g2d.drawString("Matrícula / Concepto", 85, y);
                    g2d.drawString("Nombre Alumno / Fec Venc", 185, y);
                    g2d.drawString("Importe", 320, y);
                    g2d.drawString("% Beca", 365, y);
                    g2d.drawString("Imp Beca", 400, y);
                    g2d.drawString("Recargo", 445, y);
                    g2d.drawString("Pagado", 495, y);
                    y += 4;
                    g2d.drawLine(35, y, 540, y);
                    y += 10;

                    int inicio = pageIndex * filasPorPagina;
                    int fin = Math.min(inicio + filasPorPagina, listaReporte.size());
                    DecimalFormat df = new DecimalFormat("#,##0.00");

                    String ultimoRecibo = "";

                    for (int i = inicio; i < fin; i++) {
                        FilaCorte item = listaReporte.get(i);
                        g2d.setFont(new Font("SansSerif", Font.PLAIN, 7));

                        if (!item.numRec.equals(ultimoRecibo)) {
                            g2d.setFont(new Font("SansSerif", Font.BOLD, 7));
                            g2d.drawString(item.numRec, 35, y);
                            g2d.drawString(item.tipoRec, 60, y);
                            g2d.drawString(item.matricula, 85, y);

                            String nom = item.alumno;
                            if (nom.length() > 25) nom = nom.substring(0, 23) + "..";
                            g2d.drawString(nom, 185, y);
                            g2d.drawString("Grd: " + item.grado + "  Gpo: " + item.grupo, 320, y);
                            y += 9;
                            ultimoRecibo = item.numRec;
                        }

                        g2d.setFont(new Font("SansSerif", Font.PLAIN, 7));
                        String desc = item.dcpto;
                        if (desc.length() > 22) desc = desc.substring(0, 20) + "..";
                        g2d.drawString("• " + desc, 90, y);
                        g2d.drawString(item.fven, 195, y);
                        g2d.drawString(df.format(item.impte), 320, y);
                        g2d.drawString(df.format(item.pbec), 365, y);
                        g2d.drawString(df.format(item.ibec), 400, y);
                        g2d.drawString(df.format(item.irec), 445, y);
                        g2d.drawString(df.format(item.ipag), 495, y);
                        y += 10;
                    }

                    // RESUMEN Y TOTALES AL FINAL DE LA ÚLTIMA PÁGINA
                    if (pageIndex == totalPaginas - 1) {
                        y += 5;
                        g2d.drawLine(35, y, 540, y);
                        y += 12;

                        g2d.setFont(new Font("SansSerif", Font.BOLD, 8));
                        g2d.drawString("Totales Corte: " + seccionNombreReporte.toUpperCase(), 35, y);
                        y += 12;
                        g2d.drawString("Totales Forma de Pago:", 35, y);
                        y += 10;

                        g2d.setFont(new Font("SansSerif", Font.BOLD, 7));
                        g2d.drawString("Num. Recibos", 35, y);
                        g2d.drawString("Efectivo", 100, y);
                        g2d.drawString("Cheques", 175, y);
                        g2d.drawString("Dep. Bancario", 245, y);
                        g2d.drawString("Tarjetas", 330, y);
                        g2d.drawString("T. Electronica", 400, y);
                        g2d.drawString("Total Cobrado", 475, y);
                        y += 9;

                        g2d.setFont(new Font("SansSerif", Font.PLAIN, 7));
                        g2d.drawString(txtTotRecibos.getText(), 35, y);
                        g2d.drawString(txtTotEfectivo.getText(), 100, y);
                        g2d.drawString(txtTotCheques.getText(), 175, y);
                        g2d.drawString(txtTotDep.getText(), 245, y);
                        g2d.drawString(txtTotTarjetas.getText(), 330, y);
                        g2d.drawString(txtTotTransf.getText(), 400, y);
                        g2d.setFont(new Font("SansSerif", Font.BOLD, 7));
                        g2d.drawString(txtTotGlobal.getText(), 475, y);
                        y += 14;

                        g2d.drawLine(35, y, 540, y);
                        y += 10;

                        g2d.setFont(new Font("SansSerif", Font.BOLD, 7));
                        g2d.drawString("Importe", 35, y);
                        g2d.drawString("Imp Beca", 140, y);
                        g2d.drawString("Imp Desc", 240, y);
                        g2d.drawString("Imp Rec", 340, y);
                        g2d.drawString("Pagado", 475, y);
                        y += 9;

                        g2d.setFont(new Font("SansSerif", Font.PLAIN, 7));
                        g2d.drawString("$" + df.format(totImpBaseGlobal), 35, y);
                        g2d.drawString("$" + df.format(totBecaGlobal), 140, y);
                        g2d.drawString("$" + df.format(totDescGlobal), 240, y);
                        g2d.drawString("$" + df.format(totRecargoGlobal), 340, y);
                        g2d.setFont(new Font("SansSerif", Font.BOLD, 7));
                        g2d.drawString("$" + df.format(totPagadoGlobal), 475, y);
                        y += 18;

                        g2d.setFont(new Font("SansSerif", Font.ITALIC, 6));
                        g2d.drawString("Sistema Administrativo KCS  -  Kosmos Consultores, S.A. de C.V. Todos los Derechos Reservados", 35, y);
                    }

                    return Printable.PAGE_EXISTS;
                });

                if (job.printDialog()) {
                    job.print();
                    JOptionPane.showMessageDialog(this, "Corte de caja enviado a la impresora.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error durante la impresión: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        tblCCaja = new javax.swing.JTable();
        btnAddCCaja = new javax.swing.JButton();
        btnEditCCaja = new javax.swing.JButton();
        btnDeleteCCaja = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        tblCCaja.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tblCCaja);

        btnAddCCaja.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAddCCaja.setForeground(new java.awt.Color(26, 61, 99));
        btnAddCCaja.setIcon(new javax.swing.ImageIcon(getClass().getResource("/add.png"))); // NOI18N
        btnAddCCaja.setText("Añadir");
        btnAddCCaja.addActionListener(this::btnAddCCajaActionPerformed);

        btnEditCCaja.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEditCCaja.setForeground(new java.awt.Color(26, 61, 99));
        btnEditCCaja.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edit.png"))); // NOI18N
        btnEditCCaja.setText("Editar");
        btnEditCCaja.setMaximumSize(new java.awt.Dimension(93, 31));
        btnEditCCaja.setMinimumSize(new java.awt.Dimension(93, 31));
        btnEditCCaja.addActionListener(this::btnEditCCajaActionPerformed);

        btnDeleteCCaja.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDeleteCCaja.setForeground(new java.awt.Color(26, 61, 99));
        btnDeleteCCaja.setIcon(new javax.swing.ImageIcon(getClass().getResource("/delete.png"))); // NOI18N
        btnDeleteCCaja.setText("Eliminar");
        btnDeleteCCaja.addActionListener(this::btnDeleteCCajaActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAddCCaja)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditCCaja, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteCCaja)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddCCaja, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditCCaja, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteCCaja, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void btnAddCCajaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddCCajaActionPerformed
      
    }//GEN-LAST:event_btnAddCCajaActionPerformed

    private void btnEditCCajaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditCCajaActionPerformed
      
    }//GEN-LAST:event_btnEditCCajaActionPerformed

    private void btnDeleteCCajaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteCCajaActionPerformed

    }//GEN-LAST:event_btnDeleteCCajaActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddCCaja;
    private javax.swing.JButton btnDeleteCCaja;
    private javax.swing.JButton btnEditCCaja;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblCCaja;
    // End of variables declaration//GEN-END:variables
}
