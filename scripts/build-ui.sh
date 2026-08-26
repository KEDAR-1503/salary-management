#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
STATIC_DIR="$ROOT/backend/src/main/resources/static"

echo "Building Angular UI..."
cd "$ROOT/frontend"
npm ci --silent
npm run build -- --configuration production

echo "Copying UI into Spring Boot static resources..."
rm -rf "$STATIC_DIR"
mkdir -p "$STATIC_DIR"
cp -r dist/frontend/* "$STATIC_DIR/"

echo "UI build complete."
