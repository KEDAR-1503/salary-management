package com.acme.salarymgmt.compensation.api;

import com.acme.salarymgmt.compensation.domain.Employee;
import com.acme.salarymgmt.compensation.dto.CreateEmployeeRequest;
import com.acme.salarymgmt.compensation.dto.EmployeeResponse;
import com.acme.salarymgmt.compensation.dto.UpdateSalaryRequest;
import com.acme.salarymgmt.compensation.repository.EmployeeRepository;
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

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class CompensationController {

    private final CompensationService compensationService;
    private final EmployeeRepository employeeRepository;

    @PostMapping
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        Employee employee = compensationService.createEmployee(
                request.employeeIdentifier(),
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
                request.newSalary(),
                request.effectiveDate(),
                request.reason()
        );
        return ResponseEntity.ok(EmployeeResponse.fromDomain(updated));
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeResponse>> getEmployees(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<EmployeeResponse> responses = employeeRepository.findWithFilters(department, country, search, pageable)
                .map(EmployeeResponse::fromDomain);
        return ResponseEntity.ok(responses);
    }
}
