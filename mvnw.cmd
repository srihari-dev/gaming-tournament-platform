@REM Maven Wrapper startup batch script
@REM
@REM Required ENV vars:
@REM   JAVA_HOME - location of a JDK home dir

@echo off

set MAVEN_PROJECTBASEDIR=%~dp0
set MAVEN_CMD_LINE_ARGS=%*

set WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"

if exist %WRAPPER_JAR% (
    "%JAVA_HOME%\bin\java.exe" %MAVEN_OPTS% ^
        -jar %WRAPPER_JAR% %MAVEN_CMD_LINE_ARGS%
    if ERRORLEVEL 1 goto error
    goto end
)

echo Maven wrapper jar not found at %WRAPPER_JAR%
echo Please run the project setup first.
goto error

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%
exit /B %ERROR_CODE%
