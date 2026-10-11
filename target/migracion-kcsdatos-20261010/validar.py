from preparar import ROOT, WORK, BIN, CLIENT, query, rows
from pathlib import Path
import json, subprocess, hashlib, shutil

OUT=ROOT/'database/migraciones/kcsdatos-20261010'
report=json.loads((OUT/'resumen.json').read_text(encoding='utf-8'))
original=json.loads((WORK/'origen-huellas.json').read_text(encoding='utf-8'))
counts={r['tabla']:r['registros_indice_original'] for r in original}
for r in report:
    if r['tabla'] in counts:
        assert r['origen']==counts[r['tabla']], 'Conteo distinto del indice original: '+r['tabla']
        assert r['origen']==r['coincidentes']+r['insertados']+r['pendientes']
source=Path(r'C:\Users\b17za\OneDrive\Desktop\Kosmos base\data\kcsdatos')
for table in original:
    for name,expected in table['archivos'].items():
        with (source/name).open('rb') as f:
            assert hashlib.file_digest(f,'sha256').hexdigest()==expected['sha256'],name
shutil.copy2(WORK/'origen-huellas.json',OUT/'origen-huellas.json')
query('CREATE DATABASE validacion CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;')
with (OUT/'benaedu-combinada.sql').open('rb') as f:
    p=subprocess.run(CLIENT+['validacion'],stdin=f,stdout=subprocess.PIPE,stderr=subprocess.PIPE)
if p.returncode:
    raise RuntimeError(p.stderr.decode(errors='replace')[:2000])
checks=[]
for r in report:
    t=r['tabla']
    expected=r['existentes']+r['insertados']
    count=int(query(f'SELECT COUNT(*) FROM validacion.`{t}`;').strip())
    assert count==expected,(t,count,expected)
    a=query(f'CHECKSUM TABLE benaedu.`{t}` EXTENDED;').strip().split('\t')[-1]
    b=query(f'CHECKSUM TABLE validacion.`{t}` EXTENDED;').strip().split('\t')[-1]
    assert a==b and a!='NULL',(t,a,b)
    checks.append({'tabla':t,'registros':count,'checksum':a})
assert len(report)==63
actual=query("SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA='validacion';").strip()
assert actual=='63'
for table in ('users','tgpram'):
    assert next(r for r in report if r['tabla']==table)['insertados']==0
hashes={}
for file in ['benaedu-combinada.sql','solo-inserciones-revisadas.sql']:
    with (OUT/file).open('rb') as f:
        hashes[file]=hashlib.file_digest(f,'sha256').hexdigest()
validation={'tablas':63,'reimportacion':'OK','checksums':checks,'origen_sin_modificaciones_sha256':True,'conteos_iguales_a_indices_originales':True,'archivos_sha256':hashes,'totales':{k:sum(r[k] for r in report) for k in ['existentes','origen','coincidentes','insertados','pendientes']}}
(OUT/'validacion.json').write_text(json.dumps(validation,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(validation['totales']),flush=True)
print('OK: 63 tablas reimportadas, checksums iguales, archivos originales intactos.',flush=True)
