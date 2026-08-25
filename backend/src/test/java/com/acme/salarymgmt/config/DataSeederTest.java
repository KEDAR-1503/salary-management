package com.acme.salarymgmt.config;

import com.acme.salarymgmt.audit.repository.SalaryAuditLogRepository;
import com.acme.salarymgmt.compensation.repository.EmployeeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.atLeastOnce;
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
    @DisplayName("Should seed in batches when database is empty")
    void shouldSeedWhenDatabaseIsEmpty() {
        ReflectionTestUtils.setField(dataSeeder, "batchSize", 500);
        when(employeeRepository.count()).thenReturn(0L);
        when(employeeRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        dataSeeder.run(new String[0]);

        verify(employeeRepository, atLeastOnce()).saveAll(anyList());
        verify(salaryAuditLogRepository, atLeastOnce()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should be idempotent and skip seeding when records already exist")
    void shouldSkipSeedingWhenDatabaseNotEmpty() {
        when(employeeRepository.count()).thenReturn(10000L);

        dataSeeder.run(new String[0]);

        verify(employeeRepository, never()).saveAll(anyList());
        verify(salaryAuditLogRepository, never()).saveAll(anyList());
    }
}
