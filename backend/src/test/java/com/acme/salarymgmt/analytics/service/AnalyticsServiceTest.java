package com.acme.salarymgmt.analytics.service;

import com.acme.salarymgmt.analytics.dto.CountryCompensationSummary;
import com.acme.salarymgmt.analytics.dto.DepartmentCompensationSummary;
import com.acme.salarymgmt.analytics.repository.AnalyticsRepository;
import com.acme.salarymgmt.analytics.repository.CountryCompensationSummaryProjection;
import com.acme.salarymgmt.analytics.repository.DepartmentCompensationSummaryProjection;
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
        DepartmentCompensationSummaryProjection projection = new DepartmentCompensationSummaryProjection() {
            @Override public String getDepartment() { return "Engineering"; }
            @Override public String getCurrency() { return "USD"; }
            @Override public Long getHeadcount() { return 120L; }
            @Override public BigDecimal getAverageSalary() { return new BigDecimal("125000.50"); }
            @Override public BigDecimal getMedianSalary() { return new BigDecimal("120000.00"); }
        };

        when(analyticsRepository.getDepartmentSummaries()).thenReturn(List.of(projection));

        List<DepartmentCompensationSummary> result = analyticsService.getDepartmentSummaries();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).department()).isEqualTo("Engineering");
        assertThat(result.get(0).medianSalary()).isEqualByComparingTo(new BigDecimal("120000.00"));
    }

    @Test
    @DisplayName("Should return currency-separated country compensation summaries")
    void shouldReturnCountrySummaries() {
        CountryCompensationSummaryProjection projection = new CountryCompensationSummaryProjection() {
            @Override public String getCountry() { return "United States"; }
            @Override public String getCurrency() { return "USD"; }
            @Override public Long getHeadcount() { return 300L; }
            @Override public BigDecimal getAverageSalary() { return new BigDecimal("115000.00"); }
            @Override public BigDecimal getMedianSalary() { return new BigDecimal("110000.00"); }
        };

        when(analyticsRepository.getCountrySummaries()).thenReturn(List.of(projection));

        List<CountryCompensationSummary> result = analyticsService.getCountrySummaries();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).country()).isEqualTo("United States");
        assertThat(result.get(0).medianSalary()).isEqualByComparingTo(new BigDecimal("110000.00"));
    }

    @Test
    @DisplayName("Should return an empty department list when repository has no rows (negative/empty)")
    void shouldReturnEmptyDepartmentSummaries() {
        when(analyticsRepository.getDepartmentSummaries()).thenReturn(List.of());

        assertThat(analyticsService.getDepartmentSummaries()).isEmpty();
    }

    @Test
    @DisplayName("Should return an empty country list when repository has no rows (negative/empty)")
    void shouldReturnEmptyCountrySummaries() {
        when(analyticsRepository.getCountrySummaries()).thenReturn(List.of());

        assertThat(analyticsService.getCountrySummaries()).isEmpty();
    }
}
