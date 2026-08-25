package com.acme.salarymgmt.analytics.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record DepartmentCompensationSummary(
        String department,
        String currency,
        Long headcount,
        BigDecimal averageSalary,
        BigDecimal medianSalary
) {
    public DepartmentCompensationSummary(
            String department,
            String currency,
            Long headcount,
            Double averageSalary,
            Double medianSalary
    ) {
        this(
                department,
                currency != null ? currency : "",
                headcount,
                averageSalary != null ? BigDecimal.valueOf(averageSalary).setScale(2, RoundingMode.HALF_EVEN) : BigDecimal.ZERO,
                medianSalary != null ? BigDecimal.valueOf(medianSalary).setScale(2, RoundingMode.HALF_EVEN) : BigDecimal.ZERO
        );
    }
}
