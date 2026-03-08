@echo off
chcp 65001 >nul
REM ============================================================
REM Campus Recruitment System - Startup Script (Windows)
REM ============================================================

setlocal enabledelayedexpansion

REM Project directory
set "PROJECT_DIR=%~dp0"
cd /d "%PROJECT_DIR%"

REM Database config
set "DB_HOST=localhost"
set "DB_PORT=3306"
set "DB_NAME=campus_recruitment"
set "DB_USER=root"
set "DB_PASS=root"

REM Port config
set "BACKEND_PORT=8080"
set "ADMIN_PORT=8001"
set "FRONTEND_PORT=8002"

REM Logs and PID directories
set "LOGS_DIR=.logs"
set "PIDS_DIR=.pids"
if not exist "%LOGS_DIR%" mkdir "%LOGS_DIR%"
if not exist "%PIDS_DIR%" mkdir "%PIDS_DIR%"

REM Banner
echo.
echo ============================================================
echo          Campus Recruitment System - Startup
echo ============================================================
echo.

REM Error counter
set "ERROR_COUNT=0"

REM 1. Check Java
echo [1/10] Checking Java...
where java >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Java installed
    java -version 2>&1 | findstr /C:"version"
) else (
    echo [FAIL] Java not installed
    set /a ERROR_COUNT+=1
)

REM 2. Check Maven
echo.
echo [2/10] Checking Maven...
where mvn >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Maven installed
    mvn -version | findstr /C:"Apache Maven"
) else (
    echo [FAIL] Maven not installed
    set /a ERROR_COUNT+=1
)

REM 3. Check Node.js
echo.
echo [3/10] Checking Node.js...
where node >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Node.js installed
    node -v
) else (
    echo [FAIL] Node.js not installed
    set /a ERROR_COUNT+=1
)

REM 4. Check/Install pnpm
echo.
echo [4/10] Checking pnpm...
where pnpm >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] pnpm installed
    pnpm -v
) else (
    echo [!] pnpm not found, trying corepack...
    call corepack enable >nul 2>&1
    call corepack prepare pnpm@latest --activate >nul 2>&1
    where pnpm >nul 2>&1
    if !errorlevel! equ 0 (
        echo [OK] pnpm installed via corepack
        pnpm -v
    ) else (
        echo [!] corepack failed, trying npm install...
        call npm install -g pnpm
        if !errorlevel! equ 0 (
            echo [OK] pnpm installed via npm
            pnpm -v
        ) else (
            echo [FAIL] pnpm installation failed
            echo    Please run as Administrator, or manually run: corepack enable
            set /a ERROR_COUNT+=1
        )
    )
)

REM 5. Check MySQL
echo.
echo [5/10] Checking MySQL...
mysql -u%DB_USER% -p%DB_PASS% -h%DB_HOST% -P%DB_PORT% -e "SELECT 1" >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] MySQL connection successful
) else (
    echo [!] MySQL not running, trying to start...
    net start MySQL >nul 2>&1
    if %errorlevel% equ 0 (
        timeout /t 3 /nobreak >nul
        mysql -u%DB_USER% -p%DB_PASS% -h%DB_HOST% -P%DB_PORT% -e "SELECT 1" >nul 2>&1
        if %errorlevel% equ 0 (
            echo [OK] MySQL started and connected
        ) else (
            echo [FAIL] MySQL connection failed, check config
            set /a ERROR_COUNT+=1
        )
    ) else (
        echo [FAIL] Cannot start MySQL, please start manually: net start MySQL
        set /a ERROR_COUNT+=1
    )
)

REM 6. Check database and import SQL
echo.
echo [6/10] Checking database...
mysql -u%DB_USER% -p%DB_PASS% -h%DB_HOST% -P%DB_PORT% -e "USE %DB_NAME%; SHOW TABLES;" >nul 2>&1
if %errorlevel% neq 0 (
    echo [!] Database missing or incomplete, importing SQL files...
    if exist "sql\init.sql" if exist "sql\data.sql" (
        mysql -u%DB_USER% -p%DB_PASS% -h%DB_HOST% -P%DB_PORT% < sql\init.sql
        mysql -u%DB_USER% -p%DB_PASS% -h%DB_HOST% -P%DB_PORT% < sql\data.sql
        echo [OK] SQL files imported successfully
    ) else (
        echo [FAIL] SQL files not found: sql\init.sql or sql\data.sql
        set /a ERROR_COUNT+=1
    )
) else (
    echo [OK] Database exists
)

REM Check errors
if %ERROR_COUNT% gtr 0 (
    echo.
    echo [FAIL] Pre-checks failed, please fix issues above and retry
    pause
    exit /b 1
)

REM 7. Check dependencies
echo.
echo [7/10] Checking project dependencies...

REM Backend
if not exist "backend\target\classes" (
    echo [!] Backend not compiled, compiling...
    cd backend
    call mvn clean compile -q
    cd ..
    echo [OK] Backend compiled
) else (
    echo [OK] Backend dependencies ready
)

