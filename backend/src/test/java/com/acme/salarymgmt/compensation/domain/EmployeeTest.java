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
                "Staff Level 3",
                "United States",
                USD,
                new BigDecimal("120000.00"),
                TODAY,
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
                "Staff Level 3",
                "United States",
                USD,
                new BigDecimal("120000.00"),
                TODAY,
                TODAY
        );

        BigDecimal previousSalary = employee.updateSalary(new BigDecimal("135000.555"), TODAY, TODAY);

        assertThat(previousSalary).isEqualByComparingTo(new BigDecimal("120000.00"));
        assertThat(employee.getCurrentSalary()).isEqualByComparingTo(new BigDecimal("135000.56"));
        assertThat(employee.getEffectiveDate()).isEqualTo(TODAY);
    }

    @Test
    @DisplayName("Should reject salary update when effective date is in the past")
    void shouldRejectPastEffectiveDate() {
        Employee employee = Employee.create(
                "EMP-1001",
                "Jane Doe",
                "jane.doe@acme.corp",
                "Engineering",
                "Staff Level 3",
                "United States",
                USD,
                new BigDecimal("120000.00"),
                TODAY,
                TODAY
        );

        assertThatThrownBy(() -> employee.updateSalary(
                new BigDecimal("135000.00"),
                TODAY.minusDays(1),
                TODAY
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("past");
    }

    @Test
    @DisplayName("Should accept salary update when effective date is in the future")
    void shouldAcceptFutureEffectiveDate() {
        Employee employee = Employee.create(
                "EMP-1001",
                "Jane Doe",
                "jane.doe@acme.corp",
                "Engineering",
                "Staff Level 3",
                "United States",
                USD,
                new BigDecimal("120000.00"),
                TODAY,
                TODAY
        );

        LocalDate future = TODAY.plusDays(14);
        employee.updateSalary(new BigDecimal("140000.00"), future, TODAY);

        assertThat(employee.getEffectiveDate()).isEqualTo(future);
        assertThat(employee.getCurrentSalary()).isEqualByComparingTo(new BigDecimal("140000.00"));
    }

    @Test
    @DisplayName("Should reject create when effective date is in the past")
    void shouldRejectPastEffectiveDateOnCreate() {
        assertThatThrownBy(() -> Employee.create(
                "EMP-1001",
                "Jane Doe",
                "jane.doe@acme.corp",
                "Engineering",
                "Staff Level 1",
                "United States",
                USD,
                new BigDecimal("120000.00"),
                TODAY.minusDays(1),
                TODAY
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("past");
    }

    @Test
    @DisplayName("Should reject salary update when new amount is non-positive")
    void shouldRejectInvalidSalaryUpdate() {
        Employee employee = Employee.create(
                "EMP-1001",
                "Jane Doe",
                "jane.doe@acme.corp",
                "Engineering",
                "Staff Level 3",
                "United States",
                USD,
                new BigDecimal("120000.00"),
                TODAY,
                TODAY
        );

        assertThatThrownBy(() -> employee.updateSalary(BigDecimal.ZERO, TODAY, TODAY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("New salary must be strictly positive");
    }

    @Test
    @DisplayName("Should reject unknown department on create")
    void shouldRejectUnknownDepartment() {
        assertThatThrownBy(() -> Employee.create(
                "EMP-1001",
                "Jane Doe",
                "jane.doe@acme.corp",
                "Astrology",
                "Staff Level 1",
                "United States",
                USD,
                new BigDecimal("120000.00"),
                TODAY,
                TODAY
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Department");
    }

    @Test
    @DisplayName("Should reject unknown country on create")
    void shouldRejectUnknownCountry() {
        assertThatThrownBy(() -> Employee.create(
                "EMP-1001",
                "Jane Doe",
                "jane.doe@acme.corp",
                "Engineering",
                "Staff Level 1",
                "Atlantis",
                USD,
                new BigDecimal("120000.00"),
                TODAY,
                TODAY
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Country");
    }

    @Test
    @DisplayName("Should reject unknown role title on create")
    void shouldRejectUnknownRoleTitle() {
        assertThatThrownBy(() -> Employee.create(
                "EMP-1001",
                "Jane Doe",
                "jane.doe@acme.corp",
                "Engineering",
                "Chief Wizard",
                "United States",
                USD,
                new BigDecimal("120000.00"),
                TODAY,
                TODAY
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role");
    }

    @Test
    @DisplayName("Should reject currency that does not match the selected country")
    void shouldRejectCurrencyMismatchForCountry() {
        assertThatThrownBy(() -> Employee.create(
                "EMP-1001",
                "Jane Doe",
                "jane.doe@acme.corp",
                "Engineering",
                "Staff Level 1",
                "United States",
                Currency.getInstance("INR"),
                new BigDecimal("120000.00"),
                TODAY,
                TODAY
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Currency");
    }

    @Test
    @DisplayName("Should accept an employee built from the organisation catalog")
    void shouldAcceptCatalogValues() {
        Employee employee = Employee.create(
                "EMP-1001",
                "Jane Doe",
                "jane.doe@acme.corp",
                "Engineering",
                "Staff Level 1",
                "United States",
                USD,
                new BigDecimal("120000.00"),
                TODAY,
                TODAY
        );

        assertThat(employee.getDepartment()).isEqualTo("Engineering");
        assertThat(employee.getRoleTitle()).isEqualTo("Staff Level 1");
        assertThat(employee.getCountry()).isEqualTo("United States");
        assertThat(employee.getCurrencyCode()).isEqualTo("USD");
    }
}
