@echo off
echo ========================================
echo Starting Student Activity Tracker Backend
echo ========================================
echo.

cd backend

echo Checking Java version...
java -version
echo.

echo Building application...
call mvn clean install -DskipTests
echo.

if %ERRORLEVEL% EQU 0 (
    echo Build successful! Starting application...
    echo.
    echo Backend will be available at: http://localhost:8080
    echo Press Ctrl+C to stop the server
    echo.
    call mvn spring-boot:run
) else (
    echo Build failed! Please check the errors above.
    pause
)
