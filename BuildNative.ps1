.\src\native\build.ps1

Remove-Item -Path .\src\main\resources\native\lib\NativeFXWindow.dll
Copy-Item .\src\native\build\Release\NativeFXWindow.dll .\src\main\resources\native\lib\