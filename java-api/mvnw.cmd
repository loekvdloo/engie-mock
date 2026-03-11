@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET "BASE_DIR=%~dp0")

@SET MAVEN_PROJECTBASEDIR=%BASE_DIR%
@SET MVNW_REPOURL=https://repo.maven.apache.org/maven2

@CALL :find_maven_home
@IF ERRORLEVEL 1 GOTO error

@SET MAVEN_CMD_LINE_ARGS=%*
"%MAVEN_HOME%\bin\mvn.cmd" %*
@GOTO end

:find_maven_home
@SET WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties
@SET WRAPPER_URL=
@FOR /F "usebackq tokens=1,* delims==" %%A IN ("%WRAPPER_PROPERTIES%") DO (
    @IF "%%A"=="distributionUrl" SET WRAPPER_URL=%%B
)
@SET MAVEN_USER_HOME=%USERPROFILE%\.m2\wrapper
@FOR /F "delims=" %%i IN ('powershell -NoProfile -ExecutionPolicy Bypass -Command "& { $url='%WRAPPER_URL%'; $home='%MAVEN_USER_HOME%'; $name=[System.IO.Path]::GetFileNameWithoutExtension($url) -replace '-bin',''; $dir=\"$home\dists\$name\"; if (!(Test-Path \"$dir\")) { New-Item -ItemType Directory -Path $dir -Force | Out-Null; $zip=\"$dir.zip\"; Write-Host \"Downloading Maven...\"; Invoke-WebRequest -Uri $url -OutFile $zip; Expand-Archive -Path $zip -DestinationPath $dir -Force; Remove-Item $zip }; $mvn=Get-ChildItem $dir -Recurse -Filter mvn.cmd | Select-Object -First 1; $mvn.DirectoryName -replace 'bin$','' | Write-Output }"'`) DO SET "MAVEN_HOME=%%i"
@EXIT /B 0

:error
@ECHO "Error: Could not find Maven."
@EXIT /B 1

:end
