@echo off
chcp 65001 >nul
cd /d "%~dp0edu-frontend"
npm run dev
