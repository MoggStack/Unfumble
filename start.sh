#!/bin/bash

echo "Starting UnFumble Local Environment..."

# 1. Start Infrastructure (Postgres, Redis, MinIO)
echo "Starting Docker containers..."
cd infra
docker compose up -d
cd ..

# Wait for infrastructure to be ready (give it a few seconds)
sleep 5

# Create MinIO bucket if it doesn't exist
echo "Setting up MinIO bucket..."
docker run --rm --network host minio/mc \
  alias set local http://localhost:9000 minioadmin minioadmin > /dev/null 2>&1
docker run --rm --network host minio/mc \
  mb local/headshot-images > /dev/null 2>&1 || echo "Bucket may already exist, skipping."

# 2. Start Python AI Engine (in background)
echo "Starting Python AI Engine..."
cd services/ai-engine
if [ ! -d ".venv" ]; then
    echo "Creating Python virtual environment..."
    uv venv
    source .venv/bin/activate
    uv pip install -e .
else
    source .venv/bin/activate
fi
# Note: In a real environment, you might also want to start celery here:
# celery -A app.worker worker --loglevel=info &
uvicorn app.main:app --reload --port 8001 &
PYTHON_PID=$!
cd ../..

# 3. Start Java Gateway (in background)
echo "Starting Java Gateway..."
cd services/gateway
./mvnw spring-boot:run &
JAVA_PID=$!
cd ../..

echo "======================================================="
echo "All services started! Press Ctrl+C to stop everything."
echo "======================================================="

# Trap Ctrl+C to kill background processes gracefully
trap "echo 'Shutting down services...'; kill $PYTHON_PID $JAVA_PID; cd infra && docker compose stop; exit 0" SIGINT SIGTERM

# Wait indefinitely so the script doesn't exit and kill the background jobs
wait
