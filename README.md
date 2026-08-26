# ACME Salary Management

HR Manager web application for salary administration and compensation analytics across 10,000 employees in multiple countries.

## Stack

- **Frontend:** Angular 17, standalone components, Signals, `OnPush`
- **Backend:** Java 21, Spring Boot 3, Spring Security (session + CSRF)
- **Database:** PostgreSQL 16, Flyway migrations, Testcontainers for integration tests
- **Deploy:** Docker → Render (web) + Neon (Postgres)

## Quick start — GitHub Codespaces (no Docker)

The devcontainer includes PostgreSQL 16. **Rebuild the container once** if Postgres is not running:

`Command Palette` → `Dev Containers: Rebuild Container`

Then run the full demo (builds UI + seeds 10k employees + serves on one port):

```bash
chmod +x scripts/*.sh
./scripts/start-demo.sh
```

Open **http://localhost:8080** — login: `hr_manager` / `admin123`

For frontend hot-reload during development:

```bash
./scripts/run-local.sh
```

Open **http://localhost:4200** (proxies API to backend on :8080).

## Quick start — local with Docker

```bash
docker compose -f infra/docker-compose.yml up -d
./scripts/start-demo.sh
```

## Environment variables

| Variable | Default |
|----------|---------|
| `HR_MANAGER_USERNAME` | `hr_manager` |
| `HR_MANAGER_PASSWORD` | `admin123` |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/acme_salary_db` |
| `SPRING_DATASOURCE_USERNAME` | `acme_user` |
| `SPRING_DATASOURCE_PASSWORD` | `acme_password` |

## Documentation

| Document | Description |
|----------|-------------|
| [docs/requirements.md](docs/requirements.md) | Product definition and acceptance criteria |
| [docs/architecture.md](docs/architecture.md) | Technical architecture and module boundaries |
| [docs/database-architecture.md](docs/database-architecture.md) | Schema, indexes, analytics contract |
| [docs/commit-plan.md](docs/commit-plan.md) | Commit checklist |

## API

All endpoints require HR Manager session auth except `/health` and `/api/auth/login`.

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/employees` | Paginated directory |
| GET | `/api/v1/employees/{id}` | Employee detail |
| POST | `/api/v1/employees` | Create employee |
| PUT | `/api/v1/employees/{id}/salary` | Update salary (requires `version`) |
| GET | `/api/v1/employees/{id}/history` | Salary change history |
| GET | `/api/v1/analytics/departments` | Department summaries |
| GET | `/api/v1/analytics/countries` | Country summaries |
| GET | `/health` | Database health check |

## Tests

```bash
cd backend && ./mvnw test
cd frontend && npm test -- --no-watch --browsers=ChromeHeadlessNoSandbox
```

## CI

```bash
gh run list
gh run watch
```

## Deployment

See [infra/render.yaml](infra/render.yaml). Set database and HR credentials as Render secrets. Use `SPRING_PROFILES_ACTIVE=seed` on first deploy only.
