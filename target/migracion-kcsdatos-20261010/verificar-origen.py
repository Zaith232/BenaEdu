from preparar import ROOT, WORK, BIN
from pathlib import Path
import subprocess, re, json, hashlib

source=Path(r'C:\Users\b17za\OneDrive\Desktop\Kosmos base\data\kcsdatos')
result=[]
for table in (WORK/'tables.txt').read_text(encoding='utf-8-sig').splitlines():
    index=source/(table+'.MYI')
    if not index.exists():
        continue
    p=subprocess.run([str(BIN/'myisamchk.exe'),'--description',str(index)],capture_output=True)
    if p.returncode:
        raise RuntimeError(table+' no se pudo leer el indice original')
    match=re.search(rb'Data records:\s+(\d+)',p.stdout)
    assert match,table
    item={'tabla':table,'registros_indice_original':int(match[1]),'archivos':{}}
    for ext in ('frm','MYD','MYI'):
        path=source/(table+'.'+ext)
        with path.open('rb') as f:
            digest=hashlib.file_digest(f,'sha256').hexdigest()
        item['archivos'][path.name]={'bytes':path.stat().st_size,'sha256':digest}
    result.append(item)
(WORK/'origen-huellas.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
print('Indices originales leidos sin modificarlos:',len(result))
