package com.acme.salarymgmt.compensation.dto;

import com.acme.salarymgmt.compensation.domain.Employee;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeResponse(
        Long id,
        Long version,
        String employeeIdentifier,
        String fullName,
        String email,
        String department,
        String roleTitle,
        String country,
        String currency,
        BigDecimal currentSalary,
        LocalDate effectiveDate
) {
    public static EmployeeResponse fromDomain(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getVersion(),
                employee.getEmployeeIdentifier(),
                employee.getFullName(),
                employee.getEmail(),
                employee.getDepartment(),
                employee.getRoleTitle(),
                employee.getCountry(),
                employee.getCurrencyCode(),
                employee.getCurrentSalary(),
                employee.getEffectiveDate()
        );
    }
}
