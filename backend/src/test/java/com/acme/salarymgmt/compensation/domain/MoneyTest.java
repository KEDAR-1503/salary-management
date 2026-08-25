package com.acme.salarymgmt.compensation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Test
    @DisplayName("Should reject null amount during Money construction")
    void shouldRejectNullAmount() {
        assertThatThrownBy(() -> Money.of(null, USD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount must not be null");
    }

    @Test
    @DisplayName("Should reject negative amount during Money construction")
    void shouldRejectNegativeAmount() {
        BigDecimal negativeAmount = new BigDecimal("-10.00");

        assertThatThrownBy(() -> Money.of(negativeAmount, USD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Amount must not be negative");
    }

    @Test
    @DisplayName("Should reject null currency during Money construction")
    void shouldRejectNullCurrency() {
        BigDecimal validAmount = new BigDecimal("100.00");

        assertThatThrownBy(() -> Money.of(validAmount, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Currency must not be null");
    }

    @Test
    @DisplayName("Should normalise scale to 2 using HALF_EVEN rounding")
    void shouldNormaliseScaleToTwoUsingBankersRounding() {
        BigDecimal unscaled = new BigDecimal("100.555");

        Money money = Money.of(unscaled, USD);

        assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("100.56"));
        assertThat(money.amount().scale()).isEqualTo(2);
    }
}
