@echo off
setlocal
title Build College Parking Manager

echo ===================================================
echo       Building College Parking Manager
echo ===================================================

REM Set JAVA_HOME if not already set
if "%JAVA_HOME%"=="" (
    if exist "%USERPROFILE%\.jdks\openjdk-24.0.1" (
        set "JAVA_HOME=%USERPROFILE%\.jdks\openjdk-24.0.1"
    )
)

REM Check Maven
if exist ".maven\apache-maven-3.9.6\bin\mvn.cmd" (
    set "MVN_CMD=.maven\apache-maven-3.9.6\bin\mvn.cmd"
) else (
    set MVN_CMD=mvn
)

echo Using Maven: %MVN_CMD%
echo Using JAVA_HOME: %JAVA_HOME%

call "%MVN_CMD%" clean package -DskipTests

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ===================================================
    echo           BUILD COMPLETED SUCCESSFULLY!
    echo   Runnable JAR: target\college-parking-manager-1.0.0-jar-with-dependencies.jar
    echo   You can launch it by running 'run.bat'
    echo ===================================================
) else (
    echo Build failed with error code %ERRORLEVEL%.
)

endlocal
