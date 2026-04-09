#Requires -RunAsAdministrator
<#
.SYNOPSIS
    LIMS Dashboard Prerequisites Installation Script
.DESCRIPTION
    Installs Java 17, PostgreSQL 15, Node.js LTS, and nginx on Windows Server
.NOTES
    Run as Administrator
    File Name: 01-install-prerequisites.ps1
#>

param(
    [string]$InstallPath = "C:\Tools",
    [string]$PostgresPassword = "postgres123",
    [switch]$SkipJava,
    [switch]$SkipPostgres,
    [switch]$SkipNode,
    [switch]$SkipNginx
)

$ErrorActionPreference = "Stop"
$ProgressPreference = "Continue"

# Create installation directory
New-Item -ItemType Directory -Force -Path $InstallPath | Out-Null
$logFile = "$InstallPath\install-log.txt"

function Write-Log {
    param([string]$Message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    "$timestamp - $Message" | Tee-Object -FilePath $logFile -Append
}

function Install-Java {
    Write-Log "Installing Java 17 (Eclipse Temurin)..."
    
    $javaInstaller = "$InstallPath\OpenJDK17.msi"
    $javaUrl = "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.9%2B9.1/OpenJDK17U-jdk_x64_windows_hotspot_17.0.9_9.msi"
    
    # Download Java installer
    if (-not (Test-Path $javaInstaller)) {
        Write-Log "Downloading Java installer..."
        Invoke-WebRequest -Uri $javaUrl -OutFile $javaInstaller -UseBasicParsing
    }
    
    # Install Java silently
    Write-Log "Running Java installer..."
    $proc = Start-Process -FilePath "msiexec.exe" -ArgumentList "/i", $javaInstaller, "/quiet", "/norestart", "INSTALLLEVEL=1" -Wait -PassThru
    
    if ($proc.ExitCode -eq 0) {
        Write-Log "Java installed successfully"
        
        # Set JAVA_HOME
        $javaHome = "C:\Program Files\Eclipse Adoptium\jdk-17.0.9.9-hotspot"
        [Environment]::SetEnvironmentVariable("JAVA_HOME", $javaHome, "Machine")
        
        # Add to PATH
        $currentPath = [Environment]::GetEnvironmentVariable("Path", "Machine")
        if ($currentPath -notlike "*$javaHome\bin*") {
            [Environment]::SetEnvironmentVariable("Path", "$currentPath;$javaHome\bin", "Machine")
        }
        
        # Refresh environment
        $env:JAVA_HOME = $javaHome
        $env:Path = [Environment]::GetEnvironmentVariable("Path", "Machine")
        
        # Verify
        $javaVersion = java -version 2>&1
        Write-Log "Java version: $javaVersion"
    } else {
        Write-Log "ERROR: Java installation failed with exit code $($proc.ExitCode)"
        exit 1
    }
}

function Install-PostgreSQL {
    Write-Log "Installing PostgreSQL 15..."
    
    $pgInstaller = "$InstallPath\postgresql-15.5-1-windows-x64.exe"
    $pgUrl = "https://get.enterprisedb.com/postgresql/postgresql-15.5-1-windows-x64.exe"
    
    # Download PostgreSQL installer
    if (-not (Test-Path $pgInstaller)) {
        Write-Log "Downloading PostgreSQL installer..."
        Invoke-WebRequest -Uri $pgUrl -OutFile $pgInstaller -UseBasicParsing
    }
    
    # Install PostgreSQL silently
    Write-Log "Running PostgreSQL installer..."
    $installDir = "C:\Program Files\PostgreSQL\15"
    $dataDir = "C:\Program Files\PostgreSQL\15\data"
    
    $arguments = @(
        "--mode", "unattended",
        "--unattendedmodeui", "minimal",
        "--superpassword", $PostgresPassword,
        "--serverport", "5432",
        "--prefix", $installDir,
        "--datadir", $dataDir
    )
    
    $proc = Start-Process -FilePath $pgInstaller -ArgumentList $arguments -Wait -PassThru
    
    if ($proc.ExitCode -eq 0) {
        Write-Log "PostgreSQL installed successfully"
        
        # Add to PATH
        $pgBin = "$installDir\bin"
        $currentPath = [Environment]::GetEnvironmentVariable("Path", "Machine")
        if ($currentPath -notlike "*$pgBin*") {
            [Environment]::SetEnvironmentVariable("Path", "$currentPath;$pgBin", "Machine")
        }
        
        # Refresh environment
        $env:Path = [Environment]::GetEnvironmentVariable("Path", "Machine")
        
        # Configure pg_hba.conf for local trust authentication
        $pgHba = "$dataDir\pg_hba.conf"
        if (Test-Path $pgHba) {
            Write-Log "Configuring PostgreSQL authentication..."
            $content = Get-Content $pgHba
            $content = $content -replace "scram-sha-256", "trust"
            $content | Set-Content $pgHba
            
            # Restart PostgreSQL service
            Restart-Service -Name "postgresql-x64-15" -Force
            Write-Log "PostgreSQL configured and restarted"
        }
    } else {
        Write-Log "ERROR: PostgreSQL installation failed with exit code $($proc.ExitCode)"
        exit 1
    }
}

function Install-NodeJS {
    Write-Log "Installing Node.js LTS..."
    
    $nodeInstaller = "$InstallPath\nodejs-lts.msi"
    $nodeUrl = "https://nodejs.org/dist/v20.10.0/node-v20.10.0-x64.msi"
    
    # Download Node.js installer
    if (-not (Test-Path $nodeInstaller)) {
        Write-Log "Downloading Node.js installer..."
        Invoke-WebRequest -Uri $nodeUrl -OutFile $nodeInstaller -UseBasicParsing
    }
    
    # Install Node.js silently
    Write-Log "Running Node.js installer..."
    $proc = Start-Process -FilePath "msiexec.exe" -ArgumentList "/i", $nodeInstaller, "/quiet", "/norestart" -Wait -PassThru
    
    if ($proc.ExitCode -eq 0) {
        Write-Log "Node.js installed successfully"
        
        # Refresh environment
        $env:Path = [Environment]::GetEnvironmentVariable("Path", "Machine")
        
        # Verify
        $nodeVersion = node --version
        $npmVersion = npm --version
        Write-Log "Node.js version: $nodeVersion, npm version: $npmVersion"
    } else {
        Write-Log "ERROR: Node.js installation failed with exit code $($proc.ExitCode)"
        exit 1
    }
}

function Install-Nginx {
    Write-Log "Installing nginx..."
    
    $nginxZip = "$InstallPath\nginx.zip"
    $nginxUrl = "https://nginx.org/download/nginx-1.24.0.zip"
    $nginxInstallDir = "C:\nginx"
    
    # Download nginx
    if (-not (Test-Path $nginxZip)) {
        Write-Log "Downloading nginx..."
        Invoke-WebRequest -Uri $nginxUrl -OutFile $nginxZip -UseBasicParsing
    }
    
    # Extract nginx
    Write-Log "Extracting nginx..."
    if (Test-Path $nginxInstallDir) {
        Remove-Item -Path $nginxInstallDir -Recurse -Force
    }
    Expand-Archive -Path $nginxZip -DestinationPath "C:\" -Force
    Rename-Item -Path "C:\nginx-1.24.0" -NewName "nginx" -Force
    
    Write-Log "nginx installed to $nginxInstallDir"
}

# ============ MAIN EXECUTION ============

Write-Log "=== LIMS Dashboard Prerequisites Installation Started ==="
Write-Log "Install path: $InstallPath"
Write-Log "PostgreSQL password: $PostgresPassword"

if (-not $SkipJava) {
    try {
        $javaCheck = java -version 2>&1
        Write-Log "Java already installed: $javaCheck"
    } catch {
        Install-Java
    }
} else {
    Write-Log "Skipping Java installation"
}

if (-not $SkipPostgres) {
    $pgService = Get-Service -Name "postgresql-x64-15" -ErrorAction SilentlyContinue
    if ($pgService) {
        Write-Log "PostgreSQL already installed"
    } else {
        Install-PostgreSQL
    }
} else {
    Write-Log "Skipping PostgreSQL installation"
}

if (-not $SkipNode) {
    try {
        $nodeVersion = node --version
        Write-Log "Node.js already installed: $nodeVersion"
    } catch {
        Install-NodeJS
    }
} else {
    Write-Log "Skipping Node.js installation"
}

if (-not $SkipNginx) {
    if (Test-Path "C:\nginx\nginx.exe") {
        Write-Log "nginx already installed"
    } else {
        Install-Nginx
    }
} else {
    Write-Log "Skipping nginx installation"
}

Write-Log "=== Installation completed successfully ==="
Write-Log "Log file: $logFile"
