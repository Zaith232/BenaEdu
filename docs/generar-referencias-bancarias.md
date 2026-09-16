# Generar Referencias Bancarias

Acceso: **Escolar → Utilerías → Generar Referencias Bancarias**, o desde el buscador del Dashboard.

1. Seleccionar compañía, centro y ciclo (se consultan en `tescesc`).
2. Usar **Buscar alumno…** para buscar por matrícula o apellidos (sin distinguir acentos o mayúsculas) y seleccionar una fila. **Todos** quita la matrícula. Grado es una lista con opción **Todos**. Los alumnos y grados se consultan entre los cargos del ámbito seleccionado; al cambiar ciclo/centro se recargan y al cambiar grado se limpia el alumno anterior. Los filtros sin selección incluyen todos los cargos elegibles del ámbito.
3. Revisar cuenta `0160839167` y convenio CIE `881686`. Son los valores provisionales compartidos de la captura; las ediciones duran mientras la pantalla permanece abierta.
4. El recargo se incluye automáticamente cuando el concepto permite aplicarlo. Su vencimiento se calcula internamente: `2627` → `2027-08-31`, `2728` → `2028-08-31`. No hay casilla ni campo de fecha. Un código escolar no interpretable bloquea la operación con un mensaje; no se inventa una fecha.
5. En **Impresión General**, pulsar **Genera** para calcular y guardar. Se confirma el número de registros antes de insertarlos y se audita el usuario. No hay un botón de guardado separado.
6. La pestaña **Referencias** consulta las guardadas compatibles con los filtros y muestra sus advertencias. No inserta registros.
7. **Imprimir** consulta y verifica los saldos y referencias guardadas antes de abrir la impresión vertical, una ficha por alumno. Puede reimprimir sin generar nuevamente. No existe casilla de confirmación bancaria; cuenta y convenio se mantienen visibles. La impresora PDF de Windows permite obtener el PDF.

## Alcance y seguridad

- Consultar e imprimir solo leen. **Genera** calcula y, previa confirmación del lote, inserta registros en `tesrefb`: **no modifica estructuras, cargos ni pagos**.
- La ficha imprimible exige referencias guardadas y saldos verificados. La eliminación de la casilla bancaria no equivale a una validación del banco.
- Normal: bruto menos porcentaje de beca, truncado a pesos enteros; debe coincidir con `IMPTMN`. Si el cargo conserva centavos o difiere, se bloquea para no dejar saldos residuales inadvertidos. Fechas `FVINI` / `FVEN` de `tescalu`.
- Recargo: normal × 1.10, truncado a pesos enteros (DOWN), desde el día siguiente al vencimiento normal, solo con `APLREC=S`.
- Base: matrícula + `tescpto.CODREF`, sin convertir a número ni perder ceros; requiere `GENREF=S` y catálogo único por compañía, centro, sección y concepto.
- Solo cargos no cancelados y con saldo positivo. Se bloquean abonos, descuentos/recargos ya aplicados, importes incompletos/inconsistentes, catálogos ambiguos y bases repetidas dentro de la consulta.
- No reconstruye catálogos ausentes ni importa información de los SQL entregados.
- Repetir un guardado no duplica referencias idénticas. Referencias pagadas, duplicadas o con otros datos/fechas/importes se bloquean: no se sustituyen ni eliminan. La consulta no imprime históricos incompatibles; no es un reporte completo de todo tesrefb.
- El guardado necesita permisos SELECT, INSERT y LOCK TABLES. Bloquea brevemente tesrefb y los datos de origen para evitar cambios concurrentes, vuelve a calcular y verifica longitudes antes del primer INSERT.
- MyISAM no revierte un lote: si falla una inserción, el mensaje indica las inserciones confirmadas y pide consultar antes de reintentar. El reintento reconoce las existentes; no se realizan reintentos automáticos. No se garantiza recuperación de la última operación si se perdió la conexión sin respuesta.
- La cuenta y convenio no tienen columnas en tesrefb: se revisan en cada emisión. No se inventan nuevas columnas para persistirlos.
- Quedan fuera los pagos parciales, sustitución de referencias históricas, descuentos distintos de becas y reparación de catálogos. El procesamiento/conciliación de archivos bancarios pertenece a otro módulo.

## Comprobación reproducible

Compilar con JDK 25 y Maven: `mvn -o test-compile`.

Ejecutar desde la raíz en PowerShell:

```powershell
java -cp 'target/classes;target/test-classes' com.mycompany.benaedu.services.ReferenciasBancariasTest
java -cp 'target/classes;target/test-classes' com.mycompany.benaedu.services.ArchivoReferenciasBancariasTest
java '-Djava.awt.headless=true' -cp 'target/classes;target/test-classes' com.mycompany.benaedu.services.FichaReferenciasBancariasTest
```

Estas pruebas no conectan a la base. Comprueban referencias conocidas, truncamiento, fechas y rechazo de cargos ambiguos. El guardado se prueba con JDBC simulado: duplicados, estados pagados, cambios de cargo, longitudes y fallos parciales. La ficha se renderiza con datos ficticios y se comprueba su paginación.

El argumento opcional `--db-read-only` habilita consultas de integración a la conexión configurada en el proyecto para el ciclo de prueba 2627; requiere agregar el JAR de MySQL al classpath. No realiza escrituras.

La última comprobación de integración quedó pendiente porque MySQL rechazó la conexión. Antes de producción, probar el guardado con un caso controlado y validar una ficha con el banco. Durante este desarrollo no se insertaron referencias reales.
