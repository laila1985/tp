package com.example.ledger.service;

import com.example.ledger.domain.Money;

import java.math.BigDecimal;

public class InterestService {

    private static final BigDecimal DAILY_RATE =
            new BigDecimal("0.0004");

    public Money calculateDailyAccrual(Money closingBalance) {

        if (!closingBalance.isPositive()) {
            return Money.zero(closingBalance.getCurrency());
        }

        BigDecimal interest = closingBalance.amount()
                .multiply(DAILY_RATE);

        return new Money(
                closingBalance.getCurrency(),
                interest
        );
    }
}