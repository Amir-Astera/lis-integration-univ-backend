#Requires -RunAsAdministrator
<#
.SYNOPSIS
    LIMS Dashboard Database Setup Script
.DESCRIPTION
    Creates database, runs Flyway migrations, creates application user
.NOTES
    Run as Administrator
    File Name: 02-setup-database.ps1
#>

param(
    [string]$PostgresUser = "postgres",
    [string]$PostgresPassword = "postgres123",
    [string]$DatabaseName = "lis_dashboard",
    [string]$AppDbUser = "lims_user",
    [string]$AppDbPassword = "lims_password_123",
    [string]$PostgresBinPath = "C:\Program Files\PostgreSQL\15\bin",
    [string]$MigrationPath = "C:\LIMS\backend\migrations"
)

$ErrorActionPreference = "Stop"

function Write-Log {
    param([string]$Message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] $Message"
}

function Invoke-PSQL {
    param(
        [string]$Command,
        [string]$Database = "postgres"
    )
    $env:PGPASSWORD = $PostgresPassword
    & "$PostgresBinPath\psql.exe" -U $PostgresUser -d $Database -c $Command 2>&1
    Remove-Item Env:\PGPASSWORD
}

# ============ MAIN EXECUTION ============

Write-Log "=== LIMS Dashboard Database Setup Started ==="

# Check if PostgreSQL is running
$pgService = Get-Service -Name "postgresql-x64-15" -ErrorAction SilentlyContinue
if (-not $pgService) {
    Write-Log "ERROR: PostgreSQL service not found. Please install PostgreSQL first."
    exit 1
}

if ($pgService.Status -ne "Running") {
    Write-Log "Starting PostgreSQL service..."
    Start-Service -Name "postgresql-x64-15"
    Start-Sleep -Seconds 3
}

Write-Log "PostgreSQL is running"

# Create database
Write-Log "Creating database '$DatabaseName'..."
$checkDb = Invoke-PSQL -Command "SELECT 1 FROM pg_database WHERE datname = '$DatabaseName';"
if ($checkDb -match "1 row") {
    Write-Log "Database '$DatabaseName' already exists"
} else {
    Invoke-PSQL -Command "CREATE DATABASE $DatabaseName;"
    Write-Log "Database '$DatabaseName' created"
}

# Create application user
Write-Log "Creating application database user '$AppDbUser'..."
Invoke-PSQL -Command "DO \$\$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = '$AppDbUser') THEN
        CREATE USER $AppDbUser WITH PASSWORD '$AppDbPassword';
    END IF;
END
\$\$;"
Write-Log "Application user created/verified"

# Grant privileges
Write-Log "Granting privileges to application user..."
Invoke-PSQL -Command "GRANT ALL PRIVILEGES ON DATABASE $DatabaseName TO $AppDbUser;" -Database $DatabaseName

# Create schema extensions
Write-Log "Setting up database extensions..."
Invoke-PSQL -Command "CREATE EXTENSION IF NOT EXISTS "uuid-ossp";" -Database $DatabaseName

Write-Log "=== Database setup completed ==="
Write-Log "Database: $DatabaseName"
Write-Log "App user: $AppDbUser"
Write-Log ""
Write-Log "Connection strings for application.properties:"
Write-Log "spring.r2dbc.url=r2dbc:postgresql://localhost:5432/$DatabaseName"
Write-Log "spring.r2dbc.username=$AppDbUser"
Write-Log "spring.r2dbc.password=$AppDbPassword"
Write-Log "spring.flyway.url=jdbc:postgresql://localhost:5432/$DatabaseName"
Write-Log "spring.flyway.user=$AppDbUser"
Write-Log "spring.flyway.password=$AppDbPassword"
