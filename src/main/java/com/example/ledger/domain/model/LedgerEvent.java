package com.example.ledger.domain.model;

import com.example.ledger.domain.Day;
import com.example.ledger.domain.EventType;

public final class LedgerEvent {

    private final String eventId;
    private final Day bookingDay;
    private final EventType type;
    private final String accountId;
    private final Money amount;
    private final Day valueDate;

    // authorizationId for AUTHORIZATION / SETTLEMENT
    // reversedEventId for REVERSAL
    private final String authorizationId;
    private final String reversedEventId;

    public LedgerEvent(String eventId, Day bookingDay, EventType type, String accountId, Money amount, Day valueDate, String authorizationId, String reversedEventId) {
        this.eventId = eventId;
        this.bookingDay = bookingDay;
        this.type = type;
        this.accountId = accountId;
        this.amount = amount;
        this.valueDate = valueDate;
        this.authorizationId = authorizationId;
        this.reversedEventId = reversedEventId;
    }

    public String getEventId() {
        return eventId;
    }

    public Day getBookingDay() {
        return bookingDay;
    }

    public EventType getType() {
        return type;
    }

    public String getAccountId() {
        return accountId;
    }

    public Money getAmount() {
        return amount;
    }

    public Day getValueDate() {
        return valueDate;
    }

    public String getAuthorizationId() {
        return authorizationId;
    }

    public String getReversedEventId() {
        return reversedEventId;
    }
}
