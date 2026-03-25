param(
    [Parameter(Mandatory = $true)][string]$NativeVersion
)
[System.Console]::OutputEncoding = [System.Text.Encoding]::UTF8
Write-Host "NativeVersion: $NativeVersion" -ForegroundColor Yellow
.\src\native\build.ps1 -NativeVersion "$NativeVersion"

Remove-Item -Path .\src\main\resources\native\lib\NativeFXWindow-*.dll
Copy-Item .\src\native\build\Release\NativeFXWindow-*.dll .\src\main\resources\native\lib\