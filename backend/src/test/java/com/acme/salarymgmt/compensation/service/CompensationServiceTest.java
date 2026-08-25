package com.acme.salarymgmt.compensation.service;

import com.acme.salarymgmt.audit.port.AuditRecorder;
import com.acme.salarymgmt.compensation.domain.Employee;
import com.acme.salarymgmt.compensation.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompensationServiceTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final Instant FIXED_INSTANT = Instant.parse("2026-08-25T10:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AuditRecorder auditRecorder;

    private CompensationService compensationService;

    @BeforeEach
    void setUp() {
        compensationService = new CompensationService(employeeRepository, auditRecorder, FIXED_CLOCK);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("hr_manager", "pass", List.of(new SimpleGrantedAuthority("ROLE_HR_MANAGER")))
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should update salary when version matches and record audit")
    void shouldUpdateSalaryAndRecordAudit() {
        LocalDate effectiveDate = LocalDate.now(FIXED_CLOCK);
        Employee existingEmployee = Employee.create(
                "EMP-1001",
                "Alice Smith",
                "alice.smith@acme.corp",
                "Engineering",
                "Staff Engineer",
                "United States",
                USD,
                new BigDecimal("120000.00"),
                effectiveDate.minusMonths(6)
        );

        when(employeeRepository.findById(101L)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(existingEmployee)).thenReturn(existingEmployee);

        Employee updated = compensationService.updateSalary(
                101L,
                0L,
                new BigDecimal("135000.00"),
                effectiveDate,
                "Annual merit performance promotion"
        );

        assertThat(updated.getCurrentSalary()).isEqualByComparingTo(new BigDecimal("135000.00"));
        verify(auditRecorder).recordSalaryChange(
                eq(101L),
                eq(new BigDecimal("120000.00")),
                eq(new BigDecimal("135000.00")),
                eq(USD),
                eq("hr_manager"),
                eq("Annual merit performance promotion"),
                eq(FIXED_INSTANT)
        );
    }

    @Test
    @DisplayName("Should throw OptimisticLockingFailureException when client version is stale")
    void shouldRejectStaleVersion() {
        LocalDate effectiveDate = LocalDate.now(FIXED_CLOCK);
        Employee existingEmployee = Employee.create(
                "EMP-1001",
                "Alice Smith",
                "alice.smith@acme.corp",
                "Engineering",
                "Staff Engineer",
                "United States",
                USD,
                new BigDecimal("120000.00"),
                effectiveDate
        );

        when(employeeRepository.findById(101L)).thenReturn(Optional.of(existingEmployee));

        assertThatThrownBy(() -> compensationService.updateSalary(
                101L,
                99L,
                new BigDecimal("135000.00"),
                effectiveDate,
                "Annual merit performance promotion"
        ))
                .isInstanceOf(OptimisticLockingFailureException.class);
    }

    @Test
    @DisplayName("Should reject salary update when reason is shorter than 10 characters")
    void shouldRejectShortReason() {
        assertThatThrownBy(() -> compensationService.updateSalary(
                101L,
                0L,
                new BigDecimal("130000.00"),
                LocalDate.now(FIXED_CLOCK),
                "Promo"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Reason must be at least 10 characters");
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when updating non-existent employee")
    void shouldThrowWhenEmployeeNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> compensationService.updateSalary(
                999L,
                0L,
                new BigDecimal("100000.00"),
                LocalDate.now(FIXED_CLOCK),
                "Merit increase adjustment"
        ))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
