package com.acme.salarymgmt.compensation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmployeeTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Test
    @DisplayName("Should create a valid employee with initial base salary and version 0")
    void shouldCreateValidEmployee() {
        Employee employee = Employee.create(
                "EMP-1001",
                "Jane Doe",
                "jane.doe@acme.corp",
                "Engineering",
                "Staff Software Engineer",
                "United States",
                USD,
                new BigDecimal("120000.00"),
                LocalDate.now()
        );

        assertThat(employee.getEmployeeIdentifier()).isEqualTo("EMP-1001");
        assertThat(employee.getFullName()).isEqualTo("Jane Doe");
        assertThat(employee.getEmail()).isEqualTo("jane.doe@acme.corp");
        assertThat(employee.getDepartment()).isEqualTo("Engineering");
        assertThat(employee.getRoleTitle()).isEqualTo("Staff Software Engineer");
        assertThat(employee.getCountry()).isEqualTo("United States");
        assertThat(employee.getCurrency()).isEqualTo(USD);
        assertThat(employee.getCurrentSalary()).isEqualByComparingTo(new BigDecimal("120000.00"));
        assertThat(employee.getEffectiveDate()).isEqualTo(LocalDate.now());
        assertThat(employee.getVersion()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should reject employee creation when initial salary is zero or negative")
    void shouldRejectNonPositiveInitialSalary() {
        assertThatThrownBy(() -> Employee.create(
                "EMP-1002",
                "John Smith",
                "john.smith@acme.corp",
                "Sales",
                "Account Executive",
                "United States",
                USD,
                new BigDecimal("-500.00"),
                LocalDate.now()
        ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Initial salary must be strictly positive");
    }

    @Test
    @DisplayName("Should update current salary with normalized scale and advance effective date")
    void shouldUpdateSalarySuccessfully() {
        Employee employee = Employee.create(
                "EMP-1001",
                "Jane Doe",
                "jane.doe@acme.corp",
                "Engineering",
                "Staff Software Engineer",
                "United States",
                USD,
                new BigDecimal("120000.00"),
                LocalDate.now().minusDays(30)
        );

        LocalDate updateDate = LocalDate.now();
        BigDecimal previousSalary = employee.updateSalary(new BigDecimal("135000.555"), updateDate);

        assertThat(previousSalary).isEqualByComparingTo(new BigDecimal("120000.00"));
        assertThat(employee.getCurrentSalary()).isEqualByComparingTo(new BigDecimal("135000.56"));
        assertThat(employee.getEffectiveDate()).isEqualTo(updateDate);
    }

    @Test
    @DisplayName("Should reject salary update when new amount is null or non-positive")
    void shouldRejectInvalidSalaryUpdate() {
        Employee employee = Employee.create(
                "EMP-1001",
                "Jane Doe",
                "jane.doe@acme.corp",
                "Engineering",
                "Staff Software Engineer",
                "United States",
                USD,
                new BigDecimal("120000.00"),
                LocalDate.now()
        );

        assertThatThrownBy(() -> employee.updateSalary(BigDecimal.ZERO, LocalDate.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("New salary must be strictly positive");
    }
}
