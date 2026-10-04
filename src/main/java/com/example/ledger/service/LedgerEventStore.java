package com.example.ledger.service;

import com.example.ledger.domain.LedgerError;
import com.example.ledger.domain.LedgerEvent;
import com.example.ledger.exception.LedgerException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class LedgerEventStore {

    private final Map<String, LedgerEvent> events =
            new LinkedHashMap<>();

    public void add(LedgerEvent event) {

        if (events.containsKey(event.getEventId())) {
            throw new LedgerException(
                    LedgerError.EVENT_ALREADY_EXISTS,
                    "Event already exists: "
                            + event.getEventId()
            );
        }

        events.put(event.getEventId(), event);
    }

    public LedgerEvent find(String eventId) {
        return events.get(eventId);
    }

    public List<LedgerEvent> findAll() {
        return List.copyOf(events.values());
    }
}
