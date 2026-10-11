from pathlib import Path
import subprocess, json, re, os

ROOT = next(p for p in Path(__file__).resolve().parents if (p / 'pom.xml').exists())
WORK = ROOT / 'target/migracion-kcsdatos-20261010'
BIN = ROOT / 'target/installer-tools/mariadb/mariadb-10.11.16-winx64/bin'
CLIENT = [str(BIN / 'mariadb.exe'), '--no-defaults', '--host=127.0.0.1', '--port=33419', '--user=root', '--default-character-set=utf8mb4', '--batch', '--skip-column-names', '--raw', '--max-allowed-packet=256M']

def query(sql):
    p = subprocess.run(CLIENT, input=sql.encode('utf-8'), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    if p.returncode:
        raise RuntimeError(p.stderr.decode('utf-8', errors='replace')[:2000])
    return p.stdout.decode('utf-8')

def rows(sql):
    return [json.loads(line) for line in query(sql).splitlines() if line]

if __name__ == '__main__':
    print(query('SELECT VERSION();'), flush=True)
    query('CREATE DATABASE IF NOT EXISTS benaedu CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;')
    dump = Path(r'C:\Users\b17za\Downloads\benaedu (28).sql')
    with dump.open('rb') as stream:
        p = subprocess.run(CLIENT + ['benaedu'], stdin=stream, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    if p.returncode:
        raise RuntimeError(p.stderr.decode(errors='replace')[:2000])
    columns = rows("SELECT JSON_ARRAY(TABLE_SCHEMA,TABLE_NAME,COLUMN_NAME,COLUMN_TYPE,IS_NULLABLE,COLUMN_DEFAULT,COLUMN_KEY) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA IN ('benaedu','kcsdatos') ORDER BY TABLE_SCHEMA,TABLE_NAME,ORDINAL_POSITION;")
    (WORK / 'columnas.json').write_text(json.dumps(columns, ensure_ascii=False, indent=2), encoding='utf-8')
    indexes = rows("SELECT JSON_ARRAY(TABLE_SCHEMA,TABLE_NAME,INDEX_NAME,NON_UNIQUE,SEQ_IN_INDEX,COLUMN_NAME) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA IN ('benaedu','kcsdatos') ORDER BY TABLE_SCHEMA,TABLE_NAME,INDEX_NAME,SEQ_IN_INDEX;")
    (WORK / 'indices.json').write_text(json.dumps(indexes, ensure_ascii=False, indent=2), encoding='utf-8')
    tables = [x.strip() for x in (WORK / 'tables.txt').read_text(encoding='utf-8-sig').splitlines()]
    source = {x[1] for x in columns if x[0] == 'kcsdatos'}
    for t in tables:
        dest = [x[2] for x in columns if x[0] == 'benaedu' and x[1] == t]
        src = [x[2] for x in columns if x[0] == 'kcsdatos' and x[1] == t]
        count = query(f'SELECT COUNT(*) FROM benaedu.`{t}`;').strip()
        old = query(f'SELECT COUNT(*) FROM kcsdatos.`{t}`;').strip() if t in source else '-'
        print(t, count, old, 'faltan:', sorted(set(dest)-set(src)), 'extra:', sorted(set(src)-set(dest)), flush=True)
