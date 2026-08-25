package com.acme.salarymgmt.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should reject unauthenticated access to employee directory with 401 Unauthorized")
    void shouldRejectUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should permit authenticated HR Manager with HTTP Basic credentials")
    void shouldPermitAuthenticatedHrManager() throws Exception {
        mockMvc.perform(get("/api/v1/employees")
                        .with(httpBasic("hr_manager", "admin123")))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "unauthorized_user", roles = {"EMPLOYEE"})
    @DisplayName("Should return 403 Forbidden for non-HR roles")
    void shouldReturn403ForForbiddenRoles() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "hr_manager", roles = {"HR_MANAGER"})
    @DisplayName("Should reject POST mutation requests when CSRF token is missing")
    void shouldRejectPostWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/api/v1/employees")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}
