@echo off
setlocal enabledelayedexpansion

set JAVA17=
for /f "tokens=2*" %%a in ('reg query "HKLM\SOFTWARE\JavaSoft\JDK" /s /v "JavaHome" 2^>nul') do (
    echo %%b | findstr "17\." >nul && set JAVA17=%%b
)

if "%JAVA17%"=="" (
    for %%d in (C D E) do (
        for /d /r "%%d:\" %%i in (jdk-17* jdk17*) do (
            if exist "%%i\bin\javac.exe" if "!JAVA17!"=="" set JAVA17=%%i
        )
    )
)

if "%JAVA17%"=="" (
    echo Java 17 not found. Please install JDK 17.
    exit /b 1
)

echo Using Java 17 at: %JAVA17%
set JAVA_HOME=%JAVA17%
set PATH=%JAVA_HOME%\bin;%PATH%

where mvn >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo Maven is not installed or not in PATH
    exit /b 1
)

echo Building project...

mvn -B clean package dependency:copy-dependencies surefire-report:report checkstyle:checkstyle-aggregate jacoco:report -Daggregate=true -Dmaven.javadoc.skip=true

if %ERRORLEVEL% neq 0 (
    echo Build failed!
    exit /b 1
)

echo Build completed successfully!
endlocal