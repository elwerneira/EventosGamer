$ErrorActionPreference = 'Stop'
& (Join-Path $PSScriptRoot 'start-mysql.ps1')
Push-Location -LiteralPath (Split-Path -Parent $PSScriptRoot)
try {
    & .\mvnw.cmd spring-boot:run '-Dspring-boot.run.arguments=--server.address=127.0.0.1'
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo iniciar GREEGAME.' }
} finally { Pop-Location }
