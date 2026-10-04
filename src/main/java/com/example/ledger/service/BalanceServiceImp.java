package com.example.ledger.service;

import com.example.ledger.domain.model.AccountBalance;
import com.example.ledger.domain.Currency;
import com.example.ledger.domain.Day;
import com.example.ledger.domain.Money;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public final class BalanceServiceImp implements BalanceService {

    private final Map<String, AccountBalance> balances =
            new HashMap<>();

    private final Map<Day, Map<String, AccountBalance>> dailyBalances =
            new HashMap<>();

    @Override
    public AccountBalance getOrCreate(
            String accountId,
            Currency currency
    ) {
        return balances.computeIfAbsent(
                accountId,
                id -> new AccountBalance(
                        new Money(
                                currency,
                                BigDecimal.ZERO
                        )
                )
        );
    }

    @Override
    public AccountBalance get(String accountId) {

        AccountBalance balance =
                balances.get(accountId);

        if (balance == null) {
            throw new IllegalStateException(
                    "Account not found: " + accountId
            );
        }

        return balance;
    }

    @Override
    public void credit(
            String accountId,
            Money amount
    ) {
        getOrCreate(
                accountId,
                amount.getCurrency()
        ).credit(amount);
    }

    @Override
    public void debit(
            String accountId,
            Money amount
    ) {
        getOrCreate(
                accountId,
                amount.getCurrency()
        ).debit(amount);
    }

    @Override
    public void placeHold(
            String accountId,
            Money amount
    ) {
        getOrCreate(
                accountId,
                amount.getCurrency()
        ).placeHold(amount);
    }

    @Override
    public void releaseHold(
            String accountId,
            Money amount
    ) {
        getOrCreate(
                accountId,
                amount.getCurrency()
        ).releaseHold(amount);
    }

    @Override
    public Money balance(String accountId) {
        return get(accountId).getLedgerBalance();
    }

}
