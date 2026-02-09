#!/bin/sh
# Gradle wrapper - build APK with: ./gradlew assembleDebug
# If gradle-wrapper.jar is missing, run: gradle wrapper

APP_HOME=$(cd "$(dirname "$0")" && pwd)
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "gradle-wrapper.jar not found. Run: gradle wrapper"
  echo "Or build from Android Studio: Build -> Build APK(s)"
  exit 1
fi

exec java -Dorg.gradle.appname=gradlew -cp "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
