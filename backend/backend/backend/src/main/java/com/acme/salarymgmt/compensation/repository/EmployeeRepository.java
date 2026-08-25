package com.acme.salarymgmt.compensation.repository;

import com.acme.salarymgmt.compensation.domain.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmployeeIdentifier(String employeeIdentifier);

    Optional<Employee> findByEmail(String email);

    @Query("""
        SELECT e FROM Employee e
        WHERE (:department IS NULL OR LOWER(e.department) = LOWER(:department))
          AND (:country IS NULL OR LOWER(e.country) = LOWER(:country))
          AND (:searchTerm IS NULL OR (
                LOWER(e.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR
                LOWER(e.employeeIdentifier) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
          ))
    """)
    Page<Employee> findWithFilters(
            @Param("department") String department,
            @Param("country") String country,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );
}
