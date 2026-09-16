package com.mycompany.benaedu.services;

import com.mycompany.benaedu.db.ConDB;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Consulta cargos y calcula referencias BBVA 10; la persistencia se solicita por separado. */
public final class ReferenciasBancarias {
    private ReferenciasBancarias() { }

    /** Combinación existente de compañía, centro y ciclo. */
    public record Ciclo(String cia, String cc, String ciclo) { }

    /**
     * Obtiene el 31 de agosto del año final de un ciclo consecutivo AABB (por ejemplo, 2627).
     * @param ciclo código escolar cuyo año inicial se interpreta como 20AA
     * @return vencimiento del recargo independiente de la fecha del equipo
     * @throws IllegalArgumentException si el código no identifica dos años consecutivos
     */
    public static LocalDate vencimientoRecargo(String ciclo) {
        if (ciclo == null || !ciclo.matches("[0-9]{4}"))
            throw new IllegalArgumentException("El ciclo debe tener formato AABB, por ejemplo 2627.");
        int inicio = Integer.parseInt(ciclo.substring(0, 2));
        int fin = Integer.parseInt(ciclo.substring(2));
        if ((inicio + 1) % 100 != fin)
            throw new IllegalArgumentException("El ciclo debe identificar años consecutivos.");
        return LocalDate.of(2000 + inicio + 1, 8, 31);
    }

    /** Alumno y grado presentes en los cargos del ámbito escolar seleccionado. */
    public record Alumno(String matricula, String nombre, String grado) {
        /** Busca palabras de matrícula o nombre sin distinguir mayúsculas ni acentos. */
        public boolean coincide(String busqueda) {
            String texto = normalizar(matricula + " " + nombre);
            for (String palabra : normalizar(busqueda).split("\\s+"))
                if (!texto.contains(palabra)) return false;
            return true;
        }

        private static String normalizar(String texto) {
            return java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
                    .replaceAll("\\p{M}+", "").toLowerCase(java.util.Locale.ROOT).trim();
        }
    }

    /** Consulta alumnos y grados con cargos en compañía, centro y ciclo; no modifica datos. */
    public static List<Alumno> alumnos(Ciclo ciclo) throws Exception {
        List<Alumno> alumnos = new ArrayList<>();
        String sql = "SELECT DISTINCT c.MAT,COALESCE(a.NOMBRE,'') NOMBRE,COALESCE(c.GRADO,'') GRADO "
                + "FROM tescalu c LEFT JOIN (SELECT MAT,MAX(COALESCE(NULLIF(NOMCOM,''),"
                + "CONCAT_WS(' ',APATE,AMATE,NOMA))) NOMBRE FROM tesalum GROUP BY MAT) a ON a.MAT=c.MAT "
                + "WHERE c.CIA=? AND c.CC=? AND c.CESC=? AND COALESCE(TRIM(c.MAT),'')<>'' "
                + "ORDER BY NOMBRE,c.MAT,GRADO";
        try (Connection con = ConDB.getConnection(); var ps = con.prepareStatement(sql)) {
            ps.setString(1, ciclo.cia()); ps.setString(2, ciclo.cc()); ps.setString(3, ciclo.ciclo());
            try (var rs = ps.executeQuery()) {
                while (rs.next()) alumnos.add(new Alumno(rs.getString(1).trim(), rs.getString(2), rs.getString(3).trim()));
            }
        }
        return List.copyOf(alumnos);
    }

    /** Parámetros inmutables de una consulta y de la regla provisional de recargo. */
    public record Filtro(Ciclo ciclo, String matricula, String grado, boolean recargo,
                         LocalDate finRecargo) { }

    /** Datos del cargo necesarios para identificar y auditar la referencia en tesrefb. */
    public record Cargo(String seccion, String periodo, String grado, String grupo, int id,
                        String concepto, BigDecimal porcentajeBeca, BigDecimal importeBeca) { }

