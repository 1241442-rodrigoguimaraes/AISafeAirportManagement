#!/usr/bin/env bash

set -e

APP_NAME=$1
MAIN_CLASS=$2

if [ -z "$APP_NAME" ] || [ -z "$MAIN_CLASS" ]; then
  echo "Usage: ./run.sh <module-folder> <main-class>"
  exit 1
fi

JAR=$(ls $APP_NAME/target/*.jar | grep -v "original" | head -n 1)

if [ ! -f "$JAR" ]; then
  echo "JAR not found. Did you run build.sh?"
  exit 1
fi

CP="$JAR:$APP_NAME/target/dependency/*"

echo "Running $MAIN_CLASS..."

java -cp "$CP" "$MAIN_CLASS"