package com.example.ledger.report;

import com.example.ledger.domain.Day;
import com.example.ledger.domain.model.Money;

/**
 * A single overdraft-fee assessment.
 *
 * <p>{@code day} is the day assessed, which is also the value date the fee is
 * booked with. {@code balanceBeforeFee} is the closing ledger balance that
 * triggered the assessment (i.e. the balance evaluated before the fee itself
 * was posted).</p>
 */
public final class FeeAssessment {

    private final String accountId;
    private final Day day;
    private final Money amount;
    private final Money balanceBeforeFee;
    private final String reason;

    public FeeAssessment(
            String accountId,
            Day day,
            Money amount,
            Money balanceBeforeFee,
            String reason) {
        this.accountId = accountId;
        this.day = day;
        this.amount = amount;
        this.balanceBeforeFee = balanceBeforeFee;
        this.reason = reason;
    }

    public String getAccountId() {
        return accountId;
    }

    public Day getDay() {
        return day;
    }

    public Money getAmount() {
        return amount;
    }

    public Money getBalanceBeforeFee() {
        return balanceBeforeFee;
    }

    public String getReason() {
        return reason;
    }
}
