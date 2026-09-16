package com.mycompany.benaedu.services;

import com.mycompany.benaedu.db.ConDB;
import com.mycompany.benaedu.services.ArchivoBcm.Movimiento;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/** Conciliación y aplicación explícita de pagos EXP usando las tablas escolares existentes. */
public final class InterfaseBcm {
    private InterfaseBcm() { }
    /** Configuración confirmada por la cajera; los códigos EFE/CCT se mapean al catálogo escolar. */
    public record Configuracion(String cia, String banco, String cuenta, String efectivo, String cct, String usuario) { }
    /** Diagnóstico con instantáneas que permiten detectar cambios antes de aplicar. */
    public record Revision(Movimiento movimiento, String estado, String detalle,
                           Map<String, String> referencia, Map<String, String> cargo) {
        /** Indica si el movimiento pasó las validaciones para ser aplicado. */
        public boolean listo() { return estado.equals("LISTO"); }
    }
    /** Resultado de aplicación; fallo detiene el lote y conserva la información de los completados. */
    public record Aplicacion(int aplicados, String incidencia) { }

    /** Consulta un catálogo necesario para la pantalla, mediante nombres permitidos. */
    public static Map<String, String> catalogo(String tipo) throws Exception {
        String sql = switch (tipo) {
            case "CIA" -> "SELECT CIA,NCIA FROM tmcias ORDER BY CIA";
            case "BCOS", "IPAG" -> "SELECT CVE,DES FROM tmclas WHERE TBL=? ORDER BY CVE";
            default -> throw new IllegalArgumentException("Catálogo desconocido");
        };
        Map<String, String> resultado = new LinkedHashMap<>();
        try (var con = ConDB.getConnection(); var ps = con.prepareStatement(sql)) {
            if (!tipo.equals("CIA")) ps.setString(1, tipo);
            try (var rs = ps.executeQuery()) { while (rs.next()) resultado.put(rs.getString(1), rs.getString(2)); }
        }
        return resultado;
    }

    /** Revisa todos los movimientos sin escrituras ni asignación de folios. */
    public static List<Revision> revisar(Configuracion cfg, List<Movimiento> movimientos) throws Exception {
        try (var con = ConDB.getConnection()) { return revisar(con, cfg, movimientos); }
    }

    static List<Revision> revisar(Connection con, Configuracion cfg, List<Movimiento> movimientos) throws Exception {
        validarConfiguracion(con, cfg);
        List<Revision> resultado = new ArrayList<>();
        Map<String, Long> repeticiones = movimientos.stream().collect(java.util.stream.Collectors.groupingBy(Movimiento::clave, java.util.stream.Collectors.counting()));
        for (Movimiento m : movimientos) {
            if (repeticiones.get(m.clave()) > 1) resultado.add(rechazo(m, "Guía/fecha repetida dentro del archivo."));
            else {
                try { resultado.add(revisarUno(con, cfg, m)); }
                catch (IllegalArgumentException ex) { resultado.add(rechazo(m, ex.getMessage())); }
            }
        }
        Map<String, Long> cargos = resultado.stream().filter(Revision::listo).collect(java.util.stream.Collectors.groupingBy(
                r -> identidad(r.cargo()), java.util.stream.Collectors.counting()));
        return resultado.stream().map(r -> r.listo() && cargos.get(identidad(r.cargo())) > 1
                ? rechazo(r.movimiento(), "Hay varios movimientos para el mismo cargo; requieren conciliación manual.") : r).toList();
    }

