from preparar import ROOT, WORK, BIN, CLIENT, query, rows
from pathlib import Path
from collections import Counter, defaultdict
import csv, hashlib, json, subprocess, re

OUT = ROOT / 'database/migraciones/kcsdatos-20261010'
OUT.mkdir(parents=True, exist_ok=True)
COLS = json.loads((WORK / 'columnas.json').read_text(encoding='utf-8'))
INDEX = json.loads((WORK / 'indices.json').read_text(encoding='utf-8'))
KEYS = {
    'tdpol': ['CIA','TPOL','NPOL','FPOL','NPAR'],
    'tgemp': ['CIA','NEMP'], 'tgcpag': ['CPAG'], 'tgimp': ['CIMP'],
    'tgtcd': ['CMON','FECA'], 'tgtcm': ['CMON'], 'tmclasge': ['TBL'],
}

def qi(s):
    return '`' + s.replace('`', '``') + '`'

def data(schema, table, cols):
    expr = ','.join('CAST('+qi(c)+' AS CHAR CHARACTER SET utf8mb4)' for c in cols)
    return [tuple(r) for r in rows(f'SELECT JSON_ARRAY({expr}) FROM {qi(schema)}.{qi(table)};')]

def literal(v):
    return 'NULL' if v is None else "CONVERT(X'" + v.encode('utf-8').hex() + "' USING utf8mb4)"

def key(row, positions):
    return tuple(None if row[p] is None else row[p].rstrip().casefold() for p in positions)

def insert(table, cols, records, stream):
    head = f'INSERT INTO benaedu.{qi(table)} (' + ','.join(map(qi, cols)) + ') VALUES\n'
    for start in range(0, len(records), 200):
        sql = head + ',\n'.join('('+','.join(map(literal, r))+')' for r in records[start:start+200]) + ';\n'
        stream.write(sql)
        query("SET SESSION sql_mode='STRICT_ALL_TABLES,NO_AUTO_VALUE_ON_ZERO'; " + sql)

def check_self():
    assert key(('AB ',None),[0,1]) == ('ab',None)
    assert literal(None) == 'NULL'
    assert "27" in literal("'")
    a = Counter([('x',),('x',)])
    a[('x',)] -= 1
    assert a[('x',)] == 1

