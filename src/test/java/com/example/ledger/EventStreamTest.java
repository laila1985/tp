package com.example.ledger;

import com.example.ledger.domain.Currency;
import com.example.ledger.domain.Day;
import com.example.ledger.domain.EventType;
import com.example.ledger.domain.model.LedgerEvent;
import com.example.ledger.stream.EventStream;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventStreamTest {

    private final List<LedgerEvent> events = EventStream.create();

    private LedgerEvent byId(String id) {
        return events.stream().filter(e -> e.getEventId().equals(id)).findFirst().orElseThrow();
    }

    @Test
    void containsTenEventsInReplayOrder() {
        assertEquals(10, events.size());
        assertEquals(List.of("E1", "E2", "E3", "E4", "E5", "E6", "E7", "E8", "E9", "E10"),
                events.stream().map(LedgerEvent::getEventId).toList());
    }

    @Test
    void e7IsBackValuedToDay2() {
        LedgerEvent e7 = byId("E7");
        assertEquals(Day.DAY_5, e7.getBookingDay());
        assertEquals(Day.DAY_2, e7.getValueDate());
        assertEquals(EventType.DEBIT, e7.getType());
    }

    @Test
    void e9ReversesE7AtE7sValueDate() {
        LedgerEvent e9 = byId("E9");
        assertEquals(EventType.REVERSAL, e9.getType());
        assertEquals("E7", e9.getReversedEventId());
        assertEquals(Day.DAY_2, e9.getValueDate());
    }

    @Test
    void e10IsThreeEqualBhdInstalments() {
        LedgerEvent e10 = byId("E10");
        assertEquals(Currency.BHD, e10.getAmount().getCurrency());
        assertEquals(3, e10.getInstallmentCount());
        assertEquals("10.000", e10.getAmount().amount().toPlainString());
    }
}
