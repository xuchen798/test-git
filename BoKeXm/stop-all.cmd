@echo off
chcp 65001 >nul
title 博客系统一键关闭

echo ====================================
echo    博客系统一键关闭
echo ====================================
echo.

cd /d "%~dp0"

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0stop-all.ps1"

if errorlevel 1 (
    echo.
    echo [提示] 如有关闭失败的进程，可尝试以管理员身份再次运行本脚本
)

echo.
pause
