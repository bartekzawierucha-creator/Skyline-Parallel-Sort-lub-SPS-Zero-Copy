#!/usr/bin/env bash
set -euo pipefail

export ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$PWD/.android-sdk}"
export PATH="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools:$PATH"
if [[ -d /root/.local/share/mise/installs/java/17.0.2 ]]; then
  export JAVA_HOME=/root/.local/share/mise/installs/java/17.0.2
  export PATH="$JAVA_HOME/bin:$PATH"
fi

if [[ ! -x ./gradlew ]]; then
  gradle wrapper --gradle-version 8.14.3
fi

./gradlew --no-daemon assembleDebug

echo "APK: app/build/outputs/apk/debug/app-debug.apk"
