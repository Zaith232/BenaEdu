from preparar import ROOT, WORK
from pathlib import Path
from collections import Counter
import json, csv, shutil

OUT=ROOT/'database/migraciones/kcsdatos-20261010'
report=json.loads((OUT/'resumen.json').read_text(encoding='utf-8'))
validation=json.loads((OUT/'validacion.json').read_text(encoding='utf-8'))
total=validation['totales']
reasons=Counter()
for file in (OUT/'pendientes').glob('*.csv'):
    if file.name.endswith('-existentes.csv'):
        continue
    with file.open(encoding='utf-8-sig',newline='') as f:
        reader=csv.DictReader(f)
        if 'motivo' in reader.fieldnames:
            reasons.update(row['motivo'] for row in reader)
        else:
            reasons['Estructura incompatible']+=sum(1 for _ in reader)
assert sum(reasons.values())==total['pendientes']
(OUT/'motivos-pendientes.json').write_text(json.dumps(reasons,ensure_ascii=False,indent=2),encoding='utf-8')
lines=['# Combinación de kcsdatos con BenaEdu — 10 de octubre de 2026','',
'Resultado preparado en una instancia temporal de MariaDB 10.11.16, puerto 33419. La base local en 3306 no estaba disponible. No se aplicaron cambios a la instalación activa.', '',
'## Alcance','',
'Se conservaron las 63 tablas y todos los registros de `C:/Users/b17za/Downloads/benaedu (28).sql`. Se leyeron solamente las 61 tablas homónimas de `C:/Users/b17za/OneDrive/Desktop/Kosmos base/data/kcsdatos`. `users` y `tgpram` no tienen tabla homónima y se conservaron completas. No se importaron otras tablas de Kosmos.', '',
'| Resultado | Registros |','|---|---:|',
f"| Registros previos conservados | {total['existentes']:,} |",f"| Registros de origen examinados | {total['origen']:,} |",f"| Coincidencias exactas, sin volver a insertar | {total['coincidentes']:,} |",f"| Nuevos registros insertados | {total['insertados']:,} |",f"| Pendientes sin insertar | {total['pendientes']:,} |",'',
'## Archivos','',
'- `benaedu-combinada.sql`: respaldo completo del resultado verificado. Importarlo inicialmente en una base nueva y vacía; contiene CREATE TABLE/DROP TABLE. No importarlo encima de la base activa para combinarla.',
'- `solo-inserciones-revisadas.sql`: inserciones aceptadas contra este respaldo concreto; usa el nombre `benaedu`. No es idempotente. No ejecutarlo en otra versión de la base ni repetirlo; requiere una comparación nueva con la base activa.',
'- `resumen.csv`: conteos, claves de comparación y estado de cada tabla.',
'- `pendientes/<tabla>.csv`: registros de origen no insertados y motivo.',
'- `pendientes/<tabla>-existentes.csv`: registros de BenaEdu con las mismas claves, para comparar. Algunas tablas sin clave fiable no tienen este archivo.',
'- `pendientes/tafubi-estructura.csv`: datos originales de la tabla incompatible.',
'- `validacion.json`: resultado de reimportación, conteos, checksums y SHA-256 de los SQL.',
'- `origen-huellas.json`: conteos de los índices originales y SHA-256 de los archivos físicos.',
'- `integridad-origen.txt`: REPAIR/CHECK TABLE de las copias MyISAM.', '',
'## Criterios','',
'Las coincidencias exactas se compararon con todos los campos y su multiplicidad; se conservaron también duplicados existentes. Las claves de los índices originales se usaron de forma conservadora como identidad de comparación, aunque muchos índices no son únicos. Para pólizas se usó CIA/TPOL/NPOL/FPOL/NPAR. Claves existentes con valores distintos, claves vacías y claves con varias variantes de origen quedaron pendientes. Las tablas sin clave fiable y con datos previos recibieron únicamente comparación exacta; los restantes registros quedaron pendientes.', '',
'Los datos se convirtieron con los tipos del esquema destino y modo SQL estricto. Se separaron textos demasiado largos, sin recortarlos. Se mapearon los campos terminados en 0 con los correspondientes terminados en O, FADO con FADQ en tafmac, e IIMPTME/IIMPTMN con IMPTME/IMPTMN en tesfalu. tafubi tiene campos de activos en el destino y campos de ubicación en el origen: no se inventó una equivalencia.', '',
'## Motivos pendientes','',
'| Motivo | Registros |','|---|---:|']
lines += [f'| {reason} | {count:,} |' for reason,count in reasons.items()]
lines += ['', '## Validación','',
'Se comprobó la integridad de las 61 tablas MyISAM en copias. Sus conteos coinciden con los índices de los archivos originales. Las huellas SHA-256 de los archivos originales se mantuvieron iguales. Para cada tabla combinada, el resultado contiene exactamente el multiconjunto de registros previos más las inserciones aceptadas. El respaldo final se reimportó en una segunda base temporal: sus 63 tablas tienen los conteos esperados y checksums iguales.', '',
'No se validaron pantallas ni operaciones escolares sobre la base activa. Los pendientes necesitan una decisión por tabla o por clave antes de completar la combinación de esos registros. Los SQL y CSV contienen datos reales.', '']
(OUT/'LEEME.md').write_text('\n'.join(lines),encoding='utf-8')
scripts=OUT/'scripts'
scripts.mkdir(exist_ok=True)
for name in ('preparar.py','combinar.py','validar.py','verificar-origen.py','documentar.py'):
    shutil.copy2(WORK/name,scripts/name)
print(json.dumps(reasons,ensure_ascii=False),flush=True)
