package com.example.ledger.service;
import com.example.ledger.domain.model.AccountBalance;
import com.example.ledger.domain.Money;



public interface BalanceService {

    AccountBalance getOrCreate(
            String accountId,
            com.example.ledger.domain.Currency currency
    );

    AccountBalance get(String accountId);

    void credit(
            String accountId,
            Money amount
    );

    void debit(
            String accountId,
            Money amount
    );

    void placeHold(
            String accountId,
            Money amount
    );

    void releaseHold(
            String accountId,
            Money amount
    );

    Money balance(String accountId);
}


