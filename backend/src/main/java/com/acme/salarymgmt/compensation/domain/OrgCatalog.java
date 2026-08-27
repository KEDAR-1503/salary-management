package com.acme.salarymgmt.compensation.domain;

import java.util.Currency;
import java.util.List;

/**
 * Canonical ACME reference data for create-employee and filters.
 * Intentionally incomplete until the green TDD step fills it in.
 */
public final class OrgCatalog {

    private OrgCatalog() {
    }

    public static List<String> departments() {
        return List.of();
    }

    public static List<String> countries() {
        return List.of();
    }

    public static List<String> roleTitles() {
        return List.of();
    }

    public static Currency currencyForCountry(String country) {
        throw new UnsupportedOperationException("OrgCatalog not implemented yet");
    }

    public static boolean isAllowedDepartment(String department) {
        return false;
    }

    public static boolean isAllowedCountry(String country) {
        return false;
    }

    public static boolean isAllowedRoleTitle(String roleTitle) {
        return false;
    }

    public static boolean isAllowedCurrencyForCountry(String country, Currency currency) {
        return false;
    }
}
