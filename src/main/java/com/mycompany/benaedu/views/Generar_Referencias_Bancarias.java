package com.mycompany.benaedu.views;

import com.mycompany.benaedu.services.ReferenciasBancarias;
import com.mycompany.benaedu.services.ArchivoReferenciasBancarias;
import com.mycompany.benaedu.services.ReferenciasBancarias.Referencia;
import com.mycompany.benaedu.services.ReferenciasBancarias.Ciclo;
import com.mycompany.benaedu.services.ReferenciasBancarias.Alumno;
import com.mycompany.benaedu.services.ReferenciasBancarias.Filtro;
import com.mycompany.benaedu.services.ReferenciasBancarias.Resultado;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Consulta, cálculo, guardado explícito e impresión de referencias bancarias. */
public class Generar_Referencias_Bancarias extends JPanel {
    private final JComboBox<String> cia = new JComboBox<>();
    private final JComboBox<String> centro = new JComboBox<>();
    private final JComboBox<String> ciclo = new JComboBox<>();
    private final JTextField matricula = new JTextField(10);
    private final JComboBox<String> grado = new JComboBox<>();
    private final JButton buscarAlumno = new JButton("Buscar alumno…");
    private final JButton todosAlumnos = new JButton("Todos");
    private final JLabel nombreAlumno = new JLabel("Todos los alumnos");
    private List<Alumno> alumnos = List.of();
    private int versionSeleccion;
    private boolean seleccionLista;
    private boolean trabajando;
    private final JTextField cuenta = new JTextField("0160839167", 12);
    private final JTextField convenio = new JTextField("881686", 8);
    private final JButton generar = new JButton("Genera");
    private final JButton imprimir = new JButton("Imprimir");
    private final JTabbedPane pestanas = new JTabbedPane();
    private final String usuario;
    private List<Referencia> referenciasVista = List.of();
    private Filtro filtroVista;
    private boolean guardadas;
    private final JLabel estado = new JLabel("Cargando ciclos...");
    private final JTextArea avisos = new JTextArea(5, 80);
    private final DefaultTableModel modelo = new DefaultTableModel(new String[]{
        "Matrícula", "Alumno", "Concepto", "Tipo", "Desde", "Hasta", "Importe MXN", "Referencia"
    }, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private List<Ciclo> catalogo = List.of();
    private boolean actualizando;

    /** Construye el módulo y carga sus catálogos fuera del hilo de interfaz. */
    public Generar_Referencias_Bancarias() {
        this(null);
    }

    /** Construye el módulo con el usuario autenticado para auditar el guardado. */
    public Generar_Referencias_Bancarias(String usuario) {
        this(usuario, true);
    }

    /** Permite comprobar el diseño sin iniciar conexiones a la base de datos. */
    Generar_Referencias_Bancarias(String usuario, boolean cargarCatalogos) {
        super(new BorderLayout(8, 8));
        this.usuario = usuario;
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JPanel filtros = new JPanel(new GridLayout(0, 1, 4, 4));
        JPanel generales = fila(new JLabel("Compañía"), cia, new JLabel("Centro Costos"), centro, new JLabel("Ciclo Escolar"), ciclo);
        generales.setBorder(BorderFactory.createTitledBorder("Datos Generales"));
        filtros.add(generales);
        matricula.setEditable(false);
        grado.addItem("Todos");
        JPanel seleccion = new JPanel(new GridLayout(0, 1));
        seleccion.setBorder(BorderFactory.createTitledBorder("Selección Opcional"));
        seleccion.add(fila(new JLabel("Matrícula"), matricula, buscarAlumno, todosAlumnos, new JLabel("Grado"), grado));
        seleccion.add(fila(nombreAlumno));
        filtros.add(seleccion);
        JPanel banco = fila(new JLabel("Cuenta Bancaria"), cuenta, new JLabel("Convenio CIE"), convenio);
        banco.setBorder(BorderFactory.createTitledBorder("Información Bancaria — BBVA BANCOMER"));
        filtros.add(banco);
        add(filtros, BorderLayout.NORTH);
        tabla.setRowHeight(24);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] anchos = {105, 220, 220, 80, 95, 95, 100, 200};
        for (int i = 0; i < anchos.length; i++) tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        avisos.setEditable(false);
        avisos.setLineWrap(true);
        avisos.setWrapStyleWord(true);
        JScrollPane panelAvisos = new JScrollPane(avisos);
        panelAvisos.setBorder(BorderFactory.createTitledBorder("Cargos omitidos / advertencias"));
        JSplitPane resultados = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tabla), panelAvisos);
        resultados.setResizeWeight(0.8);
        JPanel general = new JPanel(new BorderLayout(10, 10));
        general.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel indicacion = new JLabel("<html><center>Se generarán las referencias de los conceptos pendientes de pago.<br>Los conceptos con recargo lo incluirán automáticamente.</center></html>", SwingConstants.CENTER);
        indicacion.setForeground(new java.awt.Color(180, 30, 30));
        general.add(indicacion, BorderLayout.CENTER);
        JPanel modalidad = fila(new JLabel("Genera Referencia de Conceptos"), generar, imprimir);
        modalidad.setBorder(BorderFactory.createTitledBorder("Modalidad"));
        general.add(modalidad, BorderLayout.SOUTH);
        pestanas.addTab("Impresión General", general);
        pestanas.addTab("Referencias", resultados);
        add(pestanas, BorderLayout.CENTER);
        add(fila(estado), BorderLayout.SOUTH);
        imprimir.setEnabled(false);
        generar.setEnabled(false);
        cia.addActionListener(e -> { if (!actualizando) actualizarCentros(); });
        centro.addActionListener(e -> { if (!actualizando) actualizarCiclos(); });
        ciclo.addActionListener(e -> { if (!actualizando) cargarAlumnos(); });
        grado.addActionListener(e -> { if (!actualizando) limpiarAlumno(); });
        buscarAlumno.addActionListener(e -> buscarAlumno());
        todosAlumnos.addActionListener(e -> limpiarAlumno());
        javax.swing.event.DocumentListener cambios = new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { invalidar(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { invalidar(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { invalidar(); }
        };
        for (JTextField campo : List.of(matricula, cuenta, convenio)) campo.getDocument().addDocumentListener(cambios);
        generar.addActionListener(e -> generar(false));
        pestanas.addChangeListener(e -> {
            if (pestanas.getSelectedIndex() == 1 && seleccionLista && !trabajando) generar(true);
        });
        imprimir.addActionListener(e -> imprimir());
        if (cargarCatalogos) cargarCiclos();
    }

    private static JPanel fila(java.awt.Component... componentes) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        for (var componente : componentes) panel.add(componente);
        return panel;
    }

    private void invalidar() {
        imprimir.setEnabled(false);
        referenciasVista = List.of();
        filtroVista = null;
        guardadas = false;
        modelo.setRowCount(0);
        avisos.setText("");
        estado.setText("Seleccione los filtros y pulse Genera o Imprimir.");
    }

    private void cargarCiclos() {
        versionSeleccion++;
        seleccionLista = false;
        invalidar();
        ocupado(true);
        estado.setText("Consultando ciclos...");
        new SwingWorker<List<Ciclo>, Void>() {
            @Override protected List<Ciclo> doInBackground() throws Exception { return ReferenciasBancarias.ciclos(); }
            @Override protected void done() {
                try {
                    catalogo = get();
                    actualizando = true;
                    cia.removeAllItems();
                    catalogo.stream().map(Ciclo::cia).distinct().forEach(cia::addItem);
                    actualizando = false;
                    actualizarCentros();
                    estado.setText(catalogo.isEmpty() ? "No hay ciclos configurados." : "Seleccione filtros y genere el borrador.");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt(); error(ex);
                } catch (ExecutionException ex) { error(ex.getCause()); }
                finally { actualizando = false; ocupado(false); }
            }
        }.execute();
    }

    private void actualizarCentros() {
        actualizando = true;
        centro.removeAllItems();
        catalogo.stream().filter(c -> c.cia().equals(cia.getSelectedItem())).map(Ciclo::cc).distinct().forEach(centro::addItem);
        actualizando = false;
        actualizarCiclos();
    }

    private void actualizarCiclos() {
        actualizando = true;
        ciclo.removeAllItems();
        catalogo.stream().filter(c -> c.cia().equals(cia.getSelectedItem()) && c.cc().equals(centro.getSelectedItem()))
                .map(Ciclo::ciclo).distinct().forEach(ciclo::addItem);
        actualizando = false;
        cargarAlumnos();
    }

    private void limpiarAlumno() {
        matricula.setText("");
        nombreAlumno.setText("Todos los alumnos");
        invalidar();
    }

    private String gradoSeleccionado() {
        return grado.getSelectedIndex() <= 0 ? "" : (String) grado.getSelectedItem();
    }

    private void cargarAlumnos() {
        int version = ++versionSeleccion;
        seleccionLista = false;
        alumnos = List.of();
        limpiarAlumno();
        actualizando = true;
        grado.removeAllItems(); grado.addItem("Todos");
        actualizando = false;
        actualizarSeleccion();
        if (ciclo.getSelectedItem() == null) return;
        Ciclo seleccionado = new Ciclo((String) cia.getSelectedItem(), (String) centro.getSelectedItem(), (String) ciclo.getSelectedItem());
        estado.setText("Cargando alumnos y grados...");
        new SwingWorker<List<Alumno>, Void>() {
            @Override protected List<Alumno> doInBackground() throws Exception { return ReferenciasBancarias.alumnos(seleccionado); }
            @Override protected void done() {
                // Una consulta anterior no debe reemplazar los alumnos del ciclo actual.
                if (version != versionSeleccion) return;
                try {
                    alumnos = get();
                    actualizando = true;
                    alumnos.stream().map(Alumno::grado).filter(g -> !g.isBlank()).distinct().sorted().forEach(grado::addItem);
                    seleccionLista = true;
                    estado.setText(alumnos.isEmpty() ? "No hay alumnos con cargos en este ciclo." : "Seleccione alumno o grado, o genere para todos.");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt(); error(ex);
                } catch (ExecutionException ex) { error(ex.getCause()); }
                finally { actualizando = false; actualizarSeleccion(); }
            }
        }.execute();
    }

    private void actualizarSeleccion() {
        buscarAlumno.setEnabled(!trabajando && seleccionLista && !alumnos.isEmpty());
        todosAlumnos.setEnabled(!trabajando && seleccionLista);
        grado.setEnabled(!trabajando && seleccionLista);
        generar.setEnabled(!trabajando && seleccionLista && !catalogo.isEmpty() && usuario != null && !usuario.isBlank());
        imprimir.setEnabled(!trabajando && seleccionLista && !catalogo.isEmpty());
        pestanas.setEnabled(!trabajando);
    }

    private void buscarAlumno() {
        List<Alumno> opciones = alumnos.stream().filter(a -> gradoSeleccionado().isEmpty() || a.grado().equals(gradoSeleccionado())).toList();
        JDialog dialogo = new JDialog(SwingUtilities.getWindowAncestor(this), "Seleccionar alumno", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        JTextField busqueda = new JTextField(30);
        DefaultTableModel datos = new DefaultTableModel(new String[]{"Matrícula", "Apellidos y nombre", "Grado"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        for (Alumno alumno : opciones) datos.addRow(new Object[]{alumno.matricula(), alumno.nombre(), alumno.grado()});
        JTable lista = new JTable(datos);
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.getColumnModel().getColumn(1).setPreferredWidth(350);
        var ordenador = new javax.swing.table.TableRowSorter<DefaultTableModel>(datos);
        lista.setRowSorter(ordenador);
        JLabel cantidad = new JLabel();
        JButton seleccionar = new JButton("Seleccionar"), cancelar = new JButton("Cancelar");
        Runnable filtrar = () -> {
            ordenador.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entrada) {
                    return opciones.get(entrada.getIdentifier()).coincide(busqueda.getText());
                }
            });
            lista.clearSelection();
            cantidad.setText(lista.getRowCount() + " coincidencias");
        };
        busqueda.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrar.run(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrar.run(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar.run(); }
        });
        seleccionar.setEnabled(false);
        lista.getSelectionModel().addListSelectionListener(e -> seleccionar.setEnabled(lista.getSelectedRow() >= 0));
        seleccionar.addActionListener(e -> {
            if (lista.getSelectedRow() < 0) return;
            Alumno alumno = opciones.get(lista.convertRowIndexToModel(lista.getSelectedRow()));
            matricula.setText(alumno.matricula());
            nombreAlumno.setText(alumno.nombre());
            invalidar();
            dialogo.dispose();
        });
        lista.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { if (e.getClickCount() == 2) seleccionar.doClick(); }
        });
        cancelar.addActionListener(e -> dialogo.dispose());
        dialogo.getRootPane().setDefaultButton(seleccionar);
        dialogo.getRootPane().registerKeyboardAction(e -> dialogo.dispose(), KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialogo.add(fila(new JLabel("Matrícula o apellidos:"), busqueda), BorderLayout.NORTH);
        dialogo.add(new JScrollPane(lista), BorderLayout.CENTER);
        dialogo.add(fila(cantidad, seleccionar, cancelar), BorderLayout.SOUTH);
        dialogo.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialogo.setSize(720, 450);
        dialogo.setLocationRelativeTo(this);
        filtrar.run();
        SwingUtilities.invokeLater(busqueda::requestFocusInWindow);
        dialogo.setVisible(true);
    }

    private Filtro filtroActual() {
            if (ciclo.getSelectedItem() == null) throw new IllegalArgumentException("Seleccione un ciclo escolar.");
            if (!cuenta.getText().trim().matches("[0-9]{10}") || !convenio.getText().trim().matches("[0-9]{1,10}"))
                throw new IllegalArgumentException("Revise la cuenta (10 dígitos) y el convenio CIE numérico.");
            if (!matricula.getText().trim().matches("[0-9]*")) throw new IllegalArgumentException("La matrícula debe ser numérica.");
            Ciclo seleccionado = new Ciclo((String) cia.getSelectedItem(), (String) centro.getSelectedItem(), (String) ciclo.getSelectedItem());
            LocalDate fin = ReferenciasBancarias.vencimientoRecargo(seleccionado.ciclo());
            return new Filtro(seleccionado, matricula.getText().trim(), gradoSeleccionado(), true, fin);
    }

    private void mostrarResultado(Resultado resultado, Filtro filtro, boolean existentes) {
        referenciasVista = resultado.referencias(); filtroVista = filtro; guardadas = existentes;
        modelo.setRowCount(0);
        for (var r : resultado.referencias()) modelo.addRow(new Object[]{r.matricula(), r.nombre(), r.concepto(),
            r.tipo(), r.inicio(), r.fin(), r.importe().setScale(2).toPlainString(), r.referencia()});
        avisos.setText(String.join("\n", resultado.avisos()));
        avisos.setCaretPosition(0);
        estado.setText(resultado.referencias().size() + " referencias; " + resultado.avisos().size() + " advertencias (pestaña Referencias).");
    }

    private void generar(boolean consultarGuardadas) {
        invalidar();
        try {
            Filtro filtro = filtroActual();
            ocupado(true);
            estado.setText("Consultando cargos y calculando...");
            new SwingWorker<Resultado, Void>() {
                @Override protected Resultado doInBackground() throws Exception {
                    return consultarGuardadas ? ArchivoReferenciasBancarias.consultar(filtro) : ReferenciasBancarias.generar(filtro);
                }
                @Override protected void done() {
                    try {
                        Resultado resultado = get();
                        mostrarResultado(resultado, filtro, consultarGuardadas);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt(); error(ex);
                    } catch (ExecutionException ex) { error(ex.getCause()); }
                    finally { ocupado(false); }
                    if (!consultarGuardadas && !referenciasVista.isEmpty()) guardar();
                }
            }.execute();
        } catch (IllegalArgumentException ex) { error(ex); }
    }

    private void ocupado(boolean ocupado) {
        trabajando = ocupado;
        generar.setEnabled(!ocupado && !catalogo.isEmpty());
        for (var componente : List.of(cia, centro, ciclo, matricula, grado, cuenta, convenio)) componente.setEnabled(!ocupado);
        imprimir.setEnabled(false);
        actualizarSeleccion();
    }

    private void imprimir() {
        final Filtro filtro;
        try { filtro = filtroActual(); }
        catch (IllegalArgumentException ex) { error(ex); return; }
        ocupado(true);
        estado.setText("Verificando referencias guardadas y saldos antes de imprimir...");
        new SwingWorker<Resultado, Void>() {
            @Override protected Resultado doInBackground() throws Exception { return ArchivoReferenciasBancarias.consultar(filtro); }
            @Override protected void done() {
                try {
                    Resultado vigente = get();
                    if (guardadas && !vigente.referencias().equals(referenciasVista)) {
                        invalidar();
                        throw new IllegalArgumentException("Cambió el saldo, estado o configuración. Revise la pestaña Referencias antes de imprimir.");
                    }
                    mostrarResultado(vigente, filtro, true);
                    if (referenciasVista.isEmpty()) throw new IllegalArgumentException("No hay referencias guardadas compatibles. Pulse Genera y revise las advertencias.");
                    imprimirVerificadas();
                } catch (InterruptedException ex) { Thread.currentThread().interrupt(); error(ex); }
                catch (ExecutionException ex) { error(ex.getCause()); }
                catch (IllegalArgumentException ex) { error(ex); }
                finally { ocupado(false); }
            }
        }.execute();
    }

    private void imprimirVerificadas() {
        try {
            java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
            java.awt.print.PageFormat pagina = job.defaultPage();
            pagina.setOrientation(java.awt.print.PageFormat.PORTRAIT);
            job.setPrintable(ficha(true), pagina);
            if (job.printDialog()) job.print();
        } catch (java.awt.print.PrinterException ex) { error(ex); }
    }

    private void guardar() {
        if (filtroVista == null || referenciasVista.isEmpty()) return;
        if (JOptionPane.showConfirmDialog(this, "Se guardarán " + referenciasVista.size()
                + " referencias en tesrefb con el usuario " + usuario + ".\nNo registra pagos ni reemplaza referencias existentes.\n"
                + "Los cargos con advertencias quedan excluidos. ¿Continuar?", "Guardar referencias", JOptionPane.YES_NO_OPTION)
                != JOptionPane.YES_OPTION) return;
        ocupado(true);
        estado.setText("Validando y guardando referencias...");
        new SwingWorker<ArchivoReferenciasBancarias.Guardado, Void>() {
            @Override protected ArchivoReferenciasBancarias.Guardado doInBackground() throws Exception {
                return ArchivoReferenciasBancarias.guardar(filtroVista, referenciasVista, usuario);
            }
            @Override protected void done() {
                try {
                    var resultado = get(); guardadas = true;
                    estado.setText("Generadas: " + resultado.nuevas() + "; ya existentes: " + resultado.existentes() + ". Puede imprimir. Revise advertencias en Referencias.");
                } catch (InterruptedException ex) { Thread.currentThread().interrupt(); error(ex); }
                catch (ExecutionException ex) { error(ex.getCause()); }
                finally { ocupado(false); }
            }
        }.execute();
    }

    private FichaReferenciasBancarias ficha(boolean definitiva) {
        return new FichaReferenciasBancarias(referenciasVista, cuenta.getText().trim(), convenio.getText().trim(), filtroVista.ciclo().ciclo(), definitiva);
    }

    private void error(Throwable ex) {
        estado.setText("No se pudo completar la operación.");
        JOptionPane.showMessageDialog(this, ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage(),
                "Referencias bancarias", JOptionPane.ERROR_MESSAGE);
    }
}
