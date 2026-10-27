#define ProductName "BenaEdu Pruebas"
#define ProductVersion "1.0.11"
#define AppId "{{B9F10B7C-81DB-4A2E-9CC0-9A346F04ED23}"
#define PayloadRoot "..\..\target\paquete-pruebas-1.0.11"

[Setup]
AppId={#AppId}
AppName={#ProductName}
AppVersion={#ProductVersion}
AppPublisher=Unidad Escolar Benavente
DefaultDirName={autopf}\BenaEdu Pruebas
DefaultGroupName=BenaEdu Pruebas
OutputDir=..\..\instaladores-pruebas
OutputBaseFilename=BenaEdu-Pruebas-1.0.11-con-base
Compression=lzma2/normal
SolidCompression=yes
ArchitecturesInstallIn64BitMode=x64
PrivilegesRequired=admin
DisableProgramGroupPage=yes
Uninstallable=yes

[Dirs]
Name: "{app}"

[Files]
Source: "{#PayloadRoot}\app\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "{#PayloadRoot}\mariadb\*"; DestDir: "{app}\database"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "{#PayloadRoot}\seed.sql"; DestDir: "{app}\database"; Flags: ignoreversion
Source: "{#PayloadRoot}\Configurar-Base.ps1"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#PayloadRoot}\Quitar-Servicio.ps1"; DestDir: "{app}"; Flags: ignoreversion

[Icons]
Name: "{autoprograms}\BenaEdu Pruebas"; Filename: "{app}\BenaEduPruebas.exe"
Name: "{autodesktop}\BenaEdu Pruebas"; Filename: "{app}\BenaEduPruebas.exe"; Tasks: desktopicon

[Tasks]
Name: "desktopicon"; Description: "Crear acceso directo en el escritorio"; GroupDescription: "Accesos directos:"
Name: "newdatabase"; Description: "Cargar la base nueva incluida (guardar copia de la base anterior)"; GroupDescription: "Base de datos:"; Flags: unchecked

[Run]
Filename: "{app}\BenaEduPruebas.exe"; Description: "Iniciar BenaEdu Pruebas"; Flags: postinstall nowait skipifsilent; Check: DatabaseReady

[UninstallRun]
Filename: "powershell.exe"; Parameters: "-NoProfile -ExecutionPolicy Bypass -File ""{app}\Quitar-Servicio.ps1"" -MariaDbBin ""{app}\database\bin"""; Flags: runhidden waituntilterminated; RunOnceId: "RemoveBenaEduDatabaseService"

[Code]
var
  BaseLista: Boolean;

function DatabaseReady: Boolean;
begin
  Result := BaseLista;
end;

procedure CurStepChanged(CurStep: TSetupStep);
var
  Parameters: String;
  ResultCode: Integer;
begin
  if CurStep = ssPostInstall then
  begin
    Parameters := '-NoProfile -ExecutionPolicy Bypass -File "' + ExpandConstant('{app}\Configurar-Base.ps1') +
      '" -MariaDbBin "' + ExpandConstant('{app}\database\bin') + '" -DumpFile "' + ExpandConstant('{app}\database\seed.sql') + '"';
    if WizardIsTaskSelected('newdatabase') then
      Parameters := Parameters + ' -UsarRespaldoIncluido';
    WizardForm.StatusLabel.Caption := 'Configurando la base de datos; espere a que termine...';
    BaseLista := False;
    if Exec(ExpandConstant('{sys}\WindowsPowerShell\v1.0\powershell.exe'), Parameters, '', SW_SHOW, ewWaitUntilTerminated, ResultCode) then
      BaseLista := ResultCode = 0;
    if not BaseLista then
      RaiseException('No se pudo configurar la base de datos. La aplicación no se iniciará. Si eligió cargar la base nueva, la copia anterior se conserva en C:\ProgramData\BenaEduPruebas-respaldo-*.');
  end;
end;
