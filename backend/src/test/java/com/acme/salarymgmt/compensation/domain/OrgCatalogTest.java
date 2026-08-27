package com.acme.salarymgmt.compensation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

class OrgCatalogTest {

    @Test
    @DisplayName("Should expose the fixed ACME department list")
    void shouldListDepartments() {
        assertThat(OrgCatalog.departments()).containsExactly(
                "Engineering", "Product", "Sales", "Marketing", "HR", "Finance", "Operations"
        );
    }

    @Test
    @DisplayName("Should expose the fixed ACME country list")
    void shouldListCountries() {
        assertThat(OrgCatalog.countries()).containsExactly(
                "United States", "United Kingdom", "Germany", "India", "Singapore"
        );
    }

    @Test
    @DisplayName("Should expose allowed role titles")
    void shouldListRoleTitles() {
        assertThat(OrgCatalog.roleTitles()).contains(
                "Staff Level 1", "Staff Level 2", "Staff Level 3", "Staff Level 4", "Staff Level 5"
        );
    }

    @Test
    @DisplayName("Should map each country to exactly one currency")
    void shouldMapCountryToCurrency() {
        assertThat(OrgCatalog.currencyForCountry("United States")).isEqualTo(Currency.getInstance("USD"));
        assertThat(OrgCatalog.currencyForCountry("United Kingdom")).isEqualTo(Currency.getInstance("GBP"));
        assertThat(OrgCatalog.currencyForCountry("Germany")).isEqualTo(Currency.getInstance("EUR"));
        assertThat(OrgCatalog.currencyForCountry("India")).isEqualTo(Currency.getInstance("INR"));
        assertThat(OrgCatalog.currencyForCountry("Singapore")).isEqualTo(Currency.getInstance("SGD"));
    }

    @Test
    @DisplayName("Should reject departments and countries outside the catalog")
    void shouldRejectUnknownValues() {
        assertThat(OrgCatalog.isAllowedDepartment("Engineering")).isTrue();
        assertThat(OrgCatalog.isAllowedDepartment("Astrology")).isFalse();
        assertThat(OrgCatalog.isAllowedCountry("India")).isTrue();
        assertThat(OrgCatalog.isAllowedCountry("Atlantis")).isFalse();
        assertThat(OrgCatalog.isAllowedRoleTitle("Staff Level 1")).isTrue();
        assertThat(OrgCatalog.isAllowedRoleTitle("Chief Wizard")).isFalse();
        assertThat(OrgCatalog.isAllowedCurrencyForCountry("United States", Currency.getInstance("USD"))).isTrue();
        assertThat(OrgCatalog.isAllowedCurrencyForCountry("United States", Currency.getInstance("INR"))).isFalse();
    }
}
