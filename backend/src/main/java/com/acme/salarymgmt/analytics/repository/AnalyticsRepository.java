package com.acme.salarymgmt.analytics.repository;

import com.acme.salarymgmt.analytics.dto.CountryCompensationSummary;
import com.acme.salarymgmt.analytics.dto.DepartmentCompensationSummary;
import com.acme.salarymgmt.compensation.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalyticsRepository extends JpaRepository<Employee, Long> {

    @Query("""
        SELECT new com.acme.salarymgmt.analytics.dto.DepartmentCompensationSummary(
            e.department,
            e.currencyCode,
            COUNT(e.id),
            AVG(e.currentSalary),
            AVG(e.currentSalary)
        )
        FROM Employee e
        GROUP BY e.department, e.currencyCode
        ORDER BY e.department ASC, e.currencyCode ASC
    """)
    List<DepartmentCompensationSummary> getDepartmentSummaries();

    @Query("""
        SELECT new com.acme.salarymgmt.analytics.dto.CountryCompensationSummary(
            e.country,
            e.currencyCode,
            COUNT(e.id),
            AVG(e.currentSalary),
            AVG(e.currentSalary)
        )
        FROM Employee e
        GROUP BY e.country, e.currencyCode
        ORDER BY e.country ASC, e.currencyCode ASC
    """)
    List<CountryCompensationSummary> getCountrySummaries();
}
