@echo off
setlocal
if exist "%~dp0gradle\wrapper\gradle-wrapper.jar" (
  java -classpath "%~dp0gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
  exit /b
)
where gradle >nul 2>nul
if errorlevel 1 (
  echo Install Gradle 8.9 and JDK 17, then run gradle wrapper --gradle-version 8.9.
  exit /b 1
)
gradle -p "%~dp0" %*
