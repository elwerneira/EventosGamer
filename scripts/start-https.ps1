$ErrorActionPreference = 'Stop'
$project = Split-Path -Parent $PSScriptRoot
& (Join-Path $PSScriptRoot 'start-mysql.ps1')
$keytool = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin\keytool.exe' } else { 'keytool.exe' }
if (!(Get-Command $keytool -ErrorAction SilentlyContinue)) { throw 'Configura JAVA_HOME con un JDK para generar el certificado de laboratorio.' }
$tlsFolder = Join-Path $project 'data\tls'
New-Item -ItemType Directory -Path $tlsFolder -Force | Out-Null
$accountName = [Security.Principal.WindowsIdentity]::GetCurrent().Name
& icacls.exe $tlsFolder /inheritance:r /grant:r "${accountName}:(OI)(CI)F" | Out-Null
if ($LASTEXITCODE -ne 0) { throw 'No se pudo restringir el acceso al certificado.' }
$passwordFile = Join-Path $tlsFolder 'keystore-password.txt'
$keyStore = Join-Path $tlsFolder 'greegame.p12'
if (!(Test-Path -LiteralPath $passwordFile)) {
    if (Test-Path -LiteralPath $keyStore) { throw 'El almacén existe y falta su contraseña; no se reemplaza.' }
    $random = [Security.Cryptography.RandomNumberGenerator]::Create()
    try {
        $bytes = New-Object byte[] 32
        $random.GetBytes($bytes)
        [IO.File]::WriteAllText($passwordFile, [Convert]::ToBase64String($bytes))
    } finally { $random.Dispose() }
}
$env:TLS_KEY_STORE_PASSWORD = [IO.File]::ReadAllText($passwordFile).Trim()
if (!(Test-Path -LiteralPath $keyStore)) {
    & $keytool -genkeypair -alias greegame -keyalg RSA -keysize 3072 -validity 365 -storetype PKCS12 -keystore $keyStore -storepass:env TLS_KEY_STORE_PASSWORD -dname 'CN=localhost, OU=Laboratorio GREEGAME, O=Proyecto academico, C=CL' -ext 'SAN=dns:localhost,ip:127.0.0.1,dns:greegame-s3-after'
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo generar el certificado.' }
}
$certificate = Join-Path $tlsFolder 'greegame-local.pem'
if (!(Test-Path -LiteralPath $certificate)) {
    & $keytool -exportcert -rfc -alias greegame -keystore $keyStore -storepass:env TLS_KEY_STORE_PASSWORD -file $certificate
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo exportar el certificado público.' }
}
$env:TLS_KEY_STORE = ([Uri]$keyStore).AbsoluteUri
Write-Output 'HTTPS de laboratorio: https://localhost:8443. El certificado es autofirmado y no acredita confianza pública.'
Push-Location -LiteralPath $project
try {
    & .\mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=mysql,https' '-Dspring-boot.run.arguments=--server.address=127.0.0.1'
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo iniciar GREEGAME con HTTPS.' }
} finally { Pop-Location }
