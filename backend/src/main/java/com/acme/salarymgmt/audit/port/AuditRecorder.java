package com.acme.salarymgmt.audit.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

public interface AuditRecorder {

    void recordSalaryChange(
            Long employeeId,
            BigDecimal previousSalary,
            BigDecimal newSalary,
            Currency currency,
            String changedBy,
            String reason,
            Instant changedAt
    );

    void recordInitialSetup(
            Long employeeId,
            BigDecimal initialSalary,
            Currency currency,
            String changedBy,
            Instant changedAt
    );
}
