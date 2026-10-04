package com.example.ledger.domain.error;

public final class LedgerErrorEntry {

    private final String eventId;
    private final LedgerError error;
    private final String message;

    public LedgerErrorEntry(
            String eventId,
            LedgerError error,
            String message
    ) {
        this.eventId = eventId;
        this.error = error;
        this.message = message;
    }

    public String getEventId() {
        return eventId;
    }

    public LedgerError getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }
}

