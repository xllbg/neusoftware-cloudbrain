@echo off
echo ========================================
echo   CloudBrain - Kill Ports
echo ========================================
echo.

echo [1/2] Killing port 8080 (backend)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8080 ^| findstr LISTENING') do (
    echo   Killing PID %%a ...
    taskkill /PID %%a /F >nul 2>&1
)

echo [2/2] Killing port 5173 (frontend)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :5173 ^| findstr LISTENING') do (
    echo   Killing PID %%a ...
    taskkill /PID %%a /F >nul 2>&1
)

echo.
echo ========================================
echo   Done - All ports cleared
echo ========================================
pause
