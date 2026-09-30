@echo off
title ShowTime Tickets
cd /d "%~dp0"
where java >nul 2>nul
if errorlevel 1 (
  echo Java was not found. Install JDK 17 or 21 from https://adoptium.net and run this again.
  pause
  exit /b 1
)
echo Starting ShowTime Tickets...
echo When you see "Started TicketBookingApplication", open http://localhost:8080
start "" cmd /c "timeout /t 45 >nul && start http://localhost:8080"
call gradlew.bat bootRun
pause