    private static Revision revisarUno(Connection con, Configuracion cfg, Movimiento m) throws SQLException {
        String forma = forma(cfg, m);
        exigir(!forma.isBlank(), "Configure la equivalencia de la forma " + m.forma() + ".");
        String sinPrefijo = m.referencia().startsWith("0") ? m.referencia().substring(1) : m.referencia();
        var refs = filas(con, "SELECT * FROM tesrefb WHERE CIA=? AND (REFB=? OR REFB=?)", cfg.cia(), m.referencia(), sinPrefijo);
        exigir(refs.size() == 1, "Referencia inexistente o ambigua en tesrefb.");
        Map<String, String> ref = refs.get(0);
        var cargos = filas(con, "SELECT * FROM tescalu WHERE CIA=? AND CC=? AND CESC=? AND MAT=? AND IDCPT=?",
                cfg.cia(), ref.get("CC"), ref.get("CESC"), ref.get("MAT"), ref.get("IDCPT"));
        exigir(cargos.size() == 1, "Cargo inexistente o duplicado.");
        Map<String, String> c = cargos.get(0);
        var pagos = filas(con, "SELECT * FROM tespalu WHERE CIA=? AND BCOPAG=? AND CTAPAG=? AND FDEP=? AND IDPAG=?",
                cfg.cia(), cfg.banco(), cfg.cuenta(), m.fecha(), m.guia());
        if (!pagos.isEmpty()) {
            exigir(pagos.size() == 1, "Existen varios pagos con la misma guía; revisar.");
            var p = pagos.get(0);
            exigir(p.get("MCAN").isEmpty() && p.get("SPAG").isEmpty(), "Movimiento previo cancelado o incompleto; no se reaplica automáticamente.");
            exigir(p.get("REFPAG").equals(ref.get("REFB")) && dinero(p, "IMPMN").compareTo(m.importe()) == 0
                    && p.get("FMAPAG").equals(forma), "La guía ya existe con otros datos.");
            var recibos = filas(con, "SELECT * FROM tesralu WHERE CIA=? AND CC=? AND CESC=? AND MAT=? AND IDCPT=? AND NREC=? AND TREC=?",
                    cfg.cia(), c.get("CC"), c.get("CESC"), c.get("MAT"), c.get("IDCPT"), p.get("NREC"), p.get("TREC"));
            exigir(recibos.size() == 1 && recibos.get(0).get("MCAN").isEmpty()
                    && dinero(recibos.get(0), "IPAGMN").compareTo(m.importe()) == 0
                    && dinero(c, "IPENMN").signum() == 0 && ref.get("SREF").equals("P"),
                    "La guía tiene registros parciales o inconsistentes; revisar antes de continuar.");
            return new Revision(m, "YA REGISTRADO", "Recibo " + p.get("NREC"), ref, c);
        }
        exigir(c.get("MCAN").isEmpty() && ref.get("SREF").isEmpty(), "Cargo cancelado o referencia ya procesada.");
        exigir(c.get("CMON").equals("MXP") && dinero(c, "TCAMB").compareTo(BigDecimal.ONE) == 0,
                "Solo se admiten cargos en pesos mexicanos sin conversión de moneda.");
        exigir(dinero(c, "IPAGMN").signum() == 0 && dinero(c, "IPENMN").signum() > 0
                && dinero(c, "IPENMN").compareTo(dinero(c, "IMPTMN")) == 0, "Cargo pagado, con abonos o saldo inconsistente.");
        exigir(c.get("NCPTO").equals(ref.get("NCPTO")), "El concepto no coincide con la referencia.");
        validarImporteFecha(m, ref, c);
        var anteriores = filas(con, "SELECT NREC FROM tesralu WHERE CIA=? AND CC=? AND CESC=? AND MAT=? AND IDCPT=? AND COALESCE(MCAN,'')=''",
                cfg.cia(), c.get("CC"), c.get("CESC"), c.get("MAT"), c.get("IDCPT"));
        exigir(anteriores.isEmpty(), "El cargo tiene un recibo previo; posible registro incompleto.");
        return new Revision(m, "LISTO", c.get("MAT") + " / " + c.get("NCPTO"), ref, c);
    }

    static void validarImporteFecha(Movimiento m, Map<String, String> ref, Map<String, String> c) {
        exigir(dinero(c, "IMPMN").subtract(dinero(c, "IBECMN")).subtract(dinero(c, "IDSCMN"))
                .add(dinero(c, "IRECMN")).compareTo(dinero(c, "IMPTMN")) == 0, "El desglose del cargo no coincide con su total.");
        exigir(dinero(ref, "IMPMN").compareTo(m.importe()) == 0, "Importe bancario distinto del importe de la referencia.");
        LocalDate inicio = fecha(ref.get("FRINI")), fin = fecha(ref.get("FREF")), vence = fecha(c.get("FVEN"));
        exigir(!m.fecha().isBefore(inicio) && !m.fecha().isAfter(fin), "Pago fuera de la vigencia de la referencia.");
        exigir(dinero(ref, "PBEC").compareTo(dinero(c, "PBEC")) == 0
                && dinero(ref, "IBECMN").compareTo(dinero(c, "IBECMN")) == 0
                && dinero(ref, "PDSC").compareTo(dinero(c, "PDSC")) == 0
                && dinero(ref, "IDSCMN").compareTo(dinero(c, "IDSCMN")) == 0, "Cambió la beca o descuento del cargo.");
        BigDecimal neto = dinero(c, "IMPTMN");
        if (ref.get("TREF").equals("N")) {
            exigir(fin.equals(vence) && m.importe().compareTo(neto) == 0, "El cargo no coincide con la referencia normal.");
        } else if (ref.get("TREF").equals("R")) {
            exigir(inicio.equals(vence.plusDays(1)) && dinero(c, "IRECMN").signum() == 0, "Recargo o fecha ya ajustados; revisar manualmente.");
            BigDecimal teorico = neto.multiply(new BigDecimal("1.10"));
            // Referencias antiguas pueden redondear; se respeta el importe emitido, dentro de un peso.
            exigir(m.importe().compareTo(teorico.setScale(0, RoundingMode.DOWN)) >= 0
                    && m.importe().compareTo(teorico.setScale(0, RoundingMode.CEILING)) <= 0,
                    "El recargo no corresponde al cargo actual.");
        } else throw new IllegalArgumentException("Tipo de referencia no soportado: " + ref.get("TREF"));
    }

