#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "Running mvn package..."
mvn -DskipTests package

ZIP_NAME=sahamati-token-service.zip
echo "Creating zip $ZIP_NAME ..."
zip -r "$ZIP_NAME" \
  pom.xml \
  README.md \
  Dockerfile \
  src \
  k8s \
  scripts \
  src/main/resources/application.properties \\n  src/main/resources/import.sql

echo "Created $ZIP_NAME in $ROOT"
