package com.example.ledger.service;

import com.example.ledger.domain.EventType;
import com.example.ledger.domain.LedgerError;
import com.example.ledger.domain.LedgerEvent;
import com.example.ledger.exception.LedgerException;

public final class ReversalService {

    public void validate(
            LedgerEvent reversal,
            LedgerEvent original
    ) {
        if (reversal.getType() != EventType.REVERSAL) {
            throw new LedgerException(
                    LedgerError.EVENT_NOT_FOUND,
                    "Original event not found: "
                            + original.getReversedEventId()
            );
        }

        if (!original.getEventId()
                .equals(reversal.getReversedEventId())) {
            throw new LedgerException(
                    LedgerError.REVERSAL_ACCOUNT_MISMATCH,
                    "Reversal does not reference the supplied original event"
            );
        }

        if (!original.getAccountId()
                .equals(reversal.getAccountId())) {
            throw new LedgerException(
                    LedgerError.REVERSAL_ACCOUNT_MISMATCH,
                    "Reversal account does not match original "
            );
        }

        if (original.getType() != EventType.CREDIT
                && original.getType() != EventType.DEBIT) {
            throw new LedgerException(
                    LedgerError.REVERSAL_NOT_ALLOWED,
                    "Cannot reverse event type: "
                            + original.getType()
            );
        }

        if (!original.getAmount()
                .equals(reversal.getAmount())) {
            throw new LedgerException(
                    LedgerError.REVERSAL_AMOUNT_MISMATCH,
                    "Reversal amount does not match original"
            );
        }

        if (!original.getValueDate()
                .equals(reversal.getValueDate())) {
            throw new LedgerException(
                    LedgerError.REVERSAL_VALUE_DATE_MISMATCH,
                    "Reversal value date does not match original"
            );
        }

    }
}

