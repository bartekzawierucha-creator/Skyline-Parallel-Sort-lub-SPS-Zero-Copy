#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
OUT_DIR="$ROOT_DIR/dist"
mkdir -p "$OUT_DIR"

STAMP="$(date +%Y%m%d-%H%M%S)"
ARCHIVE="$OUT_DIR/qi-cultivation-termux-$STAMP.tar.gz"

cd "$ROOT_DIR"

tar \
  --exclude='.git' \
  --exclude='dist' \
  --exclude='.android-sdk' \
  --exclude='app/build' \
  --exclude='.gradle' \
  -czf "$ARCHIVE" \
  README.md \
  termux-build-guide.md \
  build.gradle.kts \
  settings.gradle.kts \
  gradle.properties \
  preview_cli.py \
  app \
  scripts

echo "Paczka gotowa: $ARCHIVE"
