$ErrorActionPreference = 'Stop'
if (!$env:NVD_API_KEY) { $env:NVD_API_KEY = [Environment]::GetEnvironmentVariable('NVD_API_KEY', 'User') }
if (!$env:NVD_API_KEY) { throw 'Configura NVD_API_KEY localmente antes de ejecutar el analisis. No la guardes en Git.' }
Push-Location -LiteralPath (Split-Path -Parent $PSScriptRoot)
try {
    & .\mvnw.cmd '-Psecurity-analysis' '-Dspring.profiles.active=test' verify
    if ($LASTEXITCODE -ne 0) { throw 'El analisis no finalizo correctamente. Revisa el reporte y el registro; no implica ausencia de vulnerabilidades.' }
} finally { Pop-Location }
