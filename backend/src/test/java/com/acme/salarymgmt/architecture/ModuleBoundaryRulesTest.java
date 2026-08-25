package com.acme.salarymgmt.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
        packages = "com.acme.salarymgmt",
        importOptions = {ImportOption.DoNotIncludeTests.class}
)
class ModuleBoundaryRulesTest {

    @ArchTest
    static final ArchRule audit_should_not_depend_on_compensation_internals =
            noClasses().that().resideInAPackage("..audit..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "..compensation.repository..",
                            "..compensation.service.."
                    )
                    .because("The Audit bounded context must remain decoupled from Compensation internals.");

    @ArchTest
    static final ArchRule compensation_should_not_access_audit_repository_directly =
            noClasses().that().resideInAPackage("..compensation..")
                    .should().accessClassesThat().resideInAPackage("..audit.repository..")
                    .because("Compensation must only interact with Audit via public Service contracts.");

    @ArchTest
    static final ArchRule controllers_must_not_access_repositories_directly =
            noClasses().that().resideInAPackage("..api..")
                    .should().accessClassesThat().resideInAPackage("..repository..")
                    .because("Controllers must delegate to domain services (Layered Architecture rule).");

    @ArchTest
    static final ArchRule domain_entities_must_not_use_double_or_float_for_money =
            fields().that().areDeclaredInClassesThat().resideInAPackage("..domain..")
                    .should().notHaveRawType(double.class)
                    .andShould().notHaveRawType(Double.class)
                    .andShould().notHaveRawType(float.class)
                    .andShould().notHaveRawType(Float.class)
                    .because("Floating point arithmetic is strictly forbidden for financial domains. Use BigDecimal.");
}
