# ACME Salary Management

HR Manager web application for salary administration and compensation analytics across 10,000 employees in multiple countries.

## Stack

- **Frontend:** Angular 17, Angular Material, standalone components, Signals, `OnPush`
- **Backend:** Java 21, Spring Boot 3, Spring Security (session + CSRF)
- **Database:** PostgreSQL 16, Flyway migrations, Testcontainers for integration tests
- **Deploy:** Docker → Render (web) + Neon (Postgres)

## Quick start (local)

### Prerequisites

- Java 21, Node 20, Docker

### 1. Start Postgres

```bash
docker compose -f infra/docker-compose.yml up -d
```

### 2. Backend

```bash
cd backend
./mvnw spring-boot:run \
  -Dspring-boot.run.profiles=local,seed
```

Environment variables (defaults shown):

```bash
export HR_MANAGER_USERNAME=hr_manager
export HR_MANAGER_PASSWORD=admin123
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/acme_salary_db
export SPRING_DATASOURCE_USERNAME=acme_user
export SPRING_DATASOURCE_PASSWORD=acme_password
```

### 3. Frontend (dev proxy)

```bash
cd frontend
npm install
npm start
```

Open http://localhost:4200 and sign in with the HR Manager credentials.

## Documentation

| Document | Description |
|----------|-------------|
| [docs/requirements.md](docs/requirements.md) | Product definition and acceptance criteria |
| [docs/architecture.md](docs/architecture.md) | Technical architecture and module boundaries |
| [docs/database-architecture.md](docs/database-architecture.md) | Schema, indexes, analytics contract |
| [docs/commit-plan.md](docs/commit-plan.md) | Remaining-work commit checklist |
| [docs/ai-use-log.md](docs/ai-use-log.md) | AI assistance disclosure |
| [docs/query-plans.md](docs/query-plans.md) | EXPLAIN ANALYZE evidence |

## API

All endpoints require HR Manager session auth except `/health`.

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/employees` | Paginated directory (search, department, country filters) |
| GET | `/api/v1/employees/{id}` | Employee detail |
| POST | `/api/v1/employees` | Create employee |
| PUT | `/api/v1/employees/{id}/salary` | Update salary (requires `version`) |
| GET | `/api/v1/employees/{id}/history` | Salary change history |
| GET | `/api/v1/analytics/departments` | Department compensation summaries |
| GET | `/api/v1/analytics/countries` | Country compensation summaries |
| GET | `/health` | Database health check |

## Seed data

The `seed` profile populates exactly 10,000 deterministic employees when the database is empty. It varies country and currency across five regions. Safe to rerun — skipped when `employee count > 0`.

## Tests

```bash
# Backend (unit + integration with Testcontainers)
cd backend && ./mvnw test

# Frontend
cd frontend && npm test -- --no-watch --browsers=ChromeHeadlessNoSandbox
```

## Deployment

See [infra/render.yaml](infra/render.yaml) for Render configuration. Set `HR_MANAGER_USERNAME`, `HR_MANAGER_PASSWORD`, and `DATABASE_URL` as Render secrets.
