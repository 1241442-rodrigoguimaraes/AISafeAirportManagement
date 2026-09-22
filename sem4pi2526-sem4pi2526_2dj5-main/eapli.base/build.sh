#!/usr/bin/env bash

set -e  # stop on error

if ! command -v mvn &> /dev/null; then
  echo "Maven is not installed or not in PATH"
  exit 1
fi

if ! command -v java &> /dev/null; then
  echo "Java is not installed or not in PATH"
  exit 1
fi

echo "Building project..."

mvn -B clean package dependency:copy-dependencies \
    surefire-report:report checkstyle:checkstyle-aggregate \
    -Daggregate=true

echo "Build completed successfully!"