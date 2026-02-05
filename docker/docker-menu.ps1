# PowerShell script for interactive Docker Compose management.
# It is assumed that the script is executed from the same directory
# where the 'docker-compose.yml' file is located.

# Name of the Compose file in the current directory
$ComposeFile = "docker-posgress-pgadmin.yml"

function Show-Menu {
    Clear-Host
    Write-Host "========================================" -ForegroundColor Yellow
    $Banner = @"
                                      (_)
  ___ _   _ _ __   ___ _ __ ___  _ __  ___  __
 / __| | | | '_ \ / _ \ '__/ _ \| '_ \| \ \/ /
 \__ \ |_| | | | |  __/ | | (_) | | | | |>  <
 |___/\__, |_| |_|\___|_|  \___/|_| |_|_/_/\_\
       __/ |
      |___/
"@
    Write-Host $Banner -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "1. Check running container status (docker ps)" -ForegroundColor Cyan
    Write-Host "2. View live service logs (docker compose logs -f)" -ForegroundColor Cyan
    Write-Host "3. Start the stack in background (docker compose up -d)" -ForegroundColor Green
    Write-Host "4. Stop services (docker compose stop) - Preserves data/volumes" -ForegroundColor DarkYellow
    Write-Host "5. Stop and remove containers/networks (docker compose down)" -ForegroundColor Red
    Write-Host "6. STOP, REMOVE CONTAINERS AND VOLUMES (-v) - WARNING: DELETES ALL DB DATA!" -ForegroundColor DarkRed
    Write-Host "7. Clean up unused system resources (docker system prune -f)" -ForegroundColor Magenta
    Write-Host "8. Exit"
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Yellow
}

do {
    Show-Menu
    $Choice = Read-Host "Enter option number"

    switch ($Choice) {
        "1" {
            Write-Host "--- Executing: docker ps ---" -ForegroundColor Cyan
            docker ps
            pause
        }
        "2" {
            Write-Host "--- Executing: docker compose logs -f (Press Ctrl+C to exit) ---" -ForegroundColor Cyan
            # Use -f for following the logs
            docker compose -f $ComposeFile logs -f
        }
        "3" {
            Write-Host "--- Executing: docker compose up -d (Start) ---" -ForegroundColor Green
            docker compose -f $ComposeFile up -d
            if ($LASTEXITCODE -eq 0) {
                Write-Host "Startup successful." -ForegroundColor Green
            } else {
                Write-Host "Startup error. Code: $LASTEXITCODE" -ForegroundColor Red
            }
            pause
        }
        "4" {
            Write-Host "--- Executing: docker compose stop (Stop only) ---" -ForegroundColor DarkYellow
            docker compose -f $ComposeFile stop
            if ($LASTEXITCODE -eq 0) {
                Write-Host "Services stopped successfully." -ForegroundColor Green
            } else {
                Write-Host "Stop error. Code: $LASTEXITCODE" -ForegroundColor Red
            }
            pause
        }
        "5" {
            Write-Host "--- Executing: docker compose down (Stop and remove containers) ---" -ForegroundColor Red
            docker compose -f $ComposeFile down
            if ($LASTEXITCODE -eq 0) {
                Write-Host "Shutdown successful." -ForegroundColor Green
            } else {
                Write-Host "Shutdown error. Code: $LASTEXITCODE" -ForegroundColor Red
            }
            pause
        }
        "6" {
            Write-Host "--- Executing: docker compose down -v (STOP AND REMOVE CONTAINERS AND VOLUMES) ---" -ForegroundColor DarkRed
            Write-Host "!!! WARNING: ALL PERSISTENT DATA (including DB data) WILL BE DELETED !!!" -ForegroundColor Red
            docker compose -f $ComposeFile down -v
            if ($LASTEXITCODE -eq 0) {
                Write-Host "Full cleanup and volume removal successful." -ForegroundColor Green
            } else {
                Write-Host "Full cleanup error. Code: $LASTEXITCODE" -ForegroundColor Red
            }
            pause
        }
        "7" {
            Write-Host "--- Executing: docker system prune -f (Cleanup unused resources) ---" -ForegroundColor Magenta
            # -f (force) flag bypasses the confirmation prompt, suitable for script
            docker system prune -f
            if ($LASTEXITCODE -eq 0) {
                Write-Host "System resources cleaned up." -ForegroundColor Green
            } else {
                Write-Host "Cleanup error. Code: $LASTEXITCODE" -ForegroundColor Red
            }
            pause
        }
        "8" {
            Write-Host "Exiting program. Goodbye!" -ForegroundColor Yellow
            break
        }
        default {
            Write-Host "Invalid choice. Please enter a number between 1 and 8." -ForegroundColor Red
            pause
        }
    }
}
until ($Choice -eq "8")