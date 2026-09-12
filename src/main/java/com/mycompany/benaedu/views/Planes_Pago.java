/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.benaedu.views;
import com.mycompany.benaedu.db.ConDB;
import java.awt.Window;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author b17za
 */
public class Planes_Pago extends javax.swing.JPanel {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("hh:mm:ss a");
    private final String usuarioLogueado;

    /** Identifica de forma completa un plan de pagos. */
    private record ClavePlan(String cia, String cc, String ciclo, String tipo, String plan) {}

    /** Representa una partida validada del detalle de un plan. */
    private record DetallePlan(int sec, String concepto, String descripcion, String tipoConcepto,
            BigDecimal importe, BigDecimal descuento, LocalDate fechaInicio, LocalDate fechaFin,
            String usuario, LocalDate fechaActualizacion, String horaActualizacion) {}

    /**
     * Creates new form Planes_Pago
     */
    public Planes_Pago() {
        this("Admin");
    }

    /**
     * Crea el panel usando el usuario autenticado para los campos de auditoría.
     *
     * @param usuarioLogueado alias del usuario que inició sesión
     */
    public Planes_Pago(String usuarioLogueado) {
        this.usuarioLogueado = usuarioLogueado == null || usuarioLogueado.isBlank()
                ? "Admin" : usuarioLogueado.trim();
        initComponents();
        cargarTablaPlanes();
    }

    private void cargarTablaPlanes() {
        DefaultTableModel modelo = new DefaultTableModel(
            new Object[][] {}, 
            new String[] {"Compañía", "Centro Costos", "Ciclo Escolar", "Tipo Pago", "Cve Plan", "Descripción", "Fec. Vig. Ini.", "Fec. Vig. Fin", "Usuario", "Fech. Ult. Act.", "Hora, Ult. Act."}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        tblPPagos.setModel(modelo);

        ConDB db = new ConDB();
        try {
            Connection con = db.Conectar();

            if (con != null) {
                String sql = "SELECT CIA, CC, CESC, TGPO, CGPO, DGPO, FVINI, FVFIN, USER, FEAC, HOAC FROM tesgpge "
                        + "UNION ALL "
                        + "SELECT d.CIA, d.CC, d.CESC, d.TGPO, d.CGPO, CONCAT('Plan ', d.CGPO), "
                        + "MIN(d.FVINI), MAX(d.FVFIN), '', NULL, '' "
                        + "FROM tesgpde d WHERE NOT EXISTS (SELECT 1 FROM tesgpge h "
                        + "WHERE h.CIA=d.CIA AND h.CC=d.CC AND h.CESC=d.CESC AND h.TGPO=d.TGPO AND h.CGPO=d.CGPO) "
                        + "GROUP BY d.CIA, d.CC, d.CESC, d.TGPO, d.CGPO "
                        + "ORDER BY CESC DESC, CIA, CC, CGPO";
                try (PreparedStatement ps = con.prepareStatement(sql);
                        ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Object[] fila = new Object[11];
                        fila[0] = rs.getString("CIA");
                        fila[1] = rs.getString("CC");
                        fila[2] = rs.getString("CESC");
                        fila[3] = rs.getString("TGPO");
                        fila[4] = rs.getString("CGPO");
                        fila[5] = rs.getString("DGPO");
                        fila[6] = rs.getString("FVINI") != null ? rs.getString("FVINI") : "";
                        fila[7] = rs.getString("FVFIN") != null ? rs.getString("FVFIN") : "";
                        fila[8] = rs.getString("USER") != null ? rs.getString("USER") : "";
                        fila[9] = rs.getString("FEAC") != null ? rs.getString("FEAC") : "";
                        fila[10] = rs.getString("HOAC") != null ? rs.getString("HOAC") : "";
                        modelo.addRow(fila);
                    }
                }
                adaptarTamañoColumnas();
            } else {
                throw new SQLException("No fue posible conectar con la base de datos.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar la tabla de Planes de Pago: " + e.getMessage());
        } finally {
            db.Cerrar();
        }
    }

    private void adaptarTamañoColumnas() {
        tblPPagos.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF); 
        
        for (int i = 0; i < tblPPagos.getColumnCount(); i++) {
            javax.swing.table.TableColumn columna = tblPPagos.getColumnModel().getColumn(i);
            int anchoPreferido = 60; 
            
            java.awt.Component compCabecera = tblPPagos.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(tblPPagos, columna.getHeaderValue(), false, false, 0, i);
            anchoPreferido = Math.max(anchoPreferido, compCabecera.getPreferredSize().width + 10);
            
            for (int r = 0; r < tblPPagos.getRowCount(); r++) {
                javax.swing.table.TableCellRenderer renderizador = tblPPagos.getCellRenderer(r, i);
                java.awt.Component c = tblPPagos.prepareRenderer(renderizador, r, i);
                anchoPreferido = Math.max(anchoPreferido, c.getPreferredSize().width + 15); 
            }
            columna.setPreferredWidth(anchoPreferido); 
        }
    }

