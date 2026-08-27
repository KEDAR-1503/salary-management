package com.acme.salarymgmt.compensation.api;

import com.acme.salarymgmt.audit.domain.SalaryAuditLog;
import com.acme.salarymgmt.audit.service.AuditService;
import com.acme.salarymgmt.compensation.domain.Employee;
import com.acme.salarymgmt.compensation.dto.CreateEmployeeRequest;
import com.acme.salarymgmt.compensation.dto.UpdateSalaryRequest;
import com.acme.salarymgmt.compensation.service.CompensationService;
import com.acme.salarymgmt.config.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompensationController.class)
@AutoConfigureMockMvc
@Import(GlobalExceptionHandler.class)
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
                "Staff Level 3",
                "United States",
                USD,
                new BigDecimal("135000.00"),
                LocalDate.now(),
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
    @DisplayName("PUT /api/v1/employees/{id}/salary - should reject reason shorter than 10 chars (negative)")
    void shouldRejectSalaryUpdateWithShortReason() throws Exception {
        UpdateSalaryRequest request = new UpdateSalaryRequest(
                0L,
                new BigDecimal("135000.00"),
                LocalDate.now(),
                "too short"
        );

        mockMvc.perform(put("/api/v1/employees/1/salary")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"));
    }

    @Test
    @WithMockUser(username = "hr_manager", roles = {"HR_MANAGER"})
    @DisplayName("PUT /api/v1/employees/{id}/salary - should reject past effective date (negative)")
    void shouldRejectSalaryUpdateWithPastEffectiveDate() throws Exception {
        LocalDate past = LocalDate.now().minusDays(1);
        when(compensationService.updateSalary(eq(1L), eq(0L), any(), eq(past), any()))
                .thenThrow(new IllegalArgumentException("Effective date must not be in the past"));

        UpdateSalaryRequest request = new UpdateSalaryRequest(
                0L,
                new BigDecimal("135000.00"),
                past,
                "Annual merit performance promotion"
        );

        mockMvc.perform(put("/api/v1/employees/1/salary")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Effective date must not be in the past"));
    }

    @Test
    @WithMockUser(username = "hr_manager", roles = {"HR_MANAGER"})
    @DisplayName("PUT /api/v1/employees/{id}/salary - should return 404 when employee missing (negative)")
    void shouldReturn404WhenUpdatingMissingEmployee() throws Exception {
        when(compensationService.updateSalary(eq(99L), eq(0L), any(), any(), any()))
                .thenThrow(new EntityNotFoundException("Employee not found with id: 99"));

        UpdateSalaryRequest request = new UpdateSalaryRequest(
                0L,
                new BigDecimal("135000.00"),
                LocalDate.now(),
                "Annual merit performance promotion"
        );

        mockMvc.perform(put("/api/v1/employees/99/salary")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("Employee not found with id: 99"));
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
                "Staff Level 3",
                "United States",
                USD,
                new BigDecimal("120000.00"),
                LocalDate.now(),
                LocalDate.now()
        );

        when(compensationService.getEmployee(1L)).thenReturn(employee);

        mockMvc.perform(get("/api/v1/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeIdentifier").value("EMP-1001"));
    }

    @Test
    @WithMockUser(username = "hr_manager", roles = {"HR_MANAGER"})
    @DisplayName("GET /api/v1/employees/{id} - should return 404 when employee missing (negative)")
    void shouldReturn404WhenEmployeeMissing() throws Exception {
        when(compensationService.getEmployee(99L))
                .thenThrow(new EntityNotFoundException("Employee not found with id: 99"));

        mockMvc.perform(get("/api/v1/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("Employee not found with id: 99"));
    }

    @Test
    @DisplayName("GET /api/v1/employees/{id} - should reject unauthenticated access (negative)")
    void shouldRejectUnauthenticatedEmployeeDetail() throws Exception {
        mockMvc.perform(get("/api/v1/employees/1"))
                .andExpect(status().isUnauthorized());
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
                "Staff Level 3",
                "United States",
                USD,
                new BigDecimal("120000.00"),
                LocalDate.now(),
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
    @DisplayName("GET /api/v1/employees/{id}/history - should return 404 when employee missing (negative)")
    void shouldReturn404WhenHistoryEmployeeMissing() throws Exception {
        when(compensationService.getEmployee(99L))
                .thenThrow(new EntityNotFoundException("Employee not found with id: 99"));

        mockMvc.perform(get("/api/v1/employees/99/history"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"));
    }

    @Test
    @DisplayName("GET /api/v1/employees/{id}/history - should reject unauthenticated access (negative)")
    void shouldRejectUnauthenticatedEmployeeHistory() throws Exception {
        mockMvc.perform(get("/api/v1/employees/1/history"))
                .andExpect(status().isUnauthorized());
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
                "Staff Level 3",
                "United States",
                USD,
                new BigDecimal("120000.00"),
                LocalDate.now(),
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

    @Test
    @WithMockUser(username = "hr_manager", roles = {"HR_MANAGER"})
    @DisplayName("GET /api/v1/employees - should forward exact search term to service (positive)")
    void shouldForwardExactSearchTermToService() throws Exception {
        when(compensationService.getEmployees(isNull(), isNull(), eq("Worker 1"), any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/api/v1/employees")
                        .param("search", "Worker 1")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk());

        verify(compensationService).getEmployees(isNull(), isNull(), eq("Worker 1"), any());
    }

    @Test
    @DisplayName("GET /api/v1/employees - should reject unauthenticated search (negative)")
    void shouldRejectUnauthenticatedEmployeeSearch() throws Exception {
        mockMvc.perform(get("/api/v1/employees").param("search", "Worker 1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "hr_manager", roles = {"HR_MANAGER"})
    @DisplayName("POST /api/v1/employees - should create employee without client-supplied EMP ID")
    void shouldCreateEmployee() throws Exception {
        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "Dana Lee",
                "dana.lee@acme.corp",
                "Product",
                "Staff Level 2",
                "Singapore",
                "SGD",
                new BigDecimal("98000.00"),
                LocalDate.now()
        );

        Employee created = Employee.create(
                "EMP-02001",
                request.fullName(),
                request.email(),
                request.department(),
                request.roleTitle(),
                request.country(),
                Currency.getInstance("SGD"),
                request.initialSalary(),
                request.effectiveDate(),
                LocalDate.now()
        );
        org.springframework.test.util.ReflectionTestUtils.setField(created, "id", 42L);

        when(compensationService.createEmployee(
                eq("Dana Lee"),
                eq("dana.lee@acme.corp"),
                eq("Product"),
                eq("Staff Level 2"),
                eq("Singapore"),
                eq(Currency.getInstance("SGD")),
                any(),
                any()
        )).thenReturn(created);

        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.employeeIdentifier").value("EMP-02001"))
                .andExpect(jsonPath("$.currency").value("SGD"));
    }

    @Test
    @WithMockUser(username = "hr_manager", roles = {"HR_MANAGER"})
    @DisplayName("POST /api/v1/employees - should reject blank name (negative)")
    void shouldRejectCreateWithBlankName() throws Exception {
        String body = """
                {
                  "fullName": "",
                  "email": "dana.lee@acme.corp",
                  "department": "Product",
                  "roleTitle": "Staff Level 2",
                  "country": "Singapore",
                  "currency": "SGD",
                  "initialSalary": 98000.00,
                  "effectiveDate": "2026-08-27"
                }
                """;

        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "hr_manager", roles = {"HR_MANAGER"})
    @DisplayName("POST /api/v1/employees - should reject unknown department catalog value (negative)")
    void shouldRejectCreateWithUnknownDepartment() throws Exception {
        when(compensationService.createEmployee(
                any(), any(), eq("Astrology"), any(), any(), any(), any(), any()
        )).thenThrow(new IllegalArgumentException("Department must be a catalog value"));

        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "Bad Dept",
                "bad.dept@acme.corp",
                "Astrology",
                "Staff Level 1",
                "United States",
                "USD",
                new BigDecimal("90000.00"),
                LocalDate.now()
        );

        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Department must be a catalog value"));
    }

    @Test
    @WithMockUser(username = "hr_manager", roles = {"HR_MANAGER"})
    @DisplayName("POST /api/v1/employees - should reject currency that does not match country (negative)")
    void shouldRejectCreateWithCurrencyMismatch() throws Exception {
        when(compensationService.createEmployee(
                any(), any(), any(), any(), eq("Singapore"), eq(Currency.getInstance("USD")), any(), any()
        )).thenThrow(new IllegalArgumentException("Currency does not match country"));

        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "Mismatch Hire",
                "mismatch@acme.corp",
                "Product",
                "Staff Level 2",
                "Singapore",
                "USD",
                new BigDecimal("98000.00"),
                LocalDate.now()
        );

        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Currency does not match country"));
    }

    @Test
    @WithMockUser(username = "hr_manager", roles = {"HR_MANAGER"})
    @DisplayName("POST /api/v1/employees - should reject past effective date (negative)")
    void shouldRejectCreateWithPastEffectiveDate() throws Exception {
        LocalDate past = LocalDate.now().minusDays(1);
        when(compensationService.createEmployee(
                any(), any(), any(), any(), any(), any(), any(), eq(past)
        )).thenThrow(new IllegalArgumentException("Effective date must not be in the past"));

        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "Past Hire",
                "past.hire@acme.corp",
                "Engineering",
                "Staff Level 1",
                "United States",
                "USD",
                new BigDecimal("90000.00"),
                past
        );

        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.detail").value("Effective date must not be in the past"));
    }

    @Test
    @WithMockUser(username = "hr_manager", roles = {"HR_MANAGER"})
    @DisplayName("GET /api/v1/employees/filter-options - should return organisation catalog lists")
    void shouldReturnFilterOptions() throws Exception {
        mockMvc.perform(get("/api/v1/employees/filter-options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departments[0]").value("Engineering"))
                .andExpect(jsonPath("$.countries[0]").value("United States"))
                .andExpect(jsonPath("$.roleTitles[0]").value("Staff Level 1"))
                .andExpect(jsonPath("$.roleTitles[4]").value("Staff Level 5"));
    }
}
