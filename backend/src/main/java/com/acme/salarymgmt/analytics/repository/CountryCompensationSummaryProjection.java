package com.acme.salarymgmt.analytics.repository;

import java.math.BigDecimal;

public interface CountryCompensationSummaryProjection {
    String getCountry();
    String getCurrency();
    Long getHeadcount();
    BigDecimal getAverageSalary();
    BigDecimal getMedianSalary();
}
