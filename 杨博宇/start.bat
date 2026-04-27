@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM ============================================================
REM Campus Recruitment System - QUICK START (Windows)
REM Only start services: backend + admin + frontend
REM ============================================================

set "PROJECT_DIR=%~dp0"
cd /d "%PROJECT_DIR%"

set "BACKEND_PORT=8080"
set "ADMIN_PORT=8001"
set "FRONTEND_PORT=8002"

echo.
echo ============================================================
echo      Campus Recruitment System - Quick Start
echo ============================================================
echo.

REM 1) Start backend
echo [1/3] Starting Backend (Spring Boot)...
cd backend
start "Backend" cmd /k "title Backend - Campus Recruitment && mvn spring-boot:run"
cd ..
timeout /t 2 /nobreak >nul
echo [OK] Backend started

REM 2) Start admin
echo.
echo [2/3] Starting Admin (Vue 2)...
cd admin
start "Admin" cmd /k "title Admin - Campus Recruitment && pnpm.cmd run serve "
cd ..
timeout /t 2 /nobreak >nul
echo [OK] Admin started

REM 3) Start frontend
echo.
echo [3/3] Starting Frontend (Vue 3 / Vite)...
cd frontend
start "Frontend" cmd /k "title Frontend - Campus Recruitment && pnpm.cmd run dev -- --port %FRONTEND_PORT%"
cd ..
timeout /t 2 /nobreak >nul
echo [OK] Frontend started

echo.
echo ============================================================
echo All services started!
echo ============================================================
echo   Backend API:    http://localhost:%BACKEND_PORT%
echo   Admin Panel:    http://localhost:%ADMIN_PORT%
echo   User Frontend:  http://localhost:%FRONTEND_PORT%
echo ============================================================
echo.

pause