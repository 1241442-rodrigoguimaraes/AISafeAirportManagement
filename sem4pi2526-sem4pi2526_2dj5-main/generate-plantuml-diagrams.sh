#! /bin/sh
set -e

PLANTUML_VERSION="1.2026.2"
PLANTUML_JAR="libs/plantuml-${PLANTUML_VERSION}.jar"
PLANTUML_URL="https://github.com/plantuml/plantuml/releases/download/v${PLANTUML_VERSION}/plantuml-${PLANTUML_VERSION}.jar"

# Download the jar on demand instead of committing it to the repo
if [ ! -f "$PLANTUML_JAR" ]; then
  echo "LOG: plantuml jar not found, downloading v${PLANTUML_VERSION}..."
  mkdir -p libs
  if command -v curl >/dev/null 2>&1; then
    curl -L -o "$PLANTUML_JAR" "$PLANTUML_URL"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$PLANTUML_JAR" "$PLANTUML_URL"
  else
    echo "ERROR: need curl or wget to download plantuml.jar" >&2
    exit 1
  fi
fi

echo "LOG: Generate Plantuml Diagrams"
exportFormat="svg"
#monochrome="true"
extra="-SdefaultFontSize=20"
#extra="-SdefaultFontName=Times New Roman -SdefaultFontSize=10"

# shellcheck disable=SC2044
# shellcheck disable=SC2006
for aFile in `find docs -name "*.puml" -type f`;
do
  #-Smonochrome=$monochrome
  echo "Processing file: $aFile"
  java -jar "$PLANTUML_JAR" $extra -t$exportFormat "$aFile"
done

echo "Finished"