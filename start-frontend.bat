@echo off
echo ========================================
echo Starting Student Activity Tracker Frontend
echo ========================================
echo.

cd frontend

echo Checking Node.js version...
node --version
echo.

echo Checking if node_modules exists...
if not exist "node_modules" (
    echo Installing dependencies...
    call npm install
    echo.
)

echo Starting development server...
echo.
echo Frontend will be available at: http://localhost:3000
echo Press Ctrl+C to stop the server
echo.

call npm run dev
