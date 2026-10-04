package com.example.ledger.domain.model;

import com.example.ledger.domain.AuthorizationState;

public final class Authorization {

    private final String authorizationId;
    private final String accountId;
    private final Money holdAmount;
    private Money settledAmount;


    private AuthorizationState state;


    public Authorization(String authorizationId, String accountId, Money holdAmount) {
        this.authorizationId = authorizationId;
        this.accountId = accountId;
        this.holdAmount = holdAmount;
        this.settledAmount = null;
        this.state = AuthorizationState.HOLD;
    }

    public Authorization(String authorizationId, String accountId, Money holdAmount, AuthorizationState state) {
        this.authorizationId = authorizationId;
        this.accountId = accountId;
        this.holdAmount = holdAmount;
        this.settledAmount = null;
        this.state = state;
    }

    public String getAuthorizationId() {
        return authorizationId;
    }

    public String getAccountId() {
        return accountId;
    }

    public Money getHoldAmount() {
        return holdAmount;
    }

    public AuthorizationState getState() {
        return state;
    }


    public void reject() {
        if (state != AuthorizationState.HOLD) {
            throw new IllegalStateException(
                    "Authorization cannot be rejected from state " + state
            );
        }

        state = AuthorizationState.REJECTED;
    }

    public void settle(Money settledAmount) {
        if (state != AuthorizationState.HOLD) {
            throw new IllegalStateException(
                    "Authorization cannot be settled from state " + state
            );
        }

        this.settledAmount = settledAmount;
        this.state = AuthorizationState.SETTLED;
    }
}
