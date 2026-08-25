# ACME Salary Management — Product Requirements

**Status:** Baseline for implementation
**Primary user:** HR Manager

## Product goal

Replace spreadsheet-based salary administration for ACME's 10,000 employees across multiple countries with a reliable web application for salary management and compensation insight.

## User problem

Spreadsheets make salary changes slow, hard to audit, and vulnerable to concurrent overwrites. The product provides one controlled source of current salary data and compensation insight.

## Outcomes

The first release succeeds when an authorised HR Manager can:

1. Find an employee by identifier or name and narrow the directory by country and department.
2. Create an employee with an initial annual base salary.
3. Change an employee's current salary with today's effective date and a meaningful explanation.
4. See a complete salary-change history: previous value, new value, reason, who made the change, and when.
5. See employee count, average salary, and median salary by department or country.
6. Work with a representative 10,000-employee dataset.

## In scope

- A paginated employee directory with search and country/department filters.
- Employee details: identifier, name, email, department, country, current base salary, currency, and effective date.
- Create employee and update salary workflows with clear validation and stale-update feedback.
- Currency is selected when an employee is created and cannot be changed in v1. A relocation or currency change is a future workflow, not a salary update.
- An append-only salary-change history in the application. Initial employee creation records `Initial Employee Setup`; later salary changes require a meaningful reason of at least 10 characters.
- Compensation analytics grouped by department or country.
- Currency-separated analytics. A department or country can have multiple displayed currency metrics; the product never presents their sum, average, or median as one false single-currency figure.
- A deployed demo, deterministic 10,000-employee seed data, automated verification, and a demo video.

## Product quality requirements

- Salary data remains accurate, currency-labelled, and auditable.
- Only an authenticated HR Manager may view or change salary data.
- Concurrent changes show an understandable conflict instead of silently losing data.
- Directory and analytics performance is measured against seeded data.
- The UI has labelled fields, keyboard-operable controls, and actionable validation.

## Deliberately out of scope

- Payroll, tax, bonuses, benefits, payslips, payment processing, and bank integrations.
- Currency conversion, global totals/averages, relocation, or currency changes: ACME has supplied no rate source, rate date, or reporting-currency policy.
- Employee self-service, multi-tenant support, enterprise SSO provisioning, and complex role management.
- Bulk import/export, arbitrary report building, approval workflows, notifications, and salary/audit deletion.

## Acceptance criteria

- The directory uses server-side pagination, search, and filters; it does not load all 10,000 employees into the browser.
- A successful salary update changes the current salary and adds one append-only history record with the authenticated actor and reason.
- A salary update accepts only today's effective date (UTC calendar date, `LocalDate.now(clock)`) and retains the employee's existing currency.
- A conflicting update preserves the winning change, records no false history item, and tells the HR Manager how to recover.
- Analytics visibly separate every `(department, currency)` or `(country, currency)` metric.
- A clean checkout can seed exactly 10,000 deterministic employees, run the automated checks, and start the application.