    /** Referencia calculada en memoria; guardar una referencia no registra un pago. */
    public record Referencia(String matricula, String nombre, String concepto, String tipo,
                             LocalDate inicio, LocalDate fin, BigDecimal importe, String referencia, Cargo cargo) { }

    /** Resultado y motivos de los cargos que requieren revisión manual. */
    public record Resultado(List<Referencia> referencias, List<String> avisos) { }

    /** Devuelve los ciclos disponibles, sin asumir un año o centro fijo. */
    public static List<Ciclo> ciclos() throws Exception {
        List<Ciclo> ciclos = new ArrayList<>();
        try (Connection con = ConDB.getConnection();
             var ps = con.prepareStatement("SELECT DISTINCT CIA,CC,CESC FROM tescesc "
                     + "WHERE CIA IS NOT NULL AND CC IS NOT NULL AND CESC IS NOT NULL "
                     + "ORDER BY CIA,CC,CESC DESC"); var rs = ps.executeQuery()) {
            while (rs.next()) ciclos.add(new Ciclo(rs.getString(1), rs.getString(2), rs.getString(3)));
        }
        return List.copyOf(ciclos);
    }

    /**
     * Calcula BBVA 10: fecha condensada, importe en centavos y comprobación módulo 97.
     * @param base matrícula concatenada con CODREF, conservando ceros
     * @param importe importe positivo con hasta dos decimales
     * @param fecha vencimiento bancario, no fecha de inicio del recargo
     * @return referencia completa
     * @throws IllegalArgumentException si los datos exceden el formato del algoritmo
     */
    public static String calcular(String base, BigDecimal importe, LocalDate fecha) {
        if (base == null || !base.matches("[0-9]{1,12}"))
            throw new IllegalArgumentException("Matrícula + CODREF debe tener de 1 a 12 dígitos.");
        if (importe == null || importe.signum() <= 0
                || importe.compareTo(new BigDecimal("999999999.99")) > 0)
            throw new IllegalArgumentException("Importe fuera del rango BBVA 10.");
        String centavos;
        try {
            centavos = importe.setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).toBigIntegerExact().toString();
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("El importe debe tener como máximo dos decimales.", ex);
        }
        if (fecha == null || fecha.isBefore(LocalDate.of(2014, 11, 19)))
            throw new IllegalArgumentException("La fecha BBVA 10 debe ser desde el 19/11/2014.");
        long condensada = 372L * (fecha.getYear() - 2014) + 31L * (fecha.getMonthValue() - 1) + fecha.getDayOfMonth() - 1;
        if (condensada > 9999) throw new IllegalArgumentException("La fecha excede los cuatro dígitos BBVA 10.");
        String cuerpo = base + String.format(java.util.Locale.ROOT, "%04d", condensada)
                + (ponderar(centavos, new int[]{7, 3, 1}) % 10) + "2";
        return cuerpo + String.format(java.util.Locale.ROOT, "%02d", ponderar(cuerpo, new int[]{11, 13, 17, 19, 23}) % 97 + 1);
    }

    private static int ponderar(String numero, int[] factores) {
        int suma = 0;
        for (int i = numero.length() - 1, j = 0; i >= 0; i--, j++)
            suma += (numero.charAt(i) - '0') * factores[j % factores.length];
        return suma;
    }

    /** Aplica la beca y elimina los decimales del importe resultante, según la regla indicada. */
    public static BigDecimal conBeca(BigDecimal bruto, BigDecimal porcentaje) {
        if (bruto == null || porcentaje == null || bruto.signum() <= 0 || porcentaje.signum() < 0
                || porcentaje.compareTo(new BigDecimal("100")) > 0)
            throw new IllegalArgumentException("Importe o porcentaje de beca inválido.");
        return bruto.multiply(BigDecimal.ONE.subtract(porcentaje.movePointLeft(2))).setScale(0, RoundingMode.DOWN);
    }

    /** Aplica 10 por ciento sobre el importe normal entero y elimina decimales, sin redondear. */
    public static BigDecimal conRecargo(BigDecimal normal) {
        if (normal == null || normal.signum() <= 0) throw new IllegalArgumentException("Importe normal inválido.");
        return normal.setScale(0, RoundingMode.DOWN).multiply(new BigDecimal("1.10")).setScale(0, RoundingMode.DOWN);
    }

    /**
     * Genera exclusivamente borradores de cargos pendientes sin abonos ni ajustes ambiguos.
     * Los catálogos duplicados y las bases repetidas se bloquean, no se elige una fila al azar.
     * @param filtro ámbito escolar y filtros opcionales
     * @return referencias en memoria y advertencias
     */
    public static Resultado generar(Filtro filtro) throws Exception {
        try (Connection con = ConDB.getConnection()) { return generar(con, filtro); }
    }

    static Resultado generar(Connection con, Filtro filtro) throws Exception {
        if (filtro == null || filtro.ciclo() == null) throw new IllegalArgumentException("Seleccione compañía, centro y ciclo.");
        if (filtro.recargo() && filtro.finRecargo() == null) throw new IllegalArgumentException("Capture el vencimiento bancario del recargo.");
        List<Referencia> referencias = new ArrayList<>();
        List<String> avisos = new ArrayList<>();
        Set<String> bases = new HashSet<>();
        Set<String> duplicadas = new HashSet<>();
        String sql = "SELECT c.*, p.CODIGO,p.GENERAR,p.RECARGO,p.NCAT, a.NOMBRE FROM tescalu c "
                + "LEFT JOIN (SELECT CIA,CC,SECC,NCPTO,MIN(CODREF) CODIGO,MIN(GENREF) GENERAR,"
                + "MIN(APLREC) RECARGO,COUNT(*) NCAT FROM tescpto GROUP BY CIA,CC,SECC,NCPTO) p "
                + "ON p.CIA=c.CIA AND p.CC=c.CC AND p.SECC=c.SECC AND p.NCPTO=c.NCPTO "
                + "LEFT JOIN (SELECT MAT,MAX(COALESCE(NULLIF(NOMCOM,''),"
                + "CONCAT_WS(' ',APATE,AMATE,NOMA))) NOMBRE FROM tesalum GROUP BY MAT) a "
                + "ON a.MAT=c.MAT "
                + "WHERE c.CIA=? AND c.CC=? AND c.CESC=? AND COALESCE(TRIM(c.MCAN),'')='' "
                + "AND c.IPENMN>0 AND (?='' OR c.MAT=?) AND (?='' OR c.GRADO=?) ORDER BY c.MAT,c.FVEN,c.IDCPT";
        try (var ps = con.prepareStatement(sql)) {
            ps.setString(1, filtro.ciclo().cia()); ps.setString(2, filtro.ciclo().cc()); ps.setString(3, filtro.ciclo().ciclo());
            ps.setString(4, filtro.matricula()); ps.setString(5, filtro.matricula());
            ps.setString(6, filtro.grado()); ps.setString(7, filtro.grado());
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    String mat = java.util.Objects.toString(rs.getString("MAT"), "").trim();
                    String etiqueta = mat + " / " + rs.getString("NCPTO") + " / cargo " + rs.getString("IDCPT");
                    try {
                        if (rs.getInt("NCAT") != 1) throw new IllegalArgumentException("Catálogo ausente o duplicado.");
                        if (!"S".equalsIgnoreCase(rs.getString("GENERAR"))) continue;
                        String codigo = rs.getString("CODIGO");
                        if (codigo == null || !codigo.trim().matches("[0-9]+")) throw new IllegalArgumentException("CODREF vacío o no numérico.");
                        String base = mat + codigo.trim();
                        if (!bases.add(base)) duplicadas.add(base);
                        BigDecimal total = rs.getBigDecimal("IMPTMN");
                        BigDecimal saldo = rs.getBigDecimal("IPENMN");
                        BigDecimal pagado = rs.getBigDecimal("IPAGMN");
                        BigDecimal bruto = rs.getBigDecimal("IMPMN");
                        BigDecimal beca = rs.getBigDecimal("IBECMN");
                        BigDecimal descuento = rs.getBigDecimal("IDSCMN");
                        BigDecimal recargo = rs.getBigDecimal("IRECMN");
                        validarImportes(total, saldo, pagado, bruto, beca, descuento, recargo);
                        BigDecimal porcentajeBeca = rs.getBigDecimal("PBEC");
                        BigDecimal normal = conBeca(bruto, porcentajeBeca);
                        if (normal.compareTo(total) != 0)
                            throw new IllegalArgumentException("El cargo no coincide con la beca truncada; revise su importe antes de emitir.");
                        Cargo cargo = new Cargo(rs.getString("SECC"), rs.getString("PESC"), rs.getString("GRADO"),
                                rs.getString("GRUPO"), rs.getInt("IDCPT"), rs.getString("NCPTO"), porcentajeBeca, bruto.subtract(normal));
                        Date inicioSql = rs.getDate("FVINI"), finSql = rs.getDate("FVEN");
                        if (inicioSql == null || finSql == null) throw new IllegalArgumentException("Faltan fechas del cargo.");
                        LocalDate inicio = inicioSql.toLocalDate(), fin = finSql.toLocalDate();
                        if (inicio.isAfter(fin)) throw new IllegalArgumentException("Vigencia del cargo invertida.");
                        String nombre = rs.getString("NOMBRE"), concepto = rs.getString("NCPTO") + " - " + rs.getString("DCPTO");
                        referencias.add(new Referencia(mat, nombre, concepto, "Normal", inicio, fin, normal, calcular(base, normal, fin), cargo));
                        if (filtro.recargo() && "S".equalsIgnoreCase(rs.getString("RECARGO"))) {
                            if (!filtro.finRecargo().isAfter(fin)) {
                                avisos.add(etiqueta + ": no se calculó recargo; su vencimiento debe ser posterior al normal.");
                            } else {
                                BigDecimal importe = conRecargo(normal);
                                referencias.add(new Referencia(mat, nombre, concepto, "Recargo", fin.plusDays(1), filtro.finRecargo(),
                                        importe, calcular(base, importe, filtro.finRecargo()), cargo));
                            }
                        }
                    } catch (IllegalArgumentException ex) {
                        avisos.add(etiqueta + ": " + ex.getMessage());
                    }
                }
            }
        }
        if (!duplicadas.isEmpty()) {
            referencias.removeIf(r -> duplicadas.contains(r.referencia().substring(0, r.referencia().length() - 8)));
            for (String base : duplicadas) avisos.add("Base repetida " + base + ": se omitieron todos sus cargos; revise matrícula/CODREF.");
        }
        return new Resultado(List.copyOf(referencias), List.copyOf(avisos));
    }

    static void validarImportes(BigDecimal total, BigDecimal saldo, BigDecimal pagado, BigDecimal bruto,
                                BigDecimal beca, BigDecimal descuento, BigDecimal recargo) {
        if (total == null || saldo == null || pagado == null || bruto == null || beca == null
                || descuento == null || recargo == null) throw new IllegalArgumentException("Importes incompletos.");
        if (total.signum() <= 0 || bruto.signum() <= 0 || beca.signum() < 0)
            throw new IllegalArgumentException("Importes negativos o sin cargo por cobrar.");
        if (pagado.signum() != 0 || saldo.compareTo(total) != 0)
            throw new IllegalArgumentException("Tiene abonos o saldo distinto del total; requiere revisión.");
        if (descuento.signum() != 0 || recargo.signum() != 0 || bruto.subtract(beca).compareTo(total) != 0)
            throw new IllegalArgumentException("Tiene ajustes de descuento/recargo o total inconsistente.");
    }
}
