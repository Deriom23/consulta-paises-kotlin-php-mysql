@echo off
set "ADB_EXE=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"

if not exist "%ADB_EXE%" (
    echo No se encontro ADB. Abre Android Studio y verifica la instalacion del SDK.
    pause
    exit /b 1
)

"%ADB_EXE%" devices
"%ADB_EXE%" reverse tcp:8080 tcp:8080

if errorlevel 1 (
    echo No se pudo crear la conexion USB. Revisa la depuracion USB del telefono.
) else (
    echo Conexion lista. Ya puedes ejecutar la aplicacion desde Android Studio.
)

pause
