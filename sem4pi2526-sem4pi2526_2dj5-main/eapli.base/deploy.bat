@ECHO OFF

ECHO Preparing deployment...

call build.bat
IF ERRORLEVEL 1 EXIT /B 1

IF NOT EXIST dist MKDIR dist

FOR /D %%M IN (alsafe.app.*) DO (
    FOR %%J IN (%%M\target\*.jar) DO COPY /Y "%%J" dist\
    IF EXIST "%%M\target\dependency" XCOPY /E /I /Y "%%M\target\dependency" "dist\dependency" >nul
)

ECHO Deployment package ready in .\dist
