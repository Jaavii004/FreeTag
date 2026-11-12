@echo off
chcp 65001 >nul
echo ========================================================
echo    🎴 Abriendo Plantilla Imprimible de Tarjeta FreeTag
echo ========================================================
echo.
echo Abriendo en tu navegador predeterminado...
start "" "%~dp0tarjeta_freetag_imprimible.html"
echo.
echo Presiona Ctrl + P en el navegador para imprimir.
echo Asegúrate de seleccionar Escala: 100%% (Tamaño real).
echo.
pause

