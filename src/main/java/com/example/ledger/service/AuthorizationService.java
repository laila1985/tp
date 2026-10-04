package com.example.ledger.service;

import com.example.ledger.domain.model.Authorization;
import com.example.ledger.domain.AuthorizationState;
import com.example.ledger.domain.error.LedgerError;
import com.example.ledger.domain.model.Money;
import com.example.ledger.exception.LedgerException;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AuthorizationService {

    private final Map<String, Authorization> authorizations = new LinkedHashMap<>();

    /** Registers an approved authorization (state HOLD). */
    public Authorization create(
            String authorizationId,
            String accountId,
            Money holdAmount
    ) {
        if (authorizations.containsKey(authorizationId)) {
            throw new LedgerException(
                    LedgerError.AUTHORIZATION_ALREADY_EXISTS,
                    "Authorization already exists: " + authorizationId
            );
        }

        Authorization authorization = new Authorization(
                authorizationId,
                accountId,
                holdAmount
        );

        authorizations.put(authorizationId, authorization);

        return authorization;
    }

    /** Records an authorization that was rejected at approval time (state REJECTED, no hold). */
    public Authorization createRejected(
            String authorizationId,
            String accountId,
            Money holdAmount
    ) {
        Authorization authorization = new Authorization(
                authorizationId,
                accountId,
                holdAmount,
                AuthorizationState.REJECTED
        );

        authorizations.put(authorizationId, authorization);

        return authorization;
    }

    public Authorization get(String authorizationId) {
        return authorizations.get(authorizationId);
    }

    public Authorization settle(String authorizationId, String accountId, Money settledAmount) {

        Authorization authorization = authorizations.get(authorizationId);

        if (authorization == null) {
            throw new LedgerException(
                    LedgerError.AUTHORIZATION_NOT_FOUND,
                    "Authorisation not found: " + authorizationId
            );
        }

        if (authorization.getState() != AuthorizationState.HOLD) {
            throw new LedgerException(
                    LedgerError.AUTHORIZATION_INVALID_STATE,
                    "Authorisation " + authorizationId + " is not in HOLD state but "
                            + authorization.getState()
            );
        }

        if (!authorization.getAccountId().equalsIgnoreCase(accountId)) {
            throw new LedgerException(
                    LedgerError.AUTHORIZATION_ACCOUNT_MISMATCH,
                    "Authorization account " + authorization.getAccountId()
                            + " does not match settlement account " + accountId
            );
        }

        if (settledAmount.amount().compareTo(authorization.getHoldAmount().amount()) > 0) {
            throw new LedgerException(
                    LedgerError.SETTLEMENT_AMOUNT_EXCEEDED,
                    "Settlement " + settledAmount + " exceeds hold "
                            + authorization.getHoldAmount()
            );
        }

        authorization.settle(settledAmount);
        return authorization;
    }

    public boolean exists(String authorizationId) {
        return authorizations.containsKey(authorizationId);
    }

    public Collection<Authorization> getAll() {
        return Collections.unmodifiableCollection(
                authorizations.values()
        );
    }
}


