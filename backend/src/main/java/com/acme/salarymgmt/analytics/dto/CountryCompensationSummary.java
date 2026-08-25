package com.acme.salarymgmt.analytics.dto;

import java.math.BigDecimal;

public record CountryCompensationSummary(
        String country,
        String currency,
        Long headcount,
        BigDecimal averageSalary,
        BigDecimal medianSalary
) {}
