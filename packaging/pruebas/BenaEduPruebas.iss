#define ProductName "BenaEdu Pruebas"
#define ProductVersion "1.0.9"
#define AppId "{{B9F10B7C-81DB-4A2E-9CC0-9A346F04ED23}"
#define PayloadRoot "..\..\target\paquete-pruebas-1.0.9"

[Setup]
AppId={#AppId}
AppName={#ProductName}
AppVersion={#ProductVersion}
AppPublisher=Unidad Escolar Benavente
DefaultDirName={autopf}\BenaEdu Pruebas
DefaultGroupName=BenaEdu Pruebas
OutputDir=..\..\instaladores-pruebas
OutputBaseFilename=BenaEdu-Pruebas-1.0.9-con-base
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

[Run]
Filename: "powershell.exe"; Parameters: "-NoProfile -ExecutionPolicy Bypass -File ""{app}\Configurar-Base.ps1"" -MariaDbBin ""{app}\database\bin"" -DumpFile ""{app}\database\seed.sql"""; Flags: runhidden waituntilterminated; StatusMsg: "Configurando la base de datos local de pruebas..."
Filename: "{app}\BenaEduPruebas.exe"; Description: "Iniciar BenaEdu Pruebas"; Flags: postinstall nowait skipifsilent

[UninstallRun]
Filename: "powershell.exe"; Parameters: "-NoProfile -ExecutionPolicy Bypass -File ""{app}\Quitar-Servicio.ps1"" -MariaDbBin ""{app}\database\bin"""; Flags: runhidden waituntilterminated; RunOnceId: "RemoveBenaEduDatabaseService"
