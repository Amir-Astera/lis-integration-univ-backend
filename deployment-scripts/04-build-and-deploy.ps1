#Requires -RunAsAdministrator
<#
.SYNOPSIS
    LIMS Dashboard Build and Deploy Script
.DESCRIPTION
    Builds backend and frontend, then copies files to server destination
.NOTES
    Run on development machine (not server)
    File Name: 04-build-and-deploy.ps1
#>

param(
    [string]$SourceBackend = "..\univ",
    [string]$SourceFrontend = "..\..\lis-integration-front",
    [string]$DeployTarget = "C:\LIMS",
    [string]$ServerAddress = $null,  # e.g., "192.168.1.100" - if null, deploys locally
    [string]$ServerUsername = $null,
    [string]$ServerPassword = $null,
    [switch]$SkipBuild,
    [switch]$SkipBackend,
    [switch]$SkipFrontend,
    [switch]$CreateZip
)

$ErrorActionPreference = "Stop"

function Write-Log {
    param([string]$Message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] $Message"
}

function Test-Command {
    param([string]$Command)
    try {
        Get-Command $Command -ErrorAction Stop | Out-Null
        return $true
    } catch {
        return $false
    }
}

# ============ MAIN EXECUTION ============

Write-Log "=== LIMS Dashboard Build and Deploy Started ==="

# Resolve paths
$backendPath = Resolve-Path $SourceBackend -ErrorAction SilentlyContinue
$frontendPath = Resolve-Path $SourceFrontend -ErrorAction SilentlyContinue

if (-not $backendPath) {
    Write-Log "ERROR: Backend path not found: $SourceBackend"
    exit 1
}

if (-not $frontendPath) {
    Write-Log "ERROR: Frontend path not found: $SourceFrontend"
    exit 1
}

Write-Log "Backend source: $backendPath"
Write-Log "Frontend source: $frontendPath"
Write-Log "Deploy target: $DeployTarget"

# Build Backend
if (-not $SkipBuild -and -not $SkipBackend) {
    Write-Log "Building Backend..."
    
    if (-not (Test-Command "java")) {
        Write-Log "ERROR: Java not found in PATH"
        exit 1
    }
    
    Set-Location $backendPath
    
    # Run Gradle build
    $gradleCmd = if (Test-Path "gradlew.bat") { ".\gradlew.bat" } elseif (Test-Path "gradlew") { ".\gradlew" } else { $null }
    
    if (-not $gradleCmd) {
        Write-Log "ERROR: Gradle wrapper not found"
        exit 1
    }
    
    Write-Log "Running: $gradleCmd bootJar"
    & $gradleCmd bootJar --no-daemon
    
    if ($LASTEXITCODE -ne 0) {
        Write-Log "ERROR: Backend build failed"
        exit 1
    }
    
    Write-Log "Backend build completed"
}

# Build Frontend
if (-not $SkipBuild -and -not $SkipFrontend) {
    Write-Log "Building Frontend..."
    
    if (-not (Test-Command "npm")) {
        Write-Log "ERROR: npm not found in PATH"
        exit 1
    }
    
    Set-Location $frontendPath
    
    # Install dependencies if node_modules doesn't exist
    if (-not (Test-Path "node_modules")) {
        Write-Log "Installing npm dependencies..."
        npm install
    }
    
    # Build
    Write-Log "Running: npm run build"
    npm run build
    
    if ($LASTEXITCODE -ne 0) {
        Write-Log "ERROR: Frontend build failed"
        exit 1
    }
    
    Write-Log "Frontend build completed"
}

# Find JAR file
$jarFile = Get-ChildItem -Path "$backendPath\build\libs" -Filter "*.jar" | 
    Where-Object { $_.Name -notlike "*-plain.jar" } | 
    Select-Object -First 1

if (-not $jarFile) {
    Write-Log "ERROR: JAR file not found in $backendPath\build\libs"
    exit 1
}

Write-Log "Found JAR file: $($jarFile.FullName)"