    /** Aplica solamente el conjunto que se mostró LISTO, revalidándolo bajo bloqueo antes de escribir. */
    public static Aplicacion aplicar(Configuracion cfg, List<Movimiento> movimientos, List<Revision> previa) throws Exception {
        try (var con = ConDB.getConnection()) { return aplicar(con, cfg, movimientos, previa); }
    }

    static Aplicacion aplicar(Connection con, Configuracion cfg, List<Movimiento> movimientos, List<Revision> previa) throws Exception {
        int aplicados = 0;
        boolean folio = false, tablas = false;
        String bloqueo = "benaedu_recibo_" + cfg.cia();
        try {
            var candado = filas(con, "SELECT GET_LOCK(?,10) AS OK", bloqueo);
            exigir(candado.size() == 1 && candado.get(0).get("OK").equals("1"), "No se pudo reservar el folio de recibo."); folio = true;
            ejecutar(con, "SET SESSION sql_mode=CONCAT_WS(',',@@SESSION.sql_mode,'STRICT_ALL_TABLES')");
            ejecutar(con, "SET SESSION lock_wait_timeout=15");
            ejecutar(con, "LOCK TABLES tesrefb WRITE,tescalu WRITE,tesralu WRITE,tespalu WRITE,tesalum READ,tescaj READ,tmclas READ"); tablas = true;
            var actuales = revisar(con, cfg, movimientos);
            exigir(actuales.equals(previa), "Los datos cambiaron desde la revisión; cargue y revise el archivo nuevamente.");
            var cajeros = filas(con, "SELECT NEMP FROM tescaj WHERE CIA=? AND USER=? AND ECAJ='A' ORDER BY NEMP", cfg.cia(), cfg.usuario());
        exigir(cajeros.size() == 1, "El usuario debe tener un único cajero activo en esta compañía.");
            String ncaj = cajeros.get(0).get("NEMP");
            for (Revision r : actuales) {
                if (!r.listo()) continue;
                try {
                    aplicarUno(con, cfg, r, ncaj);
                    aplicados++;
                } catch (Exception ex) {
                    return new Aplicacion(aplicados, "Detenido en movimiento " + r.movimiento().numero() + ": " + ex.getMessage()
                            + ". Puede haber registros parciales (MyISAM); no se reintenta ni se declara pagado automáticamente. Revise la guía " + r.movimiento().guia() + ".");
                }
            }
            return new Aplicacion(aplicados, "");
        } finally {
            try { if (tablas) ejecutar(con, "UNLOCK TABLES"); }
            finally { if (folio) filas(con, "SELECT RELEASE_LOCK(?) AS OK", bloqueo); }
        }
    }

