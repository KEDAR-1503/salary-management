#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"

export HR_MANAGER_USERNAME="${HR_MANAGER_USERNAME:-hr_manager}"
export HR_MANAGER_PASSWORD="${HR_MANAGER_PASSWORD:-admin123}"
export SPRING_DATASOURCE_URL="${SPRING_DATASOURCE_URL:-jdbc:postgresql://localhost:5432/acme_salary_db}"
export SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME:-acme_user}"
export SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD:-acme_password}"

echo "Checking PostgreSQL at localhost:5432..."
if ! command -v pg_isready >/dev/null 2>&1 || ! pg_isready -h localhost -p 5432 >/dev/null 2>&1; then
  echo ""
  echo "ERROR: PostgreSQL is not reachable on localhost:5432."
  echo ""
  echo "In GitHub Codespaces, rebuild the devcontainer so Postgres is provisioned:"
  echo "  Command Palette -> Dev Containers: Rebuild Container"
  echo ""
  echo "Or start Postgres locally:"
  echo "  docker compose -f infra/docker-compose.yml up -d"
  echo ""
  exit 1
fi

"$ROOT/scripts/build-ui.sh"

echo ""
echo "Starting application on http://localhost:8080 ..."
echo "Login: $HR_MANAGER_USERNAME / $HR_MANAGER_PASSWORD"
echo ""

cd "$ROOT/backend"
./mvnw spring-boot:run -Dspring-boot.run.profiles=local,seed
