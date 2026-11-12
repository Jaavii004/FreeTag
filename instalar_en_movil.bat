@echo off
chcp 65001 > nul
title Instalador FreeTag APK

set ADB_PATH=C:\Users\Usuario\AppData\Local\Android\Sdk\platform-tools\adb.exe
if not exist "%ADB_PATH%" (
    where adb >nul 2>nul
    if %errorlevel% equ 0 (
        set ADB_PATH=adb
    ) else (
        echo [ERROR] No se encontro adb.exe en tu sistema.
        pause
        exit /b 1
    )
)

echo ========================================================
echo               INSTALADOR AUTOMATICO FREETAG
echo ========================================================
echo.
echo Buscando dispositivo Android conectado...
echo.

:check_device
for /f "skip=1 tokens=1,2" %%i in ('"%ADB_PATH%" devices') do (
    if "%%j"=="device" (
        set DEVICE_ID=%%i
        goto device_found
    )
    if "%%j"=="unauthorized" (
        echo [AVISO] Dispositivo detectado pero NO AUTORIZADO.
        echo Desbloquea la pantalla de tu movil y acepta el mensaje:
        echo "Permitir depuracion por USB desde este ordenador".
        echo.
        pause
        goto check_device
    )
)

echo [!] No se ha detectado ningun telefono Android conectado por USB.
echo.
echo Por favor realiza estos pasos:
echo 1. Conecta tu movil al ordenador con un cable USB.
echo 2. Activa "Depuracion por USB" en tu movil:
echo    (Ajustes -> Opciones de desarrollador -> Depuracion por USB).
echo.
echo Pulsa una tecla cuando lo hayas conectado para volver a intentar...
pause > nul
goto check_device

:device_found
echo [+] Dispositivo detectado correctamente: %DEVICE_ID%
echo.
echo Instalando FreeTag.apk en tu movil...
"%ADB_PATH%" install -r "%~dp0FreeTag.apk"
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Hubo un problema al instalar la aplicacion.
    pause
    exit /b 1
)

echo.
echo [+] Aplicacion instalada con exito!
echo Iniciando FreeTag en tu movil...
"%ADB_PATH%" shell am start -n com.freetag.app/.ui.MainActivity > nul 2>nul

echo.
echo ========================================================
echo  TODO LISTO! FreeTag se ha abierto en tu movil.
echo ========================================================
echo.
pause

