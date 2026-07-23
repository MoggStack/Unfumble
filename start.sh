#!/bin/bash

echo "Starting UnFumble Local Environment..."

# 0. Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "======================================================="
    echo "ERROR: Docker daemon is not running!"
    echo "Please start Docker Desktop and run ./start.sh again."
    echo "======================================================="
    exit 1
fi

# 1. Start Infrastructure (Postgres, Redis, MinIO)
echo "Starting Docker containers..."
cd infra
docker compose up -d
cd ..

# Wait for infrastructure to be ready (give it a few seconds)
sleep 5

# Create MinIO bucket if it doesn't exist
echo "Setting up MinIO bucket..."
docker run --rm --network infra_default minio/mc \
  alias set local http://minio:9000 minioadmin minioadmin > /dev/null 2>&1
docker run --rm --network infra_default minio/mc \
  mb local/unfumble-images > /dev/null 2>&1 || echo "Bucket may already exist, skipping."

# 2. Start Python AI Engine (in background)
echo "Starting Python AI Engine..."
cd services/ai-engine
if [ ! -f ".env" ] && [ -f ".env.example" ]; then
    echo "Creating .env from .env.example..."
    cp .env.example .env
fi
if [ ! -d ".venv" ]; then
    echo "Creating Python virtual environment..."
    if command -v uv >/dev/null 2>&1; then
        uv venv
        if [ -f ".venv/Scripts/activate" ]; then
            source .venv/Scripts/activate
        else
            source .venv/bin/activate
        fi
        uv pip install -e .
    else
        python -m venv .venv
        if [ -f ".venv/Scripts/activate" ]; then
            source .venv/Scripts/activate
        else
            source .venv/bin/activate
        fi
        python -m pip install --upgrade pip
        pip install -e .
    fi
else
    if [ -f ".venv/Scripts/activate" ]; then
        source .venv/Scripts/activate
    else
        source .venv/bin/activate
    fi
fi
# Note: In a real environment, you might also want to start celery here:
# celery -A app.worker worker --loglevel=info &
uvicorn app.main:app --reload --port 8001 &
PYTHON_PID=$!
cd ../..

# 3. Start Java Gateway (in background)
echo "Starting Java Gateway..."
cd services/gateway
if [ -f "./mvnw" ]; then
    ./mvnw spring-boot:run &
    JAVA_PID=$!
elif [ -f "./mvnw.cmd" ]; then
    ./mvnw.cmd spring-boot:run &
    JAVA_PID=$!
else
    echo "Error: Neither ./mvnw nor ./mvnw.cmd found in services/gateway!"
fi
cd ../..

echo "======================================================="
echo "All services started! Press Ctrl+C to stop everything."
echo "======================================================="

# Trap Ctrl+C to kill background processes gracefully
trap "echo 'Shutting down services...'; kill $PYTHON_PID $JAVA_PID; cd infra && docker compose stop; exit 0" SIGINT SIGTERM

# Wait indefinitely so the script doesn't exit and kill the background jobs
wait
