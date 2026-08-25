package com.acme.salarymgmt.analytics.repository;

import java.math.BigDecimal;

public interface DepartmentCompensationSummaryProjection {
    String getDepartment();
    String getCurrency();
    Long getHeadcount();
    BigDecimal getAverageSalary();
    BigDecimal getMedianSalary();
}
