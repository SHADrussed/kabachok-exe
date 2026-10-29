#!/bin/sh
# Bootstrap for environments where the official wrapper JAR could not be downloaded.
# Uses the pinned Gradle distribution and verifies its published SHA-256.
set -eu
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
if [ -f "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" ]; then
  exec "${JAVA_HOME:+$JAVA_HOME/bin/}java" -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
fi
if command -v gradle >/dev/null 2>&1; then
  case "$(gradle --version)" in *"Gradle 8.9"*) exec gradle -p "$APP_HOME" "$@" ;; esac
fi
CACHE="${GRADLE_USER_HOME:-$HOME/.gradle}/kabachok-bootstrap"
mkdir -p "$CACHE"
if [ ! -x "$CACHE/gradle-8.9/bin/gradle" ]; then
  command -v curl >/dev/null || { echo 'curl is required to download Gradle 8.9' >&2; exit 1; }
  command -v unzip >/dev/null || { echo 'unzip is required' >&2; exit 1; }
  curl -fL --connect-timeout 15 --max-time 600 https://services.gradle.org/distributions/gradle-8.9-bin.zip -o "$CACHE/gradle.zip"
  curl -fL --connect-timeout 15 --max-time 60 https://services.gradle.org/distributions/gradle-8.9-bin.zip.sha256 -o "$CACHE/gradle.sha256"
  CHECKSUM=$(cat "$CACHE/gradle.sha256")
  (cd "$CACHE" && printf '%s  gradle.zip\n' "$CHECKSUM" | sha256sum -c -)
  unzip -q -o "$CACHE/gradle.zip" -d "$CACHE"
  rm "$CACHE/gradle.zip"
fi
exec "$CACHE/gradle-8.9/bin/gradle" -p "$APP_HOME" "$@"
