package com.example.ledger.service;

import com.example.ledger.domain.Currency;
import com.example.ledger.domain.model.Money;

public class FeeService {

    private static final Money AED_OVERDRAFT_FEE =
            Money.of("25.00", Currency.AED);

    public Money calculateOverdraftFee(
            Money closingBalance,
            boolean feeAlreadyAssessed) {

        if (feeAlreadyAssessed) {
            return Money.zero(closingBalance.getCurrency());
        }

        if (closingBalance.isNegative()
                && closingBalance.getCurrency() == Currency.AED) {
            return AED_OVERDRAFT_FEE;
        }

        return Money.zero(closingBalance.getCurrency());
    }
}
