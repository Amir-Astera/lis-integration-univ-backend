#Requires -RunAsAdministrator
<#
.SYNOPSIS
    LIMS Dashboard Application Configuration Script
.DESCRIPTION
    Creates directory structure and application.properties configuration
.NOTES
    Run as Administrator
    File Name: 03-create-config.ps1
#>

param(
    [string]$LimsPath = "C:\LIMS",
    [string]$DatabaseName = "lis_dashboard",
    [string]$DbUser = "lims_user",
    [string]$DbPassword = "lims_password_123",
    [int]$BackendPort = 8080,
    [string]$FrontendApiUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"

function Write-Log {
    param([string]$Message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] $Message"
}

# ============ MAIN EXECUTION ============

Write-Log "=== LIMS Dashboard Configuration Setup Started ==="

# Create directory structure
$directories = @(
    "$LimsPath\backend",
    "$LimsPath\frontend",
    "$LimsPath\logs",
    "$LimsPath\config",
    "$LimsPath\backup",
    "$LimsPath\uploads"
)

foreach ($dir in $directories) {
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Force -Path $dir | Out-Null
        Write-Log "Created directory: $dir"
    } else {
        Write-Log "Directory already exists: $dir"
    }
}

# Create application.properties
$appProperties = @"
# ============================================
# LIMS Dashboard Backend Configuration
# Generated: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
# ============================================

# Server Configuration
server.port=$BackendPort
server.address=0.0.0.0

# Database Configuration (R2DBC)
spring.r2dbc.url=r2dbc:postgresql://localhost:5432/$DatabaseName
spring.r2dbc.username=$DbUser
spring.r2dbc.password=$DbPassword
spring.r2dbc.pool.enabled=true
spring.r2dbc.pool.initial-size=5
spring.r2dbc.pool.max-size=20

# Flyway Migration Configuration
spring.flyway.url=jdbc:postgresql://localhost:5432/$DatabaseName
spring.flyway.user=$DbUser
spring.flyway.password=$DbPassword
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true

# Logging
logging.file.path=$LimsPath\logs
logging.file.name=$LimsPath\logs\lims-backend.log
logging.level.root=INFO
logging.level.lab.dev.med.univ=DEBUG
logging.level.org.springframework.web=INFO

# File Upload Configuration
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB
app.upload.path=$LimsPath\uploads

# CORS Configuration
cors.allowed-origins=http://localhost,http://localhost:5173,http://localhost:80

# Actuator / Health
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always

# Jackson Configuration
spring.jackson.serialization.indent-output=true
spring.jackson.date-format=yyyy-MM-dd'T'HH:mm:ss
spring.jackson.time-zone=Asia/Almaty
"@

$appPropertiesPath = "$LimsPath\config\application.properties"
$appProperties | Out-File -FilePath $appPropertiesPath -Encoding UTF8
Write-Log "Created application.properties: $appPropertiesPath"

# Create .env file for frontend
$frontendEnv = @"
# Frontend Environment Configuration
# Generated: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
VITE_API_BASE_URL=$FrontendApiUrl
VITE_APP_TITLE=LIMS Dashboard
VITE_APP_VERSION=1.0.0
"@

$frontendEnvPath = "$LimsPath\frontend\.env"
$frontendEnv | Out-File -FilePath $frontendEnvPath -Encoding UTF8
Write-Log "Created frontend .env: $frontendEnvPath"

# Create nginx configuration
$nginxConfig = @"
worker_processes  1;

events {
    worker_connections  1024;
}

http {
    include       mime.types;
    default_type  application/octet-stream;

    sendfile        on;
    keepalive_timeout  65;

    # Gzip compression
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;

    server {
        listen       80;
        server_name  localhost;

        # Frontend static files
        location / {
            root   $LimsPath\frontend;
            index  index.html index.htm;
            try_files `$uri `$uri/ /index.html;
        }

        # Backend API proxy
        location /api/ {
            proxy_pass http://localhost:$BackendPort/api/;
            proxy_http_version 1.1;
            proxy_set_header Upgrade `$http_upgrade;
            proxy_set_header Connection 'upgrade';
            proxy_set_header Host `$host;
            proxy_set_header X-Real-IP `$remote_addr;
            proxy_set_header X-Forwarded-For `$proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto `$scheme;
            proxy_cache_bypass `$http_upgrade;
            proxy_read_timeout 86400;
        }

        # Actuator endpoint proxy
        location /actuator/ {
            proxy_pass http://localhost:$BackendPort/actuator/;
            proxy_http_version 1.1;
            proxy_set_header Host `$host;
            proxy_set_header X-Real-IP `$remote_addr;
        }

        # Error pages
        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }
    }
}
"@

