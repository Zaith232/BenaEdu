package com.mycompany.benaedu.views;

import com.mycompany.benaedu.db.ConDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

/** Regresión del alta repetida con tablas de prueba que no alteran los alumnos reales. */
public final class CatalogoAlumnosGuardadoTest {
    private CatalogoAlumnosGuardadoTest() { }

    /** Comprueba altas repetidas e inscripciones existentes en MyISAM. */
    public static void main(String[] args) throws Exception {
        try (Connection con = ConDB.getConnection(); Statement stmt = con.createStatement()) {
            String tabla = "codex_test_alumnos_" + java.util.UUID.randomUUID().toString().replace("-", "");
            stmt.execute("CREATE TABLE " + tabla + " LIKE tesalum");
            try {
                stmt.execute("ALTER TABLE " + tabla + " ENGINE=MyISAM");
                stmt.execute("CREATE TEMPORARY TABLE tesaxce (MAT VARCHAR(30), CESC VARCHAR(20)) ENGINE=MyISAM");
                try (PreparedStatement ps = con.prepareStatement(Catalogo_Alumnos.SQL_ALTA_ALUMNO.replace("tesalum", tabla))) {
                    for (int i = 1; i <= 31; i++) ps.setString(i, "");
                    ps.setString(1, "PRUEBA-DUPLICADO");
                    ps.setString(8, "2026-10-02");
                    ps.setNull(11, java.sql.Types.DATE);
                    ps.setString(31, "PRUEBA-DUPLICADO");
                    igual(1, ps.executeUpdate());
                    igual(0, ps.executeUpdate());
                    ps.setString(1, "prueba-duplicado");
                    ps.setString(31, "prueba-duplicado");
                    igual(0, ps.executeUpdate());
                    ps.setString(1, "OTRA-MATRICULA");
                    ps.setString(31, "OTRA-MATRICULA");
                    igual(1, ps.executeUpdate());
                }
                try (var rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tabla)) {
                    rs.next();
                    igual(2, rs.getInt(1));
                }
                if (Catalogo_Alumnos.existeInscripcion(con, "PRUEBA-DUPLICADO", "2026")) {
                    throw new AssertionError("La inscripción todavía no existe");
                }
                stmt.execute("INSERT INTO tesaxce (MAT,CESC) VALUES ('PRUEBA-DUPLICADO','2026')");
                if (!Catalogo_Alumnos.existeInscripcion(con, "PRUEBA-DUPLICADO", "2026")) {
                    throw new AssertionError("No se reconoció la inscripción existente");
                }
                if (Catalogo_Alumnos.existeInscripcion(con, "PRUEBA-DUPLICADO", "2027")) {
                    throw new AssertionError("Se confundieron ciclos distintos");
                }
            } finally {
                stmt.execute("DROP TABLE " + tabla);
            }
        }
        System.out.println("OK: alta única por matrícula e inscripción reconocida sin depender de UPDATE.");
    }

    private static void igual(int esperado, int actual) {
        if (esperado != actual) throw new AssertionError("Esperado " + esperado + "; obtenido " + actual);
    }
}
