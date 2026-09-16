package com.mycompany.benaedu.services;

import com.mycompany.benaedu.db.ConDB;
import com.mycompany.benaedu.services.ReferenciasBancarias.*;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Persistencia compatible con tesrefb existente, sin DDL ni sustitución de referencias históricas. */
public final class ArchivoReferenciasBancarias {
    private ArchivoReferenciasBancarias() { }
    private static final String COLUMNAS = "CIA,CC,SECC,CESC,PESC,MAT,GRADO,GRUPO,IDCPT,NCPTO,DINI,MES,FRINI,TREF,FREF,IMPMN,PBEC,IBECMN,PDSC,IDSCMN,REFB,SREF,USER,FEAC,HOAC";

    /** Resultado confirmado de un guardado; las existentes no se insertan nuevamente. */
    public record Guardado(int nuevas, int existentes) { }

    /** Guarda el lote tras comprobar cargos actuales, duplicados, longitudes y estado histórico. */
    public static Guardado guardar(Filtro filtro, List<Referencia> propuestas, String usuario) throws Exception {
        try (Connection con = ConDB.getConnection()) { return guardar(con, filtro, propuestas, usuario); }
    }

    static Guardado guardar(Connection con, Filtro filtro, List<Referencia> propuestas, String usuario) throws Exception {
        if (usuario == null || usuario.isBlank()) throw new IllegalArgumentException("Se requiere el usuario de la sesión para guardar.");
        if (propuestas.isEmpty()) throw new IllegalArgumentException("No hay referencias para guardar.");
        int confirmadas = 0;
        boolean bloqueadas = false;
        try (var st = con.createStatement()) {
            st.setQueryTimeout(30);
            st.execute("SET SESSION sql_mode = CONCAT_WS(',', @@SESSION.sql_mode, 'STRICT_ALL_TABLES')");
            st.execute("SET SESSION lock_wait_timeout=15");
            // MyISAM no revierte lotes. Bloqueo de tablas también protege frente al programa antiguo.
            st.execute("LOCK TABLES tesrefb WRITE, tescalu READ, tescalu AS c READ, tescpto READ, tesalum READ");
            bloqueadas = true;
            Resultado actual = ReferenciasBancarias.generar(con, filtro);
            if (!actual.referencias().equals(propuestas))
                throw new IllegalArgumentException("Los cargos o catálogos cambiaron. Vuelva a generar antes de guardar.");
            Indice existentes = indexar(leer(con, filtro.ciclo(), propuestas));
            List<Referencia> nuevas = new ArrayList<>();
            for (Referencia r : propuestas) if (!existeCompatible(filtro.ciclo(), r, existentes.coincidencias(filtro.ciclo(), r))) nuevas.add(r);
            // Validar todas las longitudes antes del primer INSERT, incluso en servidores no estrictos.
            Map<String, Integer> longitudes = new HashMap<>();
            try (var columnas = st.executeQuery("SHOW FULL COLUMNS FROM tesrefb")) {
                while (columnas.next()) {
                    String tipo = columnas.getString("Type");
                    var m = java.util.regex.Pattern.compile("(?i)(?:var)?char\\((\\d+)\\)").matcher(tipo);
                    if (m.find()) longitudes.put(columnas.getString("Field").toUpperCase(Locale.ROOT), Integer.parseInt(m.group(1)));
                }
            }
            String[] nombres = COLUMNAS.split(",");
            List<String[]> filas = new ArrayList<>();
            for (Referencia r : nuevas) {
                String[] fila = datos(filtro.ciclo(), r, usuario);
                for (int i = 0; i < fila.length; i++) {
                    Integer limite = longitudes.get(nombres[i]);
                    if (limite != null && fila[i].length() > limite)
                        throw new IllegalArgumentException("La columna tesrefb." + nombres[i] + " no admite el dato completo. No se guardó el lote.");
                }
                filas.add(fila);
            }
            try (var ps = con.prepareStatement("INSERT INTO tesrefb (" + COLUMNAS + ") VALUES (" + String.join(",", Collections.nCopies(25, "?")) + ")")) {
                for (String[] fila : filas) {
                    for (int i = 0; i < fila.length; i++) ps.setString(i + 1, fila[i]);
                    if (ps.executeUpdate() != 1) throw new SQLException("No se confirmó la inserción de una referencia.");
                    confirmadas++;
                }
            }
            return new Guardado(confirmadas, propuestas.size() - nuevas.size());
        } catch (SQLException ex) {
            throw new SQLException("Guardado interrumpido. Inserciones confirmadas: " + confirmadas
                    + ". MyISAM no permite revertirlas; la última operación podría requerir verificación. "
                    + "Consulte las guardadas antes de reintentar; no se sobrescribirán las existentes. " + ex.getMessage(), ex);
        } finally {
            if (bloqueadas) try (var st = con.createStatement()) { st.execute("UNLOCK TABLES"); }
        }
    }

