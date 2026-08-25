CREATE SEQUENCE employee_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE employees (
    id BIGINT PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    employee_identifier VARCHAR(64) NOT NULL UNIQUE,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    department VARCHAR(50) NOT NULL,
    role_title VARCHAR(80) NOT NULL,
    country VARCHAR(50) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    current_salary NUMERIC(14, 2) NOT NULL CHECK (current_salary > 0),
    effective_date DATE NOT NULL
);

CREATE INDEX idx_emp_dept_country ON employees(department, country);
CREATE INDEX idx_emp_dept_curr ON employees(department, currency);
CREATE INDEX idx_emp_country_curr ON employees(country, currency);
