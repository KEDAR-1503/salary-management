package com.acme.salarymgmt.compensation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Money Value Object Unit Tests")
class MoneyTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency GBP = Currency.getInstance("GBP");

    @Nested
    @DisplayName("Validation & Construction Rules")
    class ConstructionValidation {

        @Test
        @DisplayName("Given a null amount, when constructing Money, then throw IllegalArgumentException")
        void shouldRejectNullAmount() {
            // Given
            BigDecimal nullAmount = null;

            // When & Then
            assertThatThrownBy(() -> Money.of(nullAmount, USD))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Amount must not be null");
        }

        @Test
        @DisplayName("Given a negative amount, when constructing Money, then throw IllegalArgumentException")
        void shouldRejectNegativeAmount() {
            // Given
            BigDecimal negativeAmount = new BigDecimal("-10.00");

            // When & Then
            assertThatThrownBy(() -> Money.of(negativeAmount, USD))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Amount must not be negative");
        }

        @Test
        @DisplayName("Given a null currency, when constructing Money, then throw IllegalArgumentException")
        void shouldRejectNullCurrency() {
            // Given
            BigDecimal validAmount = new BigDecimal("100.00");

            // When & Then
            assertThatThrownBy(() -> Money.of(validAmount, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Currency must not be null");
        }

        @Test
        @DisplayName("Given an unscaled amount, when constructing Money, then normalise scale to 2 using HALF_EVEN rounding")
        void shouldNormaliseScaleToTwoUsingBankersRounding() {
            // Given
            BigDecimal unscaled = new BigDecimal("100.555");

            // When
            Money money = Money.of(unscaled, USD);

            // Then
            assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("100.56"));
            assertThat(money.amount().scale()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Monetary Arithmetic & Currency Safety")
    class ArithmeticRules {

        @Test
        @DisplayName("Given two Money instances with identical currency, when added, then return correct sum")
        void shouldAddMoneyWithMatchingCurrency() {
            // Given
            Money initial = Money.of(new BigDecimal("100.50"), USD);
            Money addition = Money.of(new BigDecimal("50.25"), USD);

            // When
            Money result = initial.add(addition);

            // Then
            assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("150.75"));
            assertThat(result.currency()).isEqualTo(USD);
        }

        @Test
        @DisplayName("Given two Money instances with different currencies, when added, then throw IllegalArgumentException")
        void shouldRejectAdditionWithMismatchedCurrencies() {
            // Given
            Money usdMoney = Money.of(new BigDecimal("100.00"), USD);
            Money gbpMoney = Money.of(new BigDecimal("100.00"), GBP);

            // When & Then
            assertThatThrownBy(() -> usdMoney.add(gbpMoney))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Cannot add money of currency GBP to USD");
        }
    }
}
