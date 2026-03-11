# run.ps1 - Download Maven als nodig en start de Spring Boot applicatie
$ErrorActionPreference = "Stop"

# Altijd naar de map van dit script gaan (zodat pom.xml gevonden wordt)
Set-Location $PSScriptRoot

$MAVEN_VERSION = "3.9.6"
$MAVEN_DIR = "$env:USERPROFILE\.m2\wrapper\dists\apache-maven-$MAVEN_VERSION"
$MAVEN_ZIP = "$MAVEN_DIR.zip"
$MAVEN_URL = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MAVEN_VERSION/apache-maven-$MAVEN_VERSION-bin.zip"
$MVN_CMD = "$MAVEN_DIR\apache-maven-$MAVEN_VERSION\bin\mvn.cmd"

# Download Maven als het er nog niet is
if (-not (Test-Path $MVN_CMD)) {
    Write-Host "Maven niet gevonden. Downloaden van $MAVEN_URL ..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Force -Path $MAVEN_DIR | Out-Null
    Invoke-WebRequest -Uri $MAVEN_URL -OutFile $MAVEN_ZIP
    Write-Host "Maven uitpakken..." -ForegroundColor Yellow
    Expand-Archive -Path $MAVEN_ZIP -DestinationPath $MAVEN_DIR -Force
    Remove-Item $MAVEN_ZIP
    Write-Host "Maven geinstalleerd in $MAVEN_DIR" -ForegroundColor Green
}

Write-Host "Applicatie starten op http://localhost:8080 ..." -ForegroundColor Cyan
& $MVN_CMD spring-boot:run
