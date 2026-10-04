package com.example.ledger.domain.model;

import java.util.*;

public final class Account {

    private final String accountId;
    private final Money openingBalance;

    private final List<LedgerEntry> ledgerEntries = new ArrayList<>();

    private final Map<String, Authorization> authorizations =
            new HashMap<>();

    public Account(
            String accountId,
            Money openingBalance) {
        this.accountId = accountId;
        this.openingBalance = openingBalance;
    }

    public String getAccountId() {
        return accountId;
    }


    public List<LedgerEntry> getLedgerEntries() {
        return List.copyOf(ledgerEntries);
    }

}