    private static void aplicarUno(Connection con, Configuracion cfg, Revision r, String ncaj) throws Exception {
        Movimiento m = r.movimiento(); Map<String, String> c = r.cargo();
        String nrec = filas(con, "SELECT COALESCE(MAX(NREC),0)+1 AS NREC FROM tesralu WHERE CIA=?", cfg.cia()).get(0).get("NREC");
        // No reutilizar un folio que quedó reservado en tespalu por una aplicación incompleta.
        String otro = filas(con, "SELECT COALESCE(MAX(NREC),0)+1 AS NREC FROM tespalu WHERE CIA=?", cfg.cia()).get(0).get("NREC");
        nrec = new BigDecimal(nrec).max(new BigDecimal(otro)).toPlainString();
        String trec = c.get("TCONT").equals("P") ? "RP" : "RO";
        Map<String, Object> recibo = copiar(c, "CIA,CC,SECC,CESC,MAT,TALU,GRADO,GRUPO,IDCPT,NCPTO,TCPTO,DCPTO,TCONT,CUNIMN,CANT,IMPMN,TDSC,PDSC,IDSCMN,TBECA,CBECA,PBEC,IBECMN");
        var alumnos = filas(con, "SELECT NOMCOM,APATE,AMATE,NOMA FROM tesalum WHERE MAT=?", c.get("MAT"));
        exigir(alumnos.size() == 1, "Alumno inexistente o duplicado.");
        var a = alumnos.get(0);
        String nombre = a.get("NOMCOM").isBlank() ? (a.get("APATE") + " " + a.get("AMATE") + " " + a.get("NOMA")).trim() : a.get("NOMCOM");
        BigDecimal recargo = m.importe().subtract(dinero(c, "IMPTMN"));
        recibo.putAll(comunes(cfg, ncaj, nrec, trec, m));
        recibo.put("NOMALU", nombre); recibo.put("NFAC", 0); recibo.put("TFAC", ""); recibo.put("FFAC", null);
        recibo.put("PREC", r.referencia().get("TREF").equals("R") ? 10 : dinero(c, "PREC"));
        recibo.put("IRECMN", dinero(c, "IRECMN").add(recargo)); recibo.put("IMPTMN", m.importe());
        recibo.put("IPAGMN", m.importe()); recibo.put("IPENMN", 0); recibo.put("FVEN", fecha(c.get("FVEN")));
        recibo.put("FCON", null); recibo.put("FPAG", m.fecha()); recibo.put("CUNIME", 0); recibo.put("IMPME", 0); recibo.put("IPAGME", 0);
        Map<String, Object> pago = copiar(c, "CIA,CC,SECC,CESC,MAT,TALU,GRADO");
        pago.putAll(comunes(cfg, ncaj, nrec, trec, m));
        pago.put("IDPAG", m.guia()); pago.put("FDEP", m.fecha()); pago.put("FPAG", m.fecha()); pago.put("FCON", null);
        pago.put("IMPMN", m.importe()); pago.put("IMPME", 0); pago.put("CCTA", ""); pago.put("FMAPAG", forma(cfg, m));
        pago.put("BCOPAG", cfg.banco()); pago.put("CTAPAG", cfg.cuenta()); pago.put("REFPAG", r.referencia().get("REFB"));
        pago.put("SPAG", "BCM_PEND"); pago.put("RELFMA", "");
        comprobarLongitudes(con, "tespalu", pago); comprobarLongitudes(con, "tesralu", recibo);
        insertar(con, "tespalu", pago); // Reserva durable: impide repetir una operación parcialmente aplicada.
        insertar(con, "tesralu", recibo);
        int afectados = actualizar(con, "UPDATE tescalu SET IMPTMN=?,IPAGMN=?,IPENMN=0,IRECMN=?,PREC=?,USER=?,FEAC=CURDATE(),HOAC=? "
                + "WHERE CIA=? AND CC=? AND CESC=? AND MAT=? AND IDCPT=? AND IPAGMN=0 AND IPENMN=? AND COALESCE(MCAN,'')=''",
                m.importe(), m.importe(), recibo.get("IRECMN"), recibo.get("PREC"), cfg.usuario(), recibo.get("HOAC"),
                cfg.cia(), c.get("CC"), c.get("CESC"), c.get("MAT"), c.get("IDCPT"), dinero(c, "IPENMN"));
        exigir(afectados == 1, "No se confirmó la actualización única del cargo");
        exigir(actualizar(con, "UPDATE tesrefb SET SREF='P',USER=?,FEAC=CURDATE(),HOAC=? WHERE CIA=? AND CC=? AND CESC=? AND MAT=? AND IDCPT=? AND COALESCE(SREF,'')=''",
                cfg.usuario(), recibo.get("HOAC"), cfg.cia(), c.get("CC"), c.get("CESC"), c.get("MAT"), c.get("IDCPT")) >= 1, "No se marcó la referencia pagada");
        exigir(actualizar(con, "UPDATE tespalu SET SPAG='' WHERE CIA=? AND NREC=? AND TREC=? AND IDPAG=? AND FDEP=? AND SPAG='BCM_PEND'",
                cfg.cia(), nrec, trec, m.guia(), m.fecha()) == 1, "No se confirmó la finalización del pago");
    }

