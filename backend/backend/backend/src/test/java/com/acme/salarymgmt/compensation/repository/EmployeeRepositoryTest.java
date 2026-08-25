package com.acme.salarymgmt.compensation.repository;

import com.acme.salarymgmt.compensation.domain.Employee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class EmployeeRepositoryTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Autowired
    private TestEntityManager entityManager;

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
        assertThat(found.get().getCurrentSalary()).isEqualByComparingTo(new BigDecimal("150000.00"));
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
        entityManager.clear();

        // First transaction loads entity (version 0)
        Employee firstInstance = employeeRepository.findById(empId).orElseThrow();

        // Second transaction loads entity (version 0) and commits update (version becomes 1)
        Employee secondInstance = employeeRepository.findById(empId).orElseThrow();
        secondInstance.updateSalary(new BigDecimal("140000.00"), LocalDate.now());
        employeeRepository.saveAndFlush(secondInstance);
        entityManager.clear();

        // First transaction tries to update stale version 0
        firstInstance.updateSalary(new BigDecimal("145000.00"), LocalDate.now());

        assertThatThrownBy(() -> employeeRepository.saveAndFlush(firstInstance))
                .isInstanceOf(OptimisticLockingFailureException.class);
    }
}
