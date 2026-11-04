@echo off
setlocal
title College Parking Manager

echo ===================================================
echo     Launching College Parking Manager (Swing UI)
echo ===================================================

REM Try locating java
where java >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    set JAVA_CMD=java
) else if exist "%USERPROFILE%\.jdks\openjdk-24.0.1\bin\java.exe" (
    set "JAVA_CMD=%USERPROFILE%\.jdks\openjdk-24.0.1\bin\java.exe"
) else if exist "%JAVA_HOME%\bin\java.exe" (
    set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
) else (
    echo Error: Java was not found in PATH or standard location.
    echo Please install Java 17+ or configure JAVA_HOME.
    pause
    exit /b 1
)

REM Check if target jar exists
if exist "target\college-parking-manager-1.0.0-jar-with-dependencies.jar" (
    echo Running standalone JAR...
    "%JAVA_CMD%" -jar "target\college-parking-manager-1.0.0-jar-with-dependencies.jar"
) else (
    echo Building and running via Maven...
    call build.bat
    "%JAVA_CMD%" -jar "target\college-parking-manager-1.0.0-jar-with-dependencies.jar"
)

if %ERRORLEVEL% NEQ 0 (
    echo Application exited with code %ERRORLEVEL%.
    pause
)
endlocal
