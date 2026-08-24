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
        BigDecimal nullAmount = null;

        assertThatThrownBy(() -> Money.of(nullAmount, USD))
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
    @DisplayName("Should normalize unscaled amount to scale 2 using HALF_EVEN rounding")
    void shouldNormalizeScaleToTwoWithBankersRounding() {
        BigDecimal unscaled = new BigDecimal("100.555");

        Money money = Money.of(unscaled, USD);

        assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("100.56"));
        assertThat(money.amount().scale()).isEqualTo(2);
    }
}
