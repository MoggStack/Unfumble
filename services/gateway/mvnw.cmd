@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF)
@REM Maven Wrapper startup batch script, version 3.3.2
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET __MVNW_CMD__=
@SET __MVNW_ERROR__=
@SET __MVNW_SAVE_ERRORLEVEL__=
@SET __MVNW_SAVE_CD__=
@setlocal
@set ERRORLEVEL=
@for %%a in (%__MVNW_ARG0_NAME__%) do @set __MVNW_CMD__=%%~na
@set MAVEN_PROJECTBASEDIR=%~dp0

@REM Find the project base dir
@IF NOT "%MAVEN_BASEDIR%"=="" goto endDetectBaseDir
@SET EXEC_DIR=%CD%
@SET __WDP_WDIR_WALK__=true

:detectBaseDirLoop
@IF EXIST "%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties" goto endDetectBaseDir
@cd ..
@IF "%CD%"=="%EXEC_DIR%" goto endDetectBaseDir
@SET MAVEN_PROJECTBASEDIR=%CD%\
@goto detectBaseDirLoop

:endDetectBaseDir
@IF NOT "%MAVEN_BASEDIR%"=="" SET MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%

@SET MAVEN_WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties
@SET MAVEN_WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar

@IF "%JAVA_HOME%"=="" (
    SET MVNW_JAVA_HOME=java
) ELSE (
    SET MVNW_JAVA_HOME=%JAVA_HOME%\bin\java
)

@SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

@"%MVNW_JAVA_HOME%" ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  -cp "%MAVEN_WRAPPER_JAR%" %WRAPPER_LAUNCHER% %*

@SET __MVNW_SAVE_ERRORLEVEL__=%ERRORLEVEL%
@endlocal & (exit /b %__MVNW_SAVE_ERRORLEVEL__%)
