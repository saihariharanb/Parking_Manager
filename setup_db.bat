@echo off
setlocal
title Setup College Parking Database

echo ===================================================
echo   Setup / Reset College Parking MySQL Database
echo ===================================================

set /p MYSQL_USER="Enter MySQL Username [default: root]: "
if "%MYSQL_USER%"=="" set MYSQL_USER=root

set /p MYSQL_PASS="Enter MySQL Password [default: root]: "
if "%MYSQL_PASS%"=="" set MYSQL_PASS=root

echo Importing database.sql into MySQL...

where mysql >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    set MYSQL_EXE=mysql
) else if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" (
    set "MYSQL_EXE=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
) else (
    echo Error: mysql.exe was not found in PATH or standard installation.
    echo Please make sure MySQL is installed and running.
    pause
    exit /b 1
)

"%MYSQL_EXE%" -u %MYSQL_USER% --password=%MYSQL_PASS% < database.sql

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ===================================================
    echo      DATABASE 'college_parking' CREATED & SEEDED!
    echo ===================================================
) else (
    echo Database setup failed. Please verify credentials.
)

pause
endlocal
