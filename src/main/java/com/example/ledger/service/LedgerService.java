package com.example.ledger.service;

import com.example.ledger.domain.Authorization;
import com.example.ledger.domain.EventType;
import com.example.ledger.domain.LedgerEvent;

import java.util.HashMap;
import java.util.Map;

public final class LedgerService {

    private final Map<String, LedgerEvent> events = new HashMap<>();
    private final AuthorizationService authorizationService;
    private final BalanceService balanceService;
    private final ReversalService reversalService;

    public LedgerService(AuthorizationService authorizationService, BalanceService balanceService, ReversalService reversalService) {
        this.authorizationService = authorizationService;
        this.balanceService = balanceService;
        this.reversalService = reversalService;
    }


    public void process(LedgerEvent event) {

        if (events.containsKey(event.getEventId())) {
            throw new IllegalStateException(
                    "Event already exists: " + event.getEventId()
            );
        }

        switch (event.getType()) {
            case AUTHORIZATION -> processAuthorization(event);
            case SETTLEMENT -> processSettlement(event);
            case REVERSAL -> processReversal(event);
            case CREDIT -> processCredit(event);
            case DEBIT -> processDebit(event);
        }

        events.put(event.getEventId(), event);
    }

    private void processAuthorization(LedgerEvent event) {

        Authorization authorization = authorizationService.create(
                event.getAuthorizationId(),
                event.getAccountId(),
                event.getAmount()
        );
        balanceService.placeHold(
                authorization.getAccountId(),
                authorization.getHoldAmount()
        );
    }

    private void processSettlement(LedgerEvent event) {
        Authorization authorization = authorizationService.settle(event.getAuthorizationId(), event.getAmount());
        // Release the original hold
        balanceService.releaseHold(
                authorization.getAccountId(),
                authorization.getHoldAmount()
        );

        // Apply the actual settled amount
        balanceService.debit(
                authorization.getAccountId(),
                event.getAmount()
        );
    }

    private void processReversal(LedgerEvent event) {

        LedgerEvent originalEvent =
                events.get(event.getReversedEventId());

        if (originalEvent == null) {
            throw new IllegalStateException(
                    "Original event not found: "
                            + event.getReversedEventId()
            );
        }

        // if Amount Debited :
        reversalService.validate(event, originalEvent);
        if (originalEvent.getType() == EventType.CREDIT){
            balanceService.debit(
                    originalEvent.getAccountId(),
                    originalEvent.getAmount()
            );
        }
        // if Amount Credited :
        if (originalEvent.getType() == EventType.DEBIT){
            balanceService.credit(
                    originalEvent.getAccountId(),
                    originalEvent.getAmount()
            );
        }
    }

    private void processCredit(LedgerEvent event) {
        balanceService.credit(
                event.getAccountId(),
                event.getAmount()
        );
    }

    private void processDebit(LedgerEvent event) {
        balanceService.debit(
                event.getAccountId(),
                event.getAmount()
        );
    }


}

