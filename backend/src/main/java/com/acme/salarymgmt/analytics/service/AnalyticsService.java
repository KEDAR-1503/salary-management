package com.acme.salarymgmt.analytics.service;

import com.acme.salarymgmt.analytics.dto.CountryCompensationSummary;
import com.acme.salarymgmt.analytics.dto.DepartmentCompensationSummary;
import com.acme.salarymgmt.analytics.repository.AnalyticsRepository;
import com.acme.salarymgmt.analytics.repository.CountryCompensationSummaryProjection;
import com.acme.salarymgmt.analytics.repository.DepartmentCompensationSummaryProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    public List<DepartmentCompensationSummary> getDepartmentSummaries() {
        return analyticsRepository.getDepartmentSummaries().stream()
                .map(this::toDepartmentSummary)
                .toList();
    }

    public List<CountryCompensationSummary> getCountrySummaries() {
        return analyticsRepository.getCountrySummaries().stream()
                .map(this::toCountrySummary)
                .toList();
    }

    private DepartmentCompensationSummary toDepartmentSummary(DepartmentCompensationSummaryProjection projection) {
        return new DepartmentCompensationSummary(
                projection.getDepartment(),
                projection.getCurrency(),
                projection.getHeadcount(),
                projection.getAverageSalary(),
                projection.getMedianSalary()
        );
    }

    private CountryCompensationSummary toCountrySummary(CountryCompensationSummaryProjection projection) {
        return new CountryCompensationSummary(
                projection.getCountry(),
                projection.getCurrency(),
                projection.getHeadcount(),
                projection.getAverageSalary(),
                projection.getMedianSalary()
        );
    }
}
