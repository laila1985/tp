package com.example.ledger.service;

import com.example.ledger.domain.EventType;
import com.example.ledger.domain.LedgerEvent;

public final class ReversalService {

    public void validate(
            LedgerEvent reversal,
            LedgerEvent original
    ) {
        if (reversal.getType() != EventType.REVERSAL) {
            throw new IllegalStateException(
                    "Event is not a reversal"
            );
        }

        if (!original.getEventId()
                .equals(reversal.getReversedEventId())) {
            throw new IllegalStateException(
                    "Reversal does not reference the supplied original event"
            );
        }

        if (!original.getAccountId()
                .equals(reversal.getAccountId())) {
            throw new IllegalStateException(
                    "Reversal account does not match original"
            );
        }

        if (original.getType() != EventType.CREDIT
                && original.getType() != EventType.DEBIT) {
            throw new IllegalStateException(
                    "Cannot reverse event type: "
                            + original.getType()
            );
        }

        if (!original.getAmount()
                .equals(reversal.getAmount())) {
            throw new IllegalStateException(
                    "Reversal amount does not match original"
            );
        }

        if (!original.getValueDate()
                .equals(reversal.getValueDate())) {
            throw new IllegalStateException(
                    "Reversal value date does not match original"
            );
        }

    }
}

