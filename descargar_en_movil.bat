@echo off
chcp 65001 > nul
title Descargar FreeTag por WiFi en tu movil

echo ========================================================
echo       DESCARGA DIRECTA DE FREETAG POR WIFI
echo ========================================================
echo.
echo Tu telefono y este ordenador deben estar en la misma red WiFi.
echo.
echo Abre el navegador de tu movil (Chrome, etc.) e ingresa esta direccion:
echo.
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4" /c:"Direcci"') do (
    for /f "tokens=1" %%b in ("%%a") do (
        echo   http://%%b:8080/FreeTag.apk
    )
)
echo.
echo Al entrar a ese enlace en tu movil, el archivo se descargara solo.
echo Luego abrelo para instalarlo.
echo.
echo Presiona Ctrl+C para cerrar el servidor cuando termines.
echo ========================================================
echo.
python -m http.server 8080 --directory "%~dp0."

