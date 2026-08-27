package com.acme.salarymgmt.compensation.api;

import com.acme.salarymgmt.audit.dto.SalaryAuditLogResponse;
import com.acme.salarymgmt.audit.service.AuditService;
import com.acme.salarymgmt.compensation.domain.Employee;
import com.acme.salarymgmt.compensation.domain.OrgCatalog;
import com.acme.salarymgmt.compensation.dto.CreateEmployeeRequest;
import com.acme.salarymgmt.compensation.dto.EmployeeFilterOptionsResponse;
import com.acme.salarymgmt.compensation.dto.EmployeeResponse;
import com.acme.salarymgmt.compensation.dto.UpdateSalaryRequest;
import com.acme.salarymgmt.compensation.service.CompensationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Currency;
import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class CompensationController {

    private final CompensationService compensationService;
    private final AuditService auditService;

    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        Employee employee = compensationService.createEmployee(
                request.fullName(),
                request.email(),
                request.department(),
                request.roleTitle(),
                request.country(),
                Currency.getInstance(request.currency()),
                request.initialSalary(),
                request.effectiveDate()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(EmployeeResponse.fromDomain(employee));
    }

    @PutMapping("/{id}/salary")
    public ResponseEntity<EmployeeResponse> updateSalary(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSalaryRequest request
    ) {
        Employee updated = compensationService.updateSalary(
                id,
                request.version(),
                request.newSalary(),
                request.effectiveDate(),
                request.reason()
        );
        return ResponseEntity.ok(EmployeeResponse.fromDomain(updated));
    }

    @GetMapping("/filter-options")
    public ResponseEntity<EmployeeFilterOptionsResponse> getFilterOptions() {
        return ResponseEntity.ok(new EmployeeFilterOptionsResponse(
                OrgCatalog.departments(),
                OrgCatalog.countries(),
                OrgCatalog.roleTitles()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(EmployeeResponse.fromDomain(compensationService.getEmployee(id)));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<SalaryAuditLogResponse>> getEmployeeHistory(@PathVariable Long id) {
        compensationService.getEmployee(id);
        List<SalaryAuditLogResponse> history = auditService.getAuditHistory(id).stream()
                .map(SalaryAuditLogResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(history);
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeResponse>> getEmployees(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<EmployeeResponse> responses = compensationService.getEmployees(department, country, search, pageable)
                .map(EmployeeResponse::fromDomain);
        return ResponseEntity.ok(responses);
    }
}
