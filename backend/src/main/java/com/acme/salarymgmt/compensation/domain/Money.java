package com.acme.salarymgmt.compensation.domain;

import java.math.BigDecimal;
import java.util.Currency;

public record Money(BigDecimal amount, Currency currency) {

    public static Money of(BigDecimal amount, Currency currency) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount must not be null");
        }
        return new Money(amount, currency);
    }
}