# Deploy
if ($ServerAddress) {
    # Remote deployment via PowerShell remoting or file copy
    Write-Log "Deploying to remote server: $ServerAddress"
    
    # Create credential
    $securePassword = ConvertTo-SecureString $ServerPassword -AsPlainText -Force
    $credential = New-Object System.Management.Automation.PSCredential($ServerUsername, $securePassword)
    
    # Create remote session
    $session = New-PSSession -ComputerName $ServerAddress -Credential $credential -ErrorAction SilentlyContinue
    
    if (-not $session) {
        Write-Log "WARNING: PowerShell remoting failed, trying network copy..."
        
        # Map network drive if needed
        $driveLetter = "Z:"
        net use $driveLetter "\\$ServerAddress\C$" /user:$ServerUsername $ServerPassword 2>$null
        
        $remoteTarget = "$driveLetter$($DeployTarget -replace ':', '$')"
        
        # Copy files
        Copy-Item $jarFile.FullName "$remoteTarget\backend\" -Force
        Copy-Item "$frontendPath\dist\*" "$remoteTarget\frontend\" -Recurse -Force
        
        net use $driveLetter /delete 2>$null
    } else {
        # Copy via PS Session
        Invoke-Command -Session $session -ScriptBlock {
            param($Target)
            if (-not (Test-Path $Target)) {
                New-Item -ItemType Directory -Force -Path $Target | Out-Null
            }
        } -ArgumentList $DeployTarget
        
        Copy-Item $jarFile.FullName -Destination "$DeployTarget\backend\" -ToSession $session -Force
        Copy-Item "$frontendPath\dist\*" -Destination "$DeployTarget\frontend\" -ToSession $session -Recurse -Force
        
        Remove-PSSession $session
    }
} else {
    # Local deployment
    Write-Log "Deploying locally to: $DeployTarget"
    
    # Ensure directories exist
    @("$DeployTarget\backend", "$DeployTarget\frontend") | ForEach-Object {
        if (-not (Test-Path $_)) {
            New-Item -ItemType Directory -Force -Path $_ | Out-Null
        }
    }
    
    # Copy files
    Write-Log "Copying JAR file..."
    Copy-Item $jarFile.FullName "$DeployTarget\backend\univ-0.0.1-SNAPSHOT.jar" -Force
    
    Write-Log "Copying frontend files..."
    if (Test-Path "$frontendPath\dist") {
        Remove-Item "$DeployTarget\frontend\*" -Recurse -Force 2>$null
        Copy-Item "$frontendPath\dist\*" "$DeployTarget\frontend\" -Recurse -Force
    } else {
        Write-Log "WARNING: dist folder not found at $frontendPath\dist"
    }
}

Write-Log "Deployment completed!"

# Create deployment package ZIP
if ($CreateZip) {
    $zipFile = "$env:TEMP\LIMS-Deploy-$(Get-Date -Format 'yyyyMMdd-HHmmss').zip"
    Write-Log "Creating deployment package: $zipFile"
    
    $tempDeploy = "$env:TEMP\LIMS-Deploy-$(Get-Random)"
    New-Item -ItemType Directory -Force -Path $tempDeploy | Out-Null
    
    # Copy structure
    Copy-Item $jarFile.FullName "$tempDeploy\backend\univ.jar" -Force
    Copy-Item "$frontendPath\dist" "$tempDeploy\frontend" -Recurse -Force
    
    # Add deployment scripts
    if (Test-Path ".") {
        Copy-Item "*.ps1" "$tempDeploy\" -Force
        Copy-Item "*.bat" "$tempDeploy\" -Force
    }
    
    Compress-Archive -Path "$tempDeploy\*" -DestinationPath $zipFile -Force
    Remove-Item $tempDeploy -Recurse -Force
    
    Write-Log "Deployment package created: $zipFile"
}

Write-Log "=== Build and Deploy completed ==="
Write-Log ""
Write-Log "Next steps on server:"
Write-Log "1. Run: 01-install-prerequisites.ps1 (if not done)"
Write-Log "2. Run: 02-setup-database.ps1 (if not done)"
Write-Log "3. Run: 03-create-config.ps1 (if not done)"
Write-Log "4. Start application: $DeployTarget\start-backend.bat"
