@echo off

echo Removing target folders...
for /d /r . %%d in (target) do (
    if exist "%%d" rmdir /s /q "%%d"
)
mvn clean
echo Done.