    /**
     * Lee y valida las partidas capturadas antes de modificar la base de datos.
     *
     * @param modelo modelo de la tabla de partidas
     * @return partidas con valores numéricos y fechas validados
     * @throws IllegalArgumentException si una partida contiene datos inválidos
     */
    private List<DetallePlan> leerDetalles(DefaultTableModel modelo) {
        if (modelo.getRowCount() == 0) {
            throw new IllegalArgumentException("Agrega al menos un concepto al plan.");
        }

        List<DetallePlan> detalles = new ArrayList<>();
        Set<Integer> secuencias = new HashSet<>();
        for (int i = 0; i < modelo.getRowCount(); i++) {
            try {
                int sec = Integer.parseInt(modelo.getValueAt(i, 0).toString().trim());
                String concepto = modelo.getValueAt(i, 1).toString().trim();
                String descripcion = modelo.getValueAt(i, 2).toString().trim();
                BigDecimal importe = new BigDecimal(modelo.getValueAt(i, 3).toString().replace(",", "").trim());
                BigDecimal descuento = new BigDecimal(modelo.getValueAt(i, 4).toString().replace(",", "").trim());
                LocalDate fechaInicio = LocalDate.parse(modelo.getValueAt(i, 5).toString().trim());
                LocalDate fechaFin = LocalDate.parse(modelo.getValueAt(i, 6).toString().trim());

                if (sec <= 0 || !secuencias.add(sec)) {
                    throw new IllegalArgumentException("La secuencia debe ser positiva y no repetirse.");
                }
                if (concepto.isEmpty() || descripcion.isEmpty()) {
                    throw new IllegalArgumentException("El concepto y su descripción son obligatorios.");
                }
                if (importe.signum() < 0 || descuento.signum() < 0 || descuento.compareTo(BigDecimal.valueOf(100)) > 0) {
                    throw new IllegalArgumentException("El importe no puede ser negativo y el descuento debe estar entre 0 y 100.");
                }
                if (fechaFin.isBefore(fechaInicio)) {
                    throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial.");
                }

                detalles.add(new DetallePlan(sec, concepto, descripcion, "", importe, descuento,
                        fechaInicio, fechaFin, usuarioLogueado, LocalDate.now(),
                        LocalTime.now().format(FORMATO_HORA)));
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException("Partida " + (i + 1) + ": " + ex.getMessage(), ex);
            }
        }
        return detalles;
    }

