package com.example.ledger.service;

import com.example.ledger.domain.AccountBalance;
import com.example.ledger.domain.Currency;
import com.example.ledger.domain.Money;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public final class BalanceService {

    private final Map<String, AccountBalance> balances = new HashMap<>();

    public AccountBalance getOrCreate(
            String accountId,
            Currency currency
    ) {
        return balances.computeIfAbsent(
                accountId,
                id -> new AccountBalance(
                        new Money(currency, BigDecimal.ZERO)
                )
        );
    }

    public AccountBalance get(String accountId) {
        AccountBalance balance = balances.get(accountId);

        if (balance == null) {
            throw new IllegalStateException(
                    "Account not found: " + accountId
            );
        }

        return balance;
    }

    public void credit(
            String accountId,
            Money amount
    ) {
        getOrCreate(accountId, amount.getCurrency())
                .credit(amount);
    }

    public void debit(
            String accountId,
            Money amount
    ) {
        getOrCreate(accountId, amount.getCurrency())
                .debit(amount);
    }

    public void placeHold(
            String accountId,
            Money amount
    ) {
        getOrCreate(accountId, amount.getCurrency())
                .placeHold(amount);
    }

    public void releaseHold(
            String accountId,
            Money amount
    ) {
        getOrCreate(accountId, amount.getCurrency())
                .releaseHold(amount);
    }
}


