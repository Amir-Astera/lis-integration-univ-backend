@echo off
chcp 65001 >nul
title LIMS Dashboard - Shutdown
color 0C

echo ==========================================
echo    LIMS Dashboard - Shutdown
echo ==========================================
echo.

set LIMS_PATH=C:\LIMS
set NGINX_PATH=C:\nginx

:: Stop nginx
echo [1/3] Stopping nginx...
if exist "%NGINX_PATH%\nginx.exe" (
    cd /d %NGINX_PATH%
    nginx -s quit 2>nul
    echo nginx stopped.
) else (
    echo nginx not found.
)

:: Stop Backend
echo.
echo [2/3] Stopping Backend...
tasklist | find "java.exe" >nul
if %errorlevel% equ 0 (
    taskkill /F /IM java.exe /T
    echo Backend stopped.
) else (
    echo Backend was not running.
)

:: Optional: Stop PostgreSQL (usually keep it running)
echo.
echo [3/3] PostgreSQL Status:
sc query "postgresql-x64-15" | find "RUNNING" >nul
if %errorlevel% equ 0 (
    echo PostgreSQL is still running. (This is normal)
    echo Run 'net stop postgresql-x64-15' if you need to stop it.
) else (
    echo PostgreSQL is not running.
)

echo.
echo ==========================================
echo    LIMS Dashboard Stopped
echo ==========================================
echo.
pause
