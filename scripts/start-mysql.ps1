$ErrorActionPreference = 'Stop'
$project = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $project '.env'
if (!(Get-Command docker -ErrorAction SilentlyContinue)) { throw 'Instala e inicia Docker Desktop para usar este arranque.' }
if (!(Test-Path -LiteralPath $envFile)) {
    $random = [Security.Cryptography.RandomNumberGenerator]::Create()
    try {
        $bytes = New-Object byte[] 32
        $random.GetBytes($bytes)
        $password = [BitConverter]::ToString($bytes).Replace('-', '')
        $random.GetBytes($bytes)
        $rootPassword = [BitConverter]::ToString($bytes).Replace('-', '')
    } finally { $random.Dispose() }
    [IO.File]::WriteAllText($envFile, "MYSQL_PORT=3307`nMYSQL_PASSWORD=$password`nMYSQL_ROOT_PASSWORD=$rootPassword`n", [Text.UTF8Encoding]::new($false))
    if ($env:OS -eq 'Windows_NT') {
        $accountName = [Security.Principal.WindowsIdentity]::GetCurrent().Name
        & icacls.exe $envFile /inheritance:r /grant:r "${accountName}:F" | Out-Null
        if ($LASTEXITCODE -ne 0) { throw 'No se pudo restringir el acceso a .env.' }
    }
}
$jwtLine = @(Get-Content -LiteralPath $envFile | Where-Object { $_ -match '^JWT_SECRET=' })
if ($jwtLine.Count -eq 0) {
    $random = [Security.Cryptography.RandomNumberGenerator]::Create()
    try {
        $jwtBytes = New-Object byte[] 32
        $random.GetBytes($jwtBytes)
        $jwtSecret = [Convert]::ToBase64String($jwtBytes)
    } finally { $random.Dispose() }
    [IO.File]::AppendAllText($envFile, "`nJWT_SECRET=$jwtSecret`n", [Text.UTF8Encoding]::new($false))
} elseif ($jwtLine.Count -eq 1) {
    $jwtSecret = $jwtLine[0].Substring('JWT_SECRET='.Length)
} else { throw 'Debe existir una sola variable JWT_SECRET en .env.' }
try { $jwtBytes = [Convert]::FromBase64String($jwtSecret) }
catch { throw 'JWT_SECRET debe ser una clave Base64 válida.' }
if ($jwtBytes.Length -lt 32) { throw 'JWT_SECRET requiere al menos 32 bytes aleatorios.' }
$env:JWT_SECRET = $jwtSecret
Push-Location -LiteralPath $project
try {
    $json = & docker compose config --format json
    if ($LASTEXITCODE -ne 0) { throw 'Revisa compose.yaml y las variables de .env.' }
    $configuration = ($json -join "`n") | ConvertFrom-Json
    $service = $configuration.services.mysql
    if ($service.environment.MYSQL_PASSWORD -like 'CAMBIAR*' -or $service.environment.MYSQL_ROOT_PASSWORD -like 'CAMBIAR*') {
        throw 'Configura contraseñas propias en .env antes de iniciar.'
    }
    & docker compose up -d --wait --wait-timeout 180
    if ($LASTEXITCODE -ne 0) { throw 'MySQL no quedó saludable. Revisa docker compose logs mysql.' }
    $env:MYSQL_URL = "jdbc:mysql://127.0.0.1:$($service.ports[0].published)/$($service.environment.MYSQL_DATABASE)"
    $env:MYSQL_USERNAME = $service.environment.MYSQL_USER
    $env:MYSQL_PASSWORD = $service.environment.MYSQL_PASSWORD
    Write-Output "MySQL de GREEGAME listo en 127.0.0.1:$($service.ports[0].published). Conexión cargada en esta terminal."
} finally { Pop-Location }