REM Admin
if not exist "admin\node_modules" (
    echo [!] Admin dependencies not installed, installing...
    cd admin
    set "HUSKY=0"
    call pnpm install --ignore-scripts 2>&1
    if !errorlevel! neq 0 (
        echo [WARN] pnpm install had issues, trying with --no-frozen-lockfile...
        call pnpm install --ignore-scripts --no-frozen-lockfile 2>&1
    )
    set "HUSKY="
    cd ..
    if exist "admin\node_modules" (
        echo [OK] Admin dependencies installed
    ) else (
        echo [FAIL] Admin dependencies installation failed
        set /a ERROR_COUNT+=1
    )
) else (
    echo [OK] Admin dependencies ready
)

REM Frontend
if not exist "frontend\node_modules" (
    echo [!] Frontend dependencies not installed, installing...
    cd frontend
    call pnpm install 2>&1
    if !errorlevel! neq 0 (
        echo [WARN] pnpm install had issues, trying with --no-frozen-lockfile...
        call pnpm install --no-frozen-lockfile 2>&1
    )
    cd ..
    if exist "frontend\node_modules" (
        echo [OK] Frontend dependencies installed
    ) else (
        echo [FAIL] Frontend dependencies installation failed
        set /a ERROR_COUNT+=1
    )
) else (
    echo [OK] Frontend dependencies ready
)

REM 8. Check port conflicts
echo.
echo [8/10] Checking port availability...
netstat -ano | findstr ":%BACKEND_PORT% " >nul 2>&1
if %errorlevel% equ 0 (
    echo [FAIL] Port %BACKEND_PORT% (backend) is in use
    echo    Please run stop.bat or free the port manually
    pause
    exit /b 1
) else (
    echo [OK] Port %BACKEND_PORT% (backend) available
)

netstat -ano | findstr ":%ADMIN_PORT% " >nul 2>&1
if %errorlevel% equ 0 (
    echo [FAIL] Port %ADMIN_PORT% (admin) is in use
    pause
    exit /b 1
) else (
    echo [OK] Port %ADMIN_PORT% (admin) available
)

netstat -ano | findstr ":%FRONTEND_PORT% " >nul 2>&1
if %errorlevel% equ 0 (
    echo [FAIL] Port %FRONTEND_PORT% (frontend) is in use
    pause
    exit /b 1
) else (
    echo [OK] Port %FRONTEND_PORT% (frontend) available
)

REM 9. Start services
echo.
echo [9/10] Starting services...

REM Start backend
echo [Backend] Starting Spring Boot...
cd backend
start "Backend" cmd /k "title Backend - Campus Recruitment && mvn clean spring-boot:run"
cd ..
timeout /t 2 /nobreak >nul
echo [OK] Backend started

REM Start admin
echo [Admin] Starting Vue 2 admin panel...
cd admin
start "Admin" cmd /k "title Admin - Campus Recruitment && npm run serve"
cd ..
timeout /t 2 /nobreak >nul
echo [OK] Admin started

REM Start frontend
echo [Frontend] Starting Vue 3 user frontend...
cd frontend
start "Frontend" cmd /k "title Frontend - Campus Recruitment && npx vite --port %FRONTEND_PORT%"
cd ..
timeout /t 2 /nobreak >nul
echo [OK] Frontend started

REM 10. Wait for ports to be ready
echo.
echo [10/10] Waiting for services to be ready...
set "BACKEND_READY=0"
set "ADMIN_READY=0"
set "FRONTEND_READY=0"

for /l %%i in (1,1,60) do (
    netstat -ano | findstr ":%BACKEND_PORT% " >nul 2>&1
    if !errorlevel! equ 0 if !BACKEND_READY! equ 0 (
        echo [OK] Backend ready (port %BACKEND_PORT%)
        set "BACKEND_READY=1"
    )

    netstat -ano | findstr ":%ADMIN_PORT% " >nul 2>&1
    if !errorlevel! equ 0 if !ADMIN_READY! equ 0 (
        echo [OK] Admin ready (port %ADMIN_PORT%)
        set "ADMIN_READY=1"
    )

    netstat -ano | findstr ":%FRONTEND_PORT% " >nul 2>&1
    if !errorlevel! equ 0 if !FRONTEND_READY! equ 0 (
        echo [OK] Frontend ready (port %FRONTEND_PORT%)
        set "FRONTEND_READY=1"
    )

    if !BACKEND_READY! equ 1 if !ADMIN_READY! equ 1 if !FRONTEND_READY! equ 1 (
        goto :all_ready
    )

    timeout /t 1 /nobreak >nul
)

:all_ready
if %BACKEND_READY% equ 0 echo [FAIL] Backend startup timeout (port %BACKEND_PORT%)
if %ADMIN_READY% equ 0 echo [FAIL] Admin startup timeout (port %ADMIN_PORT%)
if %FRONTEND_READY% equ 0 echo [FAIL] Frontend startup timeout (port %FRONTEND_PORT%)

REM Show access info
echo.
echo ============================================================
echo          All services started successfully!
echo ============================================================
echo.
echo Access URLs:
echo   Backend API:    http://localhost:%BACKEND_PORT%
echo   Admin Panel:    http://localhost:%ADMIN_PORT%
echo   User Frontend:  http://localhost:%FRONTEND_PORT%
echo.
echo Test Accounts:
echo   Admin:      admin / 123456
echo   Student:    student1 / 123456
echo   Company:    company1 / 123456
echo.
echo Tips:
echo   - Service windows are open, check real-time logs there
echo   - Close service windows or run stop.bat to stop services
echo.
pause
