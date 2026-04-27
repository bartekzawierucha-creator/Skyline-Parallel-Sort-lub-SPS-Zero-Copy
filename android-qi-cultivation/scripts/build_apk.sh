#!/usr/bin/env bash
set -euo pipefail

export ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$PWD/.android-sdk}"
export PATH="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools:$PATH"
if [[ -n "${JAVA_HOME:-}" && ! -d "${JAVA_HOME}" ]]; then
  unset JAVA_HOME
fi
if [[ -d /root/.local/share/mise/installs/java/17.0.2 ]]; then
  export JAVA_HOME=/root/.local/share/mise/installs/java/17.0.2
  export PATH="$JAVA_HOME/bin:$PATH"
fi
if [[ -z "${JAVA_HOME:-}" ]]; then
  JAVAC_BIN="$(command -v javac || true)"
  if [[ -n "$JAVAC_BIN" ]]; then
    export JAVA_HOME="$(dirname "$(dirname "$(readlink -f "$JAVAC_BIN")")")"
    export PATH="$JAVA_HOME/bin:$PATH"
  fi
fi

if [[ ! -x ./gradlew ]]; then
  gradle wrapper --gradle-version 8.14.3
fi

AAPT2_OVERRIDE_ARGS=()
if command -v aapt2 >/dev/null 2>&1; then
  AAPT2_BIN="$(command -v aapt2)"
  echo "Using local aapt2 override: $AAPT2_BIN"
  AAPT2_OVERRIDE_ARGS+=("-Pandroid.aapt2FromMavenOverride=$AAPT2_BIN")
elif [[ "${PREFIX:-}" == *"com.termux"* ]]; then
  echo "ERROR: Termux detected but 'aapt2' was not found."
  echo "Install it first: pkg install -y aapt2"
  exit 1
fi

./gradlew --no-daemon assembleDebug "${AAPT2_OVERRIDE_ARGS[@]}"

echo "APK: app/build/outputs/apk/debug/app-debug.apk"
