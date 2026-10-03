package com.example.ledger.service;

import com.example.ledger.domain.EventType;
import com.example.ledger.domain.LedgerEvent;

public final class ReversalService {

    public void validate(
            LedgerEvent reversal,
            LedgerEvent original
    ) {
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

