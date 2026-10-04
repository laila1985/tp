package com.example.ledger.report;

import com.example.ledger.domain.Money;

import java.time.LocalDate;

public final class FeeAssessment {

    private final String accountId;
    private final Money amount;
    private final String reason;
    private final LocalDate valueDate;

    public FeeAssessment(
            String accountId,
            Money amount,
            String reason,
            LocalDate valueDate) {

        this.accountId = accountId;
        this.amount = amount;
        this.reason = reason;
        this.valueDate = valueDate;
    }

    public String getAccountId() {
        return accountId;
    }

    public Money getAmount() {
        return amount;
    }

    public String getReason() {
        return reason;
    }

    public LocalDate getValueDate() {
        return valueDate;
    }
}
