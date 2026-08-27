package com.acme.salarymgmt.compensation.repository;

import com.acme.salarymgmt.AbstractIntegrationTest;
import com.acme.salarymgmt.compensation.domain.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmployeeRepositoryTest extends AbstractIntegrationTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void clearEmployees() {
        employeeRepository.deleteAll();
    }

    @Test
    @DisplayName("Should persist and retrieve Employee entity with correct version")
    void shouldPersistAndRetrieveEmployee() {
        Employee employee = Employee.create(
                "EMP-9001",
                "Alice Walker",
                "alice.walker@acme.corp",
                "Engineering",
                "Staff Level 5",
                "United States",
                USD,
                new BigDecimal("150000.00"),
                LocalDate.now(),
                LocalDate.now()
        );

        Employee saved = employeeRepository.saveAndFlush(employee);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getVersion()).isEqualTo(0L);

        Optional<Employee> found = employeeRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmployeeIdentifier()).isEqualTo("EMP-9001");
    }

    @Test
    @DisplayName("Should throw OptimisticLockingFailureException on concurrent stale write")
    void shouldEnforceOptimisticLockingOnConcurrentUpdate() {
        Employee employee = Employee.create(
                "EMP-9002",
                "Bob Martin",
                "bob.martin@acme.corp",
                "Product",
                "Staff Level 4",
                "United States",
                USD,
                new BigDecimal("130000.00"),
                LocalDate.now(),
                LocalDate.now()
        );

        Employee saved = employeeRepository.saveAndFlush(employee);
        Long empId = saved.getId();

        Employee tx1Employee = employeeRepository.findById(empId).orElseThrow();
        Employee tx2Employee = employeeRepository.findById(empId).orElseThrow();

        tx2Employee.updateSalary(new BigDecimal("140000.00"), LocalDate.now(), LocalDate.now());
        employeeRepository.saveAndFlush(tx2Employee);

        tx1Employee.updateSalary(new BigDecimal("145000.00"), LocalDate.now(), LocalDate.now());

        assertThatThrownBy(() -> employeeRepository.saveAndFlush(tx1Employee))
                .isInstanceOf(OptimisticLockingFailureException.class);
    }

    @Test
    @DisplayName("Should list employees when all optional filters are null (Postgres null bind regression)")
    void shouldListEmployeesWithNullFilters() {
        employeeRepository.saveAndFlush(Employee.create(
                "EMP-9003",
                "Carol Nguyen",
                "carol.nguyen@acme.corp",
                "Engineering",
                "Staff Level 2",
                "United States",
                USD,
                new BigDecimal("90000.00"),
                LocalDate.now(),
                LocalDate.now()
        ));

        Page<Employee> page = employeeRepository.findWithFilters(null, null, null, PageRequest.of(0, 10));

        assertThat(page.getContent()).isNotEmpty();
        assertThat(page.getContent().stream().map(Employee::getEmployeeIdentifier))
                .contains("EMP-9003");
    }

    @Test
    @DisplayName("Should return distinct sorted departments and countries for filter dropdowns")
    void shouldReturnDistinctFilterOptions() {
        employeeRepository.saveAndFlush(Employee.create(
                "EMP-9004",
                "Dev One",
                "dev.one@acme.corp",
                "Engineering",
                "Staff Level 2",
                "India",
                Currency.getInstance("INR"),
                new BigDecimal("80000.00"),
                LocalDate.now(),
                LocalDate.now()
        ));
        employeeRepository.saveAndFlush(Employee.create(
                "EMP-9005",
                "Sales One",
                "sales.one@acme.corp",
                "Sales",
                "Staff Level 1",
                "United States",
                USD,
                new BigDecimal("90000.00"),
                LocalDate.now(),
                LocalDate.now()
        ));
        employeeRepository.saveAndFlush(Employee.create(
                "EMP-9006",
                "Dev Two",
                "dev.two@acme.corp",
                "Engineering",
                "Staff Level 2",
                "India",
                Currency.getInstance("INR"),
                new BigDecimal("85000.00"),
                LocalDate.now(),
                LocalDate.now()
        ));

        assertThat(employeeRepository.findDistinctDepartments())
                .contains("Engineering", "Sales")
                .doesNotHaveDuplicates();
        assertThat(employeeRepository.findDistinctCountries())
                .contains("India", "United States")
                .doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("Search by exact full name should return that employee and not substring neighbors")
    void shouldSearchByExactFullNameWithoutSubstringFalsePositives() {
        employeeRepository.saveAndFlush(Employee.create(
                "EMP-9101", "Worker 1", "worker.exact.1@acme.corp",
                "Engineering", "Staff Level 1", "United States", USD,
                new BigDecimal("100000.00"), LocalDate.now(), LocalDate.now()
        ));
        employeeRepository.saveAndFlush(Employee.create(
                "EMP-9110", "Worker 10", "worker.exact.10@acme.corp",
                "Engineering", "Staff Level 1", "United States", USD,
                new BigDecimal("101000.00"), LocalDate.now(), LocalDate.now()
        ));
        employeeRepository.saveAndFlush(Employee.create(
                "EMP-9111", "Worker 11", "worker.exact.11@acme.corp",
                "Engineering", "Staff Level 1", "United States", USD,
                new BigDecimal("102000.00"), LocalDate.now(), LocalDate.now()
        ));

        Page<Employee> page = employeeRepository.findWithFilters(
                null, null, "Worker 1", PageRequest.of(0, 20));

        assertThat(page.getContent().stream().map(Employee::getFullName))
                .containsExactly("Worker 1");
    }

    @Test
    @DisplayName("Search by exact full name should return every employee sharing that name")
    void shouldReturnAllEmployeesWithTheSameExactName() {
        employeeRepository.saveAndFlush(Employee.create(
                "EMP-9201", "John Smith", "john.one@acme.corp",
                "Engineering", "Staff Level 1", "United States", USD,
                new BigDecimal("100000.00"), LocalDate.now(), LocalDate.now()
        ));
        employeeRepository.saveAndFlush(Employee.create(
                "EMP-9202", "John Smith", "john.two@acme.corp",
                "Sales", "Staff Level 2", "United States", USD,
                new BigDecimal("110000.00"), LocalDate.now(), LocalDate.now()
        ));
        employeeRepository.saveAndFlush(Employee.create(
                "EMP-9203", "Johnny Smith", "johnny@acme.corp",
                "HR", "Staff Level 1", "United States", USD,
                new BigDecimal("90000.00"), LocalDate.now(), LocalDate.now()
        ));

        Page<Employee> page = employeeRepository.findWithFilters(
                null, null, "John Smith", PageRequest.of(0, 20));

        assertThat(page.getContent().stream().map(Employee::getEmployeeIdentifier))
                .containsExactlyInAnyOrder("EMP-9201", "EMP-9202")
                .doesNotContain("EMP-9203");
    }

    @Test
    @DisplayName("Search by employee identifier should require an exact match")
    void shouldSearchByExactEmployeeIdentifier() {
        employeeRepository.saveAndFlush(Employee.create(
                "EMP-9301", "Id Match", "id.match@acme.corp",
                "Engineering", "Staff Level 1", "United States", USD,
                new BigDecimal("100000.00"), LocalDate.now(), LocalDate.now()
        ));
        employeeRepository.saveAndFlush(Employee.create(
                "EMP-93010", "Id Neighbor", "id.neighbor@acme.corp",
                "Engineering", "Staff Level 1", "United States", USD,
                new BigDecimal("100000.00"), LocalDate.now(), LocalDate.now()
        ));

        Page<Employee> page = employeeRepository.findWithFilters(
                null, null, "EMP-9301", PageRequest.of(0, 20));

        assertThat(page.getContent().stream().map(Employee::getEmployeeIdentifier))
                .containsExactly("EMP-9301");
    }

    @Test
    @DisplayName("Search by email should require an exact match")
    void shouldSearchByExactEmail() {
        employeeRepository.saveAndFlush(Employee.create(
                "EMP-9401", "Email Match", "exact.email@acme.corp",
                "Engineering", "Staff Level 1", "United States", USD,
                new BigDecimal("100000.00"), LocalDate.now(), LocalDate.now()
        ));
        employeeRepository.saveAndFlush(Employee.create(
                "EMP-9402", "Email Other", "exact.email.other@acme.corp",
                "Engineering", "Staff Level 1", "United States", USD,
                new BigDecimal("100000.00"), LocalDate.now(), LocalDate.now()
        ));

        Page<Employee> page = employeeRepository.findWithFilters(
                null, null, "exact.email@acme.corp", PageRequest.of(0, 20));

        assertThat(page.getContent().stream().map(Employee::getEmail))
                .containsExactly("exact.email@acme.corp");
    }

    @Test
    @DisplayName("Search should be case-insensitive for name, email, and employee identifier")
    void shouldSearchCaseInsensitively() {
        employeeRepository.saveAndFlush(Employee.create(
                "EMP-9501", "Casey Exact", "casey.exact@acme.corp",
                "Engineering", "Staff Level 1", "United States", USD,
                new BigDecimal("100000.00"), LocalDate.now(), LocalDate.now()
        ));

        assertThat(employeeRepository.findWithFilters(
                null, null, "casey exact", PageRequest.of(0, 5)).getContent())
                .extracting(Employee::getEmployeeIdentifier)
                .containsExactly("EMP-9501");
        assertThat(employeeRepository.findWithFilters(
                null, null, "emp-9501", PageRequest.of(0, 5)).getContent())
                .extracting(Employee::getEmployeeIdentifier)
                .containsExactly("EMP-9501");
        assertThat(employeeRepository.findWithFilters(
                null, null, "CASEY.EXACT@ACME.CORP", PageRequest.of(0, 5)).getContent())
                .extracting(Employee::getEmployeeIdentifier)
                .containsExactly("EMP-9501");
    }
}
