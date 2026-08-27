package com.acme.salarymgmt.compensation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Currency;

@Entity
@Table(name = "employees")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "emp_seq_gen")
    @SequenceGenerator(name = "emp_seq_gen", sequenceName = "employee_seq", allocationSize = 50)
    private Long id;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "employee_identifier", nullable = false, unique = true, length = 64)
    private String employeeIdentifier;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "department", nullable = false, length = 50)
    private String department;

    @Column(name = "role_title", nullable = false, length = 80)
    private String roleTitle;

    @Column(name = "country", nullable = false, length = 50)
    private String country;

    @Column(name = "currency", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "current_salary", nullable = false, precision = 14, scale = 2)
    private BigDecimal currentSalary;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    public static Employee create(
            String employeeIdentifier,
            String fullName,
            String email,
            String department,
            String roleTitle,
            String country,
            Currency currency,
            BigDecimal initialSalary,
            LocalDate effectiveDate
    ) {
        validateFields(employeeIdentifier, fullName, email, department, roleTitle, country, currency, effectiveDate);
        if (initialSalary == null || initialSalary.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Initial salary must be strictly positive");
        }
        BigDecimal normalizedSalary = initialSalary.setScale(2, RoundingMode.HALF_EVEN);

        return new Employee(
                null,
                0L,
                employeeIdentifier.trim(),
                fullName.trim(),
                email.trim().toLowerCase(),
                department.trim(),
                roleTitle.trim(),
                country.trim(),
                currency.getCurrencyCode(),
                normalizedSalary,
                effectiveDate
        );
    }

    public BigDecimal updateSalary(BigDecimal newSalary, LocalDate effectiveDate, LocalDate today) {
        if (newSalary == null || newSalary.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("New salary must be strictly positive");
        }
        if (effectiveDate == null) {
            throw new IllegalArgumentException("Effective date must not be null");
        }
        if (!effectiveDate.equals(today)) {
            throw new IllegalArgumentException("Effective date must be today's date");
        }
        BigDecimal previous = this.currentSalary;
        this.currentSalary = newSalary.setScale(2, RoundingMode.HALF_EVEN);
        this.effectiveDate = effectiveDate;
        return previous;
    }

    public Currency getCurrency() {
        return Currency.getInstance(this.currencyCode);
    }

    private static void validateFields(
            String employeeIdentifier,
            String fullName,
            String email,
            String department,
            String roleTitle,
            String country,
            Currency currency,
            LocalDate effectiveDate
    ) {
        if (employeeIdentifier == null || employeeIdentifier.isBlank()) {
            throw new IllegalArgumentException("Employee identifier must not be blank");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name must not be blank");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Valid email is required");
        }
        if (department == null || department.isBlank()) {
            throw new IllegalArgumentException("Department must not be blank");
        }
        if (!OrgCatalog.isAllowedDepartment(department.trim())) {
            throw new IllegalArgumentException("Department must be a catalog value");
        }
        if (roleTitle == null || roleTitle.isBlank()) {
            throw new IllegalArgumentException("Role title must not be blank");
        }
        if (!OrgCatalog.isAllowedRoleTitle(roleTitle.trim())) {
            throw new IllegalArgumentException("Role title must be a catalog value");
        }
        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("Country must not be blank");
        }
        if (!OrgCatalog.isAllowedCountry(country.trim())) {
            throw new IllegalArgumentException("Country must be a catalog value");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency must not be null");
        }
        if (!OrgCatalog.isAllowedCurrencyForCountry(country.trim(), currency)) {
            throw new IllegalArgumentException("Currency does not match country");
        }
        if (effectiveDate == null) {
            throw new IllegalArgumentException("Effective date must not be null");
        }
    }
}
