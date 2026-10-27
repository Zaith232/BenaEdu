$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$tokens = $null
$errors = $null
$ast = [Management.Automation.Language.Parser]::ParseFile((Join-Path $PSScriptRoot 'Configurar-Base.ps1'), [ref]$tokens, [ref]$errors)
if ($errors.Count) { throw ($errors | Out-String) }
$funcion = $ast.Find({ param($node) $node -is [Management.Automation.Language.FunctionDefinitionAst] -and $node.Name -eq 'Resguardar-BaseActual' }, $true)
. ([scriptblock]::Create($funcion.Extent.Text))

# Solo carpetas temporales y servicios simulados; no toca servicios ni bases instaladas.
function Get-Service { param($Name, $ErrorAction) if ($script:simularServicio) { [pscustomobject]@{ Status = 'Running' } } }
function Stop-Service { param($Name, [switch]$Force, $ErrorAction) throw 'Fallo de parada simulado' }
$script:simularServicio = $false
$raizPruebas = (Resolve-Path (Join-Path $PSScriptRoot '..\..\target')).Path
$temporal = Join-Path $raizPruebas ('validacion-pruebas-respaldo-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $temporal | Out-Null
try {
    $datos = Join-Path $temporal 'BenaEduPruebas'
    New-Item -ItemType Directory -Path (Join-Path $datos 'data') | Out-Null
    [IO.File]::WriteAllText((Join-Path $datos 'conexion.properties'), 'configuracion-anterior')
    [IO.File]::WriteAllBytes((Join-Path $datos 'data\tabla.MYD'), [byte[]]@(0, 1, 128, 255))
    Resguardar-BaseActual $datos 'ServicioSimulado' $temporal
    $copias = @(Get-ChildItem -LiteralPath $temporal -Directory -Filter 'BenaEduPruebas-respaldo-*')
    if ((Test-Path -LiteralPath $datos) -or $copias.Count -ne 1) { throw 'No se apartó la carpeta anterior.' }
    if ([IO.File]::ReadAllText((Join-Path $copias[0].FullName 'conexion.properties')) -ne 'configuracion-anterior') { throw 'No se conservó la configuración.' }
    if ([Convert]::ToBase64String([IO.File]::ReadAllBytes((Join-Path $copias[0].FullName 'data\tabla.MYD'))) -ne 'AAGA/w==') { throw 'Cambió el contenido de la base.' }

    New-Item -ItemType Directory -Path $datos | Out-Null
    $script:simularServicio = $true
    $fallo = $false
    try { Resguardar-BaseActual $datos 'ServicioSimulado' $temporal } catch { $fallo = $true }
    if (!$fallo -or !(Test-Path -LiteralPath $datos)) { throw 'Un fallo al detener el servicio debe conservar la carpeta original.' }
    if (@(Get-ChildItem -LiteralPath $temporal -Directory -Filter 'BenaEduPruebas-respaldo-*').Count -ne 1) { throw 'Se movieron datos pese al fallo.' }
    Write-Output 'OK: copia íntegra y datos conservados si falla la parada del servicio.'
} finally {
    $resuelto = (Resolve-Path -LiteralPath $temporal).Path
    if ((Split-Path -Parent $resuelto) -ne $raizPruebas -or (Split-Path -Leaf $resuelto) -notlike 'validacion-pruebas-respaldo-*') { throw 'Ruta temporal inesperada; no se elimina.' }
    Remove-Item -LiteralPath $resuelto -Recurse -Force
}