    /**
     * Verifica el ciclo y completa el tipo de cada concepto para el centro seleccionado.
     *
     * @param con conexión activa
     * @param clave clave del plan
     * @param detalles partidas capturadas
     * @return partidas asociadas con conceptos válidos
     * @throws SQLException si el ciclo o algún concepto no pertenece al centro de costos
     */
    private List<DetallePlan> validarCatalogos(Connection con, ClavePlan clave, List<DetallePlan> detalles) throws SQLException {
        String sqlCiclo = "SELECT 1 FROM tescesc WHERE CIA=? AND CC=? AND CESC=? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sqlCiclo)) {
            ps.setString(1, clave.cia());
            ps.setString(2, clave.cc());
            ps.setString(3, clave.ciclo());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("El ciclo " + clave.ciclo() + " no pertenece al centro " + clave.cc() + ".");
                }
            }
        }

        List<DetallePlan> resultado = new ArrayList<>();
        String sqlConcepto = "SELECT TCPTO FROM tescpto WHERE CIA=? AND CC=? AND NCPTO=? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sqlConcepto)) {
            for (DetallePlan detalle : detalles) {
                ps.setString(1, clave.cia());
                ps.setString(2, clave.cc());
                ps.setString(3, detalle.concepto());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("El concepto " + detalle.concepto() + " no pertenece al centro " + clave.cc() + ".");
                    }
                    resultado.add(new DetallePlan(detalle.sec(), detalle.concepto(), detalle.descripcion(),
                            rs.getString("TCPTO"), detalle.importe(), detalle.descuento(),
                            detalle.fechaInicio(), detalle.fechaFin(), detalle.usuario(),
                            detalle.fechaActualizacion(), detalle.horaActualizacion()));
                }
            }
        }
        return resultado;
    }

    /** Comprueba si ya existe encabezado o detalle para la clave indicada. */
    private boolean existePlan(Connection con, ClavePlan clave) throws SQLException {
        String sql = "SELECT 1 FROM tesgpge WHERE CIA=? AND CC=? AND CESC=? AND TGPO=? AND CGPO=? "
                + "UNION ALL SELECT 1 FROM tesgpde WHERE CIA=? AND CC=? AND CESC=? AND TGPO=? AND CGPO=? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            asignarClave(ps, 1, clave);
            asignarClave(ps, 6, clave);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Carga una copia completa de las partidas para una posible restauración. */
    private List<DetallePlan> cargarDetalles(Connection con, ClavePlan clave) throws SQLException {
        List<DetallePlan> detalles = new ArrayList<>();
        String sql = "SELECT SEC,NCPTO,DCPTO,TCPTO,IMPTE,PDSC,FVINI,FVFIN,USER,FEAC,HOAC FROM tesgpde "
                + "WHERE CIA=? AND CC=? AND CESC=? AND TGPO=? AND CGPO=? ORDER BY SEC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            asignarClave(ps, 1, clave);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date fechaInicio = rs.getDate("FVINI");
                    Date fechaFin = rs.getDate("FVFIN");
                    Date fechaActualizacion = rs.getDate("FEAC");
                    detalles.add(new DetallePlan(rs.getInt("SEC"), rs.getString("NCPTO"), rs.getString("DCPTO"),
                            rs.getString("TCPTO"), rs.getBigDecimal("IMPTE"), rs.getBigDecimal("PDSC"),
                            fechaInicio != null ? fechaInicio.toLocalDate() : null,
                            fechaFin != null ? fechaFin.toLocalDate() : null,
                            rs.getString("USER"), fechaActualizacion != null ? fechaActualizacion.toLocalDate() : null,
                            rs.getString("HOAC")));
                }
            }
        }
        return detalles;
    }

    /** Elimina las partidas de una clave de plan. */
    private int eliminarDetalles(Connection con, ClavePlan clave) throws SQLException {
        String sql = "DELETE FROM tesgpde WHERE CIA=? AND CC=? AND CESC=? AND TGPO=? AND CGPO=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            asignarClave(ps, 1, clave);
            return ps.executeUpdate();
        }
    }

    /** Inserta una lista de partidas previamente validada. */
    private void insertarDetalles(Connection con, ClavePlan clave, List<DetallePlan> detalles) throws SQLException {
        String sql = "INSERT INTO tesgpde (CIA,CC,CESC,TGPO,CGPO,SEC,NCPTO,DCPTO,TCPTO,IMPTE,PDSC,FVINI,FVFIN,USER,FEAC,HOAC) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (DetallePlan detalle : detalles) {
                asignarClave(ps, 1, clave);
                ps.setInt(6, detalle.sec());
                ps.setString(7, detalle.concepto());
                ps.setString(8, detalle.descripcion());
                ps.setString(9, detalle.tipoConcepto());
                ps.setBigDecimal(10, detalle.importe());
                ps.setBigDecimal(11, detalle.descuento());
                ps.setDate(12, detalle.fechaInicio() != null ? Date.valueOf(detalle.fechaInicio()) : null);
                ps.setDate(13, detalle.fechaFin() != null ? Date.valueOf(detalle.fechaFin()) : null);
                ps.setString(14, detalle.usuario());
                ps.setDate(15, detalle.fechaActualizacion() != null ? Date.valueOf(detalle.fechaActualizacion()) : null);
                ps.setString(16, detalle.horaActualizacion());
                ps.addBatch();
            }
            int[] resultados = ps.executeBatch();
            if (resultados.length != detalles.size()) {
                throw new SQLException("No se guardaron todas las partidas del plan.");
            }
        }
    }

    /** Restaura las partidas anteriores después de un fallo de guardado. */
    private void restaurarDetalles(Connection con, ClavePlan clave, List<DetallePlan> originales) throws SQLException {
        eliminarDetalles(con, clave);
        insertarDetalles(con, clave, originales);
    }

    /** Asigna una clave de plan a cinco parámetros consecutivos. */
    private void asignarClave(PreparedStatement ps, int inicio, ClavePlan clave) throws SQLException {
        ps.setString(inicio, clave.cia());
        ps.setString(inicio + 1, clave.cc());
        ps.setString(inicio + 2, clave.ciclo());
        ps.setString(inicio + 3, clave.tipo());
        ps.setString(inicio + 4, clave.plan());
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
        tblPPagos = new javax.swing.JTable();
        btnAddPPagos = new javax.swing.JButton();
        btnEditPPagos = new javax.swing.JButton();
        btnDeletePPagos = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        tblPPagos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Compañia", "Centro Costos", "Ciclo Escolar", "Tipo Pago", "Cve Pan", "Descripcion", "Fec. Vig. Ini.", "Fec. Vig. Fin", "Usuario", "Fech. Ult. Act.", "Hora, Ult. Act."
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, true, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblPPagos);

        btnAddPPagos.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAddPPagos.setForeground(new java.awt.Color(26, 61, 99));
        btnAddPPagos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/add.png"))); // NOI18N
        btnAddPPagos.setText("Añadir");
        btnAddPPagos.addActionListener(this::btnAddPPagosActionPerformed);

        btnEditPPagos.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEditPPagos.setForeground(new java.awt.Color(26, 61, 99));
        btnEditPPagos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edit.png"))); // NOI18N
        btnEditPPagos.setText("Editar");
        btnEditPPagos.setMaximumSize(new java.awt.Dimension(93, 31));
        btnEditPPagos.setMinimumSize(new java.awt.Dimension(93, 31));
        btnEditPPagos.addActionListener(this::btnEditPPagosActionPerformed);

        btnDeletePPagos.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDeletePPagos.setForeground(new java.awt.Color(26, 61, 99));
        btnDeletePPagos.setIcon(new javax.swing.ImageIcon(getClass().getResource("/delete.png"))); // NOI18N
        btnDeletePPagos.setText("Eliminar");
        btnDeletePPagos.addActionListener(this::btnDeletePPagosActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAddPPagos)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditPPagos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeletePPagos)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddPPagos, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditPPagos, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeletePPagos, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void btnAddPPagosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddPPagosActionPerformed
        mostrarDialogoPlanPagos(false);
    }//GEN-LAST:event_btnAddPPagosActionPerformed

    private void btnEditPPagosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditPPagosActionPerformed
       if (tblPPagos.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un plan para editar.");
            return;
        }
        mostrarDialogoPlanPagos(true);
    }//GEN-LAST:event_btnEditPPagosActionPerformed

    private void btnDeletePPagosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeletePPagosActionPerformed
    int fila = tblPPagos.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un plan para eliminar.");
            return;
        }

        String cia = tblPPagos.getValueAt(fila, 0).toString();
        String cc = tblPPagos.getValueAt(fila, 1).toString();
        String ciclo = tblPPagos.getValueAt(fila, 2).toString();
        String tpoPlan = tblPPagos.getValueAt(fila, 3).toString();
        String plan = tblPPagos.getValueAt(fila, 4).toString();
        String desc = tblPPagos.getValueAt(fila, 5).toString();
        
        int resp = JOptionPane.showConfirmDialog(this, "¿Eliminar el plan " + desc + "?", "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);
        
        if (resp == JOptionPane.YES_OPTION) {
            ClavePlan clave = new ClavePlan(cia, cc, ciclo, tpoPlan, plan);
            ConDB db = new ConDB();
            Connection con = null;
            List<DetallePlan> respaldo = List.of();
            boolean detallesEliminados = false;
            try {
                con = db.Conectar();
                if (con == null) {
                    throw new SQLException("No fue posible conectar con la base de datos.");
                }
                respaldo = cargarDetalles(con, clave);
                con.setAutoCommit(false);
                detallesEliminados = eliminarDetalles(con, clave) > 0;

                String sql = "DELETE FROM tesgpge WHERE CIA = ? AND CC = ? AND CESC = ? AND TGPO = ? AND CGPO = ?";
                int encabezadosEliminados;
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    asignarClave(ps, 1, clave);
                    encabezadosEliminados = ps.executeUpdate();
                }

                if (!detallesEliminados && encabezadosEliminados == 0) {
                    throw new SQLException("No se encontró el plan para eliminar.");
                }

                con.commit();
                JOptionPane.showMessageDialog(this, "Plan y sus partidas eliminados correctamente.");
                cargarTablaPlanes();
            } catch (Exception e) {
                if (con != null) {
                    try {
                        con.rollback();
                        if (detallesEliminados) {
                            restaurarDetalles(con, clave, respaldo);
                        }
                    } catch (SQLException restauracion) {
                        e.addSuppressed(restauracion);
                    }
                }
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage());
            } finally {
                db.Cerrar();
            }
        }
    }//GEN-LAST:event_btnDeletePPagosActionPerformed

