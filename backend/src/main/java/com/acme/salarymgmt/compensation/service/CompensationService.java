package com.acme.salarymgmt.compensation.service;

import com.acme.salarymgmt.audit.service.AuditService;
import com.acme.salarymgmt.compensation.domain.Employee;
import com.acme.salarymgmt.compensation.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;

@Service
@RequiredArgsConstructor
public class CompensationService {

    private final EmployeeRepository employeeRepository;
    private final AuditService auditService;
    private final Clock clock;

    @Transactional
    public Employee createEmployee(
            String employeeIdentifier,
            String fullName,
            String email,
            String department,
            String roleTitle,
            String country,
            Currency currency,
            BigDecimal initialSalary,
            LocalDate effectiveDate
    ) {
        Employee employee = Employee.create(
                employeeIdentifier,
                fullName,
                email,
                department,
                roleTitle,
                country,
                currency,
                initialSalary,
                effectiveDate
        );

        Employee saved = employeeRepository.save(employee);
        String actor = resolveAuthenticatedActor();
        Instant now = Instant.now(clock);

        auditService.recordInitialSetup(
                saved.getId(),
                saved.getCurrentSalary(),
                saved.getCurrency(),
                actor,
                now
        );

        return saved;
    }

    @Transactional
    public Employee updateSalary(
            Long employeeId,
            BigDecimal newSalary,
            LocalDate effectiveDate,
            String reason
    ) {
        if (reason == null || reason.trim().length() < 10) {
            throw new IllegalArgumentException("Reason must be at least 10 characters");
        }

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + employeeId));

        BigDecimal previousSalary = employee.updateSalary(newSalary, effectiveDate);
        Employee saved = employeeRepository.save(employee);

        String actor = resolveAuthenticatedActor();
        Instant now = Instant.now(clock);

        auditService.recordSalaryChange(
                employeeId,
                previousSalary,
                saved.getCurrentSalary(),
                saved.getCurrency(),
                actor,
                reason.trim(),
                now
        );

        return saved;
    }

    private String resolveAuthenticatedActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return "system";
        }
        return authentication.getName();
    }
}
