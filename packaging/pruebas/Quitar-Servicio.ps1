param([Parameter(Mandatory = $true)][string]$MariaDbBin)
$ErrorActionPreference = 'Stop'
$serviceName = 'BenaEduPruebasDB'
$service = Get-Service -Name $serviceName -ErrorAction SilentlyContinue
if (!$service) { exit 0 }
if ($service.Status -ne 'Stopped') { Stop-Service -Name $serviceName -Force }
$mysqld = Join-Path $MariaDbBin 'mysqld.exe'
if (!(Test-Path -LiteralPath $mysqld)) { throw 'No se encontró mysqld para retirar el servicio.' }
& $mysqld --remove $serviceName
if ($LASTEXITCODE -ne 0) { throw "No se pudo retirar el servicio MariaDB (código $LASTEXITCODE)." }
