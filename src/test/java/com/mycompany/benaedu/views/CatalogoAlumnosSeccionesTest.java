package com.mycompany.benaedu.views;

import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

/** Comprueba las claves del filtro de secciones sin consultar la base escolar. */
public final class CatalogoAlumnosSeccionesTest {
    private CatalogoAlumnosSeccionesTest() { }

    /** Verifica las cuatro etiquetas, Todas y su combinación con la búsqueda. */
    public static void main(String[] args) throws Exception {
        var enlazar = Catalogo_Alumnos.class.getDeclaredMethod("enlazarFiltros",
                PreparedStatement.class, String.class, String.class, int.class);
        enlazar.setAccessible(true);
        String[] etiquetas = {"", "PRESC", "PRIM", "SEC", "BACH"};
        String[] claves = {"", "JDN", "PRI", "SEC", "BAC"};
        for (String texto : new String[]{"", "Lopez"}) {
            for (int i = 0; i < etiquetas.length; i++) {
                List<String> parametros = new ArrayList<>();
                var ps = (PreparedStatement) Proxy.newProxyInstance(
                        PreparedStatement.class.getClassLoader(), new Class<?>[]{PreparedStatement.class},
                        (proxy, metodo, valores) -> {
                            if (!"setString".equals(metodo.getName())) throw new AssertionError(metodo);
                            if (!Integer.valueOf(parametros.size() + 1).equals(valores[0])) {
                                throw new AssertionError("Orden de parámetros incorrecto");
                            }
                            parametros.add((String) valores[1]);
                            return null;
                        });
                List<String> esperados = new ArrayList<>();
                if (!texto.isEmpty()) for (int j = 0; j < 5; j++) esperados.add("%Lopez%");
                if (!claves[i].isEmpty()) esperados.add(claves[i]);
                int siguiente = (Integer) enlazar.invoke(null, ps, texto, etiquetas[i], 1);
                if (!esperados.equals(parametros) || siguiente != esperados.size() + 1) {
                    throw new AssertionError(etiquetas[i] + ": " + parametros);
                }
            }
        }
        System.out.println("OK: PRESC, PRIM, SEC y BACH enlazan las claves escolares; Todas y búsqueda compatibles.");
    }
}
