package com.acme.salarymgmt.analytics.service;

import com.acme.salarymgmt.analytics.dto.CountryCompensationSummary;
import com.acme.salarymgmt.analytics.dto.DepartmentCompensationSummary;
import com.acme.salarymgmt.analytics.repository.AnalyticsRepository;
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
        return analyticsRepository.getDepartmentSummaries();
    }

    public List<CountryCompensationSummary> getCountrySummaries() {
        return analyticsRepository.getCountrySummaries();
    }
}
