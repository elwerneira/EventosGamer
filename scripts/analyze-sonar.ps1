$ErrorActionPreference = 'Stop'
if (!$env:SONAR_TOKEN) { $env:SONAR_TOKEN = [Environment]::GetEnvironmentVariable('SONAR_TOKEN', 'User') }
if (!$env:SONAR_TOKEN) { throw 'Configura SONAR_TOKEN localmente. El proyecto SonarCloud debe usar analisis CI, no Automatic Analysis.' }
Push-Location -LiteralPath (Split-Path -Parent $PSScriptRoot)
try {
    & .\mvnw.cmd '-Dspring.profiles.active=test' test 'org.sonarsource.scanner.maven:sonar-maven-plugin:5.9.0.7382:sonar' '-Dsonar.qualitygate.wait=true'
    if ($LASTEXITCODE -ne 0) { throw 'SonarCloud no confirmo un analisis aprobado. Revisa el registro y el panel.' }
} finally { Pop-Location }