$nginxConfigPath = "$LimsPath\config\nginx.conf"
$nginxConfig | Out-File -FilePath $nginxConfigPath -Encoding UTF8
Write-Log "Created nginx.conf: $nginxConfigPath"

# Create batch file for manual backend start
$startBackendBatch = @"
@echo off
echo Starting LIMS Dashboard Backend...
echo.
cd /d $LimsPath\backend
java -jar univ-0.0.1-SNAPSHOT.jar --spring.config.location=file:$LimsPath\config\application.properties
echo.
echo Backend stopped.
pause
"@

$startBackendPath = "$LimsPath\start-backend.bat"
$startBackendBatch | Out-File -FilePath $startBackendPath -Encoding ASCII
Write-Log "Created start-backend.bat: $startBackendPath"

# Create Windows Service installation batch
$installServiceBatch = @"
@echo off
echo Installing LIMS Backend as Windows Service...
echo.
cd /d $LimsPath\backend

:: Download WinSW if not exists
if not exist "LIMS-Backend.exe" (
    echo Downloading WinSW...
    powershell -Command "Invoke-WebRequest -Uri 'https://github.com/winsw/winsw/releases/download/v2.12.0/WinSW-x64.exe' -OutFile 'LIMS-Backend.exe'"
)

:: Create WinSW XML config
echo ^<service^> > LIMS-Backend.xml
echo   ^<id^>lims-backend^</id^> >> LIMS-Backend.xml
echo   ^<name^>LIMS Dashboard Backend^</name^> >> LIMS-Backend.xml
echo   ^<description^>LIMS Dashboard Spring Boot Application^</description^> >> LIMS-Backend.xml
echo   ^<executable^>java^</executable^> >> LIMS-Backend.xml
echo   ^<arguments^>^-jar ^"$LimsPath\backend\univ-0.0.1-SNAPSHOT.jar^" ^--spring.config.location^=file:$LimsPath\config\application.properties^</arguments^> >> LIMS-Backend.xml
echo   ^<logpath^>$LimsPath\logs^</logpath^> >> LIMS-Backend.xml
echo   ^<log mode^=^"rotate^"/^> >> LIMS-Backend.xml
echo   ^<workingdirectory^>$LimsPath\backend^</workingdirectory^> >> LIMS-Backend.xml
echo ^</service^> >> LIMS-Backend.xml

:: Install service
LIMS-Backend.exe install
echo.
echo Service installed. To start: LIMS-Backend.exe start  (or use Services.msc)
pause
"@

$installServicePath = "$LimsPath\install-service.bat"
$installServiceBatch | Out-File -FilePath $installServicePath -Encoding ASCII
Write-Log "Created install-service.bat: $installServicePath"

# Create README
$readme = @"
# LIMS Dashboard - Server Deployment

## Directory Structure
```
$LimsPath
├── backend\          - Backend JAR file
├── frontend\        - Frontend static files (from dist/)
├── logs\            - Application logs
├── config\          - Configuration files
├── backup\          - Database backups
└── uploads\         - File uploads storage
```

## Quick Start

### 1. Copy Application Files
- Copy backend JAR to: $LimsPath\backend\
- Copy frontend dist files to: $LimsPath\frontend\

### 2. Start Backend (Manual)
```cmd
$LimsPath\start-backend.bat
```

### 3. Start Frontend (via nginx)
```cmd
cd C:\nginx
nginx -c $LimsPath\config\nginx.conf
```

### 4. Or Install as Windows Service
```cmd
$LimsPath\install-service.bat
```

## Configuration
- Backend config: $LimsPath\config\application.properties
- nginx config: $LimsPath\config\nginx.conf
- Frontend env: $LimsPath\frontend\.env

## URLs
- Frontend: http://localhost
- Backend API: http://localhost:$BackendPort
- Health Check: http://localhost:$BackendPort/actuator/health

## Troubleshooting
- Logs: $LimsPath\logs\
- PostgreSQL: Check service 'postgresql-x64-15'
"@

$readmePath = "$LimsPath\README.txt"
$readme | Out-File -FilePath $readmePath -Encoding UTF8
Write-Log "Created README.txt: $readmePath"

Write-Log "=== Configuration setup completed ==="
Write-Log ""
Write-Log "Next steps:"
Write-Log "1. Copy backend JAR to: $LimsPath\backend\"
Write-Log "2. Copy frontend files to: $LimsPath\frontend\"
Write-Log "3. Run: $LimsPath\start-backend.bat (to test)"
Write-Log "4. Or run: $LimsPath\install-service.bat (for production)"
