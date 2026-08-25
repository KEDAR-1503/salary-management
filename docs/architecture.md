# ACME Salary Management — Technical Architecture

**Status:** Baseline for implementation
**Companion product definition:** [requirements.md](requirements.md)  
**Database design:** [database-architecture.md](database-architecture.md)  
**Remaining work:** [commit-plan.md](commit-plan.md)

## Architecture decision

Use a **simple monorepo** containing an Angular + TypeScript frontend, a Java 21/Spring Boot modular-monolith backend, PostgreSQL, and deployment/CI configuration. This is the right size for the assessment: one developer can change a feature across UI, API, tests, and deployment in one reviewable commit history, while the backend remains deliberately separated into business contexts.

Angular is deliberate: the target role is Java/Angular-II. The application therefore demonstrates modern Angular skills alongside Java — standalone components, strict typing, Signals, `OnPush` change detection, RxJS at asynchronous boundaries, and focused component/service tests. This is a better interview signal than selecting React merely because the assessment permits it.

This is not a microservices system and does not need Nx, Turborepo, Kubernetes, or another monorepo-management platform. Maven, npm, Docker Compose, and GitHub Actions are sufficient.

## Deployment target

The deployed demo uses a **Render Free Web Service** built from the repository's Docker image and a **Neon Free PostgreSQL** database. The Docker build compiles the Angular application and packages its static output into Spring Boot, which serves both the UI and `/api` from one Render origin. This preserves cookie-based authentication without production CORS configuration. Render and Neon credentials are GitHub/Render secrets, never committed. The free service may cold-start after idling; this is acceptable for a demo and must be noted in the demo instructions.

**Open verification items (not yet confirmed against a real deployment):**

- The `/health` endpoint must query the database, not just report application liveness, so that hitting it also warms the Neon compute before a demo recording.
- `-XX:MaxRAMPercentage=75` on a 512 MB container is a starting estimate. It must be observed under real memory pressure (Spring Boot + Angular static assets + Hikari pool) before being treated as a settled figure.

## Repository layout

```text
acme-salary-management/
├── backend/                 Spring Boot API and its tests
├── frontend/                Angular UI and its tests
├── docs/                    requirements, architecture, database design, delivery evidence
├── infra/                   Docker Compose and deployment configuration
├── .devcontainer/           GitHub Codespaces environment
└── .github/workflows/       CI workflows
```

## Backend boundaries

The backend is a modular monolith. Modules communicate through application ports/events, never through another module's repository. ArchUnit tests enforce that rule.

| Module | Owns | Does not own |
| --- | --- | --- |
| `compensation` | Employee records, salary changes, validation, optimistic concurrency | Audit persistence or analytics queries |
| `audit` | Immutable salary-change history | Updating employee salary |
| `analytics` | Read-only, database-projected compensation metrics | Loading all employees into memory |
| `security` | Authentication and HR Manager authorisation | Client-supplied audit actors |

Currency code validation uses `java.util.Currency` inside domain value objects (`Money`, `Employee`); there is no separate `currency` module.

`compensation` depends on the `audit` module's public `AuditRecorder` application port, not its repository. The salary-change application service invokes that port synchronously inside its transaction after the versioned employee update. The `audit` module owns the port's implementation and persistence. This keeps the operation atomic without breaking module ownership; ArchUnit permits the public port only.

## Key technical decisions

- **Financial integrity:** Salary amounts use `BigDecimal`, are persisted as PostgreSQL `NUMERIC(14,2)`, and are normalised with `RoundingMode.HALF_EVEN`. `double` and `float` are forbidden for monetary data.
- **Concurrency:** `Employee` has JPA `@Version`. A stale write maps to RFC 7807 `409 Conflict`; its transaction must not add an audit record.
- **Audit provenance:** The backend derives `changedBy` from `SecurityContextHolder`; mutation requests cannot submit it. Initial setup has no previous salary and uses the system reason `Initial Employee Setup`; later changes require a non-blank reason of at least 10 characters.
- **Database ownership:** The relational model, migrations, indexes, query evidence, and seed strategy are defined in the database architecture document rather than duplicated here.
- **Identity for the demo:** Spring Security provides one HR Manager account whose credentials are supplied through environment variables. The Angular app and API are deployed behind one origin and use a secure, `HttpOnly`, `SameSite=Lax` session cookie with CSRF protection; local Angular development uses a proxy to the API. JWT storage and cross-origin CORS policy are therefore unnecessary in v1. Enterprise identity provisioning is explicitly deferred.
- **CSRF contract:** Spring uses `CookieCsrfTokenRepository` with the readable `XSRF-TOKEN` cookie and `X-XSRF-TOKEN` header. Angular configures `provideHttpClient(withXsrfConfiguration({ cookieName: 'XSRF-TOKEN', headerName: 'X-XSRF-TOKEN' }))`; `HttpClient` returns the token on same-origin mutation requests. The XSRF token cookie is deliberately readable by Angular; the authentication session cookie remains `HttpOnly`.
- **Free-container budget:** The container sets `JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=75`. HikariCP sets `maximumPoolSize=5` and `minimumIdle=0`, preventing a small demo runtime from spending its memory or Neon connection allowance on an oversized pool. Because `minimumIdle=0` allows the pool to fully drain, the DB-aware health check (see Deployment target) must be hit before a demo to avoid a cold pool stacking on a cold Render/Neon start.

## API and UI boundary

The API uses JSON over HTTPS and RFC 7807 Problem Details for errors. Proposed resource operations are employee list/detail/create, salary update, employee salary history, and grouped compensation analytics. The UI uses Angular, TypeScript, and Angular Material. It has a directory, employee-detail/salary-edit screen, and analytics dashboard. It uses standalone components with `OnPush` change detection; Signals hold local UI state and RxJS handles HTTP/form streams. It always renders a currency with monetary data and groups analytical cards by currency.

## Test and delivery strategy

Development is strictly incremental:

1. Write one focused, deterministic failing test (`test(...)`).
2. Add only the smallest implementation needed to pass it (`feat(...)`).
3. Improve names/design with all tests green (`refactor(...)`).

Start with domain value objects, then salary change/audit, persistence/concurrency, directory, UI, and analytics. JUnit 5 + AssertJ drive domain tests; integration tests prove database and HTTP behaviour; Angular CLI/Vitest and TestBed cover Angular services and components; ArchUnit guards boundaries. GitHub Actions runs the relevant backend, frontend, integration, formatting, and image-build checks.

## Delivery evidence

Before submission, the repository must add a short AI-use log, a local/Codespaces setup guide, deployment instructions, a health-check endpoint, seed-profile instructions, captured query-plan evidence, and the demo-video link. The deployed **demo** environment enables the explicit deterministic demo-seed profile; a production configuration leaves it disabled. GitHub Actions supplies demo credentials through repository secrets, starts PostgreSQL as an integration-test service container, runs migrations and automated checks, then builds the deployable image.

The health-check endpoint must be verified to actually touch the database (see Deployment target), and the `MaxRAMPercentage=75` setting must be verified against observed container memory before the demo video is recorded.

## Why this aligns with Incubyte

The design keeps the product small enough to deliver end-to-end but makes quality visible: test-first commits, continuous refactoring, clear ownership boundaries, database-backed performance, and production-minded deployment. It uses AI to accelerate drafting and review while leaving decisions, tests, and verification explicit in the repository — the evidence the assessment asks for.
