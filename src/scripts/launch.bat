@echo off
REM FRPS Launcher - Windows 启动脚本
REM 用法: launch.bat [选项]

set SCRIPT_DIR=%~dp0
set JAR_DIR=%SCRIPT_DIR%..
set JAR_PATH=%JAR_DIR%\frps-launcher.jar

if not exist "%JAR_PATH%" (
    echo [ERROR] 找不到 frps-launcher.jar
    echo [INFO]  期望路径: %JAR_PATH%
    echo [INFO]  请先执行: mvn clean package
    exit /b 1
)

echo [INFO] 启动 FRPS Launcher...
echo [INFO] JAR: %JAR_PATH%

java -jar "%JAR_PATH%" %*
