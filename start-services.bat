@echo off
chcp 65001 >nul

echo Loading environment variables from .env...
if exist "%~dp0.env" (
    for /f "usebackq tokens=1,* delims==" %%A in ("%~dp0.env") do (
        if not "%%A"=="" if not "%%A:~0,1%"=="#" set "%%A=%%B"
    )
    echo .env loaded successfully.
) else (
    echo [WARN] .env file not found. Using default placeholders - services may fail to connect.
    echo        Please copy .env.example to .env and fill in your passwords.
)

echo ============================================
echo  Step 1: Starting Docker middleware...
echo ============================================

docker start edu-mysql edu-nacos edu-redis
if %errorlevel% neq 0 (
    echo [ERROR] Docker containers failed to start. Is Docker Desktop running?
    pause
    exit /b 1
)

echo Waiting 15s for Nacos and Redis to be ready...
timeout /t 15 /nobreak >nul

echo ============================================
echo  Step 2: Starting backend services...
echo ============================================
echo Starting all edu-platform services...

cd /d "%~dp0edu-auth\target"
start "edu-auth(8081)" java -jar edu-auth-1.0.0.jar

timeout /t 5 /nobreak >nul

cd /d "%~dp0edu-system\target"
start "edu-system(8082)" java -jar edu-system-1.0.0.jar

timeout /t 5 /nobreak >nul

cd /d "%~dp0edu-agent\target"
start "edu-agent(8083)" java -jar edu-agent-1.0.0.jar

timeout /t 5 /nobreak >nul

cd /d "%~dp0edu-knowledge\target"
start "edu-knowledge(8084)" java -jar edu-knowledge-1.0.0.jar

timeout /t 5 /nobreak >nul

cd /d "%~dp0edu-gateway\target"
start "edu-gateway(9000)" java -jar edu-gateway-1.0.0.jar

echo All services started! Check windows for startup logs.
pause
