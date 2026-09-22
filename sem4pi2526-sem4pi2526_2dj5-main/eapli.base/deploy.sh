#!/usr/bin/env bash

set -e

echo "Preparing deployment..."

./build.sh

mkdir -p dist

cp -r alsafe.app./target/.jar dist/
cp -r alsafe.app.*/target/dependency dist/

echo "Deployment package ready in ./dist"
