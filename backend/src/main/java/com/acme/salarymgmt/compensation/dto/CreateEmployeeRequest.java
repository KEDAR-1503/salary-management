package com.acme.salarymgmt.compensation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateEmployeeRequest(
        @NotBlank String fullName,
        @NotBlank @Email String email,
        @NotBlank String department,
        @NotBlank String roleTitle,
        @NotBlank String country,
        @NotBlank String currency,
        @NotNull @Positive BigDecimal initialSalary,
        @NotNull LocalDate effectiveDate
) {}
