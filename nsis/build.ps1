$rootPath = $PSScriptRoot
$nsiPath = Join-Path $rootPath -ChildPath "installer.nsi"

& 'C:\Program Files (x86)\NSIS\makensis.exe' /INPUTCHARSET UTF8 $nsiPath