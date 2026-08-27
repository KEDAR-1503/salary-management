package com.acme.salarymgmt.compensation.repository;

import com.acme.salarymgmt.AbstractIntegrationTest;
import com.acme.salarymgmt.compensation.domain.Employee;
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
                LocalDate.now()
        ));

        assertThat(employeeRepository.findDistinctDepartments())
                .contains("Engineering", "Sales")
                .doesNotHaveDuplicates();
        assertThat(employeeRepository.findDistinctCountries())
                .contains("India", "United States")
                .doesNotHaveDuplicates();
    }
}
