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
    private static final LocalDate TODAY = LocalDate.of(2026, 8, 25);

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
                TODAY
        );

        assertThat(employee.getEmployeeIdentifier()).isEqualTo("EMP-1001");
        assertThat(employee.getVersion()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should update current salary when effective date is today")
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
                TODAY.minusDays(30)
        );

        BigDecimal previousSalary = employee.updateSalary(new BigDecimal("135000.555"), TODAY, TODAY);

        assertThat(previousSalary).isEqualByComparingTo(new BigDecimal("120000.00"));
        assertThat(employee.getCurrentSalary()).isEqualByComparingTo(new BigDecimal("135000.56"));
        assertThat(employee.getEffectiveDate()).isEqualTo(TODAY);
    }

    @Test
    @DisplayName("Should reject salary update when effective date is not today")
    void shouldRejectNonTodayEffectiveDate() {
        Employee employee = Employee.create(
                "EMP-1001",
                "Jane Doe",
                "jane.doe@acme.corp",
                "Engineering",
                "Staff Software Engineer",
                "United States",
                USD,
                new BigDecimal("120000.00"),
                TODAY
        );

        assertThatThrownBy(() -> employee.updateSalary(
                new BigDecimal("135000.00"),
                TODAY.minusDays(1),
                TODAY
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Effective date must be today's date");
    }

    @Test
    @DisplayName("Should reject salary update when new amount is non-positive")
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
                TODAY
        );

        assertThatThrownBy(() -> employee.updateSalary(BigDecimal.ZERO, TODAY, TODAY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("New salary must be strictly positive");
    }
}
