package com.acme.salarymgmt.audit.dto;

import com.acme.salarymgmt.audit.domain.SalaryAuditLog;

import java.math.BigDecimal;
import java.time.Instant;

public record SalaryAuditLogResponse(
        Long id,
        Long employeeId,
        BigDecimal previousSalary,
        BigDecimal newSalary,
        String currency,
        String changedBy,
        String reason,
        Instant changedAt
) {
    public static SalaryAuditLogResponse fromDomain(SalaryAuditLog log) {
        return new SalaryAuditLogResponse(
                log.getId(),
                log.getEmployeeId(),
                log.getPreviousSalary(),
                log.getNewSalary(),
                log.getCurrencyCode(),
                log.getChangedBy(),
                log.getReason(),
                log.getChangedAt()
        );
    }
}
