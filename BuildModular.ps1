if (Test-Path -Path .\target\) {
    rm -Recurse -Force .\target\
}

mvn -Pmodular package

jdeps.exe --list-deps --ignore-missing-deps --multi-release 9 --module-path .\target\jars\ -m sample
(jdeps.exe --list-deps --ignore-missing-deps --multi-release 9 --module-path .\target\jars\ -m sample | ForEach-Object {$_.Trim()} | Where-Object {$_}) -join ","

jdeps.exe --list-reduced-deps --ignore-missing-deps --multi-release 9 --module-path .\target\jars\ -m sample
(jdeps.exe --list-reduced-deps --ignore-missing-deps --multi-release 9 --module-path .\target\jars\ -m sample | ForEach-Object {$_.Trim()} | Where-Object {$_}) -join ","


mvn -Pmodular exec:exec@image
