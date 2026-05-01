@echo off
REM Space Diver - Compile and Run Script for Windows

echo Compiling Space Diver...
cd src
javac SpaceDiver.java

if %ERRORLEVEL% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Compilation successful! Starting game...
java SpaceDiver

pause
