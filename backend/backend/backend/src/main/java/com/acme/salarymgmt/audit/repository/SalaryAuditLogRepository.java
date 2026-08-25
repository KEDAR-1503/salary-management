package com.acme.salarymgmt.audit.repository;

import com.acme.salarymgmt.audit.domain.SalaryAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalaryAuditLogRepository extends JpaRepository<SalaryAuditLog, Long> {

    List<SalaryAuditLog> findByEmployeeIdOrderByChangedAtDesc(Long employeeId);
}
