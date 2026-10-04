package com.example.ledger.exception;

import com.example.ledger.domain.error.LedgerError;

public class LedgerException extends RuntimeException {

    private final LedgerError error;

    public LedgerException(LedgerError error) {
        super(error.name());
        this.error = error;
    }

    public LedgerException(
            LedgerError error,
            String message
    ) {
        super(message);
        this.error = error;
    }

    public LedgerError getError() {
        return error;
    }
}

