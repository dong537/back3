param(
    [switch]$Up,
    [switch]$UseLocalDb
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$composeFile = Join-Path $root 'docker-compose.yml'

Push-Location $root
try {
    Write-Host '1/3 Building backend jar...' -ForegroundColor Cyan
    mvn -q -DskipTests package

    Write-Host '2/3 Building Docker images...' -ForegroundColor Cyan
    docker compose -f $composeFile build backend frontend

    if ($Up) {
        Write-Host '3/3 Starting containers...' -ForegroundColor Cyan
        if ($UseLocalDb) {
            $originalDbHost = $env:DB_HOST
            $originalDbPort = $env:DB_PORT
            $originalDbName = $env:DB_NAME
            $originalDbUsername = $env:DB_USERNAME
            $originalDbPassword = $env:DB_PASSWORD

            $env:DB_HOST = 'mysql'
            $env:DB_PORT = '3306'
            if (-not $env:DB_NAME) { $env:DB_NAME = 'bazi' }
            if (-not $env:DB_USERNAME) { $env:DB_USERNAME = 'bazi' }
            if (-not $env:DB_PASSWORD) { $env:DB_PASSWORD = 'bazi123456' }

            try {
                docker compose -f $composeFile --profile local-db up -d
            }
            finally {
                $env:DB_HOST = $originalDbHost
                $env:DB_PORT = $originalDbPort
                $env:DB_NAME = $originalDbName
                $env:DB_USERNAME = $originalDbUsername
                $env:DB_PASSWORD = $originalDbPassword
            }
        } else {
            docker compose -f $composeFile up -d backend frontend
        }
    } else {
        Write-Host '3/3 Images are ready. Use -Up to start containers.' -ForegroundColor Green
    }
}
finally {
    Pop-Location
}
