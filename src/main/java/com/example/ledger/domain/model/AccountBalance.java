package com.example.ledger.domain.model;

public final class AccountBalance {

    private Money ledgerBalance;
    private Money heldAmount;

    public AccountBalance(Money zero) {
        this.ledgerBalance = zero;
        this.heldAmount = zero;
    }

    public AccountBalance(AccountBalance other) {
        this.ledgerBalance = other.ledgerBalance;
        this.heldAmount = other.heldAmount;
    }

    public Money getLedgerBalance() {
        return ledgerBalance;
    }

    public Money getHeldAmount() {
        return heldAmount;
    }

    public Money getAvailableBalance() {
        return ledgerBalance.subtract(heldAmount);
    }

    public void credit(Money amount) {
        ledgerBalance = ledgerBalance.add(amount);
    }

    public void debit(Money amount) {
        ledgerBalance = ledgerBalance.subtract(amount);
    }

    public void placeHold(Money amount) {
        heldAmount = heldAmount.add(amount);
        debit(amount);
    }

    public void releaseHold(Money amount) {
        heldAmount = heldAmount.subtract(amount);
    }

}