    /** Devuelve solo referencias guardadas que aún coinciden con cargos pendientes y reglas actuales. */
    public static Resultado consultar(Filtro filtro) throws Exception {
        try (Connection con = ConDB.getConnection()) {
            Resultado actual = ReferenciasBancarias.generar(con, filtro);
            Indice existentes = indexar(leer(con, filtro.ciclo(), actual.referencias()));
            List<Referencia> resultado = new ArrayList<>();
            List<String> avisos = new ArrayList<>(actual.avisos());
            for (Referencia r : actual.referencias()) {
                try {
                    if (existeCompatible(filtro.ciclo(), r, existentes.coincidencias(filtro.ciclo(), r))) resultado.add(r);
                    else avisos.add(r.matricula() + " / " + r.concepto() + " / " + r.tipo() + ": aún no está guardada.");
                } catch (IllegalArgumentException ex) { avisos.add(ex.getMessage()); }
            }
            return new Resultado(List.copyOf(resultado), List.copyOf(avisos));
        }
    }

    private static List<String[]> leer(Connection con, Ciclo ciclo, List<Referencia> propuestas) throws SQLException {
        if (propuestas.isEmpty()) return List.of();
        String sql = "SELECT " + COLUMNAS + " FROM tesrefb WHERE (CIA=? AND CC=? AND CESC=?) OR REFB IN ("
                + String.join(",", Collections.nCopies(propuestas.size(), "?")) + ")";
        List<String[]> filas = new ArrayList<>();
        try (var ps = con.prepareStatement(sql)) {
            ps.setString(1, ciclo.cia()); ps.setString(2, ciclo.cc()); ps.setString(3, ciclo.ciclo());
            for (int i = 0; i < propuestas.size(); i++) ps.setString(i + 4, propuestas.get(i).referencia());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String[] fila = new String[25];
                    for (int i = 0; i < fila.length; i++) fila[i] = Objects.toString(rs.getString(i + 1), "").trim();
                    filas.add(fila);
                }
            }
        }
        return filas;
    }

    private record Clave(String cia, String cc, String ciclo, String mat, String id, String tipo) { }

    private record Indice(Map<Clave, List<String[]>> claves, Map<String, List<String[]>> referencias) {
        List<String[]> coincidencias(Ciclo ciclo, Referencia r) {
            Clave clave = new Clave(ciclo.cia(), ciclo.cc(), ciclo.ciclo(), r.matricula(), String.valueOf(r.cargo().id()),
                    r.tipo().equals("Normal") ? "N" : "R");
            Set<String[]> resultado = new LinkedHashSet<>(claves.getOrDefault(clave, List.of()));
            resultado.addAll(referencias.getOrDefault(r.referencia(), List.of()));
            return List.copyOf(resultado);
        }
    }

    private static Indice indexar(List<String[]> filas) {
        Map<Clave, List<String[]>> claves = new HashMap<>();
        Map<String, List<String[]>> referencias = new HashMap<>();
        for (String[] f : filas) {
            claves.computeIfAbsent(new Clave(f[0], f[1], f[3], f[5], f[8], f[13]), k -> new ArrayList<>()).add(f);
            referencias.computeIfAbsent(f[20], k -> new ArrayList<>()).add(f);
        }
        return new Indice(claves, referencias);
    }

    static boolean existeCompatible(Ciclo ciclo, Referencia propuesta, List<String[]> filas) {
        String[] esperada = datos(ciclo, propuesta, "");
        List<String[]> coincidencias = filas.stream().filter(f -> f[20].equals(esperada[20]) || mismaClave(f, esperada)).toList();
        if (coincidencias.isEmpty()) return false;
        if (coincidencias.size() != 1) throw conflicto(propuesta, "hay duplicados históricos");
        String[] fila = coincidencias.get(0);
        for (int i = 0; i < 22; i++) {
            boolean iguales;
            if (i == 8 || i == 10 || i == 11 || (i >= 15 && i <= 19)) {
                try { iguales = new BigDecimal(fila[i]).compareTo(new BigDecimal(esperada[i])) == 0; }
                catch (NumberFormatException ex) { iguales = false; }
            } else iguales = fila[i].equals(esperada[i]);
            if (!iguales) throw conflicto(propuesta, "la referencia existente tiene otros datos o estado; no se reemplazará");
        }
        return true;
    }

    private static boolean mismaClave(String[] a, String[] b) {
        for (int i : new int[]{0, 1, 3, 5, 8, 13}) if (!a[i].equals(b[i])) return false;
        return true;
    }

    private static IllegalArgumentException conflicto(Referencia r, String motivo) {
        return new IllegalArgumentException(r.matricula() + " / " + r.concepto() + " / " + r.tipo() + ": " + motivo + ".");
    }

    static String[] datos(Ciclo ciclo, Referencia r, String usuario) {
        Cargo c = r.cargo();
        return new String[]{ciclo.cia(), ciclo.cc(), texto(c.seccion()), ciclo.ciclo(), texto(c.periodo()),
            r.matricula(), texto(c.grado()), texto(c.grupo()), String.valueOf(c.id()), c.concepto(),
            String.valueOf(r.inicio().getDayOfMonth()), String.valueOf(r.fin().getMonthValue()), r.inicio().toString(),
            r.tipo().equals("Normal") ? "N" : "R", r.fin().toString(), r.importe().toPlainString(),
            c.porcentajeBeca().toPlainString(), c.importeBeca().toPlainString(), "0", "0", r.referencia(), "", usuario,
            LocalDate.now().toString(), LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))};
    }

    private static String texto(String valor) { return Objects.toString(valor, "").trim(); }
}
