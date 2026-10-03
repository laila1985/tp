package com.example.ledger.domain;

import java.util.Map;

public final class AccountState {

    private final String accountId;
    private final Currency currency;

    private Money ledgerBalance;
    private final Map<String, Authorization> activeHolds;


    public AccountState(String accountId, Currency currency, Map<String, Authorization> activeHolds) {
        this.accountId = accountId;
        this.currency = currency;
        this.activeHolds = activeHolds;
    }
}
