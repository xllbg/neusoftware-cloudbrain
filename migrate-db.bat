@echo off
setlocal EnableDelayedExpansion
chcp 65001 >nul 2>&1
title CloudBrain - Database Migration

set "SCRIPT_DIR=%~dp0"
set "ENV_FILE=%SCRIPT_DIR%.env"

echo ========================================
echo   CloudBrain - Database Migration V2
echo   Add: doctor.status, doctor.reject_reason
echo ========================================
echo.

:: ===== Load .env =====
if not exist "%ENV_FILE%" (
    echo [ERROR] .env file not found at: %ENV_FILE%
    echo         Please copy .env.example to .env and fill in values.
    goto :end
)

echo [INFO] Loading config from .env...

set "ENV_DB_URL="
set "ENV_DB_USER="
set "ENV_DB_PASS="

for /f "usebackq tokens=1,* delims==" %%a in ("%ENV_FILE%") do (
    set "key=%%a"
    set "val=%%b"
    set "key=!key: =!"
    if "!key!"=="DB_URL" (
        for /f "tokens=1 delims=#" %%x in ("!val!") do set "ENV_DB_URL=%%x"
    )
    if "!key!"=="DB_USERNAME" (
        for /f "tokens=1 delims=#" %%x in ("!val!") do set "ENV_DB_USER=%%x"
    )
    if "!key!"=="DB_PASSWORD" (
        for /f "tokens=1 delims=#" %%x in ("!val!") do set "ENV_DB_PASS=%%x"
    )
)

:: ===== Trim =====
for /f "tokens=*" %%a in ("!ENV_DB_URL!")  do set "ENV_DB_URL=%%a"
for /f "tokens=*" %%a in ("!ENV_DB_USER!") do set "ENV_DB_USER=%%a"
for /f "tokens=*" %%a in ("!ENV_DB_PASS!") do set "ENV_DB_PASS=%%a"

:: ===== Parse JDBC URL: jdbc:mysql://host:port/db?params =====
set "TMP_URL=!ENV_DB_URL:jdbc:mysql://=!"
for /f "tokens=1 delims=/" %%a in ("!TMP_URL!") do set "DB_HOST_PORT=%%a"
for /f "tokens=2 delims=/" %%a in ("!TMP_URL!") do set "TMP_DB=%%a"
for /f "tokens=1 delims=?" %%a in ("!TMP_DB!") do set "DB_NAME=%%a"
for /f "tokens=1 delims=:" %%a in ("!DB_HOST_PORT!") do set "DB_HOST=%%a"
for /f "tokens=2 delims=:" %%a in ("!DB_HOST_PORT!") do set "DB_PORT=%%a"
if "!DB_PORT!"=="" set "DB_PORT=3306"

set "DB_USER=!ENV_DB_USER!"
set "DB_PASS=!ENV_DB_PASS!"

:: Trim trailing/leading spaces
for /f "tokens=*" %%a in ("!DB_HOST!") do set "DB_HOST=%%a"
for /f "tokens=*" %%a in ("!DB_PORT!") do set "DB_PORT=%%a"
for /f "tokens=*" %%a in ("!DB_NAME!") do set "DB_NAME=%%a"
for /f "tokens=*" %%a in ("!DB_USER!") do set "DB_USER=%%a"
for /f "tokens=*" %%a in ("!DB_PASS!") do set "DB_PASS=%%a"

echo [INFO] Host:   !DB_HOST!:!DB_PORT!
echo [INFO] DB:     !DB_NAME!
echo [INFO] User:   !DB_USER!
echo.

:: ===== Check mysql command =====
where mysql >nul 2>&1
if !errorlevel! neq 0 (
    echo [ERROR] mysql command not found! Add MySQL to PATH.
    echo         Example: C:\Program Files\MySQL\MySQL Server 8.4\bin
    goto :end
)

:: ===== Check connection =====
echo [1/3] Checking database connection...
mysql -h!DB_HOST! -P!DB_PORT! -u!DB_USER! -p!DB_PASS! -e "SELECT 1" !DB_NAME! >nul 2>&1
if !errorlevel! neq 0 (
    echo [ERROR] Cannot connect to MySQL!
    echo         Host: !DB_HOST!:!DB_PORT!
    echo         User: !DB_USER!
    echo         Please check MySQL service and credentials in .env
    goto :end
)
echo         Connected OK.

:: ===== Run migration =====
echo [2/3] Running migration...

echo         Adding doctor.status...
mysql -h!DB_HOST! -P!DB_PORT! -u!DB_USER! -p!DB_PASS! !DB_NAME! -e "ALTER TABLE doctor ADD COLUMN status VARCHAR(20) DEFAULT 'APPROVED' COMMENT 'status: PENDING/APPROVED/REJECTED';" 2>nul
if !errorlevel! equ 0 (
    echo         Column 'status' added.
) else (
    echo         Column 'status' already exists, skipped.
)

echo         Adding doctor.reject_reason...
mysql -h!DB_HOST! -P!DB_PORT! -u!DB_USER! -p!DB_PASS! !DB_NAME! -e "ALTER TABLE doctor ADD COLUMN reject_reason VARCHAR(255) COMMENT 'reject reason';" 2>nul
if !errorlevel! equ 0 (
    echo         Column 'reject_reason' added.
) else (
    echo         Column 'reject_reason' already exists, skipped.
)

echo         Updating existing doctors...
mysql -h!DB_HOST! -P!DB_PORT! -u!DB_USER! -p!DB_PASS! !DB_NAME! -e "UPDATE doctor SET status = 'APPROVED' WHERE status IS NULL OR status = '';" 2>nul
echo         Existing doctors set to APPROVED.

:: ===== Verify =====
echo [3/3] Verifying...
echo.
mysql -h!DB_HOST! -P!DB_PORT! -u!DB_USER! -p!DB_PASS! !DB_NAME! -e "SELECT id, username, name, status, IFNULL(reject_reason,'-') as reject_reason FROM doctor;"
echo.
echo ========================================
echo   Migration completed.
echo   Existing doctors: status = APPROVED
echo   New registrations: status = PENDING
echo ========================================

:end
echo.
pause
endlocal
