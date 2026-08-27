package com.acme.salarymgmt.analytics.repository;

import com.acme.salarymgmt.AbstractIntegrationTest;
import com.acme.salarymgmt.compensation.domain.Employee;
import com.acme.salarymgmt.compensation.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyticsRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private AnalyticsRepository analyticsRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();

        employeeRepository.save(Employee.create(
                "EMP-A1", "Alice", "alice@acme.corp", "Engineering", "Staff Level 1", "United States",
                Currency.getInstance("USD"), new BigDecimal("100000.00"), LocalDate.now(),
                LocalDate.now()
        ));
        employeeRepository.save(Employee.create(
                "EMP-A2", "Bob", "bob@acme.corp", "Engineering", "Staff Level 1", "United States",
                Currency.getInstance("USD"), new BigDecimal("120000.00"), LocalDate.now(),
                LocalDate.now()
        ));
        employeeRepository.save(Employee.create(
                "EMP-A3", "Carol", "carol@acme.corp", "Engineering", "Staff Level 1", "United Kingdom",
                Currency.getInstance("GBP"), new BigDecimal("80000.00"), LocalDate.now(),
                LocalDate.now()
        ));
    }

    @Test
    @DisplayName("Should compute median using percentile_cont grouped by department and currency")
    void shouldComputeMedianByDepartmentAndCurrency() {
        List<DepartmentCompensationSummaryProjection> summaries = analyticsRepository.getDepartmentSummaries();

        assertThat(summaries).hasSize(2);

        DepartmentCompensationSummaryProjection engineeringUsd = summaries.stream()
                .filter(s -> "Engineering".equals(s.getDepartment()) && "USD".equals(s.getCurrency()))
                .findFirst()
                .orElseThrow();

        assertThat(engineeringUsd.getHeadcount()).isEqualTo(2L);
        assertThat(engineeringUsd.getAverageSalary()).isEqualByComparingTo(new BigDecimal("110000.00"));
        assertThat(engineeringUsd.getMedianSalary()).isEqualByComparingTo(new BigDecimal("110000.00"));
    }

    @Test
    @DisplayName("Should compute country summaries grouped by country and currency (positive)")
    void shouldComputeMedianByCountryAndCurrency() {
        List<CountryCompensationSummaryProjection> summaries = analyticsRepository.getCountrySummaries();

        assertThat(summaries).hasSize(2);

        CountryCompensationSummaryProjection us = summaries.stream()
                .filter(s -> "United States".equals(s.getCountry()) && "USD".equals(s.getCurrency()))
                .findFirst()
                .orElseThrow();

        assertThat(us.getHeadcount()).isEqualTo(2L);
        assertThat(us.getAverageSalary()).isEqualByComparingTo(new BigDecimal("110000.00"));
        assertThat(us.getMedianSalary()).isEqualByComparingTo(new BigDecimal("110000.00"));

        CountryCompensationSummaryProjection uk = summaries.stream()
                .filter(s -> "United Kingdom".equals(s.getCountry()) && "GBP".equals(s.getCurrency()))
                .findFirst()
                .orElseThrow();

        assertThat(uk.getHeadcount()).isEqualTo(1L);
        assertThat(uk.getMedianSalary()).isEqualByComparingTo(new BigDecimal("80000.00"));
    }

    @Test
    @DisplayName("Should return no analytics rows when employee table is empty (negative/empty)")
    void shouldReturnEmptySummariesWhenNoEmployees() {
        employeeRepository.deleteAll();

        assertThat(analyticsRepository.getDepartmentSummaries()).isEmpty();
        assertThat(analyticsRepository.getCountrySummaries()).isEmpty();
    }
}
