@ECHO OFF

SET APP_NAME=%1
SET MAIN_CLASS=%2

IF "%APP_NAME%"=="" GOTO :usage
IF "%MAIN_CLASS%"=="" GOTO :usage

SET JAR=
FOR %%F IN (%APP_NAME%\target\*.jar) DO (
    ECHO %%F | findstr /v "original" >nul 2>&1
    IF NOT ERRORLEVEL 1 SET JAR=%%F
)

IF "%JAR%"=="" (
    ECHO JAR not found. Did you run build.bat?
    EXIT /B 1
)

SET CP=%JAR%;%APP_NAME%\target\dependency\*

ECHO Running %MAIN_CLASS%...
java -cp "%CP%" %MAIN_CLASS%
GOTO :EOF

:usage
ECHO Usage: run.bat ^<module-folder^> ^<main-class^>
EXIT /B 1
