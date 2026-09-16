package com.mycompany.benaedu.services;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import com.mycompany.benaedu.services.ReferenciasBancarias.*;

/** Simulación JDBC en memoria: prueba escrituras y fallos sin tocar ninguna base de datos. */
public final class ArchivoReferenciasBancariasTest {
    private ArchivoReferenciasBancariasTest() { }

    /** Comprueba idempotencia, conflictos, cargos cambiados y recuperación de lotes MyISAM parciales. */
    public static void main(String[] args) throws Exception {
        Filtro filtro = new Filtro(new Ciclo("12", "12100", "2627"), "", "", true, LocalDate.of(2027, 8, 31));
        Base base = new Base();
        var con = base.conexion();
        var propuestas = ReferenciasBancarias.generar(con, filtro).referencias();
        comprobar(propuestas.size() == 2, "Normal y recargo");
        var primera = ArchivoReferenciasBancarias.guardar(con, filtro, propuestas, "Admin");
        comprobar(primera.nuevas() == 2 && base.filas.size() == 2 && !base.bloqueada, "Primer guardado y desbloqueo");
        var segunda = ArchivoReferenciasBancarias.guardar(con, filtro, propuestas, "Admin");
        comprobar(segunda.nuevas() == 0 && segunda.existentes() == 2 && base.filas.size() == 2, "Idempotencia");
        base.filas.get(0)[21] = "P";
        rechaza(() -> ArchivoReferenciasBancarias.guardar(con, filtro, propuestas, "Admin"));
        comprobar(!base.bloqueada && base.filas.size() == 2, "Referencia pagada intacta");
        base.filas.get(0)[21] = "";
        base.filas.add(base.filas.get(0).clone());
        rechaza(() -> ArchivoReferenciasBancarias.guardar(con, filtro, propuestas, "Admin"));
        base.filas.remove(2);
        base.filas.get(0)[3] = "2526";
        rechaza(() -> ArchivoReferenciasBancarias.guardar(con, filtro, propuestas, "Admin"));
        base.filas.get(0)[3] = "2627";
        base.cargo.put("IPAGMN", "100");
        rechaza(() -> ArchivoReferenciasBancarias.guardar(con, filtro, propuestas, "Admin"));

        Base parcial = new Base(); parcial.fallarInsercion = 2;
        try { ArchivoReferenciasBancarias.guardar(parcial.conexion(), filtro, propuestas, "Admin"); throw new AssertionError("Fallo no propagado"); }
        catch (SQLException ex) { comprobar(ex.getMessage().contains("confirmadas: 1"), "Diagnóstico de guardado parcial"); }
        comprobar(parcial.filas.size() == 1 && !parcial.bloqueada, "MyISAM conserva la primera inserción");
        parcial.fallarInsercion = -1;
        var recuperado = ArchivoReferenciasBancarias.guardar(parcial.conexion(), filtro, propuestas, "Admin");
        comprobar(recuperado.nuevas() == 1 && recuperado.existentes() == 1 && parcial.filas.size() == 2, "Reintento sin duplicar");

        Base corta = new Base(); corta.longitud = 2;
        rechaza(() -> ArchivoReferenciasBancarias.guardar(corta.conexion(), filtro, propuestas, "Admin"));
        comprobar(corta.filas.isEmpty() && !corta.bloqueada, "No trunca ni inserta con campos insuficientes");
        System.out.println("OK: guardado simulado, idempotencia, conflictos, cambios, longitudes y recuperación parcial.");
    }

    private static void comprobar(boolean condicion, String mensaje) { if (!condicion) throw new AssertionError(mensaje); }
    @FunctionalInterface private interface Accion { void run() throws Exception; }
    private static void rechaza(Accion accion) throws Exception {
        try { accion.run(); } catch (IllegalArgumentException ex) { return; }
        throw new AssertionError("Se aceptó una operación incompatible");
    }

