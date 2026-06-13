/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.benaedu;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMaterialLighterIJTheme;
import com.mycompany.benaedu.views.Companias;
import com.mycompany.benaedu.views.Principal;
import com.mycompany.benaedu.views.Centros_costos;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;


/**
 *
 * @author b17za
 */
public class Dashboard extends javax.swing.JFrame {

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Dashboard.class.getName());
    // Mapa que guardará el nombre de la opción y la acción que debe ejecutar
 private final java.util.Map<String, Runnable> opcionesSistema = new java.util.LinkedHashMap<>();
    
    // El menú flotante que mostrará los resultados de búsqueda
    private final javax.swing.JPopupMenu menuResultadosBusqueda = new javax.swing.JPopupMenu();
    private String usuarioActual = "Usuario";
    
    private int indiceSeleccionadoBusqueda = -1;
    
    public Dashboard(String nombreUsuario) {
     this.usuarioActual = nombreUsuario;
        initComponents();
        InitStyles();
        configurarFechaYHora();
        configurarMensajeBienvenida();
        initContent();
        
        // Configuramos los 3 módulos principales
        configurarMenuGeneral();
        configurarMenuContabilidad();
        configurarMenuEscolar();
        
        configurarBuscador();
    }

    /**
     * Creates new form Dashboard
     */
    public Dashboard() {
      initComponents();
        InitStyles();
        configurarFechaYHora();
        configurarMensajeBienvenida();
        initContent();
        
        // Configuramos los 3 módulos principales
        configurarMenuGeneral();
        configurarMenuContabilidad();
        configurarMenuEscolar();
        
        configurarBuscador();
    }

    private void InitStyles() {
        lblMessage.putClientProperty("FlatLaf.style", "font: bold 30");
        lblMessage.setForeground(new Color(179,207,229));
        lblDate.putClientProperty("FlatLaf.style", "font: bold 30 ");
        lblDate.setForeground(new Color(179,207,229));
        lblTime.putClientProperty("FlatLaf.style", "font: bold 30 ");
        lblTime.setForeground(new Color(179,207,229));
        lblTitleInfo.putClientProperty("FlatLaf.style", "font: bold 24");
        lblTitleInfo.setForeground(new Color(179,207,229));

    }

  private void configurarMenuGeneral() {
        btnInfoMaestra.setText("General"); // Cambiamos el nombre del botón
        javax.swing.JPopupMenu menuGeneral = new javax.swing.JPopupMenu();

        // Categorías
        javax.swing.JMenu menuInfoMaestra = new javax.swing.JMenu("20 Información Maestra");
        javax.swing.JMenu menuTransacciones = new javax.swing.JMenu("30 Transacciones");

        // Opciones "20 Información Maestra"
        javax.swing.JMenuItem iCompanias = new javax.swing.JMenuItem("Catálogo de Compañías");
        javax.swing.JMenuItem iCentros = new javax.swing.JMenuItem("Catálogo de Centros de Costo");
        javax.swing.JMenuItem iParametros = new javax.swing.JMenuItem("Parámetros por Compañía");
        javax.swing.JMenuItem iFiscal = new javax.swing.JMenuItem("Ejercicio Fiscal");
        javax.swing.JMenuItem iEmpleados = new javax.swing.JMenuItem("Catálogo de Empleados");
        javax.swing.JMenuItem iDirecciones = new javax.swing.JMenuItem("Direcciones de Entrega");
        javax.swing.JMenuItem iClasificaciones = new javax.swing.JMenuItem("Tabla de Clasificaciones");
        javax.swing.JMenuItem iContactos = new javax.swing.JMenuItem("Catálogo de Contactos");

        menuInfoMaestra.add(iCompanias); menuInfoMaestra.add(iCentros); menuInfoMaestra.add(iParametros);
        menuInfoMaestra.add(iFiscal); menuInfoMaestra.add(iEmpleados); menuInfoMaestra.add(iDirecciones);
        menuInfoMaestra.add(iClasificaciones); menuInfoMaestra.add(iContactos);

        // Opciones "30 Transacciones"
        javax.swing.JMenuItem iCambioD = new javax.swing.JMenuItem("Tipos de Cambio Diario");
        javax.swing.JMenuItem iCondiciones = new javax.swing.JMenuItem("Condiciones de Pago");
        javax.swing.JMenuItem iImpuestos = new javax.swing.JMenuItem("Clave de Impuestos");
        javax.swing.JMenuItem iCambioM = new javax.swing.JMenuItem("Tipo de Cambio Mensual");
        javax.swing.JMenuItem iUnidad = new javax.swing.JMenuItem("Convertir Unidad de Medida");

        menuTransacciones.add(iCambioD); menuTransacciones.add(iCondiciones); menuTransacciones.add(iImpuestos);
        menuTransacciones.add(iCambioM); menuTransacciones.add(iUnidad);

        menuGeneral.add(menuInfoMaestra);
        menuGeneral.add(menuTransacciones);

        // Eventos y Hover
        btnInfoMaestra.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                menuGeneral.show(btnInfoMaestra, 0, btnInfoMaestra.getHeight());
            }
        });

        java.awt.event.MouseAdapter evtOcultar = crearEventoOcultar();
        btnInfoMaestra.addMouseListener(evtOcultar); menuGeneral.addMouseListener(evtOcultar);
        menuInfoMaestra.addMouseListener(evtOcultar); menuTransacciones.addMouseListener(evtOcultar);

        // Acciones
        iCompanias.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Companias(), "Catálogo de Compañías"));
        iCentros.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Centros_costos(), "Catálogo de Centros de Costo"));
        iParametros.addActionListener(e -> javax.swing.JOptionPane.showMessageDialog(this, "Módulo en construcción...")); // FALTANTE
        iFiscal.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Ejercicio_Fiscal(), "Ejercicio Fiscal"));
        iEmpleados.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Catalogo_Empleados(), "Catálogo de Empleados"));
        iDirecciones.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Direcciones_Entrega(), "Direcciones de Entrega"));
        iClasificaciones.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Clasificaciones(), "Tabla de Clasificaciones"));
        iContactos.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Catalogo_Contactos(), "Catálogo de Contactos"));
        
        iCambioD.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Tipos_Cambio_Diario(), "Tipos de Cambio Diario"));
        iCondiciones.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Condiciones_Pago(), "Condiciones de Pago"));
        iImpuestos.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Tipos_Impuestos(), "Clave de Impuestos"));
        iCambioM.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Tipo_Cambio(), "Tipo de Cambio Mensual"));
        iUnidad.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Factores_Convercion_Unidad(), "Convertir Unidad de Medida"));
    }

    private void configurarMenuContabilidad() {
        btnTransacciones.setText("Contabilidad"); // Cambiamos el nombre del botón
        javax.swing.JPopupMenu menuContab = new javax.swing.JPopupMenu();

        javax.swing.JMenu m20 = new javax.swing.JMenu("20 Información Maestra");
        javax.swing.JMenu m30 = new javax.swing.JMenu("30 Transacciones");
        javax.swing.JMenu m40 = new javax.swing.JMenu("40 Consultas");
        javax.swing.JMenu m50 = new javax.swing.JMenu("50 Reportes");
        javax.swing.JMenu m60 = new javax.swing.JMenu("60 Cierre Mensual");
        javax.swing.JMenu m70 = new javax.swing.JMenu("70 Cierre Anual");
        javax.swing.JMenu m80 = new javax.swing.JMenu("80 Utilerías");

        // 20
        javax.swing.JMenuItem iCtas = new javax.swing.JMenuItem("Catálogo de Cuentas"); m20.add(iCtas);
        // 30
        javax.swing.JMenuItem iPolizas = new javax.swing.JMenuItem("Captura de Pólizas"); m30.add(iPolizas);
        // 40
        javax.swing.JMenuItem iAux = new javax.swing.JMenuItem("Auxiliar de Movimientos"); 
        javax.swing.JMenuItem iPolDia = new javax.swing.JMenuItem("Póliza de Diario"); 
        m40.add(iAux); m40.add(iPolDia);
        // 50
        javax.swing.JMenuItem iBalanza = new javax.swing.JMenuItem("Balanza de Comprobación");
        javax.swing.JMenuItem iBalanzaSsc = new javax.swing.JMenuItem("Balanza de Comprobación Ssc");
        javax.swing.JMenuItem iBalance = new javax.swing.JMenuItem("Balance por Compañía");
        javax.swing.JMenuItem iResGpo = new javax.swing.JMenuItem("Estado de Resultados x Grupo");
        javax.swing.JMenuItem iRes = new javax.swing.JMenuItem("Estado de Resultados");
        javax.swing.JMenuItem iLDiario = new javax.swing.JMenuItem("Libro de Diario");
        javax.swing.JMenuItem iLMayor = new javax.swing.JMenuItem("Libro Mayor");
        javax.swing.JMenuItem iImpPol = new javax.swing.JMenuItem("Impresión de Póliza");
        m50.add(iBalanza); m50.add(iBalanzaSsc); m50.add(iBalance); m50.add(iResGpo); 
        m50.add(iRes); m50.add(iLDiario); m50.add(iLMayor); m50.add(iImpPol);
        // 60, 70, 80
        javax.swing.JMenuItem iCMes = new javax.swing.JMenuItem("Cierre de Mes"); m60.add(iCMes);
        javax.swing.JMenuItem iCAnual = new javax.swing.JMenuItem("Cierre Anual"); m70.add(iCAnual);
        javax.swing.JMenuItem iAct = new javax.swing.JMenuItem("Actualización de Saldos"); m80.add(iAct);

        menuContab.add(m20); menuContab.add(m30); menuContab.add(m40); menuContab.add(m50);
        menuContab.add(m60); menuContab.add(m70); menuContab.add(m80);

        btnTransacciones.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { menuContab.show(btnTransacciones, 0, btnTransacciones.getHeight()); }
        });

        java.awt.event.MouseAdapter evtOcultar = crearEventoOcultar();
        btnTransacciones.addMouseListener(evtOcultar); menuContab.addMouseListener(evtOcultar);
        m20.addMouseListener(evtOcultar); m30.addMouseListener(evtOcultar); m40.addMouseListener(evtOcultar);
        m50.addMouseListener(evtOcultar); m60.addMouseListener(evtOcultar); m70.addMouseListener(evtOcultar); m80.addMouseListener(evtOcultar);

        // Acciones
        iCtas.addActionListener(e -> javax.swing.JOptionPane.showMessageDialog(this, "Módulo en construcción...")); // FALTANTE
        iPolizas.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Captura_Polizas(), "Captura de Pólizas"));
        iAux.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Auxiliar_Movimientos(), "Auxiliar de Movimientos"));
        iPolDia.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Poliza_Diario(), "Póliza de Diario"));
        iBalanza.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Balanza_Comprobacion(), "Balanza de Comprobación"));
        iBalanzaSsc.addActionListener(e -> javax.swing.JOptionPane.showMessageDialog(this, "Módulo en construcción...")); // FALTANTE
        iBalance.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Balance_Compañia(), "Balance por Compañía"));
        iResGpo.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Resultado_Grupo(), "Estado de Resultados x Grupo"));
        iRes.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Estado_Resultados(), "Estado de Resultados"));
        iLDiario.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Libro_Diario(), "Libro de Diario"));
        iLMayor.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Libro_Mayor(), "Libro Mayor"));
        iImpPol.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Impresion_Poliza(), "Impresión de Póliza"));
        iCMes.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Cierre_Mes(), "Cierre de Mes"));
        iCAnual.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Cierre_Anual(), "Cierre Anual"));
        iAct.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Actualizacion_Saldo(), "Actualización de Saldos"));
    }

    private void configurarMenuEscolar() {
        btnEscolar.setText("Escolar");
        javax.swing.JPopupMenu menuEscolar = new javax.swing.JPopupMenu();

        javax.swing.JMenu m10 = new javax.swing.JMenu("10 Configuración");
        javax.swing.JMenu m20 = new javax.swing.JMenu("20 Información Maestra");
        javax.swing.JMenu m30 = new javax.swing.JMenu("30 Transacciones");
        javax.swing.JMenu m40 = new javax.swing.JMenu("40 Consultas");
        javax.swing.JMenu m50 = new javax.swing.JMenu("50 Reportes");

        // 10
        javax.swing.JMenuItem iCiclos = new javax.swing.JMenuItem("Ciclos Escolares");
        javax.swing.JMenuItem iGrados = new javax.swing.JMenuItem("Grados Escolares");
        javax.swing.JMenuItem iBecas = new javax.swing.JMenuItem("Registro de Becas y Convenios");
        javax.swing.JMenuItem iConEsc = new javax.swing.JMenuItem("Conceptos Escolares");
        javax.swing.JMenuItem iPlanes = new javax.swing.JMenuItem("Planes de Pago");
        m10.add(iCiclos); m10.add(iGrados); m10.add(iBecas); m10.add(iConEsc); m10.add(iPlanes);

        // 20
        javax.swing.JMenuItem iCtasBan = new javax.swing.JMenuItem("Cuentas Bancarias");
        javax.swing.JMenuItem iTiposAl = new javax.swing.JMenuItem("Catalogo Tipos de Alumno");
        javax.swing.JMenuItem iCajeros = new javax.swing.JMenuItem("Catalogo de Cajeros");
        javax.swing.JMenuItem iAlumnos = new javax.swing.JMenuItem("Catalogo de Alumnos");
        m20.add(iCtasBan); m20.add(iTiposAl); m20.add(iCajeros); m20.add(iAlumnos);

        // 30
        javax.swing.JMenuItem iCobranza = new javax.swing.JMenuItem("Cobranza Escolar");
        javax.swing.JMenuItem iCorte = new javax.swing.JMenuItem("Corte de Caja");
        javax.swing.JMenuItem iFacturas = new javax.swing.JMenuItem("Impresión de Facturas");
        javax.swing.JMenuItem iDep = new javax.swing.JMenuItem("Registro de Depositos");
        javax.swing.JMenuItem iCanCon = new javax.swing.JMenuItem("Cancelación de Conceptos");
        javax.swing.JMenuItem iCanRec = new javax.swing.JMenuItem("Cancelación de Recibos");
        javax.swing.JMenuItem iCanCorte = new javax.swing.JMenuItem("Cancela Contabilizacion de Corte de Caja");
        javax.swing.JMenuItem iCanDep = new javax.swing.JMenuItem("Cancela Contabilizacion de Reg Deposito");
        m30.add(iCobranza); m30.add(iCorte); m30.add(iFacturas); m30.add(iDep); 
        m30.add(iCanCon); m30.add(iCanRec); m30.add(iCanCorte); m30.add(iCanDep);

        // 40
        javax.swing.JMenuItem iEdoAl = new javax.swing.JMenuItem("Estado de Cuenta Alumnos");
        javax.swing.JMenuItem iConsPag = new javax.swing.JMenuItem("Consulta de Pagos");
        javax.swing.JMenuItem iEdoDet = new javax.swing.JMenuItem("Estado de Cuenta Detallado");
        m40.add(iEdoAl); m40.add(iConsPag); m40.add(iEdoDet);

        // 50
        javax.swing.JMenuItem iResIns = new javax.swing.JMenuItem("Resumen de Inscripciones");
        javax.swing.JMenuItem iResNoIns = new javax.swing.JMenuItem("Resumen de Alumnos No Inscritos");
        javax.swing.JMenuItem iReiRec = new javax.swing.JMenuItem("Reimpresión de Recibos");
        javax.swing.JMenuItem iReiFac = new javax.swing.JMenuItem("Reimpresión de Facturas");
        javax.swing.JMenuItem iResCon = new javax.swing.JMenuItem("Resumen de Conceptos Pagados");
        javax.swing.JMenuItem iDetCon = new javax.swing.JMenuItem("Detalle de Conceptos Pagados");
        javax.swing.JMenuItem iIngPer = new javax.swing.JMenuItem("Ingresos por Periodo");
        m50.add(iResIns); m50.add(iResNoIns); m50.add(iReiRec); m50.add(iReiFac); 
        m50.add(iResCon); m50.add(iDetCon); m50.add(iIngPer);

        menuEscolar.add(m10); menuEscolar.add(m20); menuEscolar.add(m30); menuEscolar.add(m40); menuEscolar.add(m50);

        btnEscolar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { menuEscolar.show(btnEscolar, 0, btnEscolar.getHeight()); }
        });

        java.awt.event.MouseAdapter evtOcultar = crearEventoOcultar();
        btnEscolar.addMouseListener(evtOcultar); menuEscolar.addMouseListener(evtOcultar);
        m10.addMouseListener(evtOcultar); m20.addMouseListener(evtOcultar); m30.addMouseListener(evtOcultar); m40.addMouseListener(evtOcultar); m50.addMouseListener(evtOcultar);

        // Acciones
        iCiclos.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Ciclos_Escolares(), "Ciclos Escolares"));
        iGrados.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Grados_Escolares(), "Grados Escolares"));
        iBecas.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Registro_Becas(), "Registro de Becas y Convenios"));
        iConEsc.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Conceptos_Escolares(), "Conceptos Escolares"));
        iPlanes.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Planes_Pago(), "Planes de Pago"));
        
        iCtasBan.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Cuentas_Bancarias(), "Cuentas Bancarias"));
        iTiposAl.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Catalogo_Tipo_Alumno(), "Catalogo Tipos de Alumno"));
        iCajeros.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Catalogo_Cajeros(), "Catalogo de Cajeros"));
        iAlumnos.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Catalogo_Alumnos(), "Catalogo de Alumnos"));

        iCobranza.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Cobranza_Escolar(), "Cobranza Escolar"));
        iCorte.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Corte_Caja(), "Corte de Caja"));
        iFacturas.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Impresion_Facturas(), "Impresión de Facturas"));
        iDep.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Registro_Depositos(), "Registro de Depositos"));
        iCanCon.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Cancelacion_Conceptos(), "Cancelación de Conceptos"));
        iCanRec.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Cancelacion_Recibos(), "Cancelación de Recibos"));
        iCanCorte.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Cancela_Contabilizacion_Corte(), "Cancela Contabilizacion de Corte de Caja"));
        iCanDep.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Cancela_Contabilizacion_Deposito(), "Cancela Contabilizacion de Reg Deposito"));

        iEdoAl.addActionListener(e -> javax.swing.JOptionPane.showMessageDialog(this, "Módulo en construcción...")); // FALTANTE
        iConsPag.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Consulta_Pagos(), "Consulta de Pagos"));
        iEdoDet.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Estado_Cuenta_Detallado(), "Estado de Cuenta Detallado"));

        iResIns.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Resumen_Inscripciones(), "Resumen de Inscripciones"));
        iResNoIns.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Resumen_Alumnos_No_Inscritos(), "Resumen de Alumnos No Inscritos"));
        iReiRec.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Reeimpresion_Recibos(), "Reimpresión de Recibos"));
        iReiFac.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Reeimpresion_Facturas(), "Reimpresión de Facturas"));
        iResCon.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Resumen_Conceptos_Pagados(), "Resumen de Conceptos Pagados"));
        iDetCon.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Detalle_Conceptos_Pagos(), "Detalle de Conceptos Pagados"));
        iIngPer.addActionListener(e -> mostrarPanel(new com.mycompany.benaedu.views.Ingresos_Periodo(), "Ingresos por Periodo"));
    }

    private void configurarFechaYHora() {
        // --- 1. CONFIGURAR LA FECHA ESTATICA (lblDate) ---
        java.time.LocalDate fechaActual = java.time.LocalDate.now();

        // Creamos el formato en español. 
        // EEEE = Día de la semana, dd = número, MMMM = mes en texto, yyyy = año
        java.time.format.DateTimeFormatter formatoFecha = java.time.format.DateTimeFormatter.ofPattern(
                "'Hoy es' EEEE dd 'de' MMMM 'del' yyyy",
                new java.util.Locale("es", "MX") // Forzamos el idioma español
        );

        // Al aplicar el formato, la primera letra del día suele salir en minúscula. 
        // Este pequeño truco pone la primera letra en mayúscula para que se vea mejor:
        String fechaFormateada = fechaActual.format(formatoFecha);
        fechaFormateada = fechaFormateada.substring(0, 7)
                + fechaFormateada.substring(7, 8).toUpperCase()
                + fechaFormateada.substring(8);

        lblDate.setText(fechaFormateada);

        // --- 2. CONFIGURAR EL RELOJ EN VIVO (lblTime) ---
        // Creamos un temporizador que se repite cada 1000 milisegundos (1 segundo)
        javax.swing.Timer timerReloj = new javax.swing.Timer(1000, e -> {
            java.time.LocalTime horaActual = java.time.LocalTime.now();

            // Formato de 12 horas con AM/PM (Ejemplo: "02:15:30 PM")
            // Si prefieres formato de 24 horas, cambia "hh:mm:ss a" por "HH:mm:ss"
            java.time.format.DateTimeFormatter formatoHora = java.time.format.DateTimeFormatter.ofPattern("hh:mm:ss a");

            lblTime.setText(horaActual.format(formatoHora));
        });

        // Iniciamos el reloj
        timerReloj.start();
    }
    private void configurarMensajeBienvenida() {
        // Obtenemos la hora actual de la computadora
        java.time.LocalTime horaActual = java.time.LocalTime.now();
        int hora = horaActual.getHour(); // Formato 24 hrs (0 a 23)
        String saludo;

        // Calculamos el saludo
        if (hora >= 5 && hora < 12) {
            saludo = "Buenos días";
        } else if (hora >= 12 && hora < 19) {
            saludo = "Buenas tardes";
        } else {
            saludo = "Buenas noches";
        }

        // Actualizamos la etiqueta (lblMessage)
        lblMessage.setText(saludo + ", " + usuarioActual);
    }
    
 



  private void initContent() {
        Principal pl = new Principal();
        
        // 1. ELIMINAR el tamaño fijo y la posición. El Layout se encargará de esto.
        // pl.setSize(750,430); 
        // pl.setLocation(0, 0);
        
        // 2. FORZAR a jpContainer a usar BorderLayout para que el panel se expanda
        jpContainer.setLayout(new java.awt.BorderLayout());
        
        // 3. Limpiar, agregar en el CENTRO, y recargar la vista
        jpContainer.removeAll();
        jpContainer.add(pl, java.awt.BorderLayout.CENTER);
        jpContainer.revalidate();
        jpContainer.repaint();
        
        mostrarPanel(pl, "Principal");
    }
  
  // Este método recibe cualquier JPanel y lo muestra en tu jpContainer
    private void mostrarPanel(javax.swing.JPanel p, String titulo) {
        jpContainer.removeAll();
        jpContainer.add(p, java.awt.BorderLayout.CENTER);
        jpContainer.revalidate();
        jpContainer.repaint();
        
        lblTitleInfo.setText(titulo);
    }
    
   private void configurarBuscador() {

   menuResultadosBusqueda.setFocusable(false);
        opcionesSistema.clear(); // Limpiar por si acaso

        // --- GENERAL ---
        opcionesSistema.put("Catálogo de Compañías", () -> mostrarPanel(new com.mycompany.benaedu.views.Companias(), "Catálogo de Compañías"));
        opcionesSistema.put("Catálogo de Centros de Costo", () -> mostrarPanel(new com.mycompany.benaedu.views.Centros_costos(), "Catálogo de Centros de Costo"));
        opcionesSistema.put("Parámetros por Compañía", () -> javax.swing.JOptionPane.showMessageDialog(this, "Módulo en construcción..."));
        opcionesSistema.put("Ejercicio Fiscal", () -> mostrarPanel(new com.mycompany.benaedu.views.Ejercicio_Fiscal(),"Ejercicio Fiscal"));
        opcionesSistema.put("Catálogo de Empleados", () -> mostrarPanel(new com.mycompany.benaedu.views.Catalogo_Empleados(),"Catálogo de Empleados"));
        opcionesSistema.put("Direcciones de Entrega", () -> mostrarPanel(new com.mycompany.benaedu.views.Direcciones_Entrega(),"Direcciones de Entrega"));
        opcionesSistema.put("Tabla de Clasificaciones", () -> mostrarPanel(new com.mycompany.benaedu.views.Clasificaciones(),"Tabla de Clasificaciones"));
        opcionesSistema.put("Catálogo de Contactos", () -> mostrarPanel(new com.mycompany.benaedu.views.Catalogo_Contactos(),"Catálogo de Contactos"));
        opcionesSistema.put("Tipos de Cambio Diario", () -> mostrarPanel(new com.mycompany.benaedu.views.Tipos_Cambio_Diario(), "Tipos de Cambio Diario"));
        opcionesSistema.put("Condiciones de Pago", () -> mostrarPanel(new com.mycompany.benaedu.views.Condiciones_Pago(), "Condiciones de Pago"));
        opcionesSistema.put("Clave de Impuestos", () -> mostrarPanel(new com.mycompany.benaedu.views.Tipos_Impuestos(), "Clave de Impuestos"));
        opcionesSistema.put("Tipo de Cambio Mensual", () -> mostrarPanel(new com.mycompany.benaedu.views.Tipo_Cambio(), "Tipo de Cambio Mensual"));
        opcionesSistema.put("Convertir Unidad de Medida", () -> mostrarPanel(new com.mycompany.benaedu.views.Factores_Convercion_Unidad(), "Convertir Unidad de Medida"));

        // --- CONTABILIDAD ---
        opcionesSistema.put("Catálogo de Cuentas", () -> javax.swing.JOptionPane.showMessageDialog(this, "Módulo en construcción..."));
        opcionesSistema.put("Captura de Pólizas", () -> mostrarPanel(new com.mycompany.benaedu.views.Captura_Polizas(), "Captura de Pólizas"));
        opcionesSistema.put("Auxiliar de Movimientos", () -> mostrarPanel(new com.mycompany.benaedu.views.Auxiliar_Movimientos(), "Auxiliar de Movimientos"));
        opcionesSistema.put("Póliza de Diario", () -> mostrarPanel(new com.mycompany.benaedu.views.Poliza_Diario(), "Póliza de Diario"));
        opcionesSistema.put("Balanza de Comprobación", () -> mostrarPanel(new com.mycompany.benaedu.views.Balanza_Comprobacion(), "Balanza de Comprobación"));
        opcionesSistema.put("Balanza de Comprobación Ssc", () -> javax.swing.JOptionPane.showMessageDialog(this, "Módulo en construcción..."));
        opcionesSistema.put("Balance por Compañía", () -> mostrarPanel(new com.mycompany.benaedu.views.Balance_Compañia(), "Balance por Compañía"));
        opcionesSistema.put("Estado de Resultados x Grupo", () -> mostrarPanel(new com.mycompany.benaedu.views.Resultado_Grupo(), "Estado de Resultados x Grupo"));
        opcionesSistema.put("Estado de Resultados", () -> mostrarPanel(new com.mycompany.benaedu.views.Estado_Resultados(), "Estado de Resultados"));
        opcionesSistema.put("Libro de Diario", () -> mostrarPanel(new com.mycompany.benaedu.views.Libro_Diario(), "Libro de Diario"));
        opcionesSistema.put("Libro Mayor", () -> mostrarPanel(new com.mycompany.benaedu.views.Libro_Mayor(), "Libro Mayor"));
        opcionesSistema.put("Impresión de Póliza", () -> mostrarPanel(new com.mycompany.benaedu.views.Impresion_Poliza(), "Impresión de Póliza"));
        opcionesSistema.put("Cierre de Mes", () -> mostrarPanel(new com.mycompany.benaedu.views.Cierre_Mes(), "Cierre de Mes"));
        opcionesSistema.put("Cierre Anual", () -> mostrarPanel(new com.mycompany.benaedu.views.Cierre_Anual(), "Cierre Anual"));
        opcionesSistema.put("Actualización de Saldos", () -> mostrarPanel(new com.mycompany.benaedu.views.Actualizacion_Saldo(), "Actualización de Saldos"));

        // --- ESCOLAR ---
        opcionesSistema.put("Ciclos Escolares", () -> mostrarPanel(new com.mycompany.benaedu.views.Ciclos_Escolares(), "Ciclos Escolares"));
        opcionesSistema.put("Grados Escolares", () -> mostrarPanel(new com.mycompany.benaedu.views.Grados_Escolares(), "Grados Escolares"));
        opcionesSistema.put("Registro de Becas y Convenios", () -> mostrarPanel(new com.mycompany.benaedu.views.Registro_Becas(), "Registro de Becas y Convenios"));
        opcionesSistema.put("Conceptos Escolares", () -> mostrarPanel(new com.mycompany.benaedu.views.Conceptos_Escolares(), "Conceptos Escolares"));
        opcionesSistema.put("Planes de Pago", () -> mostrarPanel(new com.mycompany.benaedu.views.Planes_Pago(), "Planes de Pago"));
        opcionesSistema.put("Cuentas Bancarias", () -> mostrarPanel(new com.mycompany.benaedu.views.Cuentas_Bancarias(), "Cuentas Bancarias"));
        opcionesSistema.put("Catalogo Tipos de Alumno", () -> mostrarPanel(new com.mycompany.benaedu.views.Catalogo_Tipo_Alumno(), "Catalogo Tipos de Alumno"));
        opcionesSistema.put("Catalogo de Cajeros", () -> mostrarPanel(new com.mycompany.benaedu.views.Catalogo_Cajeros(), "Catalogo de Cajeros"));
        opcionesSistema.put("Catalogo de Alumnos", () -> mostrarPanel(new com.mycompany.benaedu.views.Catalogo_Alumnos(), "Catalogo de Alumnos"));
        opcionesSistema.put("Cobranza Escolar", () -> mostrarPanel(new com.mycompany.benaedu.views.Cobranza_Escolar(), "Cobranza Escolar"));
        opcionesSistema.put("Corte de Caja", () -> mostrarPanel(new com.mycompany.benaedu.views.Corte_Caja(), "Corte de Caja"));
        opcionesSistema.put("Impresión de Facturas", () -> mostrarPanel(new com.mycompany.benaedu.views.Impresion_Facturas(), "Impresión de Facturas"));
        opcionesSistema.put("Registro de Depositos", () -> mostrarPanel(new com.mycompany.benaedu.views.Registro_Depositos(), "Registro de Depositos"));
        opcionesSistema.put("Cancelación de Conceptos", () -> mostrarPanel(new com.mycompany.benaedu.views.Cancelacion_Conceptos(), "Cancelación de Conceptos"));
        opcionesSistema.put("Cancelación de Recibos", () -> mostrarPanel(new com.mycompany.benaedu.views.Cancelacion_Recibos(), "Cancelación de Recibos"));
        opcionesSistema.put("Cancela Contabilizacion de Corte de Caja", () -> mostrarPanel(new com.mycompany.benaedu.views.Cancela_Contabilizacion_Corte(), "Cancela Contabilizacion de Corte de Caja"));
        opcionesSistema.put("Cancela Contabilizacion de Reg Deposito", () -> mostrarPanel(new com.mycompany.benaedu.views.Cancela_Contabilizacion_Deposito(), "Cancela Contabilizacion de Reg Deposito"));
        opcionesSistema.put("Estado de Cuenta Alumnos", () -> javax.swing.JOptionPane.showMessageDialog(this, "Módulo en construcción..."));
        opcionesSistema.put("Consulta de Pagos", () -> mostrarPanel(new com.mycompany.benaedu.views.Consulta_Pagos(), "Consulta de Pagos"));
        opcionesSistema.put("Estado de Cuenta Detallado", () -> mostrarPanel(new com.mycompany.benaedu.views.Estado_Cuenta_Detallado(), "Estado de Cuenta Detallado"));
        opcionesSistema.put("Resumen de Inscripciones", () -> mostrarPanel(new com.mycompany.benaedu.views.Resumen_Inscripciones(), "Resumen de Inscripciones"));
        opcionesSistema.put("Resumen de Alumnos No Inscritos", () -> mostrarPanel(new com.mycompany.benaedu.views.Resumen_Alumnos_No_Inscritos(), "Resumen de Alumnos No Inscritos"));
        opcionesSistema.put("Reimpresión de Recibos", () -> mostrarPanel(new com.mycompany.benaedu.views.Reeimpresion_Recibos(), "Reimpresión de Recibos"));
        opcionesSistema.put("Reimpresión de Facturas", () -> mostrarPanel(new com.mycompany.benaedu.views.Reeimpresion_Facturas(), "Reimpresión de Facturas"));
        opcionesSistema.put("Resumen de Conceptos Pagados", () -> mostrarPanel(new com.mycompany.benaedu.views.Resumen_Conceptos_Pagados(), "Resumen de Conceptos Pagados"));
        opcionesSistema.put("Detalle de Conceptos Pagados", () -> mostrarPanel(new com.mycompany.benaedu.views.Detalle_Conceptos_Pagos(), "Detalle de Conceptos Pagados"));
        opcionesSistema.put("Ingresos por Periodo", () -> mostrarPanel(new com.mycompany.benaedu.views.Ingresos_Periodo(), "Ingresos por Periodo"));

    txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            buscarOpciones(txtSearch.getText());
        }

        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            buscarOpciones(txtSearch.getText());
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            buscarOpciones(txtSearch.getText());
        }
    });

    txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
        @Override
        public void keyPressed(java.awt.event.KeyEvent evt) {

            int key = evt.getKeyCode();

            if (key == java.awt.event.KeyEvent.VK_DOWN) {
                evt.consume();

                if (menuResultadosBusqueda.isVisible()
                        && menuResultadosBusqueda.getComponentCount() > 0) {

                    indiceSeleccionadoBusqueda++;

                    if (indiceSeleccionadoBusqueda >= menuResultadosBusqueda.getComponentCount()) {
                        indiceSeleccionadoBusqueda = 0;
                    }

                    seleccionarItemBusqueda(indiceSeleccionadoBusqueda);
                }
            }

            if (key == java.awt.event.KeyEvent.VK_UP) {
                evt.consume();

                if (menuResultadosBusqueda.isVisible()
                        && menuResultadosBusqueda.getComponentCount() > 0) {

                    indiceSeleccionadoBusqueda--;

                    if (indiceSeleccionadoBusqueda < 0) {
                        indiceSeleccionadoBusqueda = menuResultadosBusqueda.getComponentCount() - 1;
                    }

                    seleccionarItemBusqueda(indiceSeleccionadoBusqueda);
                }
            }

            if (key == java.awt.event.KeyEvent.VK_ENTER) {
                evt.consume();

                if (menuResultadosBusqueda.isVisible()
                        && menuResultadosBusqueda.getComponentCount() > 0) {

                    if (indiceSeleccionadoBusqueda < 0) {
                        indiceSeleccionadoBusqueda = 0;
                    }

                    java.awt.Component comp = menuResultadosBusqueda.getComponent(indiceSeleccionadoBusqueda);

                    if (comp instanceof javax.swing.JMenuItem item) {
                        item.doClick();
                    }
                }
            }

            if (key == java.awt.event.KeyEvent.VK_ESCAPE) {
                menuResultadosBusqueda.setVisible(false);
                indiceSeleccionadoBusqueda = -1;
            }
        }
    });
}
 private void buscarOpciones(String texto) {

    menuResultadosBusqueda.removeAll();
    indiceSeleccionadoBusqueda = -1;

    texto = texto.trim();

    if (texto.isEmpty()) {
        menuResultadosBusqueda.setVisible(false);
        return;
    }

    String textoBusqueda = texto.toLowerCase();
    int coincidencias = 0;

    for (java.util.Map.Entry<String, Runnable> opcion : opcionesSistema.entrySet()) {

        String nombreOpcion = opcion.getKey();

        if (nombreOpcion.toLowerCase().contains(textoBusqueda)) {

            javax.swing.JMenuItem itemResultado = new javax.swing.JMenuItem(nombreOpcion);

            itemResultado.setFocusable(false);

            itemResultado.addActionListener(e -> {
                opcion.getValue().run();

                txtSearch.setText("");
                menuResultadosBusqueda.setVisible(false);
                indiceSeleccionadoBusqueda = -1;

                javax.swing.SwingUtilities.invokeLater(() -> {
                    txtSearch.requestFocusInWindow();
                });
            });

            menuResultadosBusqueda.add(itemResultado);
            coincidencias++;
        }
    }

    if (coincidencias > 0) {

        if (!menuResultadosBusqueda.isVisible()) {
            menuResultadosBusqueda.show(txtSearch, 0, txtSearch.getHeight());
        }

        menuResultadosBusqueda.revalidate();
        menuResultadosBusqueda.repaint();

        javax.swing.SwingUtilities.invokeLater(() -> {
            txtSearch.requestFocusInWindow();
        });

    } else {
        menuResultadosBusqueda.setVisible(false);
    }
}
 
 private void seleccionarItemBusqueda(int indice) {

    for (int i = 0; i < menuResultadosBusqueda.getComponentCount(); i++) {

        java.awt.Component comp = menuResultadosBusqueda.getComponent(i);

        if (comp instanceof javax.swing.JMenuItem item) {

            if (i == indice) {
                item.setArmed(true);
                item.setBackground(new java.awt.Color(95, 143, 255));
                item.setForeground(java.awt.Color.WHITE);
            } else {
                item.setArmed(false);
                item.setBackground(java.awt.Color.WHITE);
                item.setForeground(java.awt.Color.BLACK);
            }
        }
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

        background = new javax.swing.JPanel();
        jpMenu = new javax.swing.JPanel();
        btnInfoMaestra = new javax.swing.JButton();
        btnTransacciones = new javax.swing.JButton();
        btnEscolar = new javax.swing.JButton();
        lblLogo = new javax.swing.JLabel();
        jpHeader = new javax.swing.JPanel();
        lblMessage = new javax.swing.JLabel();
        lblDate = new javax.swing.JLabel();
        lblTime = new javax.swing.JLabel();
        txtSearch = new javax.swing.JTextField();
        imgSearch = new javax.swing.JLabel();
        lblTitleInfo = new javax.swing.JLabel();
        jpContainer = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        setMinimumSize(new java.awt.Dimension(1020, 664));

        background.setBackground(new java.awt.Color(255, 255, 255));

        jpMenu.setBackground(new java.awt.Color(26, 61, 99));
        jpMenu.setPreferredSize(new java.awt.Dimension(270, 640));

        btnInfoMaestra.setBackground(new java.awt.Color(179, 207, 229));
        btnInfoMaestra.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnInfoMaestra.setForeground(new java.awt.Color(26, 61, 99));
        btnInfoMaestra.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hom3.png"))); // NOI18N
        btnInfoMaestra.setText("Informacion Maestra");
        btnInfoMaestra.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
        btnInfoMaestra.setBorderPainted(false);
        btnInfoMaestra.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnInfoMaestra.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnInfoMaestra.setIconTextGap(10);
        btnInfoMaestra.addActionListener(this::btnInfoMaestraActionPerformed);

        btnTransacciones.setBackground(new java.awt.Color(179, 207, 229));
        btnTransacciones.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnTransacciones.setForeground(new java.awt.Color(26, 61, 99));
        btnTransacciones.setIcon(new javax.swing.ImageIcon(getClass().getResource("/transaccion.png"))); // NOI18N
        btnTransacciones.setText("Contabilidad");
        btnTransacciones.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
        btnTransacciones.setBorderPainted(false);
        btnTransacciones.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnTransacciones.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnTransacciones.setIconTextGap(10);

        btnEscolar.setBackground(new java.awt.Color(179, 207, 229));
        btnEscolar.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEscolar.setForeground(new java.awt.Color(26, 61, 99));
        btnEscolar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/escolar2.png"))); // NOI18N
        btnEscolar.setText("Escolar");
        btnEscolar.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 1));
        btnEscolar.setBorderPainted(false);
        btnEscolar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEscolar.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnEscolar.setIconTextGap(10);

        lblLogo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/benaedu (5).png"))); // NOI18N

        javax.swing.GroupLayout jpMenuLayout = new javax.swing.GroupLayout(jpMenu);
        jpMenu.setLayout(jpMenuLayout);
        jpMenuLayout.setHorizontalGroup(
            jpMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpMenuLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnTransacciones, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnInfoMaestra, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 258, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnEscolar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jpMenuLayout.setVerticalGroup(
            jpMenuLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpMenuLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblLogo, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnInfoMaestra, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnTransacciones, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnEscolar, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jpHeader.setBackground(new java.awt.Color(26, 61, 99));
        jpHeader.setForeground(new java.awt.Color(255, 255, 255));
        jpHeader.setPreferredSize(new java.awt.Dimension(650, 150));

        lblMessage.setBackground(new java.awt.Color(0, 0, 0));
        lblMessage.setForeground(new java.awt.Color(91, 173, 197));
        lblMessage.setText("Buenas Tardes, Usuario");

        lblDate.setForeground(new java.awt.Color(0, 153, 153));
        lblDate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/calendario.png"))); // NOI18N
        lblDate.setText("Hoy es {dayname} de {month} del {year}  ");
        lblDate.setIconTextGap(10);

        lblTime.setForeground(new java.awt.Color(0, 153, 153));
        lblTime.setIcon(new javax.swing.ImageIcon(getClass().getResource("/clock.png"))); // NOI18N
        lblTime.setText("Hora");
        lblTime.setIconTextGap(10);

        txtSearch.setBackground(new java.awt.Color(179, 207, 229));
        txtSearch.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        txtSearch.setForeground(new java.awt.Color(26, 61, 99));
        txtSearch.setToolTipText("");

        imgSearch.setForeground(new java.awt.Color(91, 173, 197));
        imgSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/buscar.png"))); // NOI18N

        lblTitleInfo.setForeground(new java.awt.Color(91, 173, 197));
        lblTitleInfo.setText("Titulo");

        javax.swing.GroupLayout jpHeaderLayout = new javax.swing.GroupLayout(jpHeader);
        jpHeader.setLayout(jpHeaderLayout);
        jpHeaderLayout.setHorizontalGroup(
            jpHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpHeaderLayout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(jpHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblMessage)
                    .addGroup(jpHeaderLayout.createSequentialGroup()
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(imgSearch))
                    .addComponent(lblTitleInfo))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 289, Short.MAX_VALUE)
                .addGroup(jpHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblDate)
                    .addComponent(lblTime))
                .addGap(21, 21, 21))
        );
        jpHeaderLayout.setVerticalGroup(
            jpHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpHeaderLayout.createSequentialGroup()
                .addGroup(jpHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpHeaderLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(lblDate))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jpHeaderLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblMessage)))
                .addGroup(jpHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpHeaderLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(lblTime))
                    .addGroup(jpHeaderLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblTitleInfo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jpHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(imgSearch))))
                .addContainerGap(58, Short.MAX_VALUE))
        );

        jpContainer.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jpContainerLayout = new javax.swing.GroupLayout(jpContainer);
        jpContainer.setLayout(jpContainerLayout);
        jpContainerLayout.setHorizontalGroup(
            jpContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 914, Short.MAX_VALUE)
        );
        jpContainerLayout.setVerticalGroup(
            jpContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 508, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout backgroundLayout = new javax.swing.GroupLayout(background);
        background.setLayout(backgroundLayout);
        backgroundLayout.setHorizontalGroup(
            backgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundLayout.createSequentialGroup()
                .addComponent(jpMenu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(backgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(backgroundLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jpContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jpHeader, javax.swing.GroupLayout.DEFAULT_SIZE, 920, Short.MAX_VALUE)))
        );
        backgroundLayout.setVerticalGroup(
            backgroundLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundLayout.createSequentialGroup()
                .addComponent(jpHeader, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jpContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jpMenu, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 664, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(background, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(background, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnInfoMaestraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInfoMaestraActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnInfoMaestraActionPerformed

    // Método 1: Verifica de forma global dónde está el cursor
    private boolean isMouseOverAnyMenuOrButton() {
        java.awt.Point cursorLoc = java.awt.MouseInfo.getPointerInfo().getLocation();

        // 1. Revisar si el mouse está sobre alguno de los botones principales
        javax.swing.JButton[] botones = {btnInfoMaestra, btnTransacciones, btnEscolar};
        for (javax.swing.JButton btn : botones) {
            if (btn != null && btn.isShowing()) {
                java.awt.Rectangle bounds = new java.awt.Rectangle(btn.getLocationOnScreen(), btn.getSize());
                if (bounds.contains(cursorLoc)) {
                    return true; // Está sobre un botón principal, no cerrar
                }
            }
        }

        // 2. Revisar si el mouse está sobre CUALQUIER menú o submenú abierto actualmente
        javax.swing.MenuElement[] path = javax.swing.MenuSelectionManager.defaultManager().getSelectedPath();
        for (javax.swing.MenuElement element : path) {
            java.awt.Component comp = element.getComponent();
            if (comp != null && comp.isShowing()) {
                java.awt.Rectangle bounds = new java.awt.Rectangle(comp.getLocationOnScreen(), comp.getSize());
                if (bounds.contains(cursorLoc)) {
                    return true; // Está adentro de un menú o submenú, no cerrar
                }
            }
        }

        return false; // El mouse está afuera de todo
    }

    // Método 2: Crea el temporizador inteligente
    private java.awt.event.MouseAdapter crearEventoOcultar() {
        return new java.awt.event.MouseAdapter() {
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                // Le damos 250ms (un cuarto de segundo) para que el movimiento se sienta fluido
                javax.swing.Timer timer = new javax.swing.Timer(250, e -> {
                    // Si el mouse NO está en ningún botón ni submenú válido, limpiamos la pantalla
                    if (!isMouseOverAnyMenuOrButton()) {
                        javax.swing.MenuSelectionManager.defaultManager().clearSelectedPath();
                    }
                });
                timer.setRepeats(false);
                timer.start();
            }
        };
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        FlatMTMaterialLighterIJTheme.setup();

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Dashboard().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel background;
    private javax.swing.JButton btnEscolar;
    private javax.swing.JButton btnInfoMaestra;
    private javax.swing.JButton btnTransacciones;
    private javax.swing.JLabel imgSearch;
    private javax.swing.JPanel jpContainer;
    private javax.swing.JPanel jpHeader;
    private javax.swing.JPanel jpMenu;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblLogo;
    private javax.swing.JLabel lblMessage;
    private javax.swing.JLabel lblTime;
    private javax.swing.JLabel lblTitleInfo;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
