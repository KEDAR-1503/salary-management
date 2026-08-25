package com.acme.salarymgmt.audit.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SalaryAuditLogTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Test
    @DisplayName("Should create valid audit log for salary modification")
    void shouldCreateValidAuditLogForModification() {
        Long employeeId = 1001L;
        BigDecimal previousSalary = new BigDecimal("80000.00");
        BigDecimal newSalary = new BigDecimal("90000.00");
        String changedBy = "hr_manager";
        String reason = "Annual performance merit increase";
        Instant now = Instant.now();

        SalaryAuditLog log = SalaryAuditLog.recordChange(
                employeeId,
                previousSalary,
                newSalary,
                USD,
                changedBy,
                reason,
                now
        );

        assertThat(log.getEmployeeId()).isEqualTo(employeeId);
        assertThat(log.getPreviousSalary()).isEqualByComparingTo(previousSalary);
        assertThat(log.getNewSalary()).isEqualByComparingTo(newSalary);
        assertThat(log.getCurrency()).isEqualTo(USD);
        assertThat(log.getChangedBy()).isEqualTo("hr_manager");
        assertThat(log.getReason()).isEqualTo(reason);
        assertThat(log.getChangedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should create valid audit log for initial employee setup with null previous salary")
    void shouldCreateValidAuditLogForInitialSetup() {
        Long employeeId = 1002L;
        BigDecimal newSalary = new BigDecimal("75000.00");
        String changedBy = "hr_manager";
        Instant now = Instant.now();

        SalaryAuditLog log = SalaryAuditLog.recordInitialSetup(
                employeeId,
                newSalary,
                USD,
                changedBy,
                now
        );

        assertThat(log.getEmployeeId()).isEqualTo(employeeId);
        assertThat(log.getPreviousSalary()).isNull();
        assertThat(log.getNewSalary()).isEqualByComparingTo(newSalary);
        assertThat(log.getReason()).isEqualTo("Initial Employee Setup");
    }

    @Test
    @DisplayName("Should reject reason with less than 10 trimmed characters for modifications")
    void shouldRejectShortReasonOnModification() {
        Long employeeId = 1001L;
        BigDecimal previousSalary = new BigDecimal("80000.00");
        BigDecimal newSalary = new BigDecimal("90000.00");
        String changedBy = "hr_manager";
        Instant now = Instant.now();

        assertThatThrownBy(() -> SalaryAuditLog.recordChange(
                employeeId,
                previousSalary,
                newSalary,
                USD,
                changedBy,
                "Promotion",
                now
        ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Reason must be at least 10 characters");
    }
}