private void mostrarDialogoPlanPagos(boolean modoEdicion) {
        Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
        String tituloVentana = modoEdicion ? "Modificar Plan de Pagos" : "Agregar Plan de Pagos";

        JDialog dialogo = new JDialog((java.awt.Frame) ventanaPadre, tituloVentana, true);
        dialogo.setSize(850, 600);
        dialogo.setLayout(null);
        dialogo.setResizable(false);

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");

        // --- CLASE LOCAL PARA REUTILIZAR EL BUSCADOR FLOTANTE ---
        class BuscadorFlotante {
            void configurar(JTextField txtClave, JTextField txtDesc, JButton boton, Object[][] datos) {
                String[] columnas = {"Clave", "Descripción"};
                Runnable mostrarPopup = () -> {
                    javax.swing.JPopupMenu popup = new javax.swing.JPopupMenu();
                    popup.setFocusable(false);
                    DefaultTableModel mod = new DefaultTableModel(datos, columnas) {
                        @Override public boolean isCellEditable(int r, int c) { return false; }
                    };
                    JTable tabla = new JTable(mod);
                    tabla.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
                    tabla.getColumnModel().getColumn(0).setPreferredWidth(80);
                    tabla.getColumnModel().getColumn(1).setPreferredWidth(250);

                    javax.swing.table.TableRowSorter<DefaultTableModel> sorter = new javax.swing.table.TableRowSorter<>(mod);
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

        // --- CARGA DE DATOS PARA LOS BUSCADORES ---
        java.util.function.Function<String, Object[][]> cargarDatos = (query) -> {
            java.util.List<Object[]> lista = new java.util.ArrayList<>();
            try {
                ConDB db = new ConDB();
                Connection con = db.Conectar();
                if (con != null) {
                    PreparedStatement ps = con.prepareStatement(query);
                    ResultSet rs = ps.executeQuery();
                    while(rs.next()) lista.add(new Object[]{rs.getString(1), rs.getString(2)});
                    rs.close(); ps.close(); db.Cerrar();
                }
            } catch(Exception e) {}
            return lista.toArray(new Object[0][0]);
        };

        Object[][] dCC = cargarDatos.apply("SELECT CVE, DES1 FROM tgcc WHERE CVE IN ('12100', '12200', '12300', '12400') ORDER BY CVE");
        Object[][] dCiclo = cargarDatos.apply("SELECT DISTINCT CESC, CDSC FROM tescesc ORDER BY CESC DESC");
        Object[][] dTipoPlan = cargarDatos.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'TGPO' ORDER BY CVE");
        Object[][] dCptos = cargarDatos.apply("SELECT DISTINCT NCPTO, DCPTO FROM tescpto ORDER BY NCPTO");

        // --- 1. SECCIÓN SUPERIOR ---
        JLabel lblCia = new JLabel("Compañía");
        lblCia.setBounds(20, 15, 80, 25);
        JComboBox<String> cmbCia = new JComboBox<>();
        cmbCia.setBounds(110, 15, 60, 25);
        JLabel lblCiaDesc = new JLabel();
        lblCiaDesc.setBounds(180, 15, 250, 25);

        try {
            ConDB db = new ConDB();
            Connection con = db.Conectar();
            if (con != null) {
                ResultSet rs = con.prepareStatement("SELECT CIA, NCIA FROM tmcias").executeQuery();
                while(rs.next()){ 
                    cmbCia.addItem(rs.getString("CIA")); 
                    lblCiaDesc.setText(rs.getString("NCIA")); 
                }
                rs.close(); db.Cerrar();
            }
        } catch (Exception ex) {}

        // Buscador Centro de Costos
        JLabel lblCC = new JLabel("Centro Costos");
        lblCC.setBounds(20, 45, 90, 25);
        JTextField txtCC = new JTextField();
        txtCC.setBounds(110, 45, 60, 25);
        JButton btnCC = new JButton("▼");
        btnCC.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
        btnCC.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnCC.setBounds(170, 45, 20, 25);
        JTextField txtCCDesc = new JTextField();
        txtCCDesc.setBounds(195, 45, 235, 25);
        txtCCDesc.setEditable(false); txtCCDesc.setBackground(new java.awt.Color(240,240,240));
        buscador.configurar(txtCC, txtCCDesc, btnCC, dCC);

        // Buscador Ciclo Escolar
        JLabel lblCiclo = new JLabel("Ciclo Escolar");
        lblCiclo.setBounds(20, 75, 90, 25);
        JTextField txtCiclo = new JTextField();
        txtCiclo.setBounds(110, 75, 60, 25);
        JButton btnCiclo = new JButton("▼");
        btnCiclo.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
        btnCiclo.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnCiclo.setBounds(170, 75, 20, 25);
        JTextField txtCicloDesc = new JTextField();
        txtCicloDesc.setBounds(195, 75, 235, 25);
        txtCicloDesc.setEditable(false); txtCicloDesc.setBackground(new java.awt.Color(240,240,240));
        buscador.configurar(txtCiclo, txtCicloDesc, btnCiclo, dCiclo);

        // Plan de Pagos y Buscador Tipo de Plan
        JLabel lblPlan = new JLabel("Plan de Pagos");
        lblPlan.setBounds(20, 105, 90, 25);
        JTextField txtPlan = new JTextField();
        txtPlan.setBounds(110, 105, 80, 25);

        JLabel lblTipoPlan = new JLabel("Tpo Plan");
        lblTipoPlan.setBounds(250, 105, 60, 25);
        JTextField txtTipoPlan = new JTextField();
        txtTipoPlan.setBounds(315, 105, 50, 25);
        JButton btnTipoPlan = new JButton("▼");
        btnTipoPlan.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
        btnTipoPlan.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnTipoPlan.setBounds(365, 105, 20, 25);
        JTextField txtTipoPlanDesc = new JTextField();
        txtTipoPlanDesc.setBounds(390, 105, 150, 25);
        txtTipoPlanDesc.setEditable(false); txtTipoPlanDesc.setBackground(new java.awt.Color(240,240,240));
        buscador.configurar(txtTipoPlan, txtTipoPlanDesc, btnTipoPlan, dTipoPlan);

        JLabel lblDesc = new JLabel("Descripción");
        lblDesc.setBounds(20, 135, 90, 25);
        JTextField txtDesc = new JTextField();
        txtDesc.setBounds(110, 135, 430, 25);

        if (modoEdicion) {
            cmbCia.setEnabled(false);
            txtCC.setEditable(false); btnCC.setEnabled(false);
            txtCiclo.setEditable(false); btnCiclo.setEnabled(false);
            txtPlan.setEditable(false);
            txtTipoPlan.setEditable(false); btnTipoPlan.setEnabled(false);
        }

        dialogo.add(lblCia); dialogo.add(cmbCia); dialogo.add(lblCiaDesc);
        dialogo.add(lblCC); dialogo.add(txtCC); dialogo.add(btnCC); dialogo.add(txtCCDesc);
        dialogo.add(lblCiclo); dialogo.add(txtCiclo); dialogo.add(btnCiclo); dialogo.add(txtCicloDesc);
        dialogo.add(lblPlan); dialogo.add(txtPlan);
        dialogo.add(lblTipoPlan); dialogo.add(txtTipoPlan); dialogo.add(btnTipoPlan); dialogo.add(txtTipoPlanDesc);
        dialogo.add(lblDesc); dialogo.add(txtDesc);

        // --- 2. PESTAÑAS Y MARCOS ---
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setBounds(15, 175, 800, 320);

        JPanel pnlGenerales = new JPanel(null);

        // Marco: Vigencia del Plan
        JPanel pnlVigencia = new JPanel(null);
        pnlVigencia.setBorder(BorderFactory.createTitledBorder("Vigencia del Plan de Pagos"));
        pnlVigencia.setBounds(10, 10, 775, 60);

        JLabel lblFecIni = new JLabel("Fecha Inicial"); lblFecIni.setBounds(50, 20, 80, 25);
        com.toedter.calendar.JDateChooser fecIniVigencia = new com.toedter.calendar.JDateChooser();
        fecIniVigencia.setDateFormatString("yyyy-MM-dd"); fecIniVigencia.setBounds(140, 20, 110, 25);

        JLabel lblFecFin = new JLabel("Fecha Final"); lblFecFin.setBounds(500, 20, 80, 25);
        com.toedter.calendar.JDateChooser fecFinVigencia = new com.toedter.calendar.JDateChooser();
        fecFinVigencia.setDateFormatString("yyyy-MM-dd"); fecFinVigencia.setBounds(590, 20, 110, 25);

        pnlVigencia.add(lblFecIni); pnlVigencia.add(fecIniVigencia);
        pnlVigencia.add(lblFecFin); pnlVigencia.add(fecFinVigencia);
        pnlGenerales.add(pnlVigencia);

        // Marco: Partidas / Detalle
        JPanel pnlDetalle = new JPanel(null);
        pnlDetalle.setBorder(BorderFactory.createEtchedBorder());
        pnlDetalle.setBounds(10, 80, 775, 200);

        // Títulos de captura
        pnlDetalle.add(new JLabel("Sec")).setBounds(10, 10, 30, 20);
        pnlDetalle.add(new JLabel("Concepto")).setBounds(45, 10, 80, 20);
        pnlDetalle.add(new JLabel("Descripción")).setBounds(170, 10, 150, 20);
        pnlDetalle.add(new JLabel("Costo Unitario")).setBounds(365, 10, 90, 20);
        pnlDetalle.add(new JLabel("% Dscto")).setBounds(465, 10, 60, 20);
        pnlDetalle.add(new JLabel("Fecha Inicial")).setBounds(535, 10, 80, 20);
        pnlDetalle.add(new JLabel("Fecha Final")).setBounds(645, 10, 80, 20);

        // Campos de captura 
        JTextField txtSec = new JTextField("1"); txtSec.setBounds(10, 30, 30, 25);
        
        JTextField txtCptoCode = new JTextField(); txtCptoCode.setBounds(45, 30, 60, 25);
        JButton btnCptoCode = new JButton("▼"); btnCptoCode.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
        btnCptoCode.setMargin(new java.awt.Insets(0,0,0,0)); btnCptoCode.setBounds(105, 30, 20, 25);
        
        JTextField txtDescDet = new JTextField(); txtDescDet.setBounds(130, 30, 225, 25);
        buscador.configurar(txtCptoCode, txtDescDet, btnCptoCode, dCptos);

        JTextField txtCosto = new JTextField("0.00"); txtCosto.setBounds(365, 30, 90, 25); txtCosto.setHorizontalAlignment(JTextField.RIGHT);
        JTextField txtDscto = new JTextField("0.00"); txtDscto.setBounds(465, 30, 60, 25); txtDscto.setHorizontalAlignment(JTextField.RIGHT);
        
        com.toedter.calendar.JDateChooser txtFecIniDet = new com.toedter.calendar.JDateChooser();
        txtFecIniDet.setDateFormatString("yyyy-MM-dd"); txtFecIniDet.setBounds(535, 30, 100, 25);
        
        com.toedter.calendar.JDateChooser txtFecFinDet = new com.toedter.calendar.JDateChooser();
        txtFecFinDet.setDateFormatString("yyyy-MM-dd"); txtFecFinDet.setBounds(645, 30, 100, 25);
        
        JButton btnOk = new JButton("OK"); btnOk.setBounds(750, 30, 20, 25); btnOk.setMargin(new java.awt.Insets(0,0,0,0));
        
        pnlDetalle.add(txtSec); pnlDetalle.add(txtCptoCode); pnlDetalle.add(btnCptoCode); pnlDetalle.add(txtDescDet); pnlDetalle.add(txtCosto); 
        pnlDetalle.add(txtDscto); pnlDetalle.add(txtFecIniDet); pnlDetalle.add(txtFecFinDet); pnlDetalle.add(btnOk);

        // Tabla interna de Detalles
        DefaultTableModel modDetalle = new DefaultTableModel(
            new Object[][]{}, 
            new String[]{"Sec", "Concepto", "Descripción", "Costo Unitario", "Porc. Descuento", "Fec Vig Ini", "Fec Vig Fin"}
        ) { @Override public boolean isCellEditable(int r, int c) { return true; } };

        JTable tblDetalle = new JTable(modDetalle);
        tblDetalle.setToolTipText("Puedes editar las celdas y presionar Supr para quitar una partida.");
        tblDetalle.getInputMap().put(javax.swing.KeyStroke.getKeyStroke("DELETE"), "eliminarPartida");
        tblDetalle.getActionMap().put("eliminarPartida", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int filaSeleccionada = tblDetalle.getSelectedRow();
                if (filaSeleccionada >= 0) {
                    modDetalle.removeRow(tblDetalle.convertRowIndexToModel(filaSeleccionada));
                }
            }
        });
        JScrollPane scrollDetalle = new JScrollPane(tblDetalle);
        scrollDetalle.setBounds(10, 65, 755, 125);
        pnlDetalle.add(scrollDetalle);

        // Evento Agregar Partida al Grid
        btnOk.addActionListener(e -> {
            String cpto = txtCptoCode.getText().trim();
            String descrip = txtDescDet.getText().trim();
            String fi = txtFecIniDet.getDate() != null ? sdf.format(txtFecIniDet.getDate()) : "";
            String ff = txtFecFinDet.getDate() != null ? sdf.format(txtFecFinDet.getDate()) : "";

            if (cpto.isEmpty() || descrip.isEmpty()) {
                JOptionPane.showMessageDialog(dialogo, "Seleccione un concepto para agregar.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            modDetalle.addRow(new Object[]{
                txtSec.getText().trim(), cpto, descrip,
                txtCosto.getText().trim(), txtDscto.getText().trim(), fi, ff
            });

            txtSec.setText(String.valueOf(modDetalle.getRowCount() + 1)); 
            txtCptoCode.setText("");
            txtDescDet.setText("");
            txtCosto.setText("0.00");
            txtDscto.setText("0.00");
        });

        pnlGenerales.add(pnlDetalle);
        pestanas.addTab("Datos Generales", pnlGenerales);
        dialogo.add(pestanas);

        // --- 3. BOTONES INFERIORES ---
        JButton btnAceptar = new JButton("Aceptar");
        btnAceptar.setBounds(300, 510, 100, 40);
        JButton btnSalir = new JButton("Salir");
        btnSalir.setBounds(430, 510, 100, 40);

        dialogo.add(btnAceptar);
        dialogo.add(btnSalir);

        final ClavePlan[] claveOriginal = new ClavePlan[1];
        List<DetallePlan> detallesOriginales = new ArrayList<>();
        final boolean[] edicionCargada = {!modoEdicion};

        // --- 4. CARGAR DATOS SI ES MODO EDICIÓN ---
        if (modoEdicion) {
            int fila = tblPPagos.getSelectedRow();
            String ciaFila = tblPPagos.getValueAt(fila, 0).toString();
            String ccFila = tblPPagos.getValueAt(fila, 1).toString();
            String cicloFila = tblPPagos.getValueAt(fila, 2).toString();
            String tpoPlanFila = tblPPagos.getValueAt(fila, 3).toString();
            String planFila = tblPPagos.getValueAt(fila, 4).toString();
            claveOriginal[0] = new ClavePlan(ciaFila, ccFila, cicloFila, tpoPlanFila, planFila);

            cmbCia.setSelectedItem(ciaFila);
            txtCC.setText(ccFila);
            txtCiclo.setText(cicloFila);
            txtTipoPlan.setText(tpoPlanFila);
            txtPlan.setText(planFila);
            txtDesc.setText(tblPPagos.getValueAt(fila, 5).toString());

            try {
                String fechaInicioFila = tblPPagos.getValueAt(fila, 6).toString();
                String fechaFinFila = tblPPagos.getValueAt(fila, 7).toString();
                if (!fechaInicioFila.isEmpty()) fecIniVigencia.setDate(sdf.parse(fechaInicioFila));
                if (!fechaFinFila.isEmpty()) fecFinVigencia.setDate(sdf.parse(fechaFinFila));
            } catch (java.text.ParseException ex) {
                JOptionPane.showMessageDialog(dialogo, "Las fechas del plan no tienen un formato válido.");
            }

            try {
                ConDB db = new ConDB();
                Connection con = db.Conectar();
                if (con != null) {
                    // Cargar Encabezado (tesgpge)
                    String sqlGe = "SELECT DGPO, FVINI, FVFIN FROM tesgpge WHERE CIA=? AND CC=? AND CESC=? AND TGPO=? AND CGPO=?";
                    PreparedStatement psGe = con.prepareStatement(sqlGe);
                    psGe.setString(1, ciaFila);
                    psGe.setString(2, ccFila);
                    psGe.setString(3, cicloFila);
                    psGe.setString(4, tpoPlanFila);
                    psGe.setString(5, planFila);
                    ResultSet rsGe = psGe.executeQuery();

                    if (rsGe.next()) {
                        txtDesc.setText(rsGe.getString("DGPO") != null ? rsGe.getString("DGPO") : "");
                        
                        try {
                            if (rsGe.getString("FVINI") != null && !rsGe.getString("FVINI").isEmpty()) {
                                fecIniVigencia.setDate(sdf.parse(rsGe.getString("FVINI")));
                            }
                            if (rsGe.getString("FVFIN") != null && !rsGe.getString("FVFIN").isEmpty()) {
                                fecFinVigencia.setDate(sdf.parse(rsGe.getString("FVFIN")));
                            }
                        } catch(Exception ex) {}
                    }
                    rsGe.close(); psGe.close();

                    // Cargar Detalle (tesgpde)
                    modDetalle.setRowCount(0);
                    java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");
                    detallesOriginales.addAll(cargarDetalles(con, claveOriginal[0]));
                    for (DetallePlan detalle : detallesOriginales) {
                        modDetalle.addRow(new Object[]{
                            detalle.sec(), detalle.concepto(), detalle.descripcion(),
                            df.format(detalle.importe() != null ? detalle.importe() : BigDecimal.ZERO),
                            df.format(detalle.descuento() != null ? detalle.descuento() : BigDecimal.ZERO),
                            detalle.fechaInicio() != null ? detalle.fechaInicio().toString() : "",
                            detalle.fechaFin() != null ? detalle.fechaFin().toString() : ""
                        });
                    }
                    db.Cerrar();

                    txtSec.setText(String.valueOf(modDetalle.getRowCount() + 1));
                    edicionCargada[0] = true;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(dialogo, "Error al cargar datos del plan: " + e.getMessage());
            }
        }

        // --- 5. EVENTOS ---
        btnSalir.addActionListener(e -> dialogo.dispose());

        btnAceptar.addActionListener(e -> {
            String cia = cmbCia.getSelectedItem() != null ? cmbCia.getSelectedItem().toString() : "";
            String cc = txtCC.getText().trim();
            String ciclo = txtCiclo.getText().trim();
            String plan = txtPlan.getText().trim();
            String desc = txtDesc.getText().trim();
            String tipoPlan = txtTipoPlan.getText().trim();

            String fvini = fecIniVigencia.getDate() != null ? sdf.format(fecIniVigencia.getDate()) : "";
            String fvfin = fecFinVigencia.getDate() != null ? sdf.format(fecFinVigencia.getDate()) : "";

            if (cia.isEmpty() || plan.isEmpty() || desc.isEmpty() || cc.isEmpty() || ciclo.isEmpty()
                    || tipoPlan.isEmpty() || fvini.isEmpty() || fvfin.isEmpty()) {
                JOptionPane.showMessageDialog(dialogo,
                        "Compañía, Centro de Costos, Ciclo, Tipo, Plan, Descripción y vigencia son obligatorios.",
                        "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (modoEdicion && !edicionCargada[0]) {
                JOptionPane.showMessageDialog(dialogo, "No se cargaron los datos originales; cierra y vuelve a abrir el plan.",
                        "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LocalDate fechaInicioPlan;
            LocalDate fechaFinPlan;
            List<DetallePlan> detallesCapturados;
            try {
                fechaInicioPlan = LocalDate.parse(fvini);
                fechaFinPlan = LocalDate.parse(fvfin);
                if (fechaFinPlan.isBefore(fechaInicioPlan)) {
                    throw new IllegalArgumentException("La fecha final del plan no puede ser anterior a la inicial.");
                }
                detallesCapturados = leerDetalles(modDetalle);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialogo, ex.getMessage(), "Datos inválidos", JOptionPane.WARNING_MESSAGE);
                return;
            }

            ClavePlan clave = new ClavePlan(cia, cc, ciclo, tipoPlan, plan);
            ClavePlan clavePersistida = modoEdicion ? claveOriginal[0] : clave;
            ConDB db = new ConDB();
            Connection con = null;
            boolean detallesModificados = false;
            try {
                con = db.Conectar();
                if (con == null) {
                    throw new SQLException("No fue posible conectar con la base de datos.");
                }

                List<DetallePlan> detallesValidados = validarCatalogos(con, clave, detallesCapturados);
                if (!modoEdicion && existePlan(con, clave)) {
                    throw new SQLException("Ya existe el plan " + plan + " para ese centro, ciclo y tipo.");
                }

                con.setAutoCommit(false);
                int encabezadosActualizados = 0;
                if (modoEdicion) {
                    String sqlActualizar = "UPDATE tesgpge SET DGPO=?,FVINI=?,FVFIN=?,USER=?,FEAC=CURDATE(),HOAC=DATE_FORMAT(NOW(), '%r') "
                            + "WHERE CIA=? AND CC=? AND CESC=? AND TGPO=? AND CGPO=?";
                    try (PreparedStatement ps = con.prepareStatement(sqlActualizar)) {
                        ps.setString(1, desc);
                        ps.setDate(2, Date.valueOf(fechaInicioPlan));
                        ps.setDate(3, Date.valueOf(fechaFinPlan));
                        ps.setString(4, usuarioLogueado);
                        asignarClave(ps, 5, clavePersistida);
                        encabezadosActualizados = ps.executeUpdate();
                    }
                }

                if (!modoEdicion || encabezadosActualizados == 0) {
                    String sqlInsertar = "INSERT INTO tesgpge (CIA,CC,CESC,TGPO,CGPO,DGPO,FVINI,FVFIN,USER,FEAC,HOAC) "
                            + "VALUES (?,?,?,?,?,?,?,?,?,CURDATE(),DATE_FORMAT(NOW(), '%r'))";
                    try (PreparedStatement ps = con.prepareStatement(sqlInsertar)) {
                        asignarClave(ps, 1, clave);
                        ps.setString(6, desc);
                        ps.setDate(7, Date.valueOf(fechaInicioPlan));
                        ps.setDate(8, Date.valueOf(fechaFinPlan));
                        ps.setString(9, usuarioLogueado);
                        ps.executeUpdate();
                    }
                }

                detallesModificados = true;
                eliminarDetalles(con, clavePersistida);
                insertarDetalles(con, clave, detallesValidados);
                con.commit();

                JOptionPane.showMessageDialog(dialogo, "Plan de Pagos y sus partidas guardados con éxito.");
                dialogo.dispose();
                cargarTablaPlanes();
            } catch (Exception ex) {
                String mensaje = ex.getMessage();
                if (con != null) {
                    try {
                        con.rollback();
                        if (detallesModificados) {
                            restaurarDetalles(con, clavePersistida,
                                    modoEdicion ? detallesOriginales : List.of());
                        }
                    } catch (SQLException restauracion) {
                        mensaje += " No fue posible restaurar las partidas anteriores: " + restauracion.getMessage();
                    }
                }
                JOptionPane.showMessageDialog(dialogo, "Error al guardar: " + mensaje, "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                db.Cerrar();
            }
        });

        // --- 6. MOSTRAR ---
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddPPagos;
    private javax.swing.JButton btnDeletePPagos;
    private javax.swing.JButton btnEditPPagos;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblPPagos;
    // End of variables declaration//GEN-END:variables
}
