param(
    [string]$PackageName = "com.example.dogapp",
    [string]$LaunchActivity = ".MainActivity"
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$apkPath = Join-Path $repoRoot "app\build\outputs\apk\debug\app-debug.apk"
$localPropertiesPath = Join-Path $repoRoot "local.properties"

if (!(Test-Path $apkPath)) {
    & (Join-Path $repoRoot "gradlew.bat") ":app:assembleDebug" "--no-daemon"
}

$sdkDir = $null
if (Test-Path $localPropertiesPath) {
    $sdkLine = Get-Content $localPropertiesPath |
        Where-Object { $_ -match "^sdk\.dir=" } |
        Select-Object -First 1
    if ($sdkLine) {
        $sdkDir = ($sdkLine -replace "^sdk\.dir=", "") -replace "\\:", ":"
    }
}

$adbCandidates = @(
    $(if ($sdkDir) { Join-Path $sdkDir "platform-tools\adb.exe" }),
    "$env:ANDROID_HOME\platform-tools\adb.exe",
    "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
)
$adb = $adbCandidates | Where-Object { $_ -and (Test-Path $_) } | Select-Object -First 1
if (!$adb) {
    throw "adb.exe를 찾을 수 없습니다. Android Studio SDK Platform-Tools를 설치해 주세요."
}

$devices = & $adb devices
$serial = $devices |
    Select-String -Pattern "^\S+\s+device$" |
    ForEach-Object { ($_ -split "\s+")[0] } |
    Select-Object -First 1

if (!$serial) {
    throw "연결된 에뮬레이터/휴대폰이 없습니다. Android Studio에서 local.properties의 SDK로 에뮬레이터를 켠 뒤 다시 실행하세요."
}

& $adb -s $serial install -r $apkPath
& $adb -s $serial shell am start -n "$PackageName/$LaunchActivity"

Write-Host "DogLog 앱 설치 및 실행 완료: $serial"