    private static Map<String, Object> comunes(Configuracion cfg, String ncaj, String nrec, String trec, Movimiento m) {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("PESC", "P"); datos.put("NREC", nrec); datos.put("TREC", trec); datos.put("FREC", m.fecha());
        datos.put("CMON", "MXP"); datos.put("TCAMB", 1); datos.put("NCAJ", ncaj); datos.put("RELPOL", 0); datos.put("RELPOC", 0);
        datos.put("MCAN", ""); datos.put("USER", cfg.usuario()); datos.put("FEAC", LocalDate.now());
        datos.put("HOAC", LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
        return datos;
    }

    private static void validarConfiguracion(Connection con, Configuracion cfg) throws SQLException {
        exigir(cfg != null && cfg.cia() != null && !cfg.cia().isBlank() && cfg.usuario() != null && !cfg.usuario().isBlank(), "Se requiere compañía y usuario autenticado.");
        exigir(cfg.cuenta() != null && cfg.cuenta().matches("[0-9]{10,18}"), "Cuenta bancaria inválida.");
        exigir(filas(con, "SELECT CVE FROM tmclas WHERE TBL='BCOS' AND CVE=?", cfg.banco()).size() == 1, "Seleccione un banco válido del catálogo.");
        for (String forma : List.of(cfg.efectivo(), cfg.cct()))
            exigir(filas(con, "SELECT CVE FROM tmclas WHERE TBL='IPAG' AND CVE=?", forma).size() == 1, "Seleccione equivalencias EFE y CCT válidas del catálogo.");
    }
    private static String forma(Configuracion cfg, Movimiento m) {
        return switch (m.forma()) { case "EFE" -> cfg.efectivo(); case "CCT" -> cfg.cct(); default -> ""; };
    }
    private static Revision rechazo(Movimiento m, String detalle) { return new Revision(m, "REVISAR", detalle, Map.of(), Map.of()); }
    private static String identidad(Map<String, String> c) { return c.get("CIA") + "/" + c.get("CC") + "/" + c.get("CESC") + "/" + c.get("MAT") + "/" + c.get("IDCPT"); }
    private static void exigir(boolean cumple, String mensaje) { if (!cumple) throw new IllegalArgumentException(mensaje); }
    private static BigDecimal dinero(Map<String, String> fila, String campo) {
        try { return new BigDecimal(fila.get(campo)); } catch (RuntimeException ex) { throw new IllegalArgumentException("Importe ausente o inválido: " + campo); }
    }
    private static LocalDate fecha(String valor) {
        try { return LocalDate.parse(valor); } catch (RuntimeException ex) { throw new IllegalArgumentException("Fecha del cargo o referencia inválida."); }
    }
    private static Map<String, Object> copiar(Map<String, String> fila, String campos) {
        Map<String, Object> datos = new LinkedHashMap<>(); for (String campo : campos.split(",")) datos.put(campo, fila.get(campo)); return datos;
    }
    private static List<Map<String, String>> filas(Connection con, String sql, Object... args) throws SQLException {
        try (var ps = con.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i]);
            try (var rs = ps.executeQuery()) {
                List<Map<String, String>> filas = new ArrayList<>(); var meta = rs.getMetaData();
                while (rs.next()) {
                    Map<String, String> fila = new LinkedHashMap<>();
                    for (int i = 1; i <= meta.getColumnCount(); i++) fila.put(meta.getColumnLabel(i).toUpperCase(Locale.ROOT), Objects.toString(rs.getString(i), "").trim());
                    filas.add(Collections.unmodifiableMap(fila));
                }
                return filas;
            }
        }
    }
    private static void comprobarLongitudes(Connection con, String tabla, Map<String, Object> datos) throws SQLException {
        for (var columna : filas(con, "SHOW FULL COLUMNS FROM " + tabla)) {
            var matcher = java.util.regex.Pattern.compile("(?i)(?:var)?char\\((\\d+)\\)").matcher(columna.get("TYPE"));
            Object valor = datos.get(columna.get("FIELD").toUpperCase(Locale.ROOT));
            if (valor != null && matcher.find()) exigir(valor.toString().length() <= Integer.parseInt(matcher.group(1)), "El dato no cabe en " + tabla + "." + columna.get("FIELD"));
        }
    }
    private static void insertar(Connection con, String tabla, Map<String, Object> datos) throws SQLException {
        exigir(actualizar(con, "INSERT INTO " + tabla + " (" + String.join(",", datos.keySet()) + ") VALUES ("
                + String.join(",", Collections.nCopies(datos.size(), "?")) + ")", datos.values().toArray()) == 1, "No se confirmó el registro en " + tabla);
    }
    private static int actualizar(Connection con, String sql, Object... args) throws SQLException {
        try (var ps = con.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i] instanceof LocalDate d ? java.sql.Date.valueOf(d) : args[i]);
            return ps.executeUpdate();
        }
    }
    private static void ejecutar(Connection con, String sql) throws SQLException { try (var st = con.createStatement()) { st.execute(sql); } }
}
