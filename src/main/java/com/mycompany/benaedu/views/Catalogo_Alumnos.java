/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.benaedu.views;

import com.mycompany.benaedu.Dashboard;
import com.mycompany.benaedu.db.ConDB;
import java.awt.Component;
import java.awt.Window;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author b17za
 */
public class Catalogo_Alumnos extends javax.swing.JPanel {

    private SwingWorker<Void, Void> workerCargaActual;
    private javax.swing.Timer debounceTimer;

    private javax.swing.table.TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtBuscarMatricula;

    private String usuarioLogueado = "Admin";

    // --- VARIABLES DE PAGINACIÓN ---
    private int paginaActual = 1;
    private int registrosPorPagina = 50;
    private int totalPaginas = 1;
    private int totalRegistros = 0;

    private JLabel lblPaginacionInfo;
    private JButton btnAnterior;
    private JButton btnSiguiente;

    public Catalogo_Alumnos(String usuarioLogueado) {
        if (usuarioLogueado != null && !usuarioLogueado.trim().isEmpty()) {
            this.usuarioLogueado = usuarioLogueado.trim();
        }
        initComponents();
        configurarBuscadorUI();
        cargarTablaAlumnos();
    }

    /**
     * Creates new form Catalago_Alumnos
     */
    public Catalogo_Alumnos() {
        initComponents();
        configurarBuscadorUI();
        cargarTablaAlumnos();
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

    private void adaptarTamañoColumnas() {
        jTable2.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < jTable2.getColumnCount(); i++) {
            javax.swing.table.TableColumn columna = jTable2.getColumnModel().getColumn(i);
            int anchoPreferido = 60;
            java.awt.Component compCabecera = jTable2.getTableHeader().getDefaultRenderer()
                    .getTableCellRendererComponent(jTable2, columna.getHeaderValue(), false, false, 0, i);
            anchoPreferido = Math.max(anchoPreferido, compCabecera.getPreferredSize().width + 10);

            for (int r = 0; r < jTable2.getRowCount(); r++) {
                javax.swing.table.TableCellRenderer renderizador = jTable2.getCellRenderer(r, i);
                java.awt.Component c = jTable2.prepareRenderer(renderizador, r, i);
                anchoPreferido = Math.max(anchoPreferido, c.getPreferredSize().width + 15);
            }
            columna.setPreferredWidth(anchoPreferido);
        }
    }

    private void calcularTotalPaginas(Connection con, String filtroBusqueda) throws Exception {
        StringBuilder sqlCount = new StringBuilder("SELECT COUNT(*) FROM tesalum a ");
        if (!filtroBusqueda.isEmpty()) {
            sqlCount.append(" WHERE a.MAT LIKE ? OR a.NOMA LIKE ? OR a.APATE LIKE ? ");
        }

        PreparedStatement psCount = con.prepareStatement(sqlCount.toString());
        if (!filtroBusqueda.isEmpty()) {
            String term = "%" + filtroBusqueda + "%";
            psCount.setString(1, term);
            psCount.setString(2, term);
            psCount.setString(3, term);
        }

        ResultSet rsCount = psCount.executeQuery();
        if (rsCount.next()) {
            totalRegistros = rsCount.getInt(1);
        }
        rsCount.close();
        psCount.close();

        totalPaginas = (int) Math.ceil((double) totalRegistros / registrosPorPagina);
        if (totalPaginas == 0) {
            totalPaginas = 1;
        }
    }

    private void cargarTablaAlumnos() {

        // Cancela cualquier carga anterior que siga en curso (evita resultados viejos pisando nuevos)
        if (workerCargaActual != null && !workerCargaActual.isDone()) {
            workerCargaActual.cancel(true);
        }

        String textoBusqueda = (txtBuscarMatricula != null) ? txtBuscarMatricula.getText().trim() : "";

        // Estructuras para pasar datos del hilo de fondo al hilo de UI
        final java.util.List<Object[]> filasCargadas = new java.util.ArrayList<>();
        final int[] paginaFinal = new int[1];
        final int[] totalPagFinal = new int[1];
        final int[] totalRegFinal = new int[1];
        final Exception[] errorCarga = new Exception[1];

        workerCargaActual = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                try {
                    ConDB db = new ConDB();
                    Connection con = db.Conectar();

                    if (con != null) {
                        calcularTotalPaginas(con, textoBusqueda);

                        if (paginaActual > totalPaginas) {
                            paginaActual = totalPaginas;
                        }
                        if (paginaActual < 1) {
                            paginaActual = 1;
                        }

                        int offset = (paginaActual - 1) * registrosPorPagina;

                        StringBuilder sql = new StringBuilder(
                                "SELECT "
                                + "COALESCE(x.CIA, '12') AS CIA, "
                                + "COALESCE(x.CC, '') AS CC, "
                                + "COALESCE(x.SECC, '') AS SECC, "
                                + "COALESCE(x.CESC, '') AS CESC, "
                                + "a.MAT, "
                                + "COALESCE(x.TALU, '') AS TALU, "
                                + "a.NOMA, a.APATE, a.AMATE, "
                                + "COALESCE(x.GRADO, '') AS GRADO, "
                                + "COALESCE(x.GRUPO, '') AS GRUPO, "
                                + "a.SEXO, a.TPOPER, a.REGFIS, a.USOCFDI, "
                                + "a.FALT, a.FBAJ, a.STSALU, a.USER, a.FEAC, a.HOAC "
                                + "FROM tesalum a "
                                + "LEFT JOIN tesaxce x ON a.MAT = x.MAT AND x.CESC = (SELECT MAX(CESC) FROM tesaxce WHERE MAT = a.MAT) "
                        );

                        if (!textoBusqueda.isEmpty()) {
                            sql.append(" WHERE a.MAT LIKE ? OR a.NOMA LIKE ? OR a.APATE LIKE ? ");
                        }

                        sql.append(" ORDER BY a.MAT ASC LIMIT ? OFFSET ?");

                        PreparedStatement ps = con.prepareStatement(sql.toString());
                        int paramIdx = 1;

                        if (!textoBusqueda.isEmpty()) {
                            String term = "%" + textoBusqueda + "%";
                            ps.setString(paramIdx++, term);
                            ps.setString(paramIdx++, term);
                            ps.setString(paramIdx++, term);
                        }

                        ps.setInt(paramIdx++, registrosPorPagina);
                        ps.setInt(paramIdx++, offset);

                        ResultSet rs = ps.executeQuery();

                        while (rs.next()) {
                            if (isCancelled()) {
                                rs.close();
                                ps.close();
                                db.Cerrar();
                                return null;
                            }
                            Object[] fila = new Object[21];
                            fila[0] = rs.getString("CIA");
                            fila[1] = rs.getString("CC");
                            fila[2] = rs.getString("SECC");
                            fila[3] = rs.getString("CESC");
                            fila[4] = rs.getString("MAT");
                            fila[5] = rs.getString("TALU");
                            fila[6] = rs.getString("NOMA");
                            fila[7] = rs.getString("APATE");
                            fila[8] = rs.getString("AMATE");
                            fila[9] = rs.getString("GRADO");
                            fila[10] = rs.getString("GRUPO");
                            fila[11] = rs.getString("SEXO");
                            fila[12] = rs.getString("TPOPER");
                            fila[13] = rs.getString("REGFIS");
                            fila[14] = rs.getString("USOCFDI");
                            fila[15] = rs.getString("FALT");
                            fila[16] = rs.getString("FBAJ");
                            fila[17] = rs.getString("STSALU");
                            fila[18] = rs.getString("USER");
                            fila[19] = rs.getString("FEAC");
                            fila[20] = rs.getString("HOAC");

                            filasCargadas.add(fila);
                        }
                        rs.close();
                        ps.close();
                        db.Cerrar();

                        paginaFinal[0] = paginaActual;
                        totalPagFinal[0] = totalPaginas;
                        totalRegFinal[0] = totalRegistros;
                    }
                } catch (Exception e) {
                    errorCarga[0] = e;
                }
                return null;
            }

