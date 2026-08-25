package com.acme.salarymgmt.analytics.service;

import com.acme.salarymgmt.analytics.dto.CountryCompensationSummary;
import com.acme.salarymgmt.analytics.dto.DepartmentCompensationSummary;
import com.acme.salarymgmt.analytics.repository.AnalyticsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private AnalyticsRepository analyticsRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    @DisplayName("Should return currency-separated department compensation summaries")
    void shouldReturnDepartmentSummaries() {
        DepartmentCompensationSummary summary = new DepartmentCompensationSummary(
                "Engineering",
                "USD",
                120L,
                new BigDecimal("125000.50"),
                new BigDecimal("120000.00")
        );

        when(analyticsRepository.getDepartmentSummaries()).thenReturn(List.of(summary));

        List<DepartmentCompensationSummary> result = analyticsService.getDepartmentSummaries();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).department()).isEqualTo("Engineering");
        assertThat(result.get(0).currency()).isEqualTo("USD");
        assertThat(result.get(0).headcount()).isEqualTo(120L);
        assertThat(result.get(0).averageSalary()).isEqualByComparingTo(new BigDecimal("125000.50"));
        assertThat(result.get(0).medianSalary()).isEqualByComparingTo(new BigDecimal("120000.00"));
    }

    @Test
    @DisplayName("Should return currency-separated country compensation summaries")
    void shouldReturnCountrySummaries() {
        CountryCompensationSummary summary = new CountryCompensationSummary(
                "United States",
                "USD",
                300L,
                new BigDecimal("115000.00"),
                new BigDecimal("110000.00")
        );

        when(analyticsRepository.getCountrySummaries()).thenReturn(List.of(summary));

        List<CountryCompensationSummary> result = analyticsService.getCountrySummaries();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).country()).isEqualTo("United States");
        assertThat(result.get(0).currency()).isEqualTo("USD");
        assertThat(result.get(0).headcount()).isEqualTo(300L);
        assertThat(result.get(0).averageSalary()).isEqualByComparingTo(new BigDecimal("115000.00"));
        assertThat(result.get(0).medianSalary()).isEqualByComparingTo(new BigDecimal("110000.00"));
    }
}
