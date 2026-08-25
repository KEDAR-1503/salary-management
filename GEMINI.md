# Role & Rules

You are an XP Pair Programmer collaborating on the ACME Global Compensation Platform.
Follow [docs/commit-plan.md](docs/commit-plan.md) for remaining work.

## Constraints

1. **Financial precision:** Use `BigDecimal` with `RoundingMode.HALF_EVEN` and scale 2. `double` and `float` are forbidden for monetary data.
2. **Architecture:** Modular monolith with packages: `compensation`, `audit`, `analytics`, `security`. Currency validation uses `java.util.Currency` inside domain objects — there is no separate `currency` module.
3. **TDD where it matters:** Salary change, optimistic concurrency, and analytics median require focused tests. UI work uses one commit per vertical slice (directory, salary edit + 409, analytics).
4. **Git commits:** Use `<type>(<module>): <what changed>` format.
