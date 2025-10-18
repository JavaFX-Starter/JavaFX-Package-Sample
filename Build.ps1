.\auto-update-helper\Build.ps1

if (Test-Path -Path .\target\) {
    rm -Recurse -Force .\target\
}

mvn -Pwin clean package exec:exec@image
cp .\auto-update-helper\build\Release\auto-update-helper.exe .\target\buildImage\JavaFXSample\
.\target\buildImage\JavaFXSample\AppUpdateTool.exe
