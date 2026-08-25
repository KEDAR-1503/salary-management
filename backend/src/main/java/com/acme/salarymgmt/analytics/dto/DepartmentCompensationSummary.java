package com.acme.salarymgmt.analytics.dto;

import java.math.BigDecimal;

public record DepartmentCompensationSummary(
        String department,
        String currency,
        Long headcount,
        BigDecimal averageSalary,
        BigDecimal medianSalary
) {}
