#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"

export HR_MANAGER_USERNAME="${HR_MANAGER_USERNAME:-hr_manager}"
export HR_MANAGER_PASSWORD="${HR_MANAGER_PASSWORD:-admin123}"
export SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL:-jdbc:postgresql://localhost:5432/acme_salary_db}"
export SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME:-acme_user}"
export SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD:-acme_password}"

echo "Starting backend on :8080 (profiles: local,seed)..."
cd "$ROOT/backend"
./mvnw spring-boot:run -Dspring-boot.run.profiles=local,seed &
BACKEND_PID=$!

echo "Waiting for /health..."
for i in $(seq 1 60); do
  if curl -sf http://localhost:8080/health >/dev/null 2>&1; then
    echo "Backend is up."
    break
  fi
  sleep 2
done

echo "Starting frontend on :4200 (proxy -> backend)..."
cd "$ROOT/frontend"
npm start &
FRONTEND_PID=$!

echo ""
echo "App running:"
echo "  Frontend: http://localhost:4200"
echo "  Backend:  http://localhost:8080"
echo "  Login:    hr_manager / admin123"
echo ""
echo "Press Ctrl+C to stop."

trap "kill $BACKEND_PID $FRONTEND_PID 2>/dev/null" EXIT
wait
