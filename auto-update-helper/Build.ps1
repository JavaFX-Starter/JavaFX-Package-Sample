$projectDir = $PSScriptRoot
$buildDir = Join-Path -Path $projectDir -ChildPath "build"
Write-Host "项目目录: $projectDir" -ForegroundColor Blue
Write-Host "项目构建目录: $buildDir" -ForegroundColor Blue

if (Test-Path -Path $buildDir) {
    Write-Host "删除之前的构建目录: $buildDir" -ForegroundColor Gray
    Remove-Item -Recurse -Force $buildDir
}

cmake -S $projectDir -B $buildDir
cmake --build $buildDir --config Release