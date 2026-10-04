package com.example.ledger.service;

import com.example.ledger.domain.Currency;
import com.example.ledger.domain.model.Money;

import java.math.BigDecimal;
import java.util.List;

public class InterestService {

    private static final BigDecimal DAILY_RATE =
            new BigDecimal("0.0004");

    /**
     * Rounded daily accrual for one day. Only positive closing balances accrue.
     */
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

    /**
     * Capitalized total = the sum of the already-rounded daily accruals.
     *
     * <p>This is what guarantees the requirement that the rounded daily
     * accruals sum <em>exactly</em> to the capitalized total: the total is not
     * an independently rounded figure, it is literally their sum, so no
     * rounding remainder can ever be "discarded".</p>
     */
    public Money capitalizedTotal(Currency currency, List<Money> dailyAccruals) {

        Money total = Money.zero(currency);

        for (Money accrual : dailyAccruals) {
            total = total.add(accrual);
        }

        return total;
    }
}
