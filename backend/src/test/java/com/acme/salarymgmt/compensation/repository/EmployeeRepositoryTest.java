package com.acme.salarymgmt.compensation.repository;

import com.acme.salarymgmt.AbstractIntegrationTest;
import com.acme.salarymgmt.compensation.domain.Employee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;

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
                "Principal Architect",
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
                "Product Lead",
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
}
