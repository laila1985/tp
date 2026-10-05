package com.example.ledger.domain.model;

/**
 * Value object describing the state of one account at a point in time.
 *
 * <p>The ledger balance is the sum of posted entries. Holds never change the
 * ledger balance; they only affect the available balance, which is
 * {@code ledgerBalance - heldAmount}.</p>
 */
public final class AccountBalance {

    private Money ledgerBalance;
    private Money heldAmount;
    private Money availableAmount;

    /** Creates a balance whose ledger equals the supplied opening balance and has no holds. */
    public AccountBalance(Money openingBalance) {
        this.ledgerBalance = openingBalance;
        this.heldAmount = Money.zero(openingBalance.getCurrency());
        this.availableAmount = Money.zero(openingBalance.getCurrency());
    }

    public AccountBalance(Money ledgerBalance, Money heldAmount, Money availableAmount) {
        this.ledgerBalance = ledgerBalance;
        this.heldAmount = heldAmount;
        this.availableAmount =availableAmount;
    }

    public AccountBalance(AccountBalance other) {
        this(other.ledgerBalance, other.heldAmount, other.availableAmount);
    }

    public Money getLedgerBalance() {
        return ledgerBalance;
    }

    public Money getHeldAmount() {
        return heldAmount;
    }

    /** Available balance = ledger balance minus active holds. Never mutates this object. */
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
    }

    public void releaseHold(Money amount) {
        heldAmount = heldAmount.subtract(amount);
    }
}

