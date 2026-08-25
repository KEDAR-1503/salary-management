# ACME Salary Management — Remaining Work Commit Plan

**Status:** Reference for remaining implementation — tick rows as commits land  
**Companion documents:** [requirements.md](requirements.md), [architecture.md](architecture.md), [database-architecture.md](database-architecture.md)

## How to use this document

Each row is one commit covering a vertical slice of work. TDD applies to salary change, concurrency, and analytics — not to value-object micro-steps. Deviations from this plan should be noted in the AI-use log.

Commit message format: `<type>(<module>): <what changed>`

---

## Already in repo (do not replay)

- [x] Domain `Money`, `Employee`, audit entity
- [x] Create employee, directory GET, salary PUT, analytics endpoints (`/api/v1/...`)
- [x] HTTP Basic + CSRF (to be replaced by session auth)
- [x] Flyway SQL migrations (not yet wired)
- [x] Angular HTTP services + specs (no UI components yet)

---

## Remaining commits (~18)

| # | Type | Commit | Status |
|---|------|--------|--------|
| 1 | `docs` | Rewrite commit plan, move docs to `docs/`, README entry, UTC timezone rule, drop empty `currency` module | [ ] |
| 2 | `chore` | Wire Flyway + Hibernate validate + Testcontainers Postgres; retire H2 for integration tests | [ ] |
| 3 | `feat` | Domain today-only date, currency immutability, reason validation via `Clock` | [ ] |
| 4 | `feat` | Client `version` on salary PUT; stale write returns 409 with integration test | [ ] |
| 5 | `feat` | GET `/api/v1/employees/{id}` and GET `/api/v1/employees/{id}/history` | [ ] |
| 6 | `feat` | Native SQL `percentile_cont` median + NUMERIC averages on Postgres | [ ] |
| 7 | `feat` | Session cookie auth + env-var HR credentials; permit `/health` | [ ] |
| 8 | `feat` | Chunked deterministic 10k seed (`seed` profile, batch size 500) | [ ] |
| 9 | `feat` | DB-touching `/health` endpoint | [ ] |
| 10 | `feat` | Angular login + XSRF-aware session auth | [ ] |
| 11 | `feat` | Angular employee directory (search, filter, pagination) | [ ] |
| 12 | `feat` | Angular create-employee form | [ ] |
| 13 | `feat` | Angular employee detail, history, salary edit with 409 recovery | [ ] |
| 14 | `feat` | Angular analytics dashboard (currency-separated cards) | [ ] |
| 15 | `ci` | Full pipeline: backend tests + Testcontainers + ArchUnit + frontend tests | [ ] |
| 16 | `chore` | Multi-stage Docker build; Render + Neon deployment config | [ ] |
| 17 | `docs` | README setup guide, EXPLAIN ANALYZE evidence, AI-use log | [ ] |
| 18 | `docs` | Demo video link (after deployment) | [ ] |

---

## Rules

- Do not replay Phase 1–10 micro-commits from the original plan.
- Fix median and optimistic-lock contract before building UI on the wrong API.
- Prefer Testcontainers PostgreSQL over H2 for analytics and index evidence.
- Seed before capturing `EXPLAIN ANALYZE` evidence.
