package com.acme.salarymgmt.compensation.api;

import com.acme.salarymgmt.compensation.domain.Employee;
import com.acme.salarymgmt.compensation.dto.CreateEmployeeRequest;
import com.acme.salarymgmt.compensation.dto.UpdateSalaryRequest;
import com.acme.salarymgmt.compensation.repository.EmployeeRepository;
import com.acme.salarymgmt.compensation.service.CompensationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
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
    private EmployeeRepository employeeRepository;

    @Test
    @WithMockUser(username = "hr_manager", roles = {"HR_MANAGER"})
    @DisplayName("POST /api/v1/employees - should return 201 Created on valid employee registration")
    void shouldCreateEmployee() throws Exception {
        CreateEmployeeRequest request = new CreateEmployeeRequest(
                "EMP-1001",
                "Alice Smith",
                "alice.smith@acme.corp",
                "Engineering",
                "Staff Engineer",
                "United States",
                "USD",
                new BigDecimal("120000.00"),
                LocalDate.now()
        );

        Employee saved = Employee.create(
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

        when(compensationService.createEmployee(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(saved);

        mockMvc.perform(post("/api/v1/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeIdentifier").value("EMP-1001"))
                .andExpect(jsonPath("$.currentSalary").value(120000.00));
    }

    @Test
    @WithMockUser(username = "hr_manager", roles = {"HR_MANAGER"})
    @DisplayName("PUT /api/v1/employees/{id}/salary - should return 200 OK on successful salary update")
    void shouldUpdateSalary() throws Exception {
        UpdateSalaryRequest request = new UpdateSalaryRequest(
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

        when(compensationService.updateSalary(eq(1L), any(), any(), any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/employees/1/salary")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentSalary").value(135000.00));
    }

    @Test
    @WithMockUser(username = "hr_manager", roles = {"HR_MANAGER"})
    @DisplayName("PUT /api/v1/employees/{id}/salary - should return 400 Bad Request when reason is shorter than 10 characters")
    void shouldReturn400WhenReasonTooShort() throws Exception {
        UpdateSalaryRequest invalidRequest = new UpdateSalaryRequest(
                new BigDecimal("135000.00"),
                LocalDate.now(),
                "Promo"
        );

        mockMvc.perform(put("/api/v1/employees/1/salary")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "hr_manager", roles = {"HR_MANAGER"})
    @DisplayName("PUT /api/v1/employees/{id}/salary - should return 409 Conflict on optimistic lock collision")
    void shouldReturn409OnOptimisticLockCollision() throws Exception {
        UpdateSalaryRequest request = new UpdateSalaryRequest(
                new BigDecimal("135000.00"),
                LocalDate.now(),
                "Annual merit performance promotion"
        );

        when(compensationService.updateSalary(eq(1L), any(), any(), any()))
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

        when(employeeRepository.findWithFilters(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(emp), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/v1/employees")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].employeeIdentifier").value("EMP-1001"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
