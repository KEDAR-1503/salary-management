-- Expression indexes for case-insensitive exact directory search
-- (UNIQUE already covers raw email / employee_identifier; LOWER() needs its own indexes)

CREATE INDEX IF NOT EXISTS idx_emp_full_name_lower
    ON employees (LOWER(full_name));

CREATE INDEX IF NOT EXISTS idx_emp_email_lower
    ON employees (LOWER(email));

CREATE INDEX IF NOT EXISTS idx_emp_identifier_lower
    ON employees (LOWER(employee_identifier));
