package com.acme.salarymgmt.compensation.domain;

import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Fixed ACME organization catalog for create/update and filters.
 * Free-text department / country / role / currency is not allowed.
 */
public final class OrgCatalog {

    private static final List<String> DEPARTMENTS = List.of(
            "Engineering", "Product", "Sales", "Marketing", "HR", "Finance", "Operations");

    private static final List<String> COUNTRIES = List.of(
            "United States", "United Kingdom", "Germany", "India", "Singapore");

    private static final List<String> ROLE_TITLES = List.of(
            "Staff Level 1", "Staff Level 2", "Staff Level 3", "Staff Level 4", "Staff Level 5");

    private static final Map<String, Currency> CURRENCY_BY_COUNTRY = Map.of(
            "United States", Currency.getInstance("USD"),
            "United Kingdom", Currency.getInstance("GBP"),
            "Germany", Currency.getInstance("EUR"),
            "India", Currency.getInstance("INR"),
            "Singapore", Currency.getInstance("SGD"));

    private static final Set<String> DEPARTMENT_SET = Set.copyOf(DEPARTMENTS);
    private static final Set<String> COUNTRY_SET = Set.copyOf(COUNTRIES);
    private static final Set<String> ROLE_TITLE_SET = Set.copyOf(ROLE_TITLES);

    private OrgCatalog() {}

    public static List<String> departments() {
        return DEPARTMENTS;
    }

    public static List<String> countries() {
        return COUNTRIES;
    }

    public static List<String> roleTitles() {
        return ROLE_TITLES;
    }

    public static Currency currencyForCountry(String country) {
        Currency currency = CURRENCY_BY_COUNTRY.get(country);
        if (currency == null) {
            throw new IllegalArgumentException("Unknown country: " + country);
        }
        return currency;
    }

    public static boolean isAllowedDepartment(String department) {
        return department != null && DEPARTMENT_SET.contains(department);
    }

    public static boolean isAllowedCountry(String country) {
        return country != null && COUNTRY_SET.contains(country);
    }

    public static boolean isAllowedRoleTitle(String roleTitle) {
        return roleTitle != null && ROLE_TITLE_SET.contains(roleTitle);
    }

    public static boolean isAllowedCurrencyForCountry(String country, Currency currency) {
        if (country == null || currency == null) {
            return false;
        }
        Currency expected = CURRENCY_BY_COUNTRY.get(country);
        return expected != null && expected.equals(currency);
    }
}
