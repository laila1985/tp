package com.example.ledger.service;

import com.example.ledger.domain.model.Authorization;
import com.example.ledger.domain.AuthorizationState;
import com.example.ledger.domain.error.LedgerError;
import com.example.ledger.domain.model.Money;
import com.example.ledger.exception.LedgerException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class AuthorizationService {

    private final Map<String, Authorization> authorizations = new HashMap<>();

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

    public Authorization getAuthorisaction(String authorizationId) {
        Authorization authorization = authorizations.get(authorizationId);

        if (authorization == null) {
            throw new IllegalStateException(
                    "Authorization not found: " + authorizationId
            );
        }

        return authorization;
    }


    public Authorization settle(String authorizationId, String accountId, Money settledAmount) {

        if (!exists(authorizationId)){
            reject(authorizationId, accountId, settledAmount);
            throw new LedgerException(
                    LedgerError.AUTHORIZATION_NOT_FOUND,
                    "Authorisation not found: "
                            + authorizationId
            );
        }

        Authorization authorization =getAuthorisaction(authorizationId);
        if (authorization.getState() != AuthorizationState.HOLD){
            reject(authorizationId, accountId, settledAmount);
            throw new LedgerException(
                    LedgerError.AUTHORIZATION_INVALID_STATE,
                    "Authorisation State : "
                            + authorization.getState()
            );
        }
        if (!authorization.getAccountId().equalsIgnoreCase(accountId)){
            reject(authorizationId, accountId, settledAmount);
            throw new LedgerException(
                    LedgerError.AUTHORIZATION_ACCOUNT_MISMATCH,
                    "Original account Id: " + authorization.getAccountId()+
                            " Current Account Id : " + accountId
            );
        }

        authorization.settle(settledAmount);
        return authorization;
    }

    public void reject(String authorizationId, String accountId, Money settledAmount) {
        Authorization authorization = new Authorization(
                authorizationId,
                accountId,
                settledAmount
        );

        authorizations.put(authorizationId, authorization);
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

