#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"

export HR_MANAGER_USERNAME="${HR_MANAGER_USERNAME:-hr_manager}"
export HR_MANAGER_PASSWORD="${HR_MANAGER_PASSWORD:-admin123}"
export SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL:-jdbc:postgresql://localhost:5432/acme_salary_db}"
export SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME:-acme_user}"
export SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD:-acme_password}"

if ! command -v pg_isready >/dev/null 2>&1 || ! pg_isready -h localhost -p 5432 >/dev/null 2>&1; then
  echo "ERROR: PostgreSQL not reachable. Rebuild devcontainer or start docker compose."
  exit 1
fi

echo "Starting backend on :8080 (profiles: local,seed)..."
cd "$ROOT/backend"
./mvnw spring-boot:run -Dspring-boot.run.profiles=local,seed &
BACKEND_PID=$!

echo "Waiting for /health..."
for _ in $(seq 1 60); do
  if curl -sf http://localhost:8080/health >/dev/null 2>&1; then
    echo "Backend is up."
    break
  fi
  sleep 2
done

echo "Starting frontend dev server on :4200 (proxy -> :8080)..."
cd "$ROOT/frontend"
npm start &
FRONTEND_PID=$!

echo ""
echo "Dev mode running:"
echo "  Frontend: http://localhost:4200"
echo "  Backend:  http://localhost:8080"
echo "  Login:    $HR_MANAGER_USERNAME / $HR_MANAGER_PASSWORD"
echo ""
echo "Press Ctrl+C to stop."

trap "kill $BACKEND_PID $FRONTEND_PID 2>/dev/null" EXIT
wait
