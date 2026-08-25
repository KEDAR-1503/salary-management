package com.acme.salarymgmt.audit.service;

import com.acme.salarymgmt.audit.domain.SalaryAuditLog;
import com.acme.salarymgmt.audit.repository.SalaryAuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final SalaryAuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void recordSalaryChange(
            Long employeeId,
            BigDecimal previousSalary,
            BigDecimal newSalary,
            Currency currency,
            String changedBy,
            String reason,
            Instant changedAt
    ) {
        SalaryAuditLog auditLog = SalaryAuditLog.recordChange(
                employeeId,
                previousSalary,
                newSalary,
                currency,
                changedBy,
                reason,
                changedAt
        );
        auditLogRepository.save(auditLog);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void recordInitialSetup(
            Long employeeId,
            BigDecimal initialSalary,
            Currency currency,
            String changedBy,
            Instant changedAt
    ) {
        SalaryAuditLog auditLog = SalaryAuditLog.recordInitialSetup(
                employeeId,
                initialSalary,
                currency,
                changedBy,
                changedAt
        );
        auditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public List<SalaryAuditLog> getAuditHistory(Long employeeId) {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID must not be null");
        }
        return auditLogRepository.findByEmployeeIdOrderByChangedAtDesc(employeeId);
    }
}
