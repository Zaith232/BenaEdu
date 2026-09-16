package com.mycompany.benaedu.services;

import com.mycompany.benaedu.services.ArchivoBcm.Movimiento;
import com.mycompany.benaedu.services.InterfaseBcm.*;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/** Pruebas JDBC simuladas: ninguna inserción se ejecuta en una base real. */
public final class InterfaseBcmTest {
    private InterfaseBcmTest() { }
    private static final Configuracion CFG = new Configuracion("12", "BBVA", "0160839167", "EF", "TR", "Admin");
    private static final Movimiento NORMAL = new Movimiento(1, LocalDate.of(2026, 10, 10), "EFE", 123,
            "01210008050647544283", "PRUEBA", new BigDecimal("1993.00"));

    /** Comprueba aplicación normal/recargo, repetición, conflictos y fallos parciales MyISAM. */
    public static void main(String[] args) throws Exception {
        Base b = new Base(); var con = b.conexion();
        var revision = InterfaseBcm.revisar(con, CFG, List.of(NORMAL));
        exigir(revision.get(0).listo() && b.escrituras == 0, "Revisión no debe escribir");
        var resultado = InterfaseBcm.aplicar(con, CFG, List.of(NORMAL), revision);
        exigir(resultado.aplicados() == 1 && resultado.incidencia().isEmpty(), "Pago normal");
        exigir(b.pagos.size() == 1 && b.recibos.size() == 1 && b.cargo.get("IPENMN").equals("0"), "Registro integral");
        exigir(!b.tablas && !b.folio && b.ref.get("SREF").equals("P"), "Bloqueos liberados y referencia pagada");
        var repetido = InterfaseBcm.revisar(con, CFG, List.of(NORMAL));
        exigir(repetido.get(0).estado().equals("YA REGISTRADO"), "Reimportación detectada");
        InterfaseBcm.aplicar(con, CFG, List.of(NORMAL), repetido);
        exigir(b.pagos.size() == 1 && b.recibos.size() == 1, "No duplicar pagos");

        Base recargo = new Base(); recargo.ref.put("TREF", "R"); recargo.ref.put("FRINI", "2026-10-13");
        recargo.ref.put("FREF", "2027-08-31"); recargo.ref.put("IMPMN", "2192");
        var mora = new Movimiento(1, LocalDate.of(2026, 10, 13), "CCT", 124, NORMAL.referencia(), "", new BigDecimal("2192"));
        var revisarMora = InterfaseBcm.revisar(recargo.conexion(), CFG, List.of(mora));
        exigir(revisarMora.get(0).listo(), "Recargo listo");
        InterfaseBcm.aplicar(recargo.conexion(), CFG, List.of(mora), revisarMora);
        exigir(new BigDecimal(recargo.cargo.get("IRECMN")).compareTo(new BigDecimal("199")) == 0, "Recargo efectivo correcto");

        Base invalido = new Base();
        exigir(!InterfaseBcm.revisar(invalido.conexion(), CFG, List.of(NORMAL, NORMAL)).get(0).listo(), "Duplicado interno");
        invalido.cargo.put("IPAGMN", "10");
        exigir(!InterfaseBcm.revisar(invalido.conexion(), CFG, List.of(NORMAL)).get(0).listo(), "No aplicar abonos");
        invalido.cargo.put("IPAGMN", "0"); invalido.ref.put("IMPMN", "2000");
        exigir(!InterfaseBcm.revisar(invalido.conexion(), CFG, List.of(NORMAL)).get(0).listo(), "Importe distinto");
        invalido.ref.put("IMPMN", "1993"); invalido.ref.put("FREF", "2026-10-09");
        exigir(!InterfaseBcm.revisar(invalido.conexion(), CFG, List.of(NORMAL)).get(0).listo(), "Fuera de vigencia");

        for (int fallo = 2; fallo <= 5; fallo++) {
            Base parcial = new Base(); parcial.fallar = fallo;
            var antes = InterfaseBcm.revisar(parcial.conexion(), CFG, List.of(NORMAL));
            var detenido = InterfaseBcm.aplicar(parcial.conexion(), CFG, List.of(NORMAL), antes);
            exigir(detenido.aplicados() == 0 && !detenido.incidencia().isEmpty(), "No anunciar éxito parcial");
            exigir(!parcial.tablas && !parcial.folio && parcial.pagos.get(0).get("SPAG").equals("BCM_PEND"), "Reserva pendiente conservada");
            exigir(!InterfaseBcm.revisar(parcial.conexion(), CFG, List.of(NORMAL)).get(0).listo(), "No duplicar tras fallo parcial");
        }
        Base cambio = new Base(); var antes = InterfaseBcm.revisar(cambio.conexion(), CFG, List.of(NORMAL));
        cambio.cargo.put("IPAGMN", "1");
        try { InterfaseBcm.aplicar(cambio.conexion(), CFG, List.of(NORMAL), antes); throw new AssertionError("Aplicó datos modificados"); }
        catch (IllegalArgumentException esperado) { exigir(cambio.escrituras == 0 && !cambio.tablas, "Revalidación sin escrituras"); }
        System.out.println("OK: revisión, aplicación, recargo, duplicados, cambios y fallos parciales (JDBC simulado).");
    }

