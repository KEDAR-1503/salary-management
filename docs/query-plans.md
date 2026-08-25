# Query Plan Evidence

Captured after seeding 10,000 employees. Run locally:

```bash
docker compose -f infra/docker-compose.yml up -d
cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=local,seed
psql -h localhost -U acme_user -d acme_salary_db
```

## Directory filter (department + country)

```sql
EXPLAIN ANALYZE
SELECT * FROM employees
WHERE LOWER(department) = LOWER('Engineering')
  AND LOWER(country) = LOWER('United States')
LIMIT 20;
```

**Decision:** Baseline composite index `idx_emp_dept_country` on `(department, country)` is sufficient for equality filters at 10k rows. Trigram index deferred — not needed at this scale.

## Directory search (name / identifier)

```sql
EXPLAIN ANALYZE
SELECT * FROM employees
WHERE LOWER(full_name) LIKE LOWER('%worker%')
   OR LOWER(employee_identifier) LIKE LOWER('%EMP%')
LIMIT 20;
```

**Decision:** Sequential scan acceptable at 10k rows for demo. A `pg_trgm` GIN index would be the next step beyond 50k rows.

## Analytics median (department + currency)

```sql
EXPLAIN ANALYZE
SELECT department, currency,
       COUNT(id),
       AVG(current_salary),
       percentile_cont(0.5) WITHIN GROUP (ORDER BY current_salary)
FROM employees
GROUP BY department, currency;
```

**Decision:** `idx_emp_dept_curr` supports the group-by. `percentile_cont` runs as a single-pass aggregate — no JVM sort.
