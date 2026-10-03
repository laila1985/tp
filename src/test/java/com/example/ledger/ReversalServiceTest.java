package com.example.ledger;


import com.example.ledger.domain.EventType;
import com.example.ledger.domain.LedgerEvent;
import com.example.ledger.domain.Money;
import com.example.ledger.domain.Currency;
import com.example.ledger.domain.Day;
import com.example.ledger.service.ReversalService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ReversalServiceTest {

    private final ReversalService service = new ReversalService();


    @Test
    void shouldValidateCreditReversal() {

        LedgerEvent original = event(
                "E1",
                EventType.CREDIT,
                "ACC-001",
                "100.00",
                Day.DAY_1
        );

        LedgerEvent reversal = reversal(
                "E2",
                "E1",
                "100.00",
                Day.DAY_1
        );

        assertDoesNotThrow(() ->
                service.validate(reversal, original)
        );
    }

    @Test
    void shouldValidateDebitReversal() {

        LedgerEvent original = event(
                "E1",
                EventType.DEBIT,
                "ACC-001",
                "100.00",
                Day.DAY_1
        );

        LedgerEvent reversal = reversal(
                "E2",
                "E1",
                "100.00",
                Day.DAY_1
        );

        assertDoesNotThrow(() ->
                service.validate(reversal, original)
        );
    }

    @Test
    void shouldRejectAuthorizationReversal() {

        LedgerEvent original = new LedgerEvent(
                "E1",
                Day.DAY_1,
                EventType.AUTHORIZATION,
                "ACC-001",
                money("100.00"),
                Day.DAY_1,
                "Auth-A",
                null
        );

        LedgerEvent reversal = reversal(
                "E2",
                "E1",
                "100.00",
                Day.DAY_1
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.validate(reversal, original)
                );

        assertEquals(
                "Cannot reverse event type: AUTHORIZATION",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectNonReversalEvent() {

        LedgerEvent original = event(
                "E1",
                EventType.DEBIT,
                "ACC-001",
                "100.00",
                Day.DAY_1
        );

        LedgerEvent notReversal = event(
                "E2",
                EventType.CREDIT,
                "ACC-001",
                "100.00",
                Day.DAY_1
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.validate(
                                notReversal,
                                original
                        )
                );

        assertEquals(
                "Event is not a reversal",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectWhenOriginalEventIsNotReferenced() {

        LedgerEvent original = event(
                "E1",
                EventType.DEBIT,
                "ACC-001",
                "100.00",
                Day.DAY_1
        );

        LedgerEvent reversal = reversal(
                "E2",
                "E999",
                "100.00",
                Day.DAY_1
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.validate(
                                reversal,
                                original
                        )
                );

        assertEquals(
                "Reversal does not reference the supplied original event",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectAccountMismatch() {

        LedgerEvent original = event(
                "E1",
                EventType.DEBIT,
                "ACC-001",
                "100.00",
                Day.DAY_1
        );

        LedgerEvent reversal = new LedgerEvent(
                "E2",
                Day.DAY_2,
                EventType.REVERSAL,
                "ACC-002",
                money("100.00"),
                Day.DAY_1,
                null,
                "E1"
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.validate(
                                reversal,
                                original
                        )
                );

        assertEquals(
                "Reversal account does not match original",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectUnsupportedOriginalEventType() {

        LedgerEvent original = new LedgerEvent(
                "E1",
                Day.DAY_1,
                EventType.SETTLEMENT,
                "ACC-001",
                money("100.00"),
                Day.DAY_1,
                "Auth-A",
                null
        );

        LedgerEvent reversal = reversal(
                "E2",
                "E1",
                "100.00",
                Day.DAY_1
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.validate(
                                reversal,
                                original
                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains("Cannot reverse event type")
        );
    }

    @Test
    void shouldRejectAmountMismatch() {

        LedgerEvent original = event(
                "E1",
                EventType.DEBIT,
                "ACC-001",
                "100.00",
                Day.DAY_1
        );

        LedgerEvent reversal = reversal(
                "E2",
                "E1",
                "90.00",
                Day.DAY_1
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.validate(
                                reversal,
                                original
                        )
                );

        assertEquals(
                "Reversal amount does not match original",
                exception.getMessage()
        );
    }

    @Test
    void shouldRejectValueDateMismatch() {

        LedgerEvent original = event(
                "E1",
                EventType.DEBIT,
                "ACC-001",
                "100.00",
                Day.DAY_2
        );

        LedgerEvent reversal = reversal(
                "E2",
                "E1",
                "100.00",
                Day.DAY_1
        );

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> service.validate(
                                reversal,
                                original
                        )
                );

        assertEquals(
                "Reversal value date does not match original",
                exception.getMessage()
        );
    }

    private LedgerEvent event(
            String id,
            EventType type,
            String accountId,
            String amount,
            Day valueDate
    ) {
        return new LedgerEvent(
                id,
                Day.DAY_1,
                type,
                accountId,
                money(amount),
                valueDate,
                null,
                null
        );
    }

    private LedgerEvent reversal(
            String id,
            String originalId,
            String amount,
            Day valueDate
    ) {
        return new LedgerEvent(
                id,
                Day.DAY_2,
                EventType.REVERSAL,
                "ACC-001",
                money(amount),
                valueDate,
                null,
                originalId
        );
    }

    private Money money(String amount) {
        return new Money(
                Currency.AED,
                new BigDecimal(amount)
        );
    }
}

