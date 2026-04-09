@echo off
chcp 65001 >nul
title LIMS Dashboard - Complete Startup
color 0A

echo ==========================================
echo    LIMS Dashboard - Complete Startup
echo ==========================================
echo.

set LIMS_PATH=C:\LIMS
set NGINX_PATH=C:\nginx

:: Check if running as administrator
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo WARNING: Not running as Administrator!
    echo Some features may not work properly.
    echo.
    pause
)

:: Check PostgreSQL
echo [1/4] Checking PostgreSQL...
sc query "postgresql-x64-15" >nul
if %errorlevel% neq 0 (
    echo ERROR: PostgreSQL service not found!
    echo Please run: 01-install-prerequisites.ps1
    pause
    exit /b 1
)

sc query "postgresql-x64-15" | find "RUNNING" >nul
if %errorlevel% neq 0 (
    echo Starting PostgreSQL...
    net start "postgresql-x64-15"
    timeout /t 3 /nobreak >nul
) else (
    echo PostgreSQL is already running.
)

:: Start Backend
echo.
echo [2/4] Starting Backend...
if not exist "%LIMS_PATH%\backend\univ-0.0.1-SNAPSHOT.jar" (
    echo ERROR: Backend JAR not found!
    echo Expected: %LIMS_PATH%\backend\univ-0.0.1-SNAPSHOT.jar
    echo Please run build-and-deploy first.
    pause
    exit /b 1
)

:: Check if backend already running
tasklist | find "java.exe" | find "univ" >nul
if %errorlevel% equ 0 (
    echo Backend is already running.
) else (
    start "LIMS Backend" cmd /c "cd /d %LIMS_PATH%\backend && java -jar univ-0.0.1-SNAPSHOT.jar --spring.config.location=file:%LIMS_PATH%/config/application.properties > %LIMS_PATH%\logs\backend-console.log 2>&1"
    echo Backend starting...
    timeout /t 10 /nobreak >nul
)

:: Check Backend Health
echo.
echo [3/4] Checking Backend Health...
curl -s http://localhost:8080/actuator/health | find "UP" >nul
if %errorlevel% equ 0 (
    echo Backend is healthy! [OK]
) else (
    echo WARNING: Backend health check failed.
    echo Waiting 5 more seconds...
    timeout /t 5 /nobreak >nul
    curl -s http://localhost:8080/actuator/health | find "UP" >nul
    if %errorlevel% equ 0 (
        echo Backend is now healthy! [OK]
    ) else (
        echo ERROR: Backend failed to start properly.
        echo Check logs: %LIMS_PATH%\logs\
    )
)

:: Start nginx
echo.
echo [4/4] Starting Frontend (nginx)...
if not exist "%NGINX_PATH%\nginx.exe" (
    echo WARNING: nginx not found at %NGINX_PATH%
    echo Frontend will not be available.
    goto :skip_nginx
)

tasklist | find "nginx.exe" >nul
if %errorlevel% equ 0 (
    echo nginx is already running.
) else (
    cd /d %NGINX_PATH%
    start nginx -c %LIMS_PATH%\config\nginx.conf
    echo nginx started.
)

:skip_nginx
echo.
echo ==========================================
echo    LIMS Dashboard Started Successfully!
echo ==========================================
echo.
echo URLs:
echo   Frontend: http://localhost
echo   Backend:  http://localhost:8080
echo   Health:   http://localhost:8080/actuator/health
echo.
echo Logs: %LIMS_PATH%\logs\
echo.
echo Press any key to exit this window...
echo (Applications will continue running in background)
pause >nul
