package com.example.ledger.domain.model;

import com.example.ledger.domain.Currency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Money {

    private final Currency currency;
    private final BigDecimal amount;

    public Money(Currency currency, BigDecimal amount) {
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }

        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }

        this.currency = currency;
        this.amount = amount.setScale(
                currency.getScale(),
                RoundingMode.HALF_UP
        );
    }

    public static Money of(String value, Currency currency) {
        return new Money(currency, new BigDecimal(value));
    }

    public BigDecimal amount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public boolean isNegative() {
        return amount.signum() < 0;
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    public Money negate() {
        return new Money(currency, amount.negate());
    }

    public static Money zero(Currency currency) {
        return new Money(currency, BigDecimal.ZERO);
    }

    public Money add(Money other) {
        checkCurrency(other);

        return new Money(
                currency,
                amount.add(other.amount)
        );
    }

    public Money subtract(Money other) {
        checkCurrency(other);

        return new Money(
                currency,
                amount.subtract(other.amount)
        );
    }

    private void checkCurrency(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("Money cannot be null");
        }

        if (currency != other.currency) {
            throw new IllegalArgumentException(
                    "Currency mismatch: "
                            + currency
                            + " vs "
                            + other.currency
            );
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Money money)) {
            return false;
        }

        return currency == money.currency
                && amount.equals(money.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currency, amount);
    }

    @Override
    public String toString() {
        return currency + " " + amount;
    }
}