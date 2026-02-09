@rem Gradle wrapper for Windows - build APK with: gradlew.bat assembleDebug
@rem If you see "Could not find or load gradle-wrapper.jar", run once: gradle wrapper
@rem (requires Gradle installed), or build from Android Studio instead.

@if "%DEBUG%"=="" @echo off

set APP_HOME=%~dp0
set WRAPPER_JAR=%APP_HOME%gradle\wrapper\gradle-wrapper.jar

if not exist "%WRAPPER_JAR%" (
  echo.
  echo gradle-wrapper.jar not found.
  echo Option 1: Open android folder in Android Studio and use Build - Build APK
  echo Option 2: Install Gradle then run in this folder: gradle wrapper
  echo.
  exit /b 1
)

set CLASSPATH=%WRAPPER_JAR%

java -Dorg.gradle.appname=gradlew -cp "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
