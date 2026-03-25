param(
    [Parameter(Mandatory = $true)][string]$NativeVersion
)

$rootPath = $PSScriptRoot
$buildPath = Join-Path $rootPath -ChildPath "build"

if (Test-Path -Path $buildPath) {
    Remove-Item -Recurse -Force $buildPath
}

Write-Host "cmake:" -ForegroundColor Blue
Write-Host "    工作目录: $rootPath" -ForegroundColor Blue
Write-Host "    生成目录: $buildPath" -ForegroundColor Blue

cmake -S $rootPath -B $buildPath -DNATIVE_VERSION="$NativeVersion"
cmake --build $buildPath --config Release

Write-Host "已完成编译" -ForegroundColor Green
