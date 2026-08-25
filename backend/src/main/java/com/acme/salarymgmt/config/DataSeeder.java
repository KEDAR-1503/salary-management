package com.acme.salarymgmt.config;

import com.acme.salarymgmt.audit.domain.SalaryAuditLog;
import com.acme.salarymgmt.audit.repository.SalaryAuditLogRepository;
import com.acme.salarymgmt.compensation.domain.Employee;
import com.acme.salarymgmt.compensation.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@Profile("seed")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final SalaryAuditLogRepository salaryAuditLogRepository;

    @Value("${app.seed.batch-size:500}")
    private int batchSize;

    private static final String[] DEPARTMENTS = {
            "Engineering", "Product", "Sales", "Marketing", "HR", "Finance", "Operations"
    };

    private static final String[] COUNTRIES = {
            "United States", "United Kingdom", "Germany", "India", "Singapore"
    };

    private static final String[] CURRENCIES = {
            "USD", "GBP", "EUR", "INR", "SGD"
    };

    @Override
    public void run(String... args) {
        if (employeeRepository.count() != 0) {
            log.info("Database already seeded. Skipping initial data population.");
            return;
        }

        log.info("Starting deterministic chunked seeding of 10,000 employees...");
        Random random = new Random(42);
        LocalDate effectiveDate = LocalDate.now();
        Instant changedAt = Instant.now();

        List<Employee> batch = new ArrayList<>(batchSize);
        int totalSeeded = 0;

        for (int i = 1; i <= 10000; i++) {
            int countryIdx = random.nextInt(COUNTRIES.length);
            String country = COUNTRIES[countryIdx];
            String currencyCode = CURRENCIES[countryIdx];
            Currency currency = Currency.getInstance(currencyCode);
            String department = DEPARTMENTS[random.nextInt(DEPARTMENTS.length)];
            BigDecimal salary = generateBaseSalary(currencyCode, random);

            Employee employee = Employee.create(
                    String.format("EMP-%05d", i),
                    "Worker " + i,
                    String.format("worker.%05d@acme.corp", i),
                    department,
                    "Staff Level " + ((i % 5) + 1),
                    country,
                    currency,
                    salary,
                    effectiveDate
            );
            batch.add(employee);

            if (batch.size() == batchSize || i == 10000) {
                totalSeeded += seedBatch(batch, changedAt);
                batch.clear();
            }
        }

        log.info("Successfully seeded {} employees and initial audit logs.", totalSeeded);
    }

    @Transactional
    protected int seedBatch(List<Employee> employees, Instant changedAt) {
        List<Employee> savedEmployees = employeeRepository.saveAll(employees);
        List<SalaryAuditLog> auditLogs = new ArrayList<>(savedEmployees.size());

        for (Employee emp : savedEmployees) {
            auditLogs.add(SalaryAuditLog.recordInitialSetup(
                    emp.getId(),
                    emp.getCurrentSalary(),
                    emp.getCurrency(),
                    "system_seeder",
                    changedAt
            ));
        }

        salaryAuditLogRepository.saveAll(auditLogs);
        return savedEmployees.size();
    }

    private BigDecimal generateBaseSalary(String currency, Random random) {
        long baseAmount = switch (currency) {
            case "USD" -> 80_000L + random.nextInt(120_000);
            case "GBP" -> 60_000L + random.nextInt(80_000);
            case "EUR" -> 65_000L + random.nextInt(85_000);
            case "INR" -> 1_500_000L + random.nextInt(3_000_000);
            case "SGD" -> 90_000L + random.nextInt(110_000);
            default -> 50_000L;
        };
        return BigDecimal.valueOf(baseAmount).setScale(2, RoundingMode.HALF_EVEN);
    }
}
