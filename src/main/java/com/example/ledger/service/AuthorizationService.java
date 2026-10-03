package com.example.ledger.service;

import com.example.ledger.domain.Authorization;
import com.example.ledger.domain.Money;

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
            throw new IllegalStateException(
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

    public Authorization get(String authorizationId) {
        Authorization authorization = authorizations.get(authorizationId);

        if (authorization == null) {
            throw new IllegalStateException(
                    "Authorization not found: " + authorizationId
            );
        }

        return authorization;
    }


    public Authorization settle(String authorizationId, Money settledAmount) {
        Authorization authorization = authorizations.get(authorizationId);
        authorization.settle(settledAmount);
        return authorization;
    }

    public void reject(String authorizationId) {
        get(authorizationId).reject();
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

