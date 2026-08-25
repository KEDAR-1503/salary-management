package com.acme.salarymgmt.analytics.repository;

import com.acme.salarymgmt.compensation.domain.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalyticsRepository extends JpaRepository<Employee, Long> {

    @Query(value = """
        SELECT e.department AS department,
               e.currency AS currency,
               COUNT(e.id) AS headcount,
               AVG(e.current_salary) AS averageSalary,
               percentile_cont(0.5) WITHIN GROUP (ORDER BY e.current_salary) AS medianSalary
        FROM employees e
        GROUP BY e.department, e.currency
        ORDER BY e.department ASC, e.currency ASC
        """, nativeQuery = true)
    List<DepartmentCompensationSummaryProjection> getDepartmentSummaries();

    @Query(value = """
        SELECT e.country AS country,
               e.currency AS currency,
               COUNT(e.id) AS headcount,
               AVG(e.current_salary) AS averageSalary,
               percentile_cont(0.5) WITHIN GROUP (ORDER BY e.current_salary) AS medianSalary
        FROM employees e
        GROUP BY e.country, e.currency
        ORDER BY e.country ASC, e.currency ASC
        """, nativeQuery = true)
    List<CountryCompensationSummaryProjection> getCountrySummaries();
}
