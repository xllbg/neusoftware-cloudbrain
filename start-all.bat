@echo off
echo ========================================
echo   CloudBrain Platform Launcher
echo ========================================
echo.

if not exist "%~dp0.env" (
    echo [WARNING] .env file not found. Please copy .env.example to .env
    echo.
)

cd /d "%~dp0backend"
echo [1/2] Starting backend (port 8080)...
start "CloudBrain-Backend" cmd /k "title CloudBrain-Backend && mvn spring-boot:run"

echo Waiting for backend to start...
timeout /t 10 /nobreak >nul

cd /d "%~dp0frontend"
echo [2/2] Starting frontend (port 5173)...
start "CloudBrain-Frontend" cmd /k "title CloudBrain-Frontend && npm run dev"

echo.
echo ========================================
echo   Services starting...
echo   Backend:  http://localhost:8080
echo   Frontend: http://localhost:5173
echo   API Doc:  http://localhost:8080/doc.html
echo ========================================
echo.
pause
