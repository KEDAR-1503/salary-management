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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        assertThat(captured.getNewSalary()).isEqualByComparingTo(new BigDecimal("85000.00"));
        assertThat(captured.getCurrency()).isEqualTo(USD);
        assertThat(captured.getChangedBy()).isEqualTo(changedBy);
        assertThat(captured.getReason()).isEqualTo(reason);
        assertThat(captured.getChangedAt()).isEqualTo(changedAt);
    }

    @Test
    @DisplayName("Should persist initial setup audit log (positive)")
    void shouldPersistInitialSetupAuditLog() {
        Instant changedAt = Instant.parse("2026-08-27T10:00:00Z");

        auditService.recordInitialSetup(
                42L,
                new BigDecimal("98000.00"),
                Currency.getInstance("SGD"),
                "hr_manager",
                changedAt
        );

        ArgumentCaptor<SalaryAuditLog> captor = ArgumentCaptor.forClass(SalaryAuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        SalaryAuditLog captured = captor.getValue();
        assertThat(captured.getEmployeeId()).isEqualTo(42L);
        assertThat(captured.getPreviousSalary()).isNull();
        assertThat(captured.getNewSalary()).isEqualByComparingTo(new BigDecimal("98000.00"));
        assertThat(captured.getReason()).isEqualTo("Initial Employee Setup");
    }

    @Test
    @DisplayName("Should return audit history newest first (positive)")
    void shouldReturnAuditHistoryNewestFirst() {
        SalaryAuditLog newer = SalaryAuditLog.recordChange(
                7L,
                new BigDecimal("100000.00"),
                new BigDecimal("110000.00"),
                USD,
                "hr_manager",
                "Annual merit performance promotion",
                Instant.parse("2026-08-27T12:00:00Z")
        );
        when(auditLogRepository.findByEmployeeIdOrderByChangedAtDesc(7L)).thenReturn(List.of(newer));

        List<SalaryAuditLog> history = auditService.getAuditHistory(7L);

        assertThat(history).containsExactly(newer);
        verify(auditLogRepository).findByEmployeeIdOrderByChangedAtDesc(7L);
    }

    @Test
    @DisplayName("Should reject null employee id when loading history (negative)")
    void shouldRejectNullEmployeeIdForHistory() {
        assertThatThrownBy(() -> auditService.getAuditHistory(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Employee ID");
    }
}
