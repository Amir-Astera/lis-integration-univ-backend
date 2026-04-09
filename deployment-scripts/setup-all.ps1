#Requires -RunAsAdministrator
<#
.SYNOPSIS
    LIMS Dashboard - Complete Setup Script
.DESCRIPTION
    Master script that runs all setup steps in sequence
.NOTES
    Run as Administrator on the target server
    File Name: setup-all.ps1
#>

param(
    [string]$PostgresPassword = "postgres123",
    [string]$AppDbPassword = "lims_password_123",
    [string]$LimsPath = "C:\LIMS",
    [string]$BackendJarPath = $null,  # Path to pre-built JAR file
    [string]$FrontendDistPath = $null, # Path to pre-built frontend dist folder
    [switch]$SkipPrerequisites,
    [switch]$SkipDatabase,
    [switch]$SkipConfig,
    [switch]$SkipCopyFiles,
    [switch]$AutoStart
)

$ErrorActionPreference = "Stop"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

function Write-Log {
    param([string]$Message, [string]$Level = "INFO")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $color = switch ($Level) {
        "ERROR" { "Red" }
        "WARN" { "Yellow" }
        "SUCCESS" { "Green" }
        default { "White" }
    }
    Write-Host "[$timestamp] [$Level] $Message" -ForegroundColor $color
}

function Invoke-Step {
    param(
        [string]$Name,
        [scriptblock]$Script
    )
    Write-Log "=== Starting: $Name ==="
    try {
        & $Script
        Write-Log "=== Completed: $Name ===" -Level "SUCCESS"
        return $true
    } catch {
        Write-Log "=== Failed: $Name ===" -Level "ERROR"
        Write-Log $_.Exception.Message -Level "ERROR"
        return $false
    }
}

# ============ MAIN EXECUTION ============

Write-Log @"

╔══════════════════════════════════════════════════════════════╗
║                                                              ║
║           LIMS Dashboard - Complete Setup                    ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝

Target Directory: $LimsPath
PostgreSQL Password: $PostgresPassword
Application DB Password: $AppDbPassword

"@

$overallSuccess = $true

# Step 1: Prerequisites
if (-not $SkipPrerequisites) {
    $success = Invoke-Step -Name "Install Prerequisites" -Script {
        $script = "$scriptDir\01-install-prerequisites.ps1"
        if (-not (Test-Path $script)) {
            throw "Script not found: $script"
        }
        & $script -PostgresPassword $PostgresPassword -InstallPath "C:\Tools"
    }
    $overallSuccess = $overallSuccess -and $success
} else {
    Write-Log "Skipping prerequisites installation" -Level "WARN"
}

# Step 2: Database Setup
if (-not $SkipDatabase -and $overallSuccess) {
    $success = Invoke-Step -Name "Setup Database" -Script {
        $script = "$scriptDir\02-setup-database.ps1"
        if (-not (Test-Path $script)) {
            throw "Script not found: $script"
        }
        & $script -PostgresPassword $PostgresPassword -AppDbPassword $AppDbPassword
    }
    $overallSuccess = $overallSuccess -and $success
} else {
    Write-Log "Skipping database setup" -Level "WARN"
}

# Step 3: Configuration
if (-not $SkipConfig -and $overallSuccess) {
    $success = Invoke-Step -Name "Create Configuration" -Script {
        $script = "$scriptDir\03-create-config.ps1"
        if (-not (Test-Path $script)) {
            throw "Script not found: $script"
        }
        & $script -LimsPath $LimsPath -DbPassword $AppDbPassword
    }
    $overallSuccess = $overallSuccess -and $success
} else {
    Write-Log "Skipping configuration" -Level "WARN"
}

# Step 4: Copy Application Files
if (-not $SkipCopyFiles -and $overallSuccess) {
    if ($BackendJarPath -or $FrontendDistPath) {
        $success = Invoke-Step -Name "Copy Application Files" -Script {
            # Ensure directories exist
            @("$LimsPath\backend", "$LimsPath\frontend") | ForEach-Object {
                if (-not (Test-Path $_)) {
                    New-Item -ItemType Directory -Force -Path $_ | Out-Null
                    Write-Log "Created directory: $_"
                }
            }
            
            # Copy Backend JAR
            if ($BackendJarPath -and (Test-Path $BackendJarPath)) {
                Copy-Item $BackendJarPath "$LimsPath\backend\univ-0.0.1-SNAPSHOT.jar" -Force
                Write-Log "Copied backend JAR: $BackendJarPath"
            } elseif ($BackendJarPath) {
                Write-Log "Backend JAR not found: $BackendJarPath" -Level "WARN"
            }
            
            # Copy Frontend
            if ($FrontendDistPath -and (Test-Path $FrontendDistPath)) {
                Remove-Item "$LimsPath\frontend\*" -Recurse -Force -ErrorAction SilentlyContinue
                Copy-Item "$FrontendDistPath\*" "$LimsPath\frontend\" -Recurse -Force
                Write-Log "Copied frontend files from: $FrontendDistPath"
            } elseif ($FrontendDistPath) {
                Write-Log "Frontend dist not found: $FrontendDistPath" -Level "WARN"
            }
        }
        $overallSuccess = $overallSuccess -and $success
    } else {
        Write-Log "No file paths provided, skipping file copy" -Level "WARN"
        Write-Log "You need to manually copy:"
        Write-Log "  - Backend JAR to: $LimsPath\backend\univ-0.0.1-SNAPSHOT.jar"
        Write-Log "  - Frontend dist to: $LimsPath\frontend\"
    }
}

# Final Status
Write-Log @"

╔══════════════════════════════════════════════════════════════╗
║                                                              ║
║                  Setup Summary                                 ║
║                                                              ║
╚══════════════════════════════════════════════════════════════╝

"@

if ($overallSuccess) {
    Write-Log "✓ Setup completed successfully!" -Level "SUCCESS"
    Write-Log ""
    Write-Log "Next steps:"
    Write-Log "1. Copy your application files if not done:"
    Write-Log "   - Backend JAR → $LimsPath\backend\"
    Write-Log "   - Frontend dist → $LimsPath\frontend\"
    Write-Log ""
    Write-Log "2. Start the application:"
    Write-Log "   Manual:    $LimsPath\start-backend.bat"
    Write-Log "   Or:        $scriptDir\05-start-lims.bat"
    Write-Log "   Service:   $LimsPath\install-service.bat"
    Write-Log ""
    Write-Log "3. Access the application:"
    Write-Log "   Frontend: http://localhost"
    Write-Log "   Backend:  http://localhost:8080"
    Write-Log "   Health:   http://localhost:8080/actuator/health"
    
    if ($AutoStart) {
        Write-Log ""
        Write-Log "Auto-starting application..."
        & "$scriptDir\05-start-lims.bat"
    }
} else {
    Write-Log "✗ Setup completed with errors!" -Level "ERROR"
    Write-Log "Please check the logs above and retry failed steps."
    exit 1
}

Write-Log ""
Write-Log "For help, see: $LimsPath\README.txt"