    private static final class Base {
        final List<String[]> filas = new ArrayList<>();
        final Map<String, Object> cargo = new HashMap<>();
        boolean bloqueada;
        int inserciones, fallarInsercion = -1, longitud = 100;
        Base() {
            String[][] campos = {{"MAT","121000805"},{"NCAT","1"},{"GENERAR","S"},{"CODIGO","06"},
                {"IMPTMN","1993"},{"IPENMN","1993"},{"IPAGMN","0"},{"IMPMN","2215"},{"IBECMN","222"},
                {"IDSCMN","0"},{"IRECMN","0"},{"PBEC","10"},{"SECC","JDN"},{"PESC","A"},{"GRADO","1J"},
                {"GRUPO",""},{"IDCPT","207820"},{"NCPTO","COL06"},{"DCPTO","COLEGIATURA OCTUBRE"},
                {"NOMBRE","ALUMNO DE PRUEBA"},{"RECARGO","S"}};
            for (String[] campo : campos) cargo.put(campo[0], campo[1]);
            cargo.put("FVINI", java.sql.Date.valueOf("2026-10-01")); cargo.put("FVEN", java.sql.Date.valueOf("2026-10-12"));
        }
        Connection conexion() {
            return proxy(Connection.class, (obj, method, args) -> switch (method.getName()) {
                case "createStatement" -> sentencia();
                case "prepareStatement" -> preparar((String) args[0]);
                case "close" -> null;
                default -> throw new UnsupportedOperationException(method.getName());
            });
        }
        Statement sentencia() {
            return proxy(Statement.class, (obj, method, args) -> switch (method.getName()) {
                case "close", "setQueryTimeout" -> null;
                case "execute" -> {
                    String sql = (String) args[0];
                    if (sql.startsWith("LOCK TABLES")) bloqueada = true;
                    if (sql.equals("UNLOCK TABLES")) bloqueada = false;
                    yield true;
                }
                case "executeQuery" -> resultado(List.of(Map.of("Field", "REFB", "Type", "varchar(" + longitud + ")")));
                default -> throw new UnsupportedOperationException(method.getName());
            });
        }
        PreparedStatement preparar(String sql) {
            Map<Integer, String> parametros = new HashMap<>();
            return proxy(PreparedStatement.class, (obj, method, args) -> switch (method.getName()) {
                case "close" -> null;
                case "setString" -> { parametros.put((Integer) args[0], (String) args[1]); yield null; }
                case "executeQuery" -> {
                    if (sql.startsWith("SELECT c.*")) yield resultado(List.of(cargo));
                    List<Map<String, Object>> rows = new ArrayList<>();
                    for (String[] fila : filas) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 0; i < fila.length; i++) row.put(String.valueOf(i + 1), fila[i]);
                        rows.add(row);
                    }
                    yield resultado(rows);
                }
                case "executeUpdate" -> {
                    comprobar(bloqueada, "INSERT fuera del bloqueo");
                    if (++inserciones == fallarInsercion) throw new SQLException("Fallo simulado de escritura");
                    String[] fila = new String[25];
                    for (int i = 0; i < fila.length; i++) fila[i] = parametros.get(i + 1);
                    filas.add(fila); yield 1;
                }
                default -> throw new UnsupportedOperationException(method.getName());
            });
        }
    }

    private static ResultSet resultado(List<? extends Map<String, ?>> filas) {
        int[] indice = {-1};
        return proxy(ResultSet.class, (obj, method, args) -> {
            if (method.getName().equals("next")) return ++indice[0] < filas.size();
            if (method.getName().equals("close")) return null;
            Object valor = filas.get(indice[0]).get(String.valueOf(args[0]));
            return switch (method.getName()) {
                case "getString" -> valor == null ? null : valor.toString();
                case "getInt" -> valor == null ? 0 : Integer.parseInt(valor.toString());
                case "getBigDecimal" -> valor == null ? null : new BigDecimal(valor.toString());
                case "getDate" -> valor;
                default -> throw new UnsupportedOperationException(method.getName());
            };
        });
    }

    private static <T> T proxy(Class<T> tipo, java.lang.reflect.InvocationHandler handler) {
        return tipo.cast(Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo}, handler));
    }
}
