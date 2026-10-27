param(
    [Parameter(Mandatory = $true)][string]$MariaDbBin,
    [Parameter(Mandatory = $true)][string]$DumpFile,
    [string]$DataRoot = "$env:ProgramData\BenaEduPruebas",
    [string]$ServiceName = 'BenaEduPruebasDB',
    [int]$Port = 3307,
    [switch]$UsarRespaldoIncluido
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

function New-Secret([int]$Length = 32) {
    $bytes = [byte[]]::new($Length)
    $generator = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try { $generator.GetBytes($bytes) } finally { $generator.Dispose() }
    $alphabet = 'abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789'
    -join ($bytes | ForEach-Object { $alphabet[$_ % $alphabet.Length] })
}

function Invoke-MariaDb([string[]]$Arguments, [string]$InputFile = $null) {
    $client = Join-Path $MariaDbBin 'mariadb.exe'
    if (!(Test-Path -LiteralPath $client)) { $client = Join-Path $MariaDbBin 'mysql.exe' }
    if (!(Test-Path -LiteralPath $client)) { throw "No se encontró el cliente MariaDB en $MariaDbBin" }
    $quotedArguments = $Arguments | ForEach-Object { '"' + $_.Replace('"', '\"') + '"' }
    $start = [System.Diagnostics.ProcessStartInfo]::new()
    $start.FileName = $client
    $start.Arguments = [string]::Join(' ', $quotedArguments)
    $start.UseShellExecute = $false
    $start.RedirectStandardInput = $true
    $process = [System.Diagnostics.Process]::new()
    $process.StartInfo = $start
    if (!$process.Start()) { throw 'No se pudo iniciar el cliente MariaDB.' }
    if ($InputFile) {
        $source = [System.IO.File]::OpenRead($InputFile)
        try { $source.CopyTo($process.StandardInput.BaseStream) } finally { $source.Dispose(); $process.StandardInput.Close() }
    } else { $process.StandardInput.Close() }
    $process.WaitForExit()
    if ($process.ExitCode -ne 0) { throw "MariaDB terminó con código $($process.ExitCode)." }
}

function Resguardar-BaseActual([string]$Ruta, [string]$Servicio, [string]$Bin) {
    $origen = (Resolve-Path -LiteralPath $Ruta).Path
    $carpeta = Get-Item -LiteralPath $origen -Force
    $padre = Split-Path -Parent $origen
    if (!$padre -or !$carpeta.PSIsContainer -or ($carpeta.Attributes -band [IO.FileAttributes]::ReparsePoint)) {
        throw "Ruta de datos insegura para respaldar: $origen"
    }
    $destino = Join-Path $padre ($carpeta.Name + '-respaldo-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '-' + [guid]::NewGuid().ToString('N').Substring(0, 8))
    $service = Get-Service -Name $Servicio -ErrorAction SilentlyContinue
    if ($service) {
        if ($service.Status -ne 'Stopped') { Stop-Service -Name $Servicio -Force -ErrorAction Stop }
        & (Join-Path $Bin 'mysqld.exe') --remove $Servicio
        if ($LASTEXITCODE -ne 0) { throw 'No se pudo retirar el servicio anterior; se conserva la base original.' }
        $deadline = (Get-Date).AddSeconds(30)
        while (Get-Service -Name $Servicio -ErrorAction SilentlyContinue) {
            if ((Get-Date) -ge $deadline) { throw 'El servicio anterior sigue registrado; cierre BenaEdu y vuelva a intentar.' }
            Start-Sleep -Milliseconds 500
        }
    }
    Move-Item -LiteralPath $origen -Destination $destino -ErrorAction Stop
    Write-Output "Base anterior guardada en: $destino"
}

$configFile = Join-Path $DataRoot 'conexion.properties'
$dbBin = (Resolve-Path -LiteralPath $MariaDbBin).Path
$mariaHome = Split-Path -Parent $dbBin
if (!(Test-Path -LiteralPath $DumpFile)) { throw "No se encontró el respaldo incluido: $DumpFile" }
if (Test-Path -LiteralPath $DataRoot) {
    $existingRoot = Get-Item -LiteralPath $DataRoot -Force
    if (!$existingRoot.PSIsContainer -or ($existingRoot.Attributes -band [System.IO.FileAttributes]::ReparsePoint)) { throw "La ruta de datos no es una carpeta normal: $DataRoot" }
}
if ($UsarRespaldoIncluido -and (Test-Path -LiteralPath $DataRoot)) {
    Resguardar-BaseActual $DataRoot $ServiceName $dbBin
}
New-Item -ItemType Directory -Path $DataRoot -Force | Out-Null

# Una instalación existente conserva su base y sus credenciales; jamás se vuelve a importar el respaldo.
if (!(Test-Path -LiteralPath $configFile)) {
    $mysqld = Join-Path $dbBin 'mysqld.exe'
    $initialize = Join-Path $dbBin 'mariadb-install-db.exe'
    if (!(Test-Path -LiteralPath $initialize)) { $initialize = Join-Path $dbBin 'mysql_install_db.exe' }
    $datadir = Join-Path $DataRoot 'data'
    $ini = Join-Path $DataRoot 'my.ini'
    $rootPassword = New-Secret
    $appPassword = New-Secret
    @"
[mysqld]
basedir=$($mariaHome -replace '\\','/')
datadir=$($datadir -replace '\\','/')
port=$Port
bind-address=127.0.0.1
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
"@ | Set-Content -LiteralPath $ini -Encoding Ascii
    & $initialize "--datadir=$datadir" "--service=$ServiceName" "--password=$rootPassword" "--port=$Port" "--config=$ini" --silent
    if ($LASTEXITCODE -ne 0) { throw "No se pudo inicializar MariaDB (código $LASTEXITCODE)" }
    Start-Service -Name $ServiceName
    $deadline = (Get-Date).AddMinutes(2)
    $ready = $false
    do {
        try { Invoke-MariaDb @('--protocol=tcp', "--host=127.0.0.1", "--port=$Port", '-uroot', "-p$rootPassword", '-e', 'SELECT 1'); $ready = $true; break }
        catch { Start-Sleep -Seconds 2 }
    } while ((Get-Date) -lt $deadline)
    if (!$ready) { throw 'MariaDB no aceptó conexiones; no se importó el respaldo.' }
    Invoke-MariaDb @('--protocol=tcp', '--host=127.0.0.1', "--port=$Port", '-uroot', "-p$rootPassword", '-e', 'CREATE DATABASE benaedu CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci')
    Invoke-MariaDb @('--protocol=tcp', '--host=127.0.0.1', "--port=$Port", '-uroot', "-p$rootPassword", 'benaedu') $DumpFile
    $sql = "CREATE USER IF NOT EXISTS 'benaedu_pruebas'@'127.0.0.1' IDENTIFIED BY '$appPassword';`n" +
        "CREATE USER IF NOT EXISTS 'benaedu_pruebas'@'localhost' IDENTIFIED BY '$appPassword';`n" +
        "GRANT SELECT, INSERT, UPDATE, DELETE, LOCK TABLES, CREATE, ALTER, TRIGGER ON benaedu.* TO 'benaedu_pruebas'@'127.0.0.1';`n" +
        "GRANT SELECT, INSERT, UPDATE, DELETE, LOCK TABLES, CREATE, ALTER, TRIGGER ON benaedu.* TO 'benaedu_pruebas'@'localhost';`n" +
        'FLUSH PRIVILEGES;'
    $sqlFile = Join-Path $DataRoot 'setup.sql'
    $sql | Set-Content -LiteralPath $sqlFile -Encoding Ascii
    Invoke-MariaDb @('--protocol=tcp', '--host=127.0.0.1', "--port=$Port", '-uroot', "-p$rootPassword") $sqlFile
    Remove-Item -LiteralPath $sqlFile -Force
    @"
url=jdbc:mysql://127.0.0.1:$Port/benaedu?sslMode=DISABLED
user=benaedu_pruebas
password=$appPassword
"@ | Set-Content -LiteralPath $configFile -Encoding Ascii
    Remove-Variable rootPassword, appPassword
} else {
    $service = Get-Service -Name $ServiceName -ErrorAction Stop
    if ($service.Status -ne 'Running') { Start-Service -Name $ServiceName }
}

$acl = Get-Acl -LiteralPath $DataRoot
$acl.SetAccessRuleProtection($true, $false)
$acl.SetAccessRule([System.Security.AccessControl.FileSystemAccessRule]::new('NT AUTHORITY\SYSTEM', 'FullControl', 'ContainerInherit,ObjectInherit', 'None', 'Allow'))
$acl.SetAccessRule([System.Security.AccessControl.FileSystemAccessRule]::new('BUILTIN\Administrators', 'FullControl', 'ContainerInherit,ObjectInherit', 'None', 'Allow'))
$acl.SetAccessRule([System.Security.AccessControl.FileSystemAccessRule]::new('BUILTIN\Users', 'ReadAndExecute', 'ContainerInherit,ObjectInherit', 'None', 'Allow'))
Set-Acl -LiteralPath $DataRoot -AclObject $acl
Write-Output "Base local lista. Servicio: $ServiceName; puerto: $Port."
