@echo off
chcp 65001 >nul
REM ============================================================
REM 校园招聘系统 - 停止脚本 (Windows)
REM ============================================================

setlocal enabledelayedexpansion

REM 项目目录
set "PROJECT_DIR=%~dp0"
cd /d "%PROJECT_DIR%"

REM 端口配置
set "BACKEND_PORT=8080"
set "ADMIN_PORT=8001"
set "FRONTEND_PORT=8002"

echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║              校园招聘系统 - 停止脚本                       ║
echo ╚════════════════════════════════════════════════════════════╝
echo.

REM 停止函数
set "STOPPED=0"

REM 停止后端
echo [!] 正在停止后端服务 (端口 %BACKEND_PORT%)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%BACKEND_PORT% "') do (
    taskkill /F /PID %%a >nul 2>&1
    if !errorlevel! equ 0 (
        echo [✓] 后端服务已停止 (PID: %%a)
        set "STOPPED=1"
    )
)
if %STOPPED% equ 0 (
    echo [✓] 后端服务未运行
)
set "STOPPED=0"

REM 停止管理端
echo.
echo [!] 正在停止管理端服务 (端口 %ADMIN_PORT%)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%ADMIN_PORT% "') do (
    taskkill /F /PID %%a >nul 2>&1
    if !errorlevel! equ 0 (
        echo [✓] 管理端服务已停止 (PID: %%a)
        set "STOPPED=1"
    )
)
if %STOPPED% equ 0 (
    echo [✓] 管理端服务未运行
)
set "STOPPED=0"

REM 停止前端
echo.
echo [!] 正在停止前端服务 (端口 %FRONTEND_PORT%)...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%FRONTEND_PORT% "') do (
    taskkill /F /PID %%a >nul 2>&1
    if !errorlevel! equ 0 (
        echo [✓] 前端服务已停止 (PID: %%a)
        set "STOPPED=1"
    )
)
if %STOPPED% equ 0 (
    echo [✓] 前端服务未运行
)

REM 关闭服务窗口
echo.
echo [!] 正在关闭服务窗口...
taskkill /FI "WINDOWTITLE eq 后端服务*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq 管理端*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq 前端*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Backend*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Admin*" /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Frontend*" /F >nul 2>&1
echo [✓] 服务窗口已关闭

REM 清理 PID 目录
if exist ".pids" (
    rmdir /s /q ".pids" >nul 2>&1
    echo [✓] PID 文件已清理
)

REM 可选：清理日志目录（注释掉以保留日志）
REM if exist ".logs" (
REM     rmdir /s /q ".logs" >nul 2>&1
REM     echo [✓] 日志文件已清理
REM )

echo.
echo ╔════════════════════════════════════════════════════════════╗
echo ║                    所有服务已停止                          ║
echo ╚════════════════════════════════════════════════════════════╝
echo.
pause
