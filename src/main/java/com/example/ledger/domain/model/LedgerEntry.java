package com.example.ledger.domain.model;

import com.example.ledger.domain.Money;

import java.time.LocalDate;

public final class LedgerEntry {

    private final LocalDate valueDate;
    private final Money amount;

    public LedgerEntry(
            LocalDate valueDate,
            Money amount) {

        this.valueDate = valueDate;
        this.amount = amount;
    }

    public LocalDate getValueDate() {
        return valueDate;
    }

    public Money getAmount() {
        return amount;
    }
}
