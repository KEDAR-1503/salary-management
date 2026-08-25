package com.acme.salarymgmt.analytics.api;

import com.acme.salarymgmt.analytics.dto.CountryCompensationSummary;
import com.acme.salarymgmt.analytics.dto.DepartmentCompensationSummary;
import com.acme.salarymgmt.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/departments")
    public ResponseEntity<List<DepartmentCompensationSummary>> getDepartmentSummaries() {
        return ResponseEntity.ok(analyticsService.getDepartmentSummaries());
    }

    @GetMapping("/countries")
    public ResponseEntity<List<CountryCompensationSummary>> getCountrySummaries() {
        return ResponseEntity.ok(analyticsService.getCountrySummaries());
    }
}