if __name__ == '__main__':
    check_self()
    query('CREATE DATABASE IF NOT EXISTS normalizado CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;')
    dest_tables = sorted({c[1] for c in COLS if c[0] == 'benaedu'})
    source_tables = {c[1] for c in COLS if c[0] == 'kcsdatos'}
    checks = []
    report = []
    pending_dir = OUT / 'pendientes'
    pending_dir.mkdir(exist_ok=True)
    with (OUT / 'solo-inserciones-revisadas.sql').open('w', encoding='utf-8', newline='\n') as sqlfile:
        sqlfile.write('-- Preparado exclusivamente contra benaedu (28).sql. Revisar antes de aplicar a otra base.\nSET NAMES utf8mb4;\nSET SESSION sql_mode=\'STRICT_ALL_TABLES,NO_AUTO_VALUE_ON_ZERO\';\n')
        for table in dest_tables:
            cols = [c[2] for c in COLS if c[0] == 'benaedu' and c[1] == table]
            baseline = data('benaedu', table, cols)
            stat = {'tabla':table,'existentes':len(baseline),'origen':0,'coincidentes':0,'insertados':0,'pendientes':0,'clave':'','estado':''}
            if table not in source_tables:
                stat['estado'] = 'Sin tabla homonima en origen; conservada'
                report.append(stat)
                continue
            original_count = int(query(f'SELECT COUNT(*) FROM kcsdatos.{qi(table)};').strip())
            repair = query(f'REPAIR TABLE kcsdatos.{qi(table)};')
            checks.append(repair)
            check = query(f'CHECK TABLE kcsdatos.{qi(table)} EXTENDED;')
            checks.append(check)
            if not any(line.endswith('\tstatus\tOK') for line in check.splitlines()):
                raise RuntimeError('CHECK TABLE fallo: '+table+' '+check)
            src_cols = [c[2] for c in COLS if c[0] == 'kcsdatos' and c[1] == table]
            stat['origen'] = int(query(f'SELECT COUNT(*) FROM kcsdatos.{qi(table)};').strip())
            assert original_count == stat['origen'], 'La reparacion cambio el conteo: '+table
            mapped = []
            for c in cols:
                if c in src_cols:
                    mapped.append(c)
                elif c.endswith('0') and c[:-1]+'O' in src_cols:
                    mapped.append(c[:-1]+'O')
                elif table == 'tafmac' and c == 'FADO':
                    mapped.append('FADQ')
                elif table == 'tesfalu' and c in ('IIMPTME','IIMPTMN'):
                    mapped.append(c[1:])
                else:
                    mapped.append(None)
            if None in mapped:
                stat['pendientes'] = stat['origen']
                stat['estado'] = 'Estructura incompatible; tabla completa pendiente'
                with (pending_dir / (table+'-estructura.csv')).open('w',encoding='utf-8-sig',newline='') as f:
                    w=csv.writer(f); w.writerow(src_cols); w.writerows(data('kcsdatos',table,src_cols))
                report.append(stat)
                continue
            query(f'DROP TABLE IF EXISTS normalizado.{qi(table)}; CREATE TABLE normalizado.{qi(table)} LIKE benaedu.{qi(table)};')
            lengths=[]
            for c, original in zip(cols,mapped):
                typ=next(x[3] for x in COLS if x[0]=='benaedu' and x[1]==table and x[2]==c)
                match=re.fullmatch(r'(?:var)?char\((\d+)\)',typ)
                if match:
                    lengths.append(f'({qi(original)} IS NOT NULL AND CHAR_LENGTH({qi(original)})>{match[1]})')
            invalid=' OR '.join(lengths) or 'FALSE'
            rejected=rows('SELECT JSON_ARRAY('+','.join('CAST('+qi(c)+' AS CHAR CHARACTER SET utf8mb4)' for c in mapped)+f') FROM kcsdatos.{qi(table)} WHERE {invalid};')
            result = query("SET SESSION sql_mode='STRICT_ALL_TABLES,NO_AUTO_VALUE_ON_ZERO'; " + f'INSERT INTO normalizado.{qi(table)} ('+','.join(map(qi,cols))+') SELECT '+','.join(map(qi,mapped))+f' FROM kcsdatos.{qi(table)} WHERE NOT ({invalid}); SHOW WARNINGS;')
            if result.strip():
                raise RuntimeError('Conversion con advertencias: '+table+' '+result[:1000])
            source = data('normalizado', table, cols)
            assert len(source)+len(rejected) == stat['origen']
            groups=defaultdict(list)
            for schema,t,name,nonunique,seq,col in INDEX:
                if schema=='kcsdatos' and t==table:
                    groups[name].append(col)
            identity = KEYS.get(table)
            if identity is None:
                identity = next((v for n,v in groups.items() if n.lower()==table.lower()), None)
            if identity and not all(c in cols for c in identity):
                identity=None
            stat['clave'] = ','.join(identity or [])
            positions = [cols.index(c) for c in identity] if identity else []
            before = Counter(baseline)
            available = before.copy()
            existing_keys = {key(r,positions) for r in baseline} if identity else set()
            source_variants = defaultdict(set)
            if identity:
                for r in source:
                    source_variants[key(r,positions)].add(r)
            accepted=[]; pending=[('Texto excede longitud del destino; no se recorta',tuple(r)) for r in rejected]
            for r in source:
                if available[r]>0:
                    available[r]-=1
                    stat['coincidentes']+=1
                    continue
                k=key(r,positions) if identity else None
                if identity and (k in existing_keys or len(source_variants[k])>1 or any(x is None or x=='' for x in k)):
                    pending.append(('Clave existente o ambigua',r))
                elif not identity and baseline:
                    pending.append(('Sin clave fiable para combinar',r))
                else:
                    accepted.append(r)
            if pending:
                with (pending_dir / (table+'.csv')).open('w',encoding='utf-8-sig',newline='') as f:
                    w=csv.writer(f); w.writerow(['motivo']+cols)
                    w.writerows([reason]+list(r) for reason,r in pending)
                if identity:
                    pending_keys={key(r,positions) for _,r in pending}
                    with (pending_dir / (table+'-existentes.csv')).open('w',encoding='utf-8-sig',newline='') as f:
                        w=csv.writer(f); w.writerow(cols); w.writerows(r for r in baseline if key(r,positions) in pending_keys)
            insert(table,cols,accepted,sqlfile)
            after = Counter(data('benaedu',table,cols))
            expected = before + Counter(accepted)
            assert after==expected, 'Se modificaron o perdieron registros: '+table
            stat['insertados']=len(accepted); stat['pendientes']=len(pending)
            stat['estado']='Combinada; existentes conservados'
            assert stat['origen']==stat['coincidentes']+stat['insertados']+stat['pendientes']
            report.append(stat)
            (OUT/'resumen.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
            print(table, stat['existentes'], stat['origen'], stat['coincidentes'], stat['insertados'], stat['pendientes'],flush=True)
    (OUT/'integridad-origen.txt').write_text(''.join(checks),encoding='utf-8')
    with (OUT/'resumen.csv').open('w',encoding='utf-8-sig',newline='') as f:
        w=csv.DictWriter(f,fieldnames=list(report[0])); w.writeheader(); w.writerows(report)
    (OUT/'resumen.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
    with (OUT/'benaedu-combinada.sql').open('wb') as f:
        p=subprocess.run([str(BIN/'mariadb-dump.exe'),'--no-defaults','--host=127.0.0.1','--port=33419','--user=root','--default-character-set=utf8mb4','--hex-blob','--skip-dump-date','benaedu'],stdout=f,stderr=subprocess.PIPE)
    if p.returncode:
        raise RuntimeError(p.stderr.decode(errors='replace'))
    totals={k:sum(r[k] for r in report) for k in ['existentes','origen','coincidentes','insertados','pendientes']}
    print(json.dumps(totals),flush=True)
