package com.example.ledger.domain;

public final class Account {

    private final String accountId;
    private final Currency currency;
    private final Money openingBalance;

    public Account(
            String accountId,
            Currency currency,
            Money openingBalance) {
        this.accountId = accountId;
        this.currency = currency;
        this.openingBalance = openingBalance;
    }

    public String getAccountId() {
        return accountId;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Money getOpeningBalance() {
        return openingBalance;
    }
}