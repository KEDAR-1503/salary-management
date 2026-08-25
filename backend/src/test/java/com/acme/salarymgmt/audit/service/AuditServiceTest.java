package com.acme.salarymgmt.audit.service;

import com.acme.salarymgmt.audit.domain.SalaryAuditLog;
import com.acme.salarymgmt.audit.repository.SalaryAuditLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Mock
    private SalaryAuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    @Test
    @DisplayName("Should persist salary change audit log with normalized values")
    void shouldPersistSalaryChangeAuditLog() {
        Long employeeId = 501L;
        BigDecimal previousSalary = new BigDecimal("75000.00");
        BigDecimal newSalary = new BigDecimal("85000.00");
        String changedBy = "hr_admin";
        String reason = "Merit increase for outstanding delivery";
        Instant changedAt = Instant.now();

        auditService.recordSalaryChange(
                employeeId,
                previousSalary,
                newSalary,
                USD,
                changedBy,
                reason,
                changedAt
        );

        ArgumentCaptor<SalaryAuditLog> captor = ArgumentCaptor.forClass(SalaryAuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        SalaryAuditLog captured = captor.getValue();
        assertThat(captured.getEmployeeId()).isEqualTo(employeeId);
        assertThat(captured.getPreviousSalary()).isEqualByComparingTo(previousSalary);
        assertThat(captured.getNewSalary()).isEqualByComparingTo(newSalary);
        assertThat(captured.getCurrency()).isEqualTo(USD);
        assertThat(captured.getChangedBy()).isEqualTo(changedBy);
        assertThat(captured.getReason()).isEqualTo(reason);
        assertThat(captured.getChangedAt()).isEqualTo(changedAt);
    }
}
