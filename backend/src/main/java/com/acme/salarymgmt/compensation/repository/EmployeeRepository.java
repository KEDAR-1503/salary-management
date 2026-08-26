package com.acme.salarymgmt.compensation.repository;

import com.acme.salarymgmt.compensation.domain.Employee;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByEmployeeIdentifier(String employeeIdentifier);

    Optional<Employee> findByEmail(String email);

    @Query("SELECT DISTINCT e.department FROM Employee e ORDER BY e.department")
    List<String> findDistinctDepartments();

    @Query("SELECT DISTINCT e.country FROM Employee e ORDER BY e.country")
    List<String> findDistinctCountries();

    /**
     * Optional filters via Criteria API — avoids Hibernate/Postgres binding null strings
     * as {@code bytea}, which breaks {@code LOWER(?)} in JPQL ({@code function lower(bytea) does not exist}).
     */
    default Page<Employee> findWithFilters(
            String department,
            String country,
            String searchTerm,
            Pageable pageable
    ) {
        return findAll(withFilters(department, country, searchTerm), pageable);
    }

    private static Specification<Employee> withFilters(String department, String country, String searchTerm) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (department != null && !department.isBlank()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("department")),
                        department.toLowerCase(Locale.ROOT)
                ));
            }
            if (country != null && !country.isBlank()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("country")),
                        country.toLowerCase(Locale.ROOT)
                ));
            }
            if (searchTerm != null && !searchTerm.isBlank()) {
                String pattern = "%" + searchTerm.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("fullName")), pattern),
                        cb.like(cb.lower(root.get("employeeIdentifier")), pattern)
                ));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
