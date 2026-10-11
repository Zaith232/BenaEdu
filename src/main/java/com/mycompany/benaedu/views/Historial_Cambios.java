package com.mycompany.benaedu.views;

import com.mycompany.benaedu.SesionUsuario;
import com.mycompany.benaedu.db.Auditoria;
import com.mycompany.benaedu.db.ConDB;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Consulta de auditoría para administradores, con filtros, paginación y datos anteriores/nuevos. */
public final class Historial_Cambios extends JPanel {
    private final JSpinner desde = fecha(LocalDate.now().minusDays(30));
    private final JSpinner hasta = fecha(LocalDate.now());
    private final JTextField texto = new JTextField(22);
    private final JButton consultar = new JButton("Consultar");
    private final JButton anterior = new JButton("Anterior");
    private final JButton siguiente = new JButton("Siguiente");
    private final JButton detalle = new JButton("Ver cambio");
    private final JLabel estado = new JLabel(" ");
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"Fecha", "Hora", "Usuario", "Módulo / Tabla", "Acción", "Referencia", "Campos modificados"}, 0) {
        @Override public boolean isCellEditable(int fila, int columna) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private List<Auditoria.Cambio> cambios = List.of();
    private int pagina;
    private boolean cargando;
    private LocalDate filtroDesde;
    private LocalDate filtroHasta;
    private String filtroTexto;

    /** Crea la vista únicamente para una identidad administradora autenticada. */
    public Historial_Cambios() {
        if (!SesionUsuario.esAdministrador()) throw new SecurityException("Solo el administrador puede consultar el historial.");
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JPanel filtros = new JPanel(new java.awt.GridLayout(0, 1));
        JPanel fechas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fechas.add(new JLabel("Desde:")); fechas.add(desde);
        fechas.add(new JLabel("Hasta:")); fechas.add(hasta);
        JPanel busqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        busqueda.add(new JLabel("Buscar:")); busqueda.add(texto); busqueda.add(consultar);
        filtros.add(fechas); filtros.add(busqueda);
        texto.setToolTipText("Usuario, tabla, acción, matrícula o recibo");
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pie.add(anterior); pie.add(siguiente); pie.add(detalle); pie.add(estado);
        add(filtros, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(pie, BorderLayout.SOUTH);
        tabla.setAutoCreateRowSorter(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(28);
        consultar.addActionListener(e -> nuevaConsulta());
        texto.addActionListener(e -> nuevaConsulta());
        anterior.addActionListener(e -> { pagina--; cargar(); });
        siguiente.addActionListener(e -> { pagina++; cargar(); });
        detalle.addActionListener(e -> verDetalle());
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) verDetalle();
            }
        });
        EstiloContenido.aplicar(this);
        nuevaConsulta();
    }

    private static JSpinner fecha(LocalDate dia) {
        JSpinner spinner = new JSpinner(new SpinnerDateModel(Date.from(dia.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                null, null, java.util.Calendar.DAY_OF_MONTH));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "yyyy-MM-dd"));
        return spinner;
    }

    private void nuevaConsulta() {
        if (cargando) return;
        try {
            desde.commitEdit(); hasta.commitEdit();
            filtroDesde = ((Date) desde.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            filtroHasta = ((Date) hasta.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (filtroDesde.isAfter(filtroHasta)) throw new IllegalArgumentException("La fecha inicial debe ser anterior o igual a la final.");
            filtroTexto = texto.getText();
            pagina = 0;
            cargar();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Fechas de consulta", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void cargar() {
        cargando = true;
        consultar.setEnabled(false); anterior.setEnabled(false); siguiente.setEnabled(false); detalle.setEnabled(false);
        desde.setEnabled(false); hasta.setEnabled(false); texto.setEnabled(false);
        estado.setText("Consultando historial...");
        new SwingWorker<List<Auditoria.Cambio>, Void>() {
            @Override protected List<Auditoria.Cambio> doInBackground() throws Exception {
                try (var con = ConDB.getConnection()) {
                    return Auditoria.consultar(con, filtroDesde, filtroHasta, filtroTexto, pagina);
                }
            }
            @Override protected void done() {
                try {
                    cambios = get();
                    modelo.setRowCount(0);
                    for (var cambio : cambios) {
                        var momento = cambio.fecha().toLocalDateTime();
                        modelo.addRow(new Object[]{momento.toLocalDate(), momento.toLocalTime().withNano(0), cambio.usuario(),
                                nombreModulo(cambio.tabla()), cambio.accion(), cambio.referencia(), cambio.campos()});
                    }
                    estado.setText("Página " + (pagina + 1) + " · " + cambios.size() + " cambios · Hora del servidor");
                    siguiente.setEnabled(cambios.size() == 50);
                    detalle.setEnabled(!cambios.isEmpty());
                } catch (Exception ex) {
                    // No conservar resultados que podrían pertenecer a una sesión cuyo permiso fue revocado.
                    cambios = List.of(); modelo.setRowCount(0);
                    estado.setText("No fue posible consultar el historial");
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(Historial_Cambios.this, causa.getMessage(), "Historial", JOptionPane.ERROR_MESSAGE);
                } finally {
                    cargando = false; consultar.setEnabled(true); anterior.setEnabled(pagina > 0);
                    desde.setEnabled(true); hasta.setEnabled(true); texto.setEnabled(true);
                }
            }
        }.execute();
    }

    private static String nombreModulo(String tabla) {
        String nombre = switch (tabla.toLowerCase(java.util.Locale.ROOT)) {
            case "tespalu" -> "Pagos";
            case "tesralu" -> "Recibos de alumnos";
            case "tescalu" -> "Cargos de alumnos";
            case "tesalum" -> "Alumnos";
            case "tesaxce" -> "Inscripciones";
            case "tescaj" -> "Cajeros";
            case "users" -> "Usuarios";
            default -> tabla;
        };
        return nombre.equals(tabla) ? tabla : nombre + " (" + tabla + ")";
    }

    private void verDetalle() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) return;
        var cambio = cambios.get(tabla.convertRowIndexToModel(fila));
        JTextArea datos = new JTextArea("Usuario: " + cambio.usuario() + "\nFecha y hora: " + cambio.fecha()
                + "\nAcción: " + cambio.accion() + "\nReferencia: " + cambio.referencia()
                + "\nCampos: " + cambio.campos() + "\n\nANTES:\n" + cambio.antes() + "\n\nDESPUÉS:\n" + cambio.despues(), 22, 80);
        datos.setEditable(false); datos.setLineWrap(true); datos.setWrapStyleWord(true); datos.setCaretPosition(0);
        JOptionPane.showMessageDialog(this, new JScrollPane(datos), "Detalle del cambio", JOptionPane.INFORMATION_MESSAGE);
    }
}
