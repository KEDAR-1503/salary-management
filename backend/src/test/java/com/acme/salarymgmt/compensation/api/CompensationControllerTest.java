package com.acme.salarymgmt.compensation.api;

import com.acme.salarymgmt.audit.domain.SalaryAuditLog;
import com.acme.salarymgmt.audit.service.AuditService;
import com.acme.salarymgmt.compensation.domain.Employee;
import com.acme.salarymgmt.compensation.dto.CreateEmployeeRequest;
import com.acme.salarymgmt.compensation.dto.UpdateSalaryRequest;
import com.acme.salarymgmt.compensation.service.CompensationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompensationController.class)
@AutoConfigureMockMvc
class CompensationControllerTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompensationService compensationService;

    @MockBean
    private AuditService auditService;

    @Test
    @WithMockUser(username = "hr_manager", roles = {"HR_MANAGER"})
    @DisplayName("PUT /api/v1/employees/{id}/salary - should return 200 OK on successful salary update")
    void shouldUpdateSalary() throws Exception {
        UpdateSalaryRequest request = new UpdateSalaryRequest(
                0L,
                new BigDecimal("135000.00"),
                LocalDate.now(),
                "Annual merit performance promotion"
        );

        Employee updated = Employee.create(
                "EMP-1001",
                "Alice Smith",
                "alice.smith@acme.corp",
                "Engineering",
                "Staff Engineer",
                "United States",
                USD,
                new BigDecimal("135000.00"),
                LocalDate.now()
        );

        when(compensationService.updateSalary(eq(1L), eq(0L), any(), any(), any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/employees/1/salary")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentSalary").value(135000.00));
    }

    @Test
    @WithMockUser(username = "hr_manager", roles = {"HR_MANAGER"})
    @DisplayName("PUT /api/v1/employees/{id}/salary - should return 409 Conflict on stale version")
    void shouldReturn409OnOptimisticLockCollision() throws Exception {
        UpdateSalaryRequest request = new UpdateSalaryRequest(
                0L,
                new BigDecimal("135000.00"),
                LocalDate.now(),
                "Annual merit performance promotion"
        );

        when(compensationService.updateSalary(eq(1L), eq(0L), any(), any(), any()))
                .thenThrow(new OptimisticLockingFailureException("Stale object state"));

        mockMvc.perform(put("/api/v1/employees/1/salary")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"));
    }

    @Test
    @WithMockUser(username = "hr_manager", roles = {"HR_MANAGER"})
    @DisplayName("GET /api/v1/employees/{id} - should return employee detail")
    void shouldReturnEmployeeById() throws Exception {
        Employee employee = Employee.create(
                "EMP-1001",
                "Alice Smith",
                "alice.smith@acme.corp",
                "Engineering",
                "Staff Engineer",
                "United States",
                USD,
                new BigDecimal("120000.00"),
                LocalDate.now()
        );

        when(compensationService.getEmployee(1L)).thenReturn(employee);

        mockMvc.perform(get("/api/v1/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeIdentifier").value("EMP-1001"));
    }

    @Test
    @WithMockUser(username = "hr_manager", roles = {"HR_MANAGER"})
    @DisplayName("GET /api/v1/employees/{id}/history - should return audit history newest first")
    void shouldReturnEmployeeHistory() throws Exception {
        Employee employee = Employee.create(
                "EMP-1001",
                "Alice Smith",
                "alice.smith@acme.corp",
                "Engineering",
                "Staff Engineer",
                "United States",
                USD,
                new BigDecimal("120000.00"),
                LocalDate.now()
        );

        SalaryAuditLog auditLog = SalaryAuditLog.recordInitialSetup(
                1L,
                new BigDecimal("120000.00"),
                USD,
                "hr_manager",
                Instant.parse("2026-08-25T10:00:00Z")
        );

        when(compensationService.getEmployee(1L)).thenReturn(employee);
        when(auditService.getAuditHistory(1L)).thenReturn(List.of(auditLog));

        mockMvc.perform(get("/api/v1/employees/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reason").value("Initial Employee Setup"));
    }

    @Test
    @WithMockUser(username = "hr_manager", roles = {"HR_MANAGER"})
    @DisplayName("GET /api/v1/employees - should return paginated list of employees")
    void shouldReturnPaginatedEmployees() throws Exception {
        Employee emp = Employee.create(
                "EMP-1001",
                "Alice Smith",
                "alice.smith@acme.corp",
                "Engineering",
                "Staff Engineer",
                "United States",
                USD,
                new BigDecimal("120000.00"),
                LocalDate.now()
        );

        when(compensationService.getEmployees(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(emp), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/employees")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].employeeIdentifier").value("EMP-1001"));
    }
}
