#!/usr/bin/env bash
set -euo pipefail

SDK_ROOT="${ANDROID_SDK_ROOT:-$PWD/.android-sdk}"
TOOLS_ZIP="commandlinetools-linux-11076708_latest.zip"
TOOLS_URL="https://dl.google.com/android/repository/${TOOLS_ZIP}"

mkdir -p "$SDK_ROOT/cmdline-tools"
cd "$SDK_ROOT"

if [[ ! -f "$TOOLS_ZIP" ]]; then
  curl -L -o "$TOOLS_ZIP" "$TOOLS_URL"
fi

unzip -qo "$TOOLS_ZIP" -d cmdline-tools
if [[ ! -d cmdline-tools/latest ]]; then
  mv cmdline-tools/cmdline-tools cmdline-tools/latest
fi

export ANDROID_SDK_ROOT="$SDK_ROOT"
export PATH="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools:$PATH"

yes | sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"

echo "Android SDK ready at: $ANDROID_SDK_ROOT"
