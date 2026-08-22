#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

BACKEND_DIR="$SCRIPT_DIR"
FRONTEND_DIR="$SCRIPT_DIR/../circle-react-ui"
STATIC_DIR="$BACKEND_DIR/src/main/resources/static"

IMAGE_NAME="circle-backend"
CONTAINER_NAME="circle-backend"

echo "========================================"
echo "1. Building React frontend"
echo "========================================"

cd "$FRONTEND_DIR"

npm install
npm run build

echo "========================================"
echo "2. Copying React build into Spring"
echo "========================================"

mkdir -p "$STATIC_DIR"
rm -rf "$STATIC_DIR"/*

cp -R "$FRONTEND_DIR/dist/." "$STATIC_DIR/"

echo "========================================"
echo "3. Building Spring Boot backend"
echo "========================================"

cd "$BACKEND_DIR"

./mvnw clean package

echo "========================================"
echo "4. Building Docker image"
echo "========================================"

docker build -t "$IMAGE_NAME" .

echo "========================================"
echo "5. Removing old Docker container if needed"
echo "========================================"

docker rm -f "$CONTAINER_NAME" 2>/dev/null || true

echo "========================================"
echo "6. Starting Docker container"
echo "========================================"

docker run \
  -d \
  --name "$CONTAINER_NAME" \
  -p 8080:8080 \
  -e CIRCLE_SERVER_HOST=host.docker.internal \
  -e CIRCLE_SERVER_PORT=50010 \
  "$IMAGE_NAME"

echo "========================================"
echo "BUILD AND START COMPLETE"
echo "========================================"

echo ""
echo "Open:"
echo "http://localhost:8080"
echo ""
echo "View logs with:"
echo "docker logs -f $CONTAINER_NAME"
echo ""
echo "Stop with:"
echo "docker stop $CONTAINER_NAME"
