package com.mycompany.benaedu.views;

import com.mycompany.benaedu.services.ArchivoBcm;
import com.mycompany.benaedu.services.InterfaseBcm;
import com.mycompany.benaedu.services.ArchivoBcm.Movimiento;
import com.mycompany.benaedu.services.InterfaseBcm.*;
import java.awt.*;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Lectura EXP, conciliación de solo lectura y aplicación explícita de pagos. */
public class Interfase_Bancaria_BCM extends JPanel {
    private final JComboBox<Opcion> cia = new JComboBox<>(), banco = new JComboBox<>(), efe = new JComboBox<>(), cct = new JComboBox<>();
    private final JTextField cuenta = new JTextField("0160839167", 12), archivo = new JTextField(40);
    private final JButton buscar = new JButton("Buscar…"), cargar = new JButton("Cargar y revisar"), aplicar = new JButton("Aplicar pagos");
    private final JLabel resumen = new JLabel("Cargando catálogos...");
    private final String usuario;
    private List<Movimiento> movimientos = List.of();
    private List<Revision> revisiones = List.of();
    private Configuracion configuracion;
    private boolean catalogosListos;
    private final DefaultTableModel modelo = new DefaultTableModel(new String[]{"#", "Fecha banco", "Forma", "Guía", "Referencia banco", "Importe", "Estado", "Detalle"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private record Opcion(String clave, String descripcion) {
        @Override public String toString() { return clave.isEmpty() ? descripcion : clave + " — " + descripcion; }
    }
    /** Abre la pantalla sin autorización de aplicación si no se proporcionó sesión. */
    public Interfase_Bancaria_BCM() { this(null); }
    /** Construye el módulo con el usuario autenticado. */
    public Interfase_Bancaria_BCM(String usuario) { this(usuario, true); }
    /** Permite pruebas de diseño sin conexión a la base. */
    Interfase_Bancaria_BCM(String usuario, boolean conectar) {
        super(new BorderLayout(8, 8)); this.usuario = usuario;
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JPanel filtros = new JPanel(new GridLayout(0, 1, 4, 4));
        filtros.add(fila(new JLabel("Compañía"), cia, new JLabel("Banco receptor"), banco));
        filtros.add(fila(new JLabel("Cuenta"), cuenta, new JLabel("Forma escolar EFE"), efe, new JLabel("Forma escolar CCT"), cct));
        filtros.add(fila(new JLabel("Archivo EXP"), archivo, buscar));
        filtros.add(new JLabel("La fecha y el importe se toman del banco. Cargar y revisar no registra pagos."));
        add(filtros, BorderLayout.NORTH);
        JTable tabla = new JTable(modelo); tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] anchos = {40, 95, 60, 90, 180, 95, 115, 570};
        for (int i = 0; i < anchos.length; i++) tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        JPanel pie = new JPanel(new GridLayout(0, 1)); pie.add(fila(cargar, aplicar)); pie.add(fila(resumen)); add(pie, BorderLayout.SOUTH);
        for (var combo : List.of(cia, banco, efe, cct)) {
            combo.setPreferredSize(new Dimension(155, 26)); combo.addActionListener(e -> invalidar());
        }
        var cambios = new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { invalidar(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { invalidar(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { invalidar(); }
        };
        archivo.getDocument().addDocumentListener(cambios); cuenta.getDocument().addDocumentListener(cambios);
        buscar.addActionListener(e -> {
            JFileChooser selector = new JFileChooser();
            selector.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Archivo bancario EXP", "exp"));
            if (selector.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) archivo.setText(selector.getSelectedFile().getAbsolutePath());
        });
        cargar.addActionListener(e -> cargar()); aplicar.addActionListener(e -> aplicar());
        ocupado(true); if (conectar) cargarCatalogos();
    }
    private static JPanel fila(Component... componentes) {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4)); for (Component c : componentes) fila.add(c); return fila;
    }
    private void invalidar() {
        revisiones = List.of(); movimientos = List.of(); configuracion = null; modelo.setRowCount(0);
        aplicar.setEnabled(false); resumen.setText("Seleccione el archivo y pulse Cargar y revisar.");
    }
    private void cargarCatalogos() {
        new SwingWorker<List<Map<String, String>>, Void>() {
            @Override protected List<Map<String, String>> doInBackground() throws Exception {
                return List.of(InterfaseBcm.catalogo("CIA"), InterfaseBcm.catalogo("BCOS"), InterfaseBcm.catalogo("IPAG"));
            }
            @Override protected void done() {
                try {
                    var datos = get(); llenar(cia, datos.get(0)); llenar(banco, datos.get(1)); llenar(efe, datos.get(2)); llenar(cct, datos.get(2));
                    if (cia.getItemCount() == 2) cia.setSelectedIndex(1); catalogosListos = true;
                } catch (InterruptedException ex) { Thread.currentThread().interrupt(); error(ex); }
                catch (ExecutionException ex) { error(ex.getCause()); }
                finally { ocupado(false); }
            }
        }.execute();
    }
    private static void llenar(JComboBox<Opcion> combo, Map<String, String> datos) {
        combo.removeAllItems(); combo.addItem(new Opcion("", "Seleccione…"));
        datos.forEach((codigo, nombre) -> combo.addItem(new Opcion(codigo, nombre)));
    }
    private String clave(JComboBox<Opcion> combo) {
        if (!(combo.getSelectedItem() instanceof Opcion o) || o.clave().isBlank()) throw new IllegalArgumentException("Seleccione compañía, banco y equivalencias EFE/CCT.");
        return o.clave();
    }
    private void cargar() {
        invalidar();
        try {
            if (archivo.getText().isBlank()) throw new IllegalArgumentException("Seleccione un archivo EXP.");
            if (usuario == null || usuario.isBlank()) throw new IllegalArgumentException("Abra el módulo desde la sesión autenticada.");
            Configuracion cfg = new Configuracion(clave(cia), clave(banco), cuenta.getText().trim(), clave(efe), clave(cct), usuario);
            Path ruta = Path.of(archivo.getText().trim()); ocupado(true); resumen.setText("Leyendo y conciliando; no se modifican datos...");
            new SwingWorker<List<Revision>, Void>() {
                private List<Movimiento> leidos;
                @Override protected List<Revision> doInBackground() throws Exception { leidos = ArchivoBcm.leer(ruta); return InterfaseBcm.revisar(cfg, leidos); }
                @Override protected void done() {
                    try {
                        revisiones = get(); movimientos = leidos; configuracion = cfg;
                        for (Revision r : revisiones) { var m = r.movimiento(); modelo.addRow(new Object[]{m.numero(), m.fecha(), m.forma(), m.guia(), m.referencia(), m.importe().toPlainString(), r.estado(), r.detalle()}); }
                        BigDecimal total = movimientos.stream().map(Movimiento::importe).reduce(BigDecimal.ZERO, BigDecimal::add);
                        BigDecimal listo = revisiones.stream().filter(Revision::listo).map(r -> r.movimiento().importe()).reduce(BigDecimal.ZERO, BigDecimal::add);
                        resumen.setText(movimientos.size() + " movimientos | Archivo: $" + total.toPlainString() + " | Listos: " + revisiones.stream().filter(Revision::listo).count() + " por $" + listo.toPlainString());
                    } catch (InterruptedException ex) { Thread.currentThread().interrupt(); error(ex); }
                    catch (ExecutionException ex) { error(ex.getCause()); }
                    finally { ocupado(false); }
                }
            }.execute();
        } catch (IllegalArgumentException ex) { error(ex); }
    }
    private void aplicar() {
        long cantidad = revisiones.stream().filter(Revision::listo).count();
        if (cantidad == 0 || configuracion == null) return;
        BigDecimal importe = revisiones.stream().filter(Revision::listo).map(r -> r.movimiento().importe()).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (JOptionPane.showConfirmDialog(this, "Se registrarán " + cantidad + " pagos por $" + importe.toPlainString()
                + " en la compañía " + configuracion.cia() + ", cuenta " + configuracion.cuenta()
                + ".\nSe crearán recibos y se actualizarán saldos. Los demás movimientos NO se aplicarán.\n¿Continuar?",
                "Aplicar pagos bancarios", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        ocupado(true); resumen.setText("Aplicando pagos; no cierre el programa...");
        new SwingWorker<Aplicacion, Void>() {
            @Override protected Aplicacion doInBackground() throws Exception { return InterfaseBcm.aplicar(configuracion, movimientos, revisiones); }
            @Override protected void done() {
                try {
                    Aplicacion resultado = get(); resumen.setText("Pagos completados: " + resultado.aplicados() + ". Recargue para consultar su estado.");
                    if (!resultado.incidencia().isEmpty()) JOptionPane.showMessageDialog(Interfase_Bancaria_BCM.this, resultado.incidencia(), "Lote detenido", JOptionPane.WARNING_MESSAGE);
                } catch (InterruptedException ex) { Thread.currentThread().interrupt(); error(ex); }
                catch (ExecutionException ex) { error(ex.getCause()); }
                finally {
                    for (int i = 0; i < modelo.getRowCount(); i++) if ("LISTO".equals(modelo.getValueAt(i, 6))) {
                        modelo.setValueAt("RECARGAR", i, 6); modelo.setValueAt("Recargue el archivo para comprobar el estado persistido.", i, 7);
                    }
                    revisiones = List.of(); ocupado(false);
                }
            }
        }.execute();
    }
    private void ocupado(boolean valor) {
        for (Component c : List.of(cia, banco, efe, cct, cuenta, archivo, buscar)) c.setEnabled(!valor);
        cargar.setEnabled(!valor && catalogosListos); aplicar.setEnabled(!valor && revisiones.stream().anyMatch(Revision::listo));
    }
    private void error(Throwable ex) {
        resumen.setText("No se completó la operación; revise el mensaje.");
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Interfase bancaria BCM", JOptionPane.ERROR_MESSAGE);
    }
}
