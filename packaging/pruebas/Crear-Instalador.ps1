param(
    [string]$Version = '1.0.9',
    [string]$DumpFile = 'C:\Users\b17za\Downloads\benaedu (1).sql',
    [string]$MariaDbZip = 'target\installer-tools\mariadb-10.11.16-winx64.zip',
    [string]$InnoCompiler = 'target\installer-tools\InnoSetup\ISCC.exe'
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$root = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
Set-Location $root

if ($Version -ne '1.0.9') { throw 'Actualiza también el número de versión en BenaEduPruebas.iss antes de cambiar -Version.' }
foreach ($path in @($DumpFile, $MariaDbZip, $InnoCompiler)) {
    if (!(Test-Path -LiteralPath $path)) { throw "Falta el archivo requerido: $path" }
}
$dump = (Resolve-Path -LiteralPath $DumpFile).Path
$zip = (Resolve-Path -LiteralPath $MariaDbZip).Path
$compiler = (Resolve-Path -LiteralPath $InnoCompiler).Path
$payload = Join-Path $root "target\paquete-pruebas-$Version"
$maria = Join-Path $payload 'mariadb'
$appStage = Join-Path $root 'target\jpackage-input-pruebas'
$appPayload = Join-Path $payload 'app'
$mariaExtract = Join-Path $root 'target\installer-tools\mariadb-extract'

& 'C:\Program Files\NetBeans-25\netbeans\java\maven\bin\mvn.cmd' -o -Pinstalador-pruebas package dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/pruebas-build/dependency
if ($LASTEXITCODE -ne 0) { throw 'La compilación Maven falló.' }

New-Item -ItemType Directory -Path $payload -Force | Out-Null
New-Item -ItemType Directory -Path $mariaExtract -Force | Out-Null
Expand-Archive -LiteralPath $zip -DestinationPath $mariaExtract -Force
$mariaRoot = Get-ChildItem -LiteralPath $mariaExtract -Directory | Select-Object -First 1
if (!$mariaRoot -or !(Test-Path -LiteralPath (Join-Path $mariaRoot.FullName 'bin\mariadb-install-db.exe'))) { throw 'El ZIP no contiene una carpeta ejecutable de MariaDB.' }
if (Test-Path -LiteralPath $maria) {
    $resolvedMaria = (Resolve-Path -LiteralPath $maria).Path
    $mariaItem = Get-Item -LiteralPath $resolvedMaria -Force
    if ($resolvedMaria -ne (Join-Path $payload 'mariadb') -or !$mariaItem.PSIsContainer -or ($mariaItem.Attributes -band [IO.FileAttributes]::ReparsePoint)) { throw "Ruta MariaDB de salida inesperada; no se eliminará: $resolvedMaria" }
    Remove-Item -LiteralPath $resolvedMaria -Recurse -Force
}
New-Item -ItemType Directory -Path $maria | Out-Null
Copy-Item -Path (Join-Path $mariaRoot.FullName '*') -Destination $maria -Recurse -Force

$buildOutput = Join-Path $root 'target\pruebas-build'
if (Test-Path -LiteralPath $appStage) {
    $resolvedStage = (Resolve-Path -LiteralPath $appStage).Path
    $stageItem = Get-Item -LiteralPath $resolvedStage -Force
    if ($resolvedStage -ne $appStage -or !$stageItem.PSIsContainer -or ($stageItem.Attributes -band [IO.FileAttributes]::ReparsePoint)) { throw "Carpeta de staging inesperada: $resolvedStage" }
    Remove-Item -LiteralPath $resolvedStage -Recurse -Force
}
New-Item -ItemType Directory -Path $appStage | Out-Null
Copy-Item -LiteralPath (Join-Path $buildOutput 'BenaEdu-Pruebas.jar') -Destination $appStage -Force
Get-ChildItem -LiteralPath (Join-Path $buildOutput 'dependency') -Filter '*.jar' | Copy-Item -Destination $appStage -Force
$imageRoot = Join-Path $root 'target\jpackage-pruebas'
if (Test-Path -LiteralPath $imageRoot) {
    $resolvedImageRoot = (Resolve-Path -LiteralPath $imageRoot).Path
    $expectedImageRoot = Join-Path $root 'target\jpackage-pruebas'
    $imageItem = Get-Item -LiteralPath $resolvedImageRoot -Force
    if ($resolvedImageRoot -ne $expectedImageRoot -or !$imageItem.PSIsContainer -or ($imageItem.Attributes -band [IO.FileAttributes]::ReparsePoint)) { throw "Ruta de salida inesperada; no se eliminará: $resolvedImageRoot" }
    Remove-Item -LiteralPath $resolvedImageRoot -Recurse -Force
}
& 'C:\Program Files\Java\jdk-25\bin\jpackage.exe' --type app-image --name BenaEduPruebas --app-version $Version --input $appStage --main-jar BenaEdu-Pruebas.jar --main-class com.mycompany.benaedu.Login --dest $imageRoot --java-options '-Dbenaedu.pruebas=true'
if ($LASTEXITCODE -ne 0) { throw 'jpackage no pudo crear la aplicación.' }
if (Test-Path -LiteralPath $appPayload) {
    $resolvedApp = (Resolve-Path -LiteralPath $appPayload).Path
    $appItem = Get-Item -LiteralPath $resolvedApp -Force
    if ($resolvedApp -ne $appPayload -or !$appItem.PSIsContainer -or ($appItem.Attributes -band [IO.FileAttributes]::ReparsePoint)) { throw "Carpeta de aplicación inesperada: $resolvedApp" }
    Remove-Item -LiteralPath $resolvedApp -Recurse -Force
}
New-Item -ItemType Directory -Path $appPayload | Out-Null
Copy-Item -Path (Join-Path $imageRoot 'BenaEduPruebas\*') -Destination $appPayload -Recurse -Force
Copy-Item -LiteralPath $dump -Destination (Join-Path $payload 'seed.sql') -Force
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'Configurar-Base.ps1') -Destination $payload -Force
Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'Quitar-Servicio.ps1') -Destination $payload -Force

New-Item -ItemType Directory -Path (Join-Path $root 'instaladores-pruebas') -Force | Out-Null
& $compiler (Join-Path $PSScriptRoot 'BenaEduPruebas.iss')
if ($LASTEXITCODE -ne 0) { throw 'Inno Setup no pudo compilar el instalador.' }
$installer = Join-Path $root "instaladores-pruebas\BenaEdu-Pruebas-$Version-con-base.exe"
if (!(Test-Path -LiteralPath $installer)) { throw 'ISCC terminó pero no creó el instalador esperado.' }
Get-FileHash -LiteralPath $installer -Algorithm SHA256
Get-Item -LiteralPath $installer | Select-Object FullName,Length,LastWriteTime