    private static void exigir(boolean cumple, String mensaje) { if (!cumple) throw new AssertionError(mensaje); }
    private static Map<String, String> mapa(String... pares) {
        Map<String, String> r = new LinkedHashMap<>(); for (int i = 0; i < pares.length; i += 2) r.put(pares[i], pares[i + 1]); return r;
    }
    private static final class Base {
        boolean tablas, folio; int escrituras, fallar = -1;
        final List<Map<String, String>> pagos = new ArrayList<>(), recibos = new ArrayList<>();
        final Map<String, String> ref = mapa("CIA","12","CC","12100","CESC","2627","MAT","121000805","IDCPT","207820",
                "NCPTO","COL06","REFB","1210008050647544283","SREF","","TREF","N","FRINI","2026-10-01","FREF","2026-10-12",
                "IMPMN","1993","PBEC","10","IBECMN","222","PDSC","0","IDSCMN","0");
        final Map<String, String> cargo = mapa("CIA","12","CC","12100","CESC","2627","MAT","121000805","IDCPT","207820",
                "NCPTO","COL06","MCAN","","IMPMN","2215","IMPTMN","1993","IPAGMN","0","IPENMN","1993","IRECMN","0",
                "PREC","0","PBEC","10","IBECMN","222","PDSC","0","IDSCMN","0","FVEN","2026-10-12","CMON","MXP","TCAMB","1",
                "SECC","JDN","TALU","JDN","GRADO","1J","GRUPO","A","TCPTO","C","DCPTO","COLEGIATURA OCTUBRE","TCONT","O",
                "CUNIMN","2215","CANT","1","TDSC","","TBECA","B","CBECA","BINT10");
        Connection conexion() {
            return proxy(Connection.class, (o,m,a) -> switch(m.getName()) {
                case "prepareStatement" -> preparar((String)a[0]);
                case "createStatement" -> proxy(Statement.class, (o2,m2,a2) -> {
                    if (m2.getName().equals("close")) return null;
                    if (m2.getName().equals("execute")) { String sql=(String)a2[0]; if(sql.startsWith("LOCK")) tablas=true; if(sql.startsWith("UNLOCK")) tablas=false; return true; }
                    throw new UnsupportedOperationException(m2.getName());
                });
                case "close" -> null;
                default -> throw new UnsupportedOperationException(m.getName());
            });
        }
        PreparedStatement preparar(String sql) {
            Map<Integer,Object> p = new HashMap<>();
            return proxy(PreparedStatement.class, (o,m,a) -> switch(m.getName()) {
                case "setObject" -> { p.put((Integer)a[0],a[1]); yield null; }
                case "close" -> null;
                case "executeQuery" -> resultado(consultar(sql,p));
                case "executeUpdate" -> escribir(sql,p);
                default -> throw new UnsupportedOperationException(m.getName());
            });
        }
        List<Map<String,String>> consultar(String sql, Map<Integer,Object> p) {
            if(sql.contains("GET_LOCK")) { folio=true; return List.of(mapa("OK","1")); }
            if(sql.contains("RELEASE_LOCK")) { folio=false; return List.of(mapa("OK","1")); }
            if(sql.startsWith("SHOW FULL")) return List.of(mapa("FIELD","REFPAG","TYPE","varchar(25)"));
            if(sql.contains("MAX(NREC)")) return List.of(mapa("NREC",String.valueOf(1 + Math.max(pagos.size(),recibos.size()))));
            if(sql.contains("FROM tmclas")) return List.of(mapa("CVE",p.get(1).toString()));
            if(sql.contains("FROM tescaj")) return List.of(mapa("NEMP","80"));
            if(sql.contains("FROM tesalum")) return List.of(mapa("NOMCOM","ALUMNO FICTICIO","APATE","","AMATE","","NOMA",""));
            if(sql.contains("FROM tesrefb")) return List.of(new LinkedHashMap<>(ref));
            if(sql.contains("FROM tescalu")) return List.of(new LinkedHashMap<>(cargo));
            if(sql.contains("FROM tespalu")) return pagos.stream().map(LinkedHashMap::new).map(x -> (Map<String,String>)x).toList();
            if(sql.contains("FROM tesralu")) return recibos.stream().map(LinkedHashMap::new).map(x -> (Map<String,String>)x).toList();
            throw new AssertionError(sql);
        }
        int escribir(String sql, Map<Integer,Object> p) throws SQLException {
            exigir(tablas && folio, "Escritura sin bloqueos");
            if(++escrituras == fallar) throw new SQLException("Fallo simulado");
            if(sql.startsWith("INSERT")) {
                String[] cols=sql.substring(sql.indexOf('(')+1,sql.indexOf(')')).split(","); Map<String,String> fila=new LinkedHashMap<>();
                for(int i=0;i<cols.length;i++) fila.put(cols[i], Objects.toString(p.get(i+1),""));
                if(sql.contains("tespalu")) pagos.add(fila); else recibos.add(fila);
            } else if(sql.startsWith("UPDATE tescalu")) {
                cargo.put("IMPTMN",p.get(1).toString()); cargo.put("IPAGMN",p.get(2).toString()); cargo.put("IPENMN","0");
                cargo.put("IRECMN",p.get(3).toString()); cargo.put("PREC",p.get(4).toString());
            } else if(sql.startsWith("UPDATE tesrefb")) ref.put("SREF","P");
            else if(sql.startsWith("UPDATE tespalu")) pagos.get(pagos.size()-1).put("SPAG","");
            else throw new AssertionError(sql);
            return 1;
        }
    }
    private static ResultSet resultado(List<Map<String,String>> filas) {
        int[] i={-1}; List<String> columnas=filas.isEmpty()?List.of():new ArrayList<>(filas.get(0).keySet());
        return proxy(ResultSet.class,(o,m,a) -> switch(m.getName()) {
            case "next" -> ++i[0]<filas.size();
            case "close" -> null;
            case "getString" -> filas.get(i[0]).get(a[0] instanceof Integer n?columnas.get(n-1):a[0].toString());
            case "getMetaData" -> proxy(ResultSetMetaData.class,(o2,m2,a2) -> switch(m2.getName()) {
                case "getColumnCount" -> columnas.size(); case "getColumnLabel" -> columnas.get((Integer)a2[0]-1);
                default -> throw new UnsupportedOperationException(m2.getName());
            });
            default -> throw new UnsupportedOperationException(m.getName());
        });
    }
    private static <T> T proxy(Class<T> tipo, java.lang.reflect.InvocationHandler handler) {
        return tipo.cast(Proxy.newProxyInstance(tipo.getClassLoader(),new Class<?>[]{tipo},handler));
    }
}
