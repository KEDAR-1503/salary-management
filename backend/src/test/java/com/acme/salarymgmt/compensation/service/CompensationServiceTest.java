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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

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
                "Staff Level 3",
                "United States",
                USD,
                new BigDecimal("120000.00"),
                effectiveDate,
                LocalDate.now(FIXED_CLOCK)
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
                "Staff Level 3",
                "United States",
                USD,
                new BigDecimal("120000.00"),
                effectiveDate,
                LocalDate.now(FIXED_CLOCK)
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

    @Test
    @DisplayName("Should create employee and record initial setup audit")
    void shouldCreateEmployeeAndRecordInitialAudit() {
        when(employeeRepository.findMaxEmployeeNumber()).thenReturn(2000L);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 42L);
            return saved;
        });

        Employee created = compensationService.createEmployee(
                "Dana Lee",
                "dana.lee@acme.corp",
                "Product",
                "Staff Level 2",
                "Singapore",
                Currency.getInstance("SGD"),
                new BigDecimal("98000.00"),
                LocalDate.now(FIXED_CLOCK)
        );

        assertThat(created.getId()).isEqualTo(42L);
        assertThat(created.getEmployeeIdentifier()).isEqualTo("EMP-02001");
        assertThat(created.getEffectiveDate()).isEqualTo(LocalDate.now(FIXED_CLOCK));
        verify(auditRecorder).recordInitialSetup(
                eq(42L),
                eq(new BigDecimal("98000.00")),
                eq(Currency.getInstance("SGD")),
                eq("hr_manager"),
                eq(FIXED_INSTANT)
        );
    }

    @Test
    @DisplayName("Should start employee identifiers at EMP-00001 when catalog is empty")
    void shouldStartEmployeeIdentifiersAtOneWhenEmpty() {
        when(employeeRepository.findMaxEmployeeNumber()).thenReturn(null);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Employee created = compensationService.createEmployee(
                "First Hire",
                "first.hire@acme.corp",
                "Engineering",
                "Staff Level 1",
                "United States",
                USD,
                new BigDecimal("90000.00"),
                LocalDate.now(FIXED_CLOCK)
        );

        assertThat(created.getEmployeeIdentifier()).isEqualTo("EMP-00001");
    }

    @Test
    @DisplayName("Should accept future effective date on salary update")
    void shouldAcceptFutureEffectiveDateOnUpdate() {
        LocalDate today = LocalDate.now(FIXED_CLOCK);
        LocalDate future = today.plusDays(7);
        Employee existingEmployee = Employee.create(
                "EMP-1001",
                "Alice Smith",
                "alice.smith@acme.corp",
                "Engineering",
                "Staff Level 3",
                "United States",
                USD,
                new BigDecimal("120000.00"),
                today,
                today
        );
        when(employeeRepository.findById(101L)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(existingEmployee)).thenReturn(existingEmployee);

        Employee updated = compensationService.updateSalary(
                101L,
                0L,
                new BigDecimal("135000.00"),
                future,
                "Annual merit performance promotion"
        );

        assertThat(updated.getEffectiveDate()).isEqualTo(future);
        assertThat(updated.getCurrentSalary()).isEqualByComparingTo(new BigDecimal("135000.00"));
    }

    @Test
    @DisplayName("Should reject past effective date on salary update")
    void shouldRejectPastEffectiveDateOnUpdate() {
        LocalDate today = LocalDate.now(FIXED_CLOCK);
        Employee existingEmployee = Employee.create(
                "EMP-1001",
                "Alice Smith",
                "alice.smith@acme.corp",
                "Engineering",
                "Staff Level 3",
                "United States",
                USD,
                new BigDecimal("120000.00"),
                today,
                today
        );
        when(employeeRepository.findById(101L)).thenReturn(Optional.of(existingEmployee));

        assertThatThrownBy(() -> compensationService.updateSalary(
                101L,
                0L,
                new BigDecimal("135000.00"),
                today.minusDays(1),
                "Annual merit performance promotion"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("past");
    }

    @Test
    @DisplayName("Should reject create when catalog values are invalid")
    void shouldRejectCreateWithInvalidCatalogValues() {
        assertThatThrownBy(() -> compensationService.createEmployee(
                "Bad Dept",
                "bad@acme.corp",
                "Astrology",
                "Staff Level 1",
                "United States",
                USD,
                new BigDecimal("90000.00"),
                LocalDate.now(FIXED_CLOCK)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Department");
    }

    @Test
    @DisplayName("Should reject create when effective date is in the past (negative)")
    void shouldRejectCreateWithPastEffectiveDate() {
        LocalDate today = LocalDate.now(FIXED_CLOCK);

        assertThatThrownBy(() -> compensationService.createEmployee(
                "Past Hire",
                "past.hire@acme.corp",
                "Engineering",
                "Staff Level 1",
                "United States",
                USD,
                new BigDecimal("90000.00"),
                today.minusDays(1)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("past");
    }

    @Test
    @DisplayName("Should accept create when effective date is in the future (positive)")
    void shouldAcceptCreateWithFutureEffectiveDate() {
        LocalDate today = LocalDate.now(FIXED_CLOCK);
        LocalDate future = today.plusDays(10);
        when(employeeRepository.findMaxEmployeeNumber()).thenReturn(50L);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
            Employee saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 51L);
            return saved;
        });

        Employee created = compensationService.createEmployee(
                "Future Hire",
                "future.hire@acme.corp",
                "Engineering",
                "Staff Level 1",
                "United States",
                USD,
                new BigDecimal("90000.00"),
                future
        );

        assertThat(created.getEffectiveDate()).isEqualTo(future);
        assertThat(created.getEmployeeIdentifier()).isEqualTo("EMP-00051");
    }

    @Test
    @DisplayName("Should delegate filtered employee listing to repository")
    void shouldListEmployeesWithFilters() {
        Employee emp = Employee.create(
                "EMP-1001",
                "Alice Smith",
                "alice.smith@acme.corp",
                "Engineering",
                "Staff Level 3",
                "United States",
                USD,
                new BigDecimal("120000.00"),
                LocalDate.now(FIXED_CLOCK),
                LocalDate.now(FIXED_CLOCK)
        );
        PageRequest pageable = PageRequest.of(0, 20);
        when(employeeRepository.findWithFilters("Engineering", null, null, pageable))
                .thenReturn(new PageImpl<>(List.of(emp), pageable, 1));

        Page<Employee> page = compensationService.getEmployees("Engineering", null, null, pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().getEmployeeIdentifier()).isEqualTo("EMP-1001");
    }

    @Test
    @DisplayName("Should pass exact search term through to the repository")
    void shouldPassSearchTermToRepository() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(employeeRepository.findWithFilters(null, null, "exact.email@acme.corp", pageable))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        compensationService.getEmployees(null, null, "exact.email@acme.corp", pageable);

        verify(employeeRepository).findWithFilters(null, null, "exact.email@acme.corp", pageable);
    }

    @Test
    @DisplayName("Should return distinct department and country filter options")
    void shouldReturnDistinctFilterOptions() {
        when(employeeRepository.findDistinctDepartments()).thenReturn(List.of("Engineering", "Finance"));
        when(employeeRepository.findDistinctCountries()).thenReturn(List.of("India", "United States"));

        assertThat(compensationService.getDistinctDepartments()).containsExactly("Engineering", "Finance");
        assertThat(compensationService.getDistinctCountries()).containsExactly("India", "United States");
    }
}
