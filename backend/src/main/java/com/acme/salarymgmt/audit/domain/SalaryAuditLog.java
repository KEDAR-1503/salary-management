package com.acme.salarymgmt.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Currency;

@Entity
@Table(name = "salary_audit_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SalaryAuditLog {

    public static final String INITIAL_SETUP_REASON = "Initial Employee Setup";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit_log_seq_gen")
    @SequenceGenerator(name = "audit_log_seq_gen", sequenceName = "audit_log_seq", allocationSize = 50)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "previous_salary", precision = 14, scale = 2)
    private BigDecimal previousSalary;

    @Column(name = "new_salary", nullable = false, precision = 14, scale = 2)
    private BigDecimal newSalary;

    @Column(name = "currency", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "changed_by", nullable = false, length = 100)
    private String changedBy;

    @Column(name = "reason", nullable = false, length = 255)
    private String reason;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    public static SalaryAuditLog recordChange(
            Long employeeId,
            BigDecimal previousSalary,
            BigDecimal newSalary,
            Currency currency,
            String changedBy,
            String reason,
            Instant changedAt
    ) {
        validateCommon(employeeId, newSalary, currency, changedBy, changedAt);
        if (reason == null || reason.trim().length() < 10) {
            throw new IllegalArgumentException("Reason must be at least 10 characters");
        }
        BigDecimal normalizedPrevious = previousSalary != null ? previousSalary.setScale(2, RoundingMode.HALF_EVEN) : null;
        BigDecimal normalizedNew = newSalary.setScale(2, RoundingMode.HALF_EVEN);

        return new SalaryAuditLog(
                null,
                employeeId,
                normalizedPrevious,
                normalizedNew,
                currency.getCurrencyCode(),
                changedBy.trim(),
                reason.trim(),
                changedAt
        );
    }

    public static SalaryAuditLog recordInitialSetup(
            Long employeeId,
            BigDecimal newSalary,
            Currency currency,
            String changedBy,
            Instant changedAt
    ) {
        validateCommon(employeeId, newSalary, currency, changedBy, changedAt);
        BigDecimal normalizedNew = newSalary.setScale(2, RoundingMode.HALF_EVEN);

        return new SalaryAuditLog(
                null,
                employeeId,
                null,
                normalizedNew,
                currency.getCurrencyCode(),
                changedBy.trim(),
                INITIAL_SETUP_REASON,
                changedAt
        );
    }

    public Currency getCurrency() {
        return Currency.getInstance(this.currencyCode);
    }

    private static void validateCommon(
            Long employeeId,
            BigDecimal newSalary,
            Currency currency,
            String changedBy,
            Instant changedAt
    ) {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID must not be null");
        }
        if (newSalary == null || newSalary.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("New salary must be positive");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency must not be null");
        }
        if (changedBy == null || changedBy.isBlank()) {
            throw new IllegalArgumentException("ChangedBy must not be blank");
        }
        if (changedAt == null) {
            throw new IllegalArgumentException("ChangedAt must not be null");
        }
    }
}
