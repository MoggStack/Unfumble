@echo off
setlocal

@REM Find project base directory and strip trailing backslash
set "MAVEN_PROJECTBASEDIR=%~dp0"
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"

@REM Determine Java executable
if "%JAVA_HOME%"=="" (
    set "JAVA_EXE=java"
) else (
    set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
)

set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain"

@REM Run the wrapper jar
"%JAVA_EXE%" ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  -classpath "%WRAPPER_JAR%" ^
  %WRAPPER_LAUNCHER% %*

@endlocal
