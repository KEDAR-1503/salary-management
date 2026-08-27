package com.acme.salarymgmt.analytics.api;

import com.acme.salarymgmt.analytics.dto.CountryCompensationSummary;
import com.acme.salarymgmt.analytics.dto.DepartmentCompensationSummary;
import com.acme.salarymgmt.analytics.service.AnalyticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalyticsController.class)
@AutoConfigureMockMvc
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @Test
    @WithMockUser(username = "hr_manager", roles = {"HR_MANAGER"})
    @DisplayName("GET /api/v1/analytics/departments - should return summaries (positive)")
    void shouldReturnDepartmentSummaries() throws Exception {
        when(analyticsService.getDepartmentSummaries()).thenReturn(List.of(
                new DepartmentCompensationSummary(
                        "Engineering",
                        "USD",
                        2L,
                        new BigDecimal("110000.00"),
                        new BigDecimal("110000.00")
                )
        ));

        mockMvc.perform(get("/api/v1/analytics/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].department").value("Engineering"))
                .andExpect(jsonPath("$[0].currency").value("USD"))
                .andExpect(jsonPath("$[0].headcount").value(2));

        verify(analyticsService).getDepartmentSummaries();
    }

    @Test
    @WithMockUser(username = "hr_manager", roles = {"HR_MANAGER"})
    @DisplayName("GET /api/v1/analytics/countries - should return summaries (positive)")
    void shouldReturnCountrySummaries() throws Exception {
        when(analyticsService.getCountrySummaries()).thenReturn(List.of(
                new CountryCompensationSummary(
                        "India",
                        "INR",
                        5L,
                        new BigDecimal("2000000.00"),
                        new BigDecimal("1900000.00")
                )
        ));

        mockMvc.perform(get("/api/v1/analytics/countries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].country").value("India"))
                .andExpect(jsonPath("$[0].currency").value("INR"));

        verify(analyticsService).getCountrySummaries();
    }

    @Test
    @DisplayName("GET /api/v1/analytics/departments - should reject unauthenticated access (negative)")
    void shouldRejectUnauthenticatedDepartmentAnalytics() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/departments"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/analytics/countries - should reject unauthenticated access (negative)")
    void shouldRejectUnauthenticatedCountryAnalytics() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/countries"))
                .andExpect(status().isUnauthorized());
    }
}
