CREATE SEQUENCE audit_log_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE salary_audit_logs (
    id BIGINT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    previous_salary NUMERIC(14, 2),
    new_salary NUMERIC(14, 2) NOT NULL CHECK (new_salary > 0),
    currency VARCHAR(3) NOT NULL,
    changed_by VARCHAR(100) NOT NULL,
    reason VARCHAR(255) NOT NULL CHECK (LENGTH(TRIM(reason)) >= 10),
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_audit_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE RESTRICT
);

CREATE INDEX idx_audit_emp_history ON salary_audit_logs(employee_id, changed_at DESC);