            @Override
            protected void done() {
                // Si este worker fue cancelado (llegó una búsqueda más nueva), no toques la UI
                if (isCancelled()) {
                    return;
                }

                if (errorCarga[0] != null) {
                    JOptionPane.showMessageDialog(Catalogo_Alumnos.this,
                            "Error al cargar la tabla de alumnos: " + errorCarga[0].getMessage());
                    return;
                }

                // A partir de aquí ya estamos en el EDT: seguro tocar componentes Swing
                DefaultTableModel modelo = new DefaultTableModel(
                        new Object[][]{},
                        new String[]{
                            "Compañía", "Ctro. Costos", "Sección", "Ciclo Escolar", "Matrícula", "Tipo",
                            "Nombre", "Apellido Paterno", "Apellido Materno", "Grado", "Grupo", "Sexo",
                            "Tipo Per.", "Reg. Fiscal", "Uso CFDI", "Fecha Alta", "Fecha Baja", "Estatus",
                            "Usuario", "Fech. Ult. Act", "Hora. Ult. Act"
                        }
                ) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

                for (Object[] fila : filasCargadas) {
                    modelo.addRow(fila);
                }

                jTable2.setModel(modelo);

                sorter = new javax.swing.table.TableRowSorter<>(modelo);
                jTable2.setRowSorter(sorter);

                paginaActual = paginaFinal[0];
                totalPaginas = totalPagFinal[0];
                totalRegistros = totalRegFinal[0];

                adaptarTamañoColumnas();
                actualizarEstadoPaginacion();
            }
        };

        workerCargaActual.execute();
    }

    private void configurarBuscadorUI() {
        JPanel pnlBusqueda = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));
        pnlBusqueda.setBackground(new java.awt.Color(255, 255, 255));

        JLabel lblBuscar = new JLabel("Buscar por Matrícula / Nombre:");
        lblBuscar.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));

        txtBuscarMatricula = new JTextField(20);
        JButton btnBuscarBD = new JButton("Buscar en BD");

        txtBuscarMatricula.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                // Filtro local instantáneo (barato, sin BD)
                filtrarLocal();

                // Si el usuario presiona Enter, buscar ya en BD sin esperar
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    if (debounceTimer != null) {
                        debounceTimer.stop();
                    }
                    dispararBusquedaBD();
                    return;
                }

                // Reinicia el temporizador cada vez que se suelta una tecla
                if (debounceTimer != null && debounceTimer.isRunning()) {
                    debounceTimer.stop();
                }
                debounceTimer = new javax.swing.Timer(350, ev -> dispararBusquedaBD());
                debounceTimer.setRepeats(false);
                debounceTimer.start();
            }
        });

        btnBuscarBD.addActionListener(e -> {
            paginaActual = 1;
            cargarTablaAlumnos();
        });

        pnlBusqueda.add(lblBuscar);
        pnlBusqueda.add(txtBuscarMatricula);
        pnlBusqueda.add(btnBuscarBD);

        JPanel pnlPaginacion = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 5));
        pnlPaginacion.setBackground(new java.awt.Color(255, 255, 255));

        btnAnterior = new JButton("◀ Anterior");
        btnSiguiente = new JButton("Siguiente ▶");
        lblPaginacionInfo = new JLabel("Página 1 de 1 (0 registros)");
        lblPaginacionInfo.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));

        btnAnterior.addActionListener(e -> {
            if (paginaActual > 1) {
                paginaActual--;
                cargarTablaAlumnos();
            }
        });

        btnSiguiente.addActionListener(e -> {
            if (paginaActual < totalPaginas) {
                paginaActual++;
                cargarTablaAlumnos();
            }
        });

        pnlPaginacion.add(lblPaginacionInfo);
        pnlPaginacion.add(btnAnterior);
        pnlPaginacion.add(btnSiguiente);

        JPanel pnlSur = new JPanel(new java.awt.BorderLayout());
        pnlSur.setBackground(new java.awt.Color(255, 255, 255));

        JPanel pnlBotonesAccion = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        pnlBotonesAccion.setBackground(new java.awt.Color(255, 255, 255));
        pnlBotonesAccion.add(btnAddCTAlumno);
        pnlBotonesAccion.add(btnEditCTAlumno);
        pnlBotonesAccion.add(btnDeleteCTAlumno);

        pnlSur.add(pnlBotonesAccion, java.awt.BorderLayout.WEST);
        pnlSur.add(pnlPaginacion, java.awt.BorderLayout.EAST);

        jPanel1.setLayout(new java.awt.BorderLayout(5, 5));
        jPanel1.add(pnlBusqueda, java.awt.BorderLayout.NORTH);
        jPanel1.add(jScrollPane2, java.awt.BorderLayout.CENTER);
        jPanel1.add(pnlSur, java.awt.BorderLayout.SOUTH);
    }

    private void dispararBusquedaBD() {
        String texto = txtBuscarMatricula.getText().trim();

        // Evita golpear la BD con búsquedas demasiado cortas (opcional pero recomendable)
        if (!texto.isEmpty() && texto.length() < 2) {
            return;
        }

        paginaActual = 1;
        cargarTablaAlumnos(); // ya es thread-safe internamente, se llama directo desde el EDT
    }

    private void filtrarLocal() {
        if (sorter == null) {
            return;
        }
        String texto = txtBuscarMatricula.getText().trim();
        if (texto.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + texto));
        }
    }

    private void actualizarEstadoPaginacion() {
        if (lblPaginacionInfo != null) {
            lblPaginacionInfo.setText("Página " + paginaActual + " de " + totalPaginas + " (" + totalRegistros + " alumnos)");
        }
        if (btnAnterior != null) {
            btnAnterior.setEnabled(paginaActual > 1);
        }
        if (btnSiguiente != null) {
            btnSiguiente.setEnabled(paginaActual < totalPaginas);
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

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        btnAddCTAlumno = new javax.swing.JButton();
        btnEditCTAlumno = new javax.swing.JButton();
        btnDeleteCTAlumno = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(jTable1);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        btnAddCTAlumno.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAddCTAlumno.setForeground(new java.awt.Color(26, 61, 99));
        btnAddCTAlumno.setIcon(new javax.swing.ImageIcon(getClass().getResource("/add.png"))); // NOI18N
        btnAddCTAlumno.setText("Añadir");
        btnAddCTAlumno.addActionListener(this::btnAddCTAlumnoActionPerformed);

        btnEditCTAlumno.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEditCTAlumno.setForeground(new java.awt.Color(26, 61, 99));
        btnEditCTAlumno.setIcon(new javax.swing.ImageIcon(getClass().getResource("/edit.png"))); // NOI18N
        btnEditCTAlumno.setText("Editar");
        btnEditCTAlumno.setMaximumSize(new java.awt.Dimension(93, 31));
        btnEditCTAlumno.setMinimumSize(new java.awt.Dimension(93, 31));
        btnEditCTAlumno.addActionListener(this::btnEditCTAlumnoActionPerformed);

        btnDeleteCTAlumno.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDeleteCTAlumno.setForeground(new java.awt.Color(26, 61, 99));
        btnDeleteCTAlumno.setIcon(new javax.swing.ImageIcon(getClass().getResource("/delete.png"))); // NOI18N
        btnDeleteCTAlumno.setText("Eliminar");
        btnDeleteCTAlumno.addActionListener(this::btnDeleteCTAlumnoActionPerformed);

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Compañia", "Ctro. Costos", "Seccion", "Ciclo Escolar", "Matricula", "Tipo ", "Nombre", "Apellido Paterno", "Apellido Materno", "Grado", "Grupo", "Sexo", "Tipo Per.", "Reg. Fiscal", "Uso CDFI", "Fecha Alta", "Fecha Baja", "Estatus", "Usuario", "Fech. Ult. Act", "Hora. Ult. Act"
            }
        ));
        jScrollPane2.setViewportView(jTable2);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnAddCTAlumno)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnEditCTAlumno, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDeleteCTAlumno)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 350, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddCTAlumno, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEditCTAlumno, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnDeleteCTAlumno, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    private void btnAddCTAlumnoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddCTAlumnoActionPerformed
        mostrarDialogoAlumno(false);
    }//GEN-LAST:event_btnAddCTAlumnoActionPerformed

    private void btnEditCTAlumnoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditCTAlumnoActionPerformed
        int viewRow = jTable2.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un alumno para editar.");
            return;
        }
        mostrarDialogoAlumno(true);
    }//GEN-LAST:event_btnEditCTAlumnoActionPerformed

    private void btnDeleteCTAlumnoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteCTAlumnoActionPerformed
        int viewRow = jTable2.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un alumno para eliminar.");
            return;
        }

        int modelRow = jTable2.convertRowIndexToModel(viewRow);
        String matricula = jTable2.getValueAt(modelRow, 4).toString();
        String nombre = jTable2.getValueAt(modelRow, 6).toString() + " " + jTable2.getValueAt(modelRow, 7).toString();

        int resp = JOptionPane.showConfirmDialog(this, "¿Eliminar al alumno: " + nombre + "?\nEsto podría borrar todo su historial.", "Confirmar", JOptionPane.YES_NO_OPTION);

        if (resp == JOptionPane.YES_OPTION) {
            try {
                ConDB db = new ConDB();
                Connection con = db.Conectar();
                if (con != null) {
                    String sql = "DELETE FROM tesalum WHERE MAT = ?";
                    PreparedStatement ps = con.prepareStatement(sql);
                    ps.setString(1, matricula);

                    if (ps.executeUpdate() > 0) {
                        JOptionPane.showMessageDialog(this, "Alumno eliminado.");
                        cargarTablaAlumnos();
                    }
                    ps.close();
                    db.Cerrar();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage());
            }
        }
    }//GEN-LAST:event_btnDeleteCTAlumnoActionPerformed
  private void mostrarDialogoAlumno(boolean modoEdicion) {
        Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
        String tituloVentana = modoEdicion ? "Modificar Alumno" : "Agregar Alumnos";

        JDialog dialogo = new JDialog((java.awt.Frame) ventanaPadre, tituloVentana, true);
        dialogo.setSize(800, 680);
        dialogo.setLayout(null);
        dialogo.setResizable(false);

        class BuscadorFlotante {

            void configurar(JTextField txtClave, JTextField txtDesc, JButton boton, Object[][] datos, String[] columnas, int[] anchos, java.util.function.Consumer<Object[]> onSelect) {
                Runnable mostrarPopup = () -> {
                    javax.swing.JPopupMenu popup = new javax.swing.JPopupMenu();
                    popup.setFocusable(false);
                    DefaultTableModel mod = new DefaultTableModel(datos, columnas) {
                        @Override
                        public boolean isCellEditable(int r, int c) {
                            return false;
                        }
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
                                if (onSelect != null) {
                                    Object[] rowData = new Object[mod.getColumnCount()];
                                    for (int i = 0; i < mod.getColumnCount(); i++) {
                                        rowData[i] = mod.getValueAt(modelRow, i);
                                    }
                                    onSelect.accept(rowData);
                                }
                                popup.setVisible(false);
                            }
                        }
                    });

                    int widthTotal = 0;
                    for (int w : anchos) {
                        widthTotal += w;
                    }
                    JScrollPane scroll = new JScrollPane(tabla);
                    scroll.setPreferredSize(new java.awt.Dimension(widthTotal + 20, 150));
                    popup.add(scroll);

                    String texto = txtClave.getText().trim();
                    if (!texto.isEmpty()) {
                        sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + texto));
                    }
                    popup.show(txtClave, 0, txtClave.getHeight());
                    txtClave.requestFocus();
                };

                boton.addActionListener(e -> {
                    txtClave.setText("");
                    mostrarPopup.run();
                });
                txtClave.addKeyListener(new java.awt.event.KeyAdapter() {
                    @Override
                    public void keyReleased(java.awt.event.KeyEvent e) {
                        int c = e.getKeyCode();
                        if (c == 27 || c == 10 || c == 38 || c == 40 || c == 37 || c == 39 || c == 9) {
                            return;
                        }
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
                    while (rs.next()) {
                        Object[] row = new Object[numCols];
                        for (int i = 0; i < numCols; i++) {
                            row[i] = rs.getString(i + 1);
                        }
                        lista.add(row);
                    }
                    rs.close();
                    ps.close();
                    db.Cerrar();
                }
            } catch (Exception e) {
            }
            return lista.toArray(new Object[0][0]);
        };

        // --- CARGA DE CATÁLOGOS ---
        Object[][] dCia = cargarDatosMultiple.apply("SELECT CIA, NCIA FROM tmcias ORDER BY CIA", 2);
        Object[][] dCC = cargarDatosMultiple.apply("SELECT CVE, DES1 FROM tgcc WHERE CVE IN ('12100', '12200', '12300', '12400') ORDER BY CVE", 2);
        Object[][] dTipoAl = cargarDatosMultiple.apply("SELECT TALU, DES FROM tesalutp WHERE CIA LIKE '12%' ORDER BY TALU", 2);
        Object[][] dTurno = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'TRN' ORDER BY CVE", 2);
        Object[][] dBeca = cargarDatosMultiple.apply("SELECT CBECA, DBECA, TBECA FROM tesbege ORDER BY CBECA", 3);
        Object[][] dEstatus = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'EALM' ORDER BY CVE", 2);
        Object[][] dPob = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'CD' ORDER BY CVE", 2);
        Object[][] dEdo = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'EDO' ORDER BY CVE", 2);
        Object[][] dPais = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'PAIS' ORDER BY CVE", 2);
        Object[][] dTTel = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'TTEL' ORDER BY CVE", 2);
        Object[][] dParent = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'PRT' ORDER BY CVE", 2);
        Object[][] dRegFisc = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'CSRF' ORDER BY CVE", 2);
        Object[][] dUsoCFDI = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'CSUC' ORDER BY CVE", 2);
        Object[][] dMensaje = cargarDatosMultiple.apply("SELECT CVE, DES FROM tmclas WHERE TBL = 'MSG' ORDER BY CVE", 2);

        // --- 1. CABECERA ---
        JPanel pnlTop = new JPanel(null);
        pnlTop.setBorder(BorderFactory.createEtchedBorder());
        pnlTop.setBounds(15, 10, 755, 110);

        pnlTop.add(new JLabel("Compañía")).setBounds(15, 15, 80, 25);
        JComboBox<String> cmbCia = new JComboBox<>();
        for (Object[] r : dCia) {
            cmbCia.addItem(r[0].toString());
        }
        cmbCia.setBounds(95, 15, 60, 25);
        JLabel lblCiaDesc = new JLabel("UNIDAD ESCOLAR BENAVENTE, A.C.");
        lblCiaDesc.setBounds(165, 15, 250, 25);

        pnlTop.add(new JLabel("Centro Costos")).setBounds(15, 45, 90, 25);
        JTextField txtCC = new JTextField("12100");
        txtCC.setBounds(95, 45, 60, 25);
        JButton btnCC = new JButton("▼");
        btnCC.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
        btnCC.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnCC.setBounds(155, 45, 20, 25);
        JTextField txtCCDesc = new JTextField();
        txtCCDesc.setBounds(180, 45, 250, 25);
        txtCCDesc.setEditable(false);
        txtCCDesc.setBackground(new java.awt.Color(240, 240, 240));
        buscador.configurar(txtCC, txtCCDesc, btnCC, dCC, new String[]{"Clave", "Descripción"}, new int[]{80, 250}, null);

        pnlTop.add(new JLabel("Matrícula")).setBounds(15, 75, 80, 25);
        JTextField txtMat = new JTextField();
        txtMat.setBounds(95, 75, 100, 25);

        pnlTop.add(new JLabel("Matrícula Oficial")).setBounds(210, 75, 100, 25);
        JTextField txtMatOf = new JTextField();
        txtMatOf.setBounds(315, 75, 120, 25);

        JComboBox<String> cmbGrado = new JComboBox<>();

        Runnable cargarGradosPorSeccion = () -> {
            cmbGrado.removeAllItems();
            try {
                ConDB db = new ConDB();
                Connection con = db.Conectar();
                if (con != null) {
                    PreparedStatement psG = con.prepareStatement("SELECT CGRAD, DGRAD FROM tesgrad WHERE CIA LIKE '12%' ORDER BY CGRAD");
                    ResultSet rsG = psG.executeQuery();
                    while (rsG.next()) {
                        cmbGrado.addItem(rsG.getString("CGRAD"));
                    }
                    rsG.close();
                    psG.close();
                    db.Cerrar();
                }
            } catch (Exception ex) {
            }
        };

        pnlTop.add(new JLabel("Tipo Alum")).setBounds(450, 75, 70, 25);
        JTextField txtTipoAl = new JTextField();
        txtTipoAl.setBounds(520, 75, 50, 25);
        JButton btnTipoAl = new JButton("▼");
        btnTipoAl.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
        btnTipoAl.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnTipoAl.setBounds(570, 75, 20, 25);
        buscador.configurar(txtTipoAl, null, btnTipoAl, dTipoAl, new String[]{"Clave", "Descripción"}, new int[]{60, 200}, (row) -> cargarGradosPorSeccion.run());

        JPanel pnlFoto = new JPanel();
        pnlFoto.setBorder(BorderFactory.createLineBorder(java.awt.Color.GRAY));
        pnlFoto.setBounds(630, 10, 100, 90);
        pnlFoto.add(new JLabel("Foto"));

        if (modoEdicion) {
            txtMat.setEditable(false);
            cmbCia.setEnabled(false);
        }

        pnlTop.add(cmbCia);
        pnlTop.add(lblCiaDesc);
        pnlTop.add(txtCC);
        pnlTop.add(btnCC);
        pnlTop.add(txtCCDesc);
        pnlTop.add(txtMat);
        pnlTop.add(txtMatOf);
        pnlTop.add(txtTipoAl);
        pnlTop.add(btnTipoAl);
        pnlTop.add(pnlFoto);
        dialogo.add(pnlTop);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setBounds(15, 130, 755, 430);

        // ==========================================
        // TAB 1: ESCOLAR
        // ==========================================
        JPanel pnlEscolar = new JPanel(null);

        JPanel pnlDatosAl = new JPanel(null);
        pnlDatosAl.setBorder(BorderFactory.createTitledBorder("Datos del Alumno"));
        pnlDatosAl.setBounds(10, 10, 560, 110);
        pnlDatosAl.add(new JLabel("Nombre")).setBounds(20, 20, 100, 20);
        JTextField txtNombre = new JTextField();
        txtNombre.setBounds(130, 20, 400, 20);
        pnlDatosAl.add(txtNombre);
        pnlDatosAl.add(new JLabel("Apellido Paterno")).setBounds(20, 50, 100, 20);
        JTextField txtApate = new JTextField();
        txtApate.setBounds(130, 50, 400, 20);
        pnlDatosAl.add(txtApate);
        pnlDatosAl.add(new JLabel("Apellido Materno")).setBounds(20, 80, 100, 20);
        JTextField txtAmate = new JTextField();
        txtAmate.setBounds(130, 80, 400, 20);
        pnlDatosAl.add(txtAmate);

        JPanel pnlSexo = new JPanel(null);
        pnlSexo.setBorder(BorderFactory.createTitledBorder("Sexo"));
        pnlSexo.setBounds(580, 10, 150, 75);
        JRadioButton rbM = new JRadioButton("Masculino", true);
        rbM.setBounds(10, 20, 100, 20);
        JRadioButton rbF = new JRadioButton("Femenino");
        rbF.setBounds(10, 45, 100, 20);
        ButtonGroup bgSexo = new ButtonGroup();
        bgSexo.add(rbM);
        bgSexo.add(rbF);
        pnlSexo.add(rbM);
        pnlSexo.add(rbF);

        JPanel pnlBeca = new JPanel(null);
        pnlBeca.setBorder(BorderFactory.createTitledBorder("Información de Beca"));
        pnlBeca.setBounds(10, 130, 560, 60);
        pnlBeca.add(new JLabel("Beca/Convenio")).setBounds(20, 20, 100, 25);
        JTextField txtBeca = new JTextField();
        txtBeca.setBounds(115, 20, 60, 25);
        JButton btnBeca = new JButton("▼");
        btnBeca.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
        btnBeca.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnBeca.setBounds(175, 20, 20, 25);
        JTextField txtBecaDesc = new JTextField();
        txtBecaDesc.setBounds(200, 20, 200, 25);
        txtBecaDesc.setEditable(false);
        txtBecaDesc.setBackground(new java.awt.Color(240, 240, 240));

        pnlBeca.add(new JLabel("Tipo")).setBounds(420, 10, 40, 20);
        JRadioButton rbBeca = new JRadioButton("Beca", true);
        rbBeca.setBounds(460, 10, 70, 20);
        JRadioButton rbConvenio = new JRadioButton("Convenio");
        rbConvenio.setBounds(460, 30, 80, 20);
        ButtonGroup bgBecaConv = new ButtonGroup();
        bgBecaConv.add(rbBeca);
        bgBecaConv.add(rbConvenio);

        java.util.function.Consumer<Object[]> onBecaSelect = (rowData) -> {
            String tipo = rowData[2] != null ? rowData[2].toString() : "";
            if ("C".equalsIgnoreCase(tipo)) {
                rbConvenio.setSelected(true);
            } else {
                rbBeca.setSelected(true);
            }
        };
        buscador.configurar(txtBeca, txtBecaDesc, btnBeca, dBeca, new String[]{"Clave", "Descripción", "Tipo"}, new int[]{80, 200, 50}, onBecaSelect);
        pnlBeca.add(txtBeca);
        pnlBeca.add(btnBeca);
        pnlBeca.add(txtBecaDesc);
        pnlBeca.add(rbBeca);
        pnlBeca.add(rbConvenio);

        JPanel pnlEscolaridad = new JPanel(null);
        pnlEscolaridad.setBorder(BorderFactory.createTitledBorder("Escolaridad"));
        pnlEscolaridad.setBounds(580, 95, 150, 155);

        pnlEscolaridad.add(new JLabel("Turno")).setBounds(10, 20, 40, 20);
        JTextField txtTurno = new JTextField();
        txtTurno.setBounds(60, 20, 50, 20);
        JButton btnTurno = new JButton("▼");
        btnTurno.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
        btnTurno.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnTurno.setBounds(110, 20, 20, 20);
        buscador.configurar(txtTurno, null, btnTurno, dTurno, new String[]{"Clave", "Descripción"}, new int[]{60, 150}, null);
        pnlEscolaridad.add(txtTurno);
        pnlEscolaridad.add(btnTurno);

        cargarGradosPorSeccion.run();
        pnlEscolaridad.add(new JLabel("Grado")).setBounds(10, 55, 40, 20);
        cmbGrado.setBounds(60, 55, 70, 20);
        pnlEscolaridad.add(cmbGrado);

        pnlEscolaridad.add(new JLabel("Grupo")).setBounds(10, 90, 40, 20);
        JTextField txtGrupo = new JTextField();
        txtGrupo.setBounds(60, 90, 70, 20);
        pnlEscolaridad.add(txtGrupo);
        pnlEscolaridad.add(new JLabel("# Lista")).setBounds(10, 125, 40, 20);
        JTextField txtNLista = new JTextField();
        txtNLista.setBounds(60, 125, 70, 20);
        pnlEscolaridad.add(txtNLista);

        JPanel pnlEstatus = new JPanel(null);
        pnlEstatus.setBorder(BorderFactory.createTitledBorder("Estatus de Alumno"));
        pnlEstatus.setBounds(10, 200, 560, 60);

        pnlEstatus.add(new JLabel("Estatus Alumno")).setBounds(20, 20, 100, 25);
        JTextField txtEstatus = new JTextField("A");
        txtEstatus.setBounds(120, 20, 40, 25);
        JButton btnEstatus = new JButton("▼");
        btnEstatus.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
        btnEstatus.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnEstatus.setBounds(160, 20, 20, 25);
        JTextField txtEstatusDesc = new JTextField("ACTIVO");
        txtEstatusDesc.setBounds(185, 20, 150, 25);
        txtEstatusDesc.setEditable(false);
        txtEstatusDesc.setBackground(new java.awt.Color(240, 240, 240));
        buscador.configurar(txtEstatus, txtEstatusDesc, btnEstatus, dEstatus, new String[]{"Clave", "Descripción"}, new int[]{60, 150}, null);
        pnlEstatus.add(txtEstatus);
        pnlEstatus.add(btnEstatus);
        pnlEstatus.add(txtEstatusDesc);

        pnlEstatus.add(new JLabel("Fecha Alta")).setBounds(360, 20, 80, 25);
        com.toedter.calendar.JDateChooser txtFalt = new com.toedter.calendar.JDateChooser();
        txtFalt.setDateFormatString("dd/MM/yyyy");
        txtFalt.setDate(new java.util.Date());
        txtFalt.setBounds(440, 20, 105, 25);
        pnlEstatus.add(txtFalt);

        JPanel pnlBaja = new JPanel(null);
        pnlBaja.setBorder(BorderFactory.createTitledBorder("Información de Baja"));
        pnlBaja.setBounds(10, 270, 720, 90);
        pnlBaja.add(new JLabel("Registró Baja")).setBounds(20, 20, 100, 25);
        JTextField txtUsrbaj = new JTextField();
        txtUsrbaj.setBounds(130, 20, 100, 25);
        pnlBaja.add(txtUsrbaj);
        pnlBaja.add(new JLabel("Fecha Baja")).setBounds(450, 20, 80, 25);
        com.toedter.calendar.JDateChooser txtFbaj = new com.toedter.calendar.JDateChooser();
        txtFbaj.setDateFormatString("dd/MM/yyyy");
        txtFbaj.setBounds(530, 20, 100, 25);
        pnlBaja.add(txtFbaj);
        pnlBaja.add(new JLabel("Motivo de Baja")).setBounds(20, 55, 100, 25);
        JTextField txtMotbaj = new JTextField();
        txtMotbaj.setBounds(130, 55, 500, 25);
        pnlBaja.add(txtMotbaj);

        pnlEscolar.add(pnlDatosAl);
        pnlEscolar.add(pnlSexo);
        pnlEscolar.add(pnlBeca);
        pnlEscolar.add(pnlEscolaridad);
        pnlEscolar.add(pnlEstatus);
        pnlEscolar.add(pnlBaja);

        // ==========================================
        // TAB 2: INFORMACIÓN PERSONAL
        // ==========================================
        JPanel pnlInfoPers = new JPanel(null);

        JPanel pnlLugarNac = new JPanel(null);
        pnlLugarNac.setBorder(BorderFactory.createTitledBorder("Lugar de Nacimiento"));
        pnlLugarNac.setBounds(10, 10, 730, 110);

        pnlLugarNac.add(new JLabel("Población")).setBounds(20, 20, 80, 20);
        JTextField txtPobNac = new JTextField();
        txtPobNac.setBounds(100, 20, 50, 20);
        JButton btnPobNac = new JButton("▼");
        btnPobNac.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
        btnPobNac.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnPobNac.setBounds(150, 20, 20, 20);
        buscador.configurar(txtPobNac, null, btnPobNac, dPob, new String[]{"Clave", "Descripción"}, new int[]{60, 200}, null);
        pnlLugarNac.add(txtPobNac);
        pnlLugarNac.add(btnPobNac);

        pnlLugarNac.add(new JLabel("Estado")).setBounds(20, 50, 80, 20);
        JTextField txtEdoNac = new JTextField();
        txtEdoNac.setBounds(100, 50, 50, 20);
        JButton btnEdoNac = new JButton("▼");
        btnEdoNac.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
        btnEdoNac.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnEdoNac.setBounds(150, 50, 20, 20);
        buscador.configurar(txtEdoNac, null, btnEdoNac, dEdo, new String[]{"Clave", "Descripción"}, new int[]{60, 200}, null);
        pnlLugarNac.add(txtEdoNac);
        pnlLugarNac.add(btnEdoNac);

        pnlLugarNac.add(new JLabel("País")).setBounds(20, 80, 80, 20);
        JTextField txtPaisNac = new JTextField("MEX");
        txtPaisNac.setBounds(100, 80, 50, 20);
        JButton btnPaisNac = new JButton("▼");
        btnPaisNac.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
        btnPaisNac.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnPaisNac.setBounds(150, 80, 20, 20);
        buscador.configurar(txtPaisNac, null, btnPaisNac, dPais, new String[]{"Clave", "Descripción"}, new int[]{60, 200}, null);
        pnlLugarNac.add(txtPaisNac);
        pnlLugarNac.add(btnPaisNac);

        pnlLugarNac.add(new JLabel("Fecha Nacimiento")).setBounds(470, 50, 120, 20);
        com.toedter.calendar.JDateChooser txtFnac = new com.toedter.calendar.JDateChooser();
        txtFnac.setDateFormatString("dd/MM/yyyy");
        txtFnac.setBounds(590, 50, 110, 20);
        pnlLugarNac.add(txtFnac);
        pnlInfoPers.add(pnlLugarNac);

        JPanel pnlDomicilio = new JPanel(null);
        pnlDomicilio.setBorder(BorderFactory.createTitledBorder("Domicilio Particular"));
        pnlDomicilio.setBounds(10, 130, 730, 250);

        pnlDomicilio.add(new JLabel("Calle")).setBounds(20, 30, 80, 20);
        JTextField txtCalleDom = new JTextField();
        txtCalleDom.setBounds(100, 30, 480, 20);
        pnlDomicilio.add(txtCalleDom);
        pnlDomicilio.add(new JLabel("Colonia")).setBounds(20, 60, 80, 20);
        JTextField txtColDom = new JTextField();
        txtColDom.setBounds(100, 60, 480, 20);
        pnlDomicilio.add(txtColDom);

        pnlDomicilio.add(new JLabel("Población")).setBounds(20, 90, 80, 20);
        JTextField txtPobDom = new JTextField();
        txtPobDom.setBounds(100, 90, 50, 20);
        JButton btnPobDom = new JButton("▼");
        btnPobDom.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
        btnPobDom.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnPobDom.setBounds(150, 90, 20, 20);
        buscador.configurar(txtPobDom, null, btnPobDom, dPob, new String[]{"Clave", "Descripción"}, new int[]{60, 200}, null);
        pnlDomicilio.add(txtPobDom);
        pnlDomicilio.add(btnPobDom);

        pnlDomicilio.add(new JLabel("Estado")).setBounds(20, 120, 80, 20);
        JTextField txtEdoDom = new JTextField();
        txtEdoDom.setBounds(100, 120, 50, 20);
        JButton btnEdoDom = new JButton("▼");
        btnEdoDom.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
        btnEdoDom.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnEdoDom.setBounds(150, 120, 20, 20);
        buscador.configurar(txtEdoDom, null, btnEdoDom, dEdo, new String[]{"Clave", "Descripción"}, new int[]{60, 200}, null);
        pnlDomicilio.add(txtEdoDom);
        pnlDomicilio.add(btnEdoDom);

        pnlDomicilio.add(new JLabel("C. Postal")).setBounds(20, 150, 80, 20);
        JTextField txtCpDom = new JTextField();
        txtCpDom.setBounds(100, 150, 80, 20);
        pnlDomicilio.add(txtCpDom);
        pnlDomicilio.add(new JLabel("C.U.R.P.")).setBounds(280, 150, 60, 20);
        JTextField txtCurp = new JTextField();
        txtCurp.setBounds(340, 150, 240, 20);
        pnlDomicilio.add(txtCurp);

        pnlDomicilio.add(new JLabel("Teléfono")).setBounds(20, 180, 80, 20);
        JTextField txtTipoTel = new JTextField();
        txtTipoTel.setBounds(100, 180, 50, 20);
        JButton btnTipoTel = new JButton("▼");
        btnTipoTel.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
        btnTipoTel.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnTipoTel.setBounds(150, 180, 20, 20);
        buscador.configurar(txtTipoTel, null, btnTipoTel, dTTel, new String[]{"Clave", "Descripción"}, new int[]{60, 150}, null);
        pnlDomicilio.add(txtTipoTel);
        pnlDomicilio.add(btnTipoTel);
        JTextField txtTel = new JTextField();
        txtTel.setBounds(180, 180, 400, 20);
        pnlDomicilio.add(txtTel);

        pnlInfoPers.add(pnlDomicilio);

        // ==========================================
        // TAB 3: FAMILIARES
        // ==========================================
        JPanel pnlFamiliares = new JPanel(null);
        JPanel pnlFamOuter = new JPanel(null);
        pnlFamOuter.setBorder(BorderFactory.createTitledBorder("Familia"));
        pnlFamOuter.setBounds(10, 10, 700, 180);

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
        pnlFamOuter.add(new JLabel("No Familia")).setBounds(20, 110, 80, 20);
        pnlFamOuter.add(new JComboBox<>(new String[]{""})).setBounds(100, 110, 80, 20);
        pnlFamOuter.add(new JLabel("Familia")).setBounds(20, 135, 80, 20);
        pnlFamOuter.add(new JTextField()).setBounds(100, 135, 300, 20);
        pnlFamOuter.add(new JLabel("Nombre Tutor")).setBounds(20, 160, 80, 20);
        JTextField txtNomTutor = new JTextField();
        txtNomTutor.setBounds(100, 160, 300, 20);
        pnlFamOuter.add(txtNomTutor);
        pnlFamiliares.add(pnlFamOuter);

        JPanel pnlFamInner = new JPanel(null);
        pnlFamInner.setBorder(BorderFactory.createTitledBorder("Familiares"));
        pnlFamInner.setBounds(10, 200, 700, 190);
        pnlFamInner.add(new JLabel("Sec")).setBounds(20, 20, 30, 20);
        pnlFamInner.add(new JTextField()).setBounds(60, 20, 40, 20);
        pnlFamInner.add(new JLabel("Parentesco")).setBounds(120, 20, 80, 20);
        JTextField txtParent = new JTextField();
        txtParent.setBounds(200, 20, 60, 20);
        JButton btnParent = new JButton("▼");
        btnParent.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
        btnParent.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnParent.setBounds(260, 20, 20, 20);
        buscador.configurar(txtParent, null, btnParent, dParent, new String[]{"Clave", "Descripción"}, new int[]{60, 150}, null);
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
        buscador.configurar(txtTipoTelFam, null, btnTipoTelFam, dTTel, new String[]{"Clave", "Descripción"}, new int[]{60, 150}, null);
        pnlFamInner.add(txtTipoTelFam);
        pnlFamInner.add(btnTipoTelFam);
        pnlFamInner.add(new JTextField()).setBounds(180, 140, 400, 20);

        JButton btnOkFam = new JButton("OK");
        btnOkFam.setBounds(590, 140, 50, 20);
        pnlFamInner.add(btnOkFam);
        pnlFamiliares.add(pnlFamInner);

        // ==========================================
        // TAB 4: INF. FISCAL BANCARIA
        // ==========================================
        JPanel pnlFiscBanc = new JPanel(null);
        JTabbedPane tbLateral = new JTabbedPane(JTabbedPane.RIGHT);
        tbLateral.setBounds(10, 10, 730, 380);

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
        buscador.configurar(txtPobf, null, btnPobf, dPob, new String[]{"Clave", "Descripción"}, new int[]{60, 200}, null);
        pnlSubFiscal.add(txtPobf);
        pnlSubFiscal.add(btnPobf);

        pnlSubFiscal.add(new JLabel("Estado")).setBounds(20, 170, 80, 25);
        JTextField txtEdof = new JTextField();
        txtEdof.setBounds(110, 170, 50, 25);
        JButton btnEdof = new JButton("▼");
        btnEdof.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
        btnEdof.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnEdof.setBounds(160, 170, 20, 25);
        buscador.configurar(txtEdof, null, btnEdof, dEdo, new String[]{"Clave", "Descripción"}, new int[]{60, 200}, null);
        pnlSubFiscal.add(txtEdof);
        pnlSubFiscal.add(btnEdof);

        pnlSubFiscal.add(new JLabel("País")).setBounds(20, 200, 80, 25);
        JTextField txtPaisf = new JTextField("MEX");
        txtPaisf.setBounds(110, 200, 50, 25);
        JButton btnPaisf = new JButton("▼");
        btnPaisf.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
        btnPaisf.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnPaisf.setBounds(160, 200, 20, 25);
        buscador.configurar(txtPaisf, null, btnPaisf, dPais, new String[]{"Clave", "Descripción"}, new int[]{60, 200}, null);
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
        buscador.configurar(txtRegFisc, null, btnRegFisc, dRegFisc, new String[]{"Clave", "Descripción"}, new int[]{60, 250}, null);
        pnlSubFiscal.add(txtRegFisc);
        pnlSubFiscal.add(btnRegFisc);

        pnlSubFiscal.add(new JLabel("Uso CFDI")).setBounds(20, 290, 80, 25);
        JTextField txtUsoCFDI = new JTextField();
        txtUsoCFDI.setBounds(110, 290, 50, 25);
        JButton btnUsoCFDI = new JButton("▼");
        btnUsoCFDI.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
        btnUsoCFDI.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnUsoCFDI.setBounds(160, 290, 20, 25);
        buscador.configurar(txtUsoCFDI, null, btnUsoCFDI, dUsoCFDI, new String[]{"Clave", "Descripción"}, new int[]{60, 250}, null);
        pnlSubFiscal.add(txtUsoCFDI);
        pnlSubFiscal.add(btnUsoCFDI);

        pnlSubFiscal.add(new JLabel("Teléfono")).setBounds(20, 320, 80, 25);
        JTextField txtTelFisc = new JTextField();
        txtTelFisc.setBounds(110, 320, 300, 25);
        pnlSubFiscal.add(txtTelFisc);

        JPanel pnlSubBancaria = new JPanel(null);

        tbLateral.addTab("Fiscal", pnlSubFiscal);
        tbLateral.addTab("Bancaria", pnlSubBancaria);
        pnlFiscBanc.add(tbLateral);

        // ==========================================
        // TAB 5: MENSAJES Y ACUERDOS
        // ==========================================
        JPanel pnlMensajes = new JPanel(null);
        JPanel pnlMsjTop = new JPanel(null);
        pnlMsjTop.setBorder(BorderFactory.createTitledBorder("Mensaje ó Recordatorio"));
        pnlMsjTop.setBounds(10, 10, 720, 60);
        pnlMsjTop.add(new JLabel("Mensaje")).setBounds(20, 20, 70, 25);

        JTextField txtMensaje = new JTextField();
        txtMensaje.setBounds(100, 20, 60, 25);
        JButton btnMensaje = new JButton("▼");
        btnMensaje.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
        btnMensaje.setMargin(new java.awt.Insets(0, 0, 0, 0));
        btnMensaje.setBounds(160, 20, 20, 25);
        buscador.configurar(txtMensaje, null, btnMensaje, dMensaje, new String[]{"Clave", "Descripción"}, new int[]{80, 250}, null);
        pnlMsjTop.add(txtMensaje);
        pnlMsjTop.add(btnMensaje);
        pnlMensajes.add(pnlMsjTop);

        JPanel pnlAcuerdos = new JPanel(null);
        pnlAcuerdos.setBorder(BorderFactory.createTitledBorder("Acuerdos"));
        pnlAcuerdos.setBounds(10, 80, 720, 290);
        pnlAcuerdos.add(new JLabel("Sec")).setBounds(10, 20, 30, 20);
        pnlAcuerdos.add(new JTextField("1")).setBounds(10, 40, 30, 25);
        pnlAcuerdos.add(new JLabel("Descripción")).setBounds(50, 20, 100, 20);
        pnlAcuerdos.add(new JTextField()).setBounds(50, 40, 590, 25);
        pnlAcuerdos.add(new JButton("OK")).setBounds(650, 40, 60, 25);

        JTable tblAcuerdos = new JTable(new DefaultTableModel(new Object[][]{}, new String[]{"Sec", "Acuerdos"}));
        JScrollPane scrollAcuerdos = new JScrollPane(tblAcuerdos);
        scrollAcuerdos.setBounds(10, 75, 700, 200);
        pnlAcuerdos.add(scrollAcuerdos);
        pnlMensajes.add(pnlAcuerdos);

        // TAB 6: DIARIO ACADÉMICO
        JPanel pnlDiario = new JPanel(null);

        pestanas.addTab("Escolar", pnlEscolar);
        pestanas.addTab("Información Personal", pnlInfoPers);
        pestanas.addTab("Familiares", pnlFamiliares);
        pestanas.addTab("Inf. Fiscal Bancaria", pnlFiscBanc);
        pestanas.addTab("Mensajes y Acuerdos", pnlMensajes);
        pestanas.addTab("Diario Académico", pnlDiario);

        dialogo.add(pestanas);

        JButton btnAceptar = new JButton("Aceptar");
        btnAceptar.setBounds(270, 580, 100, 40);
        JButton btnSalir = new JButton("Salir");
        btnSalir.setBounds(400, 580, 100, 40);

        dialogo.add(btnAceptar);
        dialogo.add(btnSalir);

        // ==========================================
        // MODO EDICIÓN: CARGA DE REGISTROS DE LA BD
        // ==========================================
        if (modoEdicion) {
            int viewRow = jTable2.getSelectedRow();
            if (viewRow != -1) {
                int modelRow = jTable2.convertRowIndexToModel(viewRow);
                String matriculaSel = jTable2.getValueAt(modelRow, 4).toString();
                txtMat.setText(matriculaSel);

                try {
                    ConDB db = new ConDB();
                    Connection con = db.Conectar();
                    if (con != null) {
                        String sql = "SELECT a.*, "
                                + "COALESCE(x.CC, '') AS CC_ACAD, "
                                + "COALESCE(x.GRADO, '') AS GRADO_ACAD, "
                                + "COALESCE(x.GRUPO, '') AS GRUPO_ACAD, "
                                + "COALESCE(x.TURNO, '') AS TURNO_ACAD, "
                                + "COALESCE(x.NLISTA, '') AS NLISTA_ACAD, "
                                + "COALESCE(x.CBECA, '') AS CBECA_AX, "
                                + "COALESCE(x.TBECA, '') AS TBECA_AX, "
                                + "COALESCE(x.TALU, '') AS TALU_AX "
                                + "FROM tesalum a "
                                + "LEFT JOIN tesaxce x ON a.MAT = x.MAT AND x.CESC = (SELECT MAX(CESC) FROM tesaxce WHERE MAT = a.MAT) "
                                + "WHERE a.MAT = ?";

                        PreparedStatement ps = con.prepareStatement(sql);
                        ps.setString(1, matriculaSel);
                        ResultSet rs = ps.executeQuery();

                        if (rs.next()) {
                            txtMatOf.setText(rs.getString("MATOFC") != null ? rs.getString("MATOFC") : "");
                            txtCC.setText(rs.getString("CC_ACAD"));
                            txtTipoAl.setText(rs.getString("TALU_AX"));

                            txtNombre.setText(rs.getString("NOMA") != null ? rs.getString("NOMA") : "");
                            txtApate.setText(rs.getString("APATE") != null ? rs.getString("APATE") : "");
                            txtAmate.setText(rs.getString("AMATE") != null ? rs.getString("AMATE") : "");

                            String sexo = rs.getString("SEXO");
                            if ("M".equalsIgnoreCase(sexo)) {
                                rbM.setSelected(true);
                            } else if ("F".equalsIgnoreCase(sexo)) {
                                rbF.setSelected(true);
                            }

                            String cBeca = rs.getString("CBECA_AX");
                            String tBeca = rs.getString("TBECA_AX");
                            if (cBeca != null && !cBeca.trim().isEmpty()) {
                                txtBeca.setText(cBeca);
                                if ("C".equalsIgnoreCase(tBeca)) {
                                    rbConvenio.setSelected(true);
                                } else {
                                    rbBeca.setSelected(true);
                                }

                                PreparedStatement psBeca = con.prepareStatement("SELECT DBECA FROM tesbege WHERE CBECA = ?");
                                psBeca.setString(1, cBeca);
                                ResultSet rsB = psBeca.executeQuery();
                                if (rsB.next()) {
                                    txtBecaDesc.setText(rsB.getString("DBECA"));
                                }
                                rsB.close();
                                psBeca.close();
                            }

                            txtTurno.setText(rs.getString("TURNO_ACAD"));
                            cargarGradosPorSeccion.run();
                            cmbGrado.setSelectedItem(rs.getString("GRADO_ACAD"));
                            txtGrupo.setText(rs.getString("GRUPO_ACAD"));
                            txtNLista.setText(rs.getString("NLISTA_ACAD"));

                            txtEstatus.setText(rs.getString("STSALU") != null ? rs.getString("STSALU") : "A");
                            txtUsrbaj.setText(rs.getString("USRBAJ") != null ? rs.getString("USRBAJ") : "");
                            txtMotbaj.setText(rs.getString("MOTBAJ") != null ? rs.getString("MOTBAJ") : "");

                            txtCalleDom.setText(rs.getString("CALLEF") != null ? rs.getString("CALLEF") : "");
                            txtColDom.setText(rs.getString("COLF") != null ? rs.getString("COLF") : "");
                            txtPobDom.setText(rs.getString("POBF") != null ? rs.getString("POBF") : "");
                            txtEdoDom.setText(rs.getString("EDOF") != null ? rs.getString("EDOF") : "");
                            txtCpDom.setText(rs.getString("CPF") != null ? rs.getString("CPF") : "");
                            txtCurp.setText(rs.getString("CURP") != null ? rs.getString("CURP") : "");
                            txtTel.setText(rs.getString("TEL") != null ? rs.getString("TEL") : "");

                            txtNomTutor.setText(rs.getString("NOMT") != null ? rs.getString("NOMT") : "");

                            txtNomf.setText(rs.getString("NOMF") != null ? rs.getString("NOMF") : "");
                            txtCallef.setText(rs.getString("CALLEF") != null ? rs.getString("CALLEF") : "");
                            txtExtf.setText(rs.getString("NEXTF") != null ? rs.getString("NEXTF") : "");
                            txtIntf.setText(rs.getString("NINTF") != null ? rs.getString("NINTF") : "");
                            txtColf.setText(rs.getString("COLF") != null ? rs.getString("COLF") : "");
                            txtCpf.setText(rs.getString("CPF") != null ? rs.getString("CPF") : "");
                            txtPobf.setText(rs.getString("POBF") != null ? rs.getString("POBF") : "");
                            txtEdof.setText(rs.getString("EDOF") != null ? rs.getString("EDOF") : "");
                            txtPaisf.setText(rs.getString("PAISF") != null ? rs.getString("PAISF") : "MEX");
                            txtRfc.setText(rs.getString("RFC") != null ? rs.getString("RFC") : "");

                            String tpOper = rs.getString("TPOPER");
                            if ("M".equalsIgnoreCase(tpOper)) {
                                rbMoral.setSelected(true);
                            } else {
                                rbFisica.setSelected(true);
                            }

                            txtRegFisc.setText(rs.getString("REGFIS") != null ? rs.getString("REGFIS") : "");
                            txtUsoCFDI.setText(rs.getString("USOCFDI") != null ? rs.getString("USOCFDI") : "");
                            txtTelFisc.setText(rs.getString("TEL") != null ? rs.getString("TEL") : "");
                            txtMensaje.setText(rs.getString("MSG") != null ? rs.getString("MSG") : "");
                        }
                        rs.close();
                        ps.close();
                        db.Cerrar();
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialogo, "Error al cargar datos del alumno: " + ex.getMessage());
                }
            }
        }

        btnSalir.addActionListener(e -> dialogo.dispose());

        // ==========================================
        // EVENTO GUARDAR (INSERT / UPDATE EN BASE DE DATOS)
        // ==========================================
        btnAceptar.addActionListener(e -> {
            String matricula = txtMat.getText().trim();
            String nombreStr = txtNombre.getText().trim();
            String apateStr = txtApate.getText().trim();
            String amateStr = txtAmate.getText().trim();

            if (matricula.isEmpty() || nombreStr.isEmpty() || apateStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialogo, "Matrícula, Nombre y Apellido Paterno son obligatorios.", "Atención", JOptionPane.WARNING_MESSAGE);
                return;
            }

            java.text.SimpleDateFormat sdfSql = new java.text.SimpleDateFormat("yyyy-MM-dd");
            String fAltStr = txtFalt.getDate() != null ? sdfSql.format(txtFalt.getDate()) : sdfSql.format(new java.util.Date());
            String fBajStr = txtFbaj.getDate() != null ? sdfSql.format(txtFbaj.getDate()) : null;

            String nomCom = (apateStr + " " + amateStr + " " + nombreStr).trim();
            String sexoStr = rbF.isSelected() ? "F" : "M";
            String tpoOper = rbMoral.isSelected() ? "M" : "F";
            String tBeca = rbConvenio.isSelected() ? "C" : "B";
            String usrSesion = obtenerUsuarioActivo();

            Connection con = null;

            try {
                ConDB db = new ConDB();
                con = db.Conectar();

                if (con != null) {
                    con.setAutoCommit(false); // Iniciar Transacción

                    if (modoEdicion) {
                        // 1. UPDATE tesalum
                        String sqlUpdAlum = "UPDATE tesalum SET MATOFC=?, NOMA=?, APATE=?, AMATE=?, NOMCOM=?, SEXO=?, FALT=?, STSALU=?, MOTBAJ=?, FBAJ=?, USRBAJ=?, "
                                + "NOMT=?, CURP=?, NOMF=?, CALLEF=?, NEXTF=?, NINTF=?, COLF=?, CPF=?, POBF=?, EDOF=?, PAISF=?, RFC=?, TPOPER=?, "
                                + "REGFIS=?, USOCFDI=?, TEL=?, MSG=?, USER=?, FEAC=CURDATE(), HOAC=DATE_FORMAT(NOW(), '%r') WHERE MAT=?";

                        PreparedStatement psAl = con.prepareStatement(sqlUpdAlum);
                        psAl.setString(1, txtMatOf.getText().trim());
                        psAl.setString(2, nombreStr);
                        psAl.setString(3, apateStr);
                        psAl.setString(4, amateStr);
                        psAl.setString(5, nomCom);
                        psAl.setString(6, sexoStr);
                        psAl.setString(7, fAltStr);
                        psAl.setString(8, txtEstatus.getText().trim());
                        psAl.setString(9, txtMotbaj.getText().trim());
                        psAl.setString(10, fBajStr);
                        psAl.setString(11, txtUsrbaj.getText().trim());
                        psAl.setString(12, txtNomTutor.getText().trim());
                        psAl.setString(13, txtCurp.getText().trim());
                        psAl.setString(14, txtNomf.getText().trim());
                        psAl.setString(15, txtCallef.getText().trim());
                        psAl.setString(16, txtExtf.getText().trim());
                        psAl.setString(17, txtIntf.getText().trim());
                        psAl.setString(18, txtColf.getText().trim());
                        psAl.setString(19, txtCpf.getText().trim());
                        psAl.setString(20, txtPobf.getText().trim());
                        psAl.setString(21, txtEdof.getText().trim());
                        psAl.setString(22, txtPaisf.getText().trim());
                        psAl.setString(23, txtRfc.getText().trim());
                        psAl.setString(24, tpoOper);
                        psAl.setString(25, txtRegFisc.getText().trim());
                        psAl.setString(26, txtUsoCFDI.getText().trim());
                        psAl.setString(27, txtTel.getText().trim());
                        psAl.setString(28, txtMensaje.getText().trim());
                        psAl.setString(29, usrSesion);
                        psAl.setString(30, matricula);
                        psAl.executeUpdate();
                        psAl.close();

                        // 2. UPDATE tesaxce (Ciclo escolar activo dinámico)
                        String cicloActual = "2627";
                        String sqlUpdAx = "UPDATE tesaxce SET CIA='12', CC=?, SECC=?, TALU=?, GRADO=?, TURNO=?, GRUPO=?, NLISTA=?, CBECA=?, TBECA=?, USER=?, FEAC=CURDATE(), HOAC=DATE_FORMAT(NOW(), '%r') "
                                + "WHERE MAT=? AND CESC=?";
                        PreparedStatement psAx = con.prepareStatement(sqlUpdAx);
                        psAx.setString(1, txtCC.getText().trim());
                        psAx.setString(2, txtTipoAl.getText().trim());
                        psAx.setString(3, txtTipoAl.getText().trim());
                        psAx.setString(4, cmbGrado.getSelectedItem() != null ? cmbGrado.getSelectedItem().toString() : "");
                        psAx.setString(5, txtTurno.getText().trim());
                        psAx.setString(6, txtGrupo.getText().trim());
                        psAx.setString(7, txtNLista.getText().trim());
                        psAx.setString(8, txtBeca.getText().trim());
                        psAx.setString(9, tBeca);
                        psAx.setString(10, usrSesion);
                        psAx.setString(11, matricula);
                        psAx.setString(12, cicloActual);

                        if (psAx.executeUpdate() == 0) {
                            psAx.close();
                            String sqlInsAx = "INSERT INTO tesaxce (CESC, CIA, CC, SECC, MAT, TALU, GRADO, TURNO, GRUPO, NLISTA, CBECA, TBECA, FINS, USER, FEAC, HOAC) "
                                    + "VALUES (?, '12', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURDATE(), ?, CURDATE(), DATE_FORMAT(NOW(), '%r'))";
                            PreparedStatement psInsAx = con.prepareStatement(sqlInsAx);
                            psInsAx.setString(1, cicloActual);
                            psInsAx.setString(2, txtCC.getText().trim());
                            psInsAx.setString(3, txtTipoAl.getText().trim());
                            psInsAx.setString(4, matricula);
                            psInsAx.setString(5, txtTipoAl.getText().trim());
                            psInsAx.setString(6, cmbGrado.getSelectedItem() != null ? cmbGrado.getSelectedItem().toString() : "");
                            psInsAx.setString(7, txtTurno.getText().trim());
                            psInsAx.setString(8, txtGrupo.getText().trim());
                            psInsAx.setString(9, txtNLista.getText().trim());
                            psInsAx.setString(10, txtBeca.getText().trim());
                            psInsAx.setString(11, tBeca);
                            psInsAx.setString(12, usrSesion);
                            psInsAx.executeUpdate();
                            psInsAx.close();
                        } else {
                            psAx.close();
                        }

                    } else {
                        // 1. INSERT tesalum
                        String sqlInsAlum = "INSERT INTO tesalum (MAT, MATOFC, NOMA, APATE, AMATE, NOMCOM, SEXO, FALUM, FALT, STSALU, MOTBAJ, FBAJ, USRBAJ, "
                                + "NOMT, CURP, NOMF, CALLEF, NEXTF, NINTF, COLF, CPF, POBF, EDOF, PAISF, RFC, TPOPER, REGFIS, USOCFDI, TEL, MSG, USER, FEAC, HOAC) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?, NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURDATE(), DATE_FORMAT(NOW(), '%r'))";

                        PreparedStatement psAl = con.prepareStatement(sqlInsAlum);
                        psAl.setString(1, matricula);
                        psAl.setString(2, txtMatOf.getText().trim());
                        psAl.setString(3, nombreStr);
                        psAl.setString(4, apateStr);
                        psAl.setString(5, amateStr);
                        psAl.setString(6, nomCom);
                        psAl.setString(7, sexoStr);
                        psAl.setString(8, fAltStr);
                        psAl.setString(9, txtEstatus.getText().trim());
                        psAl.setString(10, txtMotbaj.getText().trim());
                        psAl.setString(11, fBajStr);
                        psAl.setString(12, txtUsrbaj.getText().trim());
                        psAl.setString(13, txtNomTutor.getText().trim());
                        psAl.setString(14, txtCurp.getText().trim());
                        psAl.setString(15, txtNomf.getText().trim());
                        psAl.setString(16, txtCallef.getText().trim());
                        psAl.setString(17, txtExtf.getText().trim());
                        psAl.setString(18, txtIntf.getText().trim());
                        psAl.setString(19, txtColf.getText().trim());
                        psAl.setString(20, txtCpf.getText().trim());
                        psAl.setString(21, txtPobf.getText().trim());
                        psAl.setString(22, txtEdof.getText().trim());
                        psAl.setString(23, txtPaisf.getText().trim());
                        psAl.setString(24, txtRfc.getText().trim());
                        psAl.setString(25, tpoOper);
                        psAl.setString(26, txtRegFisc.getText().trim());
                        psAl.setString(27, txtUsoCFDI.getText().trim());
                        psAl.setString(28, txtTel.getText().trim());
                        psAl.setString(29, txtMensaje.getText().trim());
                        psAl.setString(30, usrSesion);
                        psAl.executeUpdate();
                        psAl.close();

                        // 2. INSERT tesaxce (Inscripción al ciclo)
                        String cicloInscripcion = "2627"; // Recuperar del control de ciclo escolar activo
                        String sqlInsAx = "INSERT INTO tesaxce (CESC, CIA, CC, SECC, MAT, TALU, GRADO, TURNO, GRUPO, NLISTA, CBECA, TBECA, FINS, USER, FEAC, HOAC) "
                                + "VALUES (?, '12', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURDATE(), ?, CURDATE(), DATE_FORMAT(NOW(), '%r'))";
                        PreparedStatement psInsAx = con.prepareStatement(sqlInsAx);
                        psInsAx.setString(1, cicloInscripcion);
                        psInsAx.setString(2, txtCC.getText().trim());
                        psInsAx.setString(3, txtTipoAl.getText().trim());
                        psInsAx.setString(4, matricula);
                        psInsAx.setString(5, txtTipoAl.getText().trim());
                        psInsAx.setString(6, cmbGrado.getSelectedItem() != null ? cmbGrado.getSelectedItem().toString() : "");
                        psInsAx.setString(7, txtTurno.getText().trim());
                        psInsAx.setString(8, txtGrupo.getText().trim());
                        psInsAx.setString(9, txtNLista.getText().trim());
                        psInsAx.setString(10, txtBeca.getText().trim());
                        psInsAx.setString(11, tBeca);
                        psInsAx.setString(12, usrSesion);
                        psInsAx.executeUpdate();
                        psInsAx.close();

                        // 3. Generación automática de cargos en 'tescalu'
                        generarCargosAlumnoTransaccional(con, "12", txtCC.getText().trim(), txtTipoAl.getText().trim(), cicloInscripcion, matricula, cmbGrado.getSelectedItem().toString(), txtGrupo.getText().trim(), usrSesion);
                    }

                    con.commit();
                    con.setAutoCommit(true);
                    db.Cerrar();

                    JOptionPane.showMessageDialog(dialogo, "Alumno guardado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    dialogo.dispose();
                    cargarTablaAlumnos();
                }
            } catch (Exception ex) {
                if (con != null) {
                    try {
                        con.rollback();
                        con.setAutoCommit(true);
                        con.close();
                    } catch (Exception exR) {
                    }
                }
                JOptionPane.showMessageDialog(dialogo, "Error al guardar el alumno: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }

    private void generarCargosAlumnoTransaccional(Connection con, String cia, String cc, String secc, String ciclo, String matricula, String grado, String grupo, String usuario) throws Exception {
        String cBeca = "", tBeca = "";
        double porcBeca = 0.0;

        // 1. Obtener la beca del alumno registrada en tesaxce
        PreparedStatement psAx = con.prepareStatement("SELECT CBECA, TBECA FROM tesaxce WHERE MAT = ? AND CESC = ? LIMIT 1");
        psAx.setString(1, matricula);
        psAx.setString(2, ciclo);
        ResultSet rsAx = psAx.executeQuery();
        if (rsAx.next()) {
            cBeca = rsAx.getString("CBECA") != null ? rsAx.getString("CBECA") : "";
            tBeca = rsAx.getString("TBECA") != null ? rsAx.getString("TBECA") : "";
        }
        rsAx.close();
        psAx.close();

        // 2. Si tiene beca, obtener porcentaje de descuento de tesbede para Colegiaturas ('C')
        if (!cBeca.isEmpty()) {
            PreparedStatement psBec = con.prepareStatement("SELECT PDSC FROM tesbede WHERE CBECA = ? AND TCPTO = 'C' LIMIT 1");
            psBec.setString(1, cBeca);
            ResultSet rsBec = psBec.executeQuery();
            if (rsBec.next()) {
                porcBeca = rsBec.getDouble("PDSC");
            }
            rsBec.close();
            psBec.close();
        }

        // 3. Obtener la plantilla de cobros de tesgpde
        String sqlPlantilla = "SELECT g.NCPTO, g.TCPTO, g.DCPTO, c.DCPTO AS DESC_REAL, c.CUNI, g.IMPTE, g.FVINI, g.FVFIN "
                + "FROM tesgpde g "
                + "INNER JOIN tescpto c ON g.CIA = c.CIA AND g.CC = c.CC AND g.NCPTO = c.NCPTO "
                + "WHERE g.CIA = ? AND g.CC = ? AND g.CESC = ? AND g.CGPO = 'INOR' "
                + "ORDER BY g.SEC ASC";

        PreparedStatement psGpo = con.prepareStatement(sqlPlantilla);
        psGpo.setString(1, cia);
        psGpo.setString(2, cc);
        psGpo.setString(3, ciclo);
        ResultSet rsGpo = psGpo.executeQuery();

        // Sentencia SQL ajustada con 24 comodines (?)
        String sqlInsCalu = "INSERT INTO tescalu (CIA, CC, SECC, CESC, PESC, MAT, TALU, GRADO, GRUPO, IDCPT, NCPTO, TCPTO, DCPTO, CMON, TCONT, TCAMB, CUNIMN, CANT, IMPMN, TDSC, PDSC, IDSCMN, NADSC, MDSC, PREC, IRECMN, NAREC, MREC, CBECA, TBECA, PBEC, IBECMN, NABEC, MBEC, IMPTMN, FVINI, FVEN, FCON, IPAGMN, IPENMN, CUNIME, IMPME, IDSCME, IRECME, IBECME, IMPTME, IPAGME, IPENME, NCAJ, RELPOL, RELPOC, MCAN, USER, FEAC, HOAC) "
                + "VALUES (?, ?, ?, ?, 'A', ?, ?, ?, ?, ?, ?, ?, ?, 'MXP', 'O', 1, ?, 1, ?, '', 0, 0, 0, '', 0, 0, 0, '', ?, ?, ?, ?, 0, '', ?, ?, ?, '0000-00-00', 0, ?, 0, 0, 0, 0, 0, 0, 0, ?, 80, 0, 0, '', ?, CURDATE(), DATE_FORMAT(NOW(), '%r'))";

        PreparedStatement psIns = con.prepareStatement(sqlInsCalu);
        int baseIdCpt = (int) (System.currentTimeMillis() % 800000) + 100000;

        while (rsGpo.next()) {
            String ncpto = rsGpo.getString("NCPTO");
            String tcpto = rsGpo.getString("TCPTO");
            String dcpto = (rsGpo.getString("DESC_REAL") != null && !rsGpo.getString("DESC_REAL").isEmpty())
                    ? rsGpo.getString("DESC_REAL") : rsGpo.getString("DCPTO");

            double impBase = rsGpo.getDouble("IMPTE") > 0 ? rsGpo.getDouble("IMPTE") : rsGpo.getDouble("CUNI");
            double descBeca = 0.0;
            double pBecaAplicada = 0.0;

            if ("C".equalsIgnoreCase(tcpto) && porcBeca > 0) {
                pBecaAplicada = porcBeca;
                descBeca = impBase * (porcBeca / 100.0);
            }

            double impTotal = impBase - descBeca;

            psIns.setString(1, cia);
            psIns.setString(2, cc);
            psIns.setString(3, secc);
            psIns.setString(4, ciclo);
            psIns.setString(5, matricula);
            psIns.setString(6, secc); // TALU
            psIns.setString(7, grado);
            psIns.setString(8, grupo);
            psIns.setInt(9, baseIdCpt++);
            psIns.setString(10, ncpto);
            psIns.setString(11, tcpto);
            psIns.setString(12, dcpto);
            psIns.setDouble(13, impBase);       // CUNIMN / IMPMN
            psIns.setDouble(14, impBase);       // IMPMN
            psIns.setString(15, cBeca);         // CBECA
            psIns.setString(16, tBeca);         // TBECA
            psIns.setDouble(17, pBecaAplicada); // PBEC
            psIns.setDouble(18, descBeca);      // IBECMN
            psIns.setDouble(19, impTotal);      // IMPTMN
            psIns.setDate(20, rsGpo.getDate("FVINI"));
            psIns.setDate(21, rsGpo.getDate("FVFIN")); // FVEN
            psIns.setDouble(22, impTotal);      // IPENMN
            psIns.setDouble(23, impTotal);      // IPENME
            psIns.setString(24, usuario);       // USER

            psIns.addBatch();
        }

        psIns.executeBatch();
        rsGpo.close();
        psGpo.close();
        psIns.close();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddCTAlumno;
    private javax.swing.JButton btnDeleteCTAlumno;
    private javax.swing.JButton btnEditCTAlumno;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    // End of variables declaration//GEN-END:variables
}
