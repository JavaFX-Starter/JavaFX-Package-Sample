$rootPath = $PSScriptRoot
$nsiPath = Join-Path $rootPath -ChildPath "installer.nsi"

& 'C:\CommandLineTools\SourceInstall\nsis\makensis.exe' /INPUTCHARSET UTF8 $nsiPath