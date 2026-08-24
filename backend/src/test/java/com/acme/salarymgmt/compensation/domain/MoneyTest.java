package com.acme.salarymgmt.compensation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Test
    @DisplayName("Should reject null amount during Money construction")
    void shouldRejectNullAmount() {
        BigDecimal nullAmount = null;

        assertThatThrownBy(() -> Money.of(nullAmount, USD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount must not be null");
    }
}
