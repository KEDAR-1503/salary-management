package com.acme.salarymgmt.compensation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateSalaryRequest(
        @NotNull Long version,
        @NotNull @Positive BigDecimal newSalary,
        @NotNull LocalDate effectiveDate,
        @NotBlank @Size(min = 10, message = "Reason must be at least 10 characters") String reason
) {}
