package com.acme.salarymgmt.compensation.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

public record Money(BigDecimal amount, Currency currency) {

    public static Money of(BigDecimal amount, Currency currency) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount must not be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must not be negative");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency must not be null");
        }
        BigDecimal scaled = amount.setScale(2, RoundingMode.HALF_EVEN);
        return new Money(scaled, currency);
    }
}
