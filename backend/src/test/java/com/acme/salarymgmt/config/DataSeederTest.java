package com.acme.salarymgmt.config;

import com.acme.salarymgmt.audit.repository.SalaryAuditLogRepository;
import com.acme.salarymgmt.compensation.repository.EmployeeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SalaryAuditLogRepository salaryAuditLogRepository;

    @InjectMocks
    private DataSeeder dataSeeder;

    @Test
    @DisplayName("Should seed exactly 10,000 employees when database is empty")
    void shouldSeedWhenDatabaseIsEmpty() {
        when(employeeRepository.count()).thenReturn(0L);

        dataSeeder.run();

        verify(employeeRepository).saveAll(anyList());
        verify(salaryAuditLogRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("Should be idempotent and skip seeding when records already exist")
    void shouldSkipSeedingWhenDatabaseNotEmpty() {
        when(employeeRepository.count()).thenReturn(10000L);

        dataSeeder.run();

        verify(employeeRepository, never()).saveAll(anyList());
        verify(salaryAuditLogRepository, never()).saveAll(anyList());
    }
}
