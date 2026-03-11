# test.ps1 - Download Maven als nodig en voer alle testen uit
$ErrorActionPreference = "Stop"

Set-Location $PSScriptRoot

$MAVEN_VERSION = "3.9.6"
$MAVEN_DIR = "$env:USERPROFILE\.m2\wrapper\dists\apache-maven-$MAVEN_VERSION"
$MAVEN_ZIP = "$MAVEN_DIR.zip"
$MAVEN_URL = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MAVEN_VERSION/apache-maven-$MAVEN_VERSION-bin.zip"
$MVN_CMD = "$MAVEN_DIR\apache-maven-$MAVEN_VERSION\bin\mvn.cmd"

if (-not (Test-Path $MVN_CMD)) {
    Write-Host "Maven niet gevonden. Downloaden..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Force -Path $MAVEN_DIR | Out-Null
    Invoke-WebRequest -Uri $MAVEN_URL -OutFile $MAVEN_ZIP
    Expand-Archive -Path $MAVEN_ZIP -DestinationPath $MAVEN_DIR -Force
    Remove-Item $MAVEN_ZIP
    Write-Host "Maven geinstalleerd." -ForegroundColor Green
}

Write-Host "Testen uitvoeren..." -ForegroundColor Cyan
& $MVN_CMD test
