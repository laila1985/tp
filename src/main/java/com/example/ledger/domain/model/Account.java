package com.example.ledger.domain.model;

import com.example.ledger.domain.Currency;

public final class Account {

    private final String accountId;
    private final Money openingBalance;

    public Account(
            String accountId,
            Money openingBalance) {
        this.accountId = accountId;
        this.openingBalance = openingBalance;
    }

    public String getAccountId() {
        return accountId;
    }

    public Money getOpeningBalance() {
        return openingBalance;
    }

    public Currency getCurrency() {
        return openingBalance.getCurrency();
    }
}