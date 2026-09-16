# Interfase Bancaria BCM

Acceso: Escolar → Utilerías → Interfase Bancaria BCM. Requiere sesión autenticada y, para aplicar, un único cajero activo del usuario en `tescaj`.

## Uso

1. Seleccionar compañía y banco receptor del catálogo. Revisar la cuenta (valor inicial `0160839167`). El EXP de muestra no contiene un encabezado con cuenta/convenio: el operador debe verificar que corresponde a esa cuenta.
2. Seleccionar las equivalencias de **EFE** y **CCT** en el catálogo escolar `tmclas/IPAG`. No se inventan códigos de formas de pago ni de bancos.
3. Buscar el `.exp` y pulsar **Cargar y revisar**. Esta acción solo consulta. Se muestra el total del archivo y el total listo para aplicar, que pueden ser diferentes.
4. Revisar cada estado: **LISTO**, **YA REGISTRADO**, **REVISAR**. Referencias desconocidas/duplicadas, importes distintos, abonos, datos inconsistentes o pagos fuera de vigencia no se aplican.
5. **Aplicar pagos** pide confirmación con cantidad, importe, compañía y cuenta. Solo aplica los movimientos LISTO. Al finalizar, recargar el archivo para comprobar el estado persistido.

## Formato y compatibilidad

El archivo facilitado contiene 202 movimientos y $547,148.00, con seis campos repetidos separados por `|`:

`fecha | forma | guía | referencia | descripción | importe`

La fecha puede tener cinco o seis dígitos (80526 → 08/05/2026). El importe está expresado en pesos, no en centavos implícitos. Se admite descripción vacía. No se usa la descripción para identificar al alumno. Cualquier registro malformado impide cargar el archivo completo.

La referencia original se conserva. Se busca una coincidencia única en `tesrefb`, permitiendo únicamente el cero inicial adicional observado en el EXP. No se deduce el alumno cortando la referencia ni se recalculan referencias históricas con otro algoritmo.

## Efectos de Aplicar

- Un recibo por movimiento en `tesralu`, con fechas del archivo y folio correlativo por compañía.
- Instrumento de pago en `tespalu`: guía en `IDPAG`, fecha bancaria en `FDEP`/`FPAG`, referencia escolar en `REFPAG`, banco, cuenta, usuario y cajero.
- Actualización de total, pagado y saldo del cargo en `tescalu`. Para referencia R se registra el recargo efectivo validado; no se pierde la diferencia como sobrepago.
- Marca `SREF=P` en las referencias pendientes del mismo cargo, para impedir otro cobro por la alternativa normal/recargo.
- No altera esquemas, no genera facturas ni contabiliza pólizas. No procesa pagos parciales, referencias D, monedas distintas de MXP o formas bancarias desconocidas.

## Duplicados y fallos

Se cotejan guía, fecha, banco, cuenta y compañía, además del cargo, importe, recibo y estado de referencia. Un mismo archivo renombrado no permite aplicar otra vez los mismos pagos. Se bloquean guías repetidas dentro del archivo y varios movimientos para un mismo cargo.

La aplicación usa el bloqueo de folio compartido con Cobranza y bloqueo de tablas; necesita permisos SELECT/INSERT/UPDATE/LOCK TABLES. Revalida todo el lote antes de escribir. MyISAM no ofrece rollback: cada movimiento reserva primero un pago con `SPAG=BCM_PEND`; únicamente se limpia al completar recibo, cargo y referencias. Si falla un paso, se detiene el lote, se informa la guía y los completados, y una nueva revisión bloquea el movimiento incompleto. **No se debe borrar el marcador ni forzar el reintento sin conciliar los registros.** Los reportes existentes pueden requerir revisión manual ante una aplicación parcial.

## Pruebas

Con JDK 25, compilar `mvn -o test-compile` y ejecutar:

```powershell
java -cp 'target/classes;target/test-classes' com.mycompany.benaedu.services.ArchivoBcmTest
java -cp 'target/classes;target/test-classes' com.mycompany.benaedu.services.InterfaseBcmTest
java '-Djava.awt.headless=true' -cp 'target/classes;target/test-classes' com.mycompany.benaedu.views.InterfaseBcmPantallaTest
```

El lector admite como argumento opcional la ruta de `5EYV8ULA.exp` para verificar sus 202 registros y total. Las escrituras se probaron con JDBC simulado, incluyendo interrupciones en cada paso y reimportación. No se aplicaron los pagos del archivo real. Falta una prueba controlada de aplicación en el entorno de la escuela y confirmar las equivalencias de formas de pago antes de producción.
