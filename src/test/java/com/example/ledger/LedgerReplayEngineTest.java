package com.example.ledger;

import com.example.ledger.domain.Currency;
import com.example.ledger.domain.Day;
import com.example.ledger.domain.EventType;
import com.example.ledger.domain.error.LedgerError;
import com.example.ledger.domain.model.Account;
import com.example.ledger.domain.model.AccountBalance;
import com.example.ledger.domain.model.LedgerEvent;
import com.example.ledger.domain.model.Money;
import com.example.ledger.report.LedgerReport;
import com.example.ledger.replay.LedgerReplayEngine;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LedgerReplayEngineTest {

    private static LedgerReplayEngine engine() {
        LedgerReplayEngine engine = new LedgerReplayEngine();
        engine.registerAccount(new Account("ACC-001", Money.of("0.00", Currency.AED)));
        engine.registerAccount(new Account("ACC-002", Money.of("0.000", Currency.BHD)));
        return engine;
    }

    private static LedgerEvent event(String id, EventType type, String accountId, String amount,
                                     Day valueDate, String authId, String reversedId) {
        return new LedgerEvent(id, Day.DAY_1, type, accountId, Money.of(amount, Currency.AED),
                valueDate, authId, reversedId);
    }

    @Test
    void rejectsUnknownAccount() {
        LedgerReport report = engine().replay(List.of(
                event("X1", EventType.CREDIT, "ACC-999", "10.00", Day.DAY_1, null, null)));

        assertEquals(1, report.getErrors().size());
        assertEquals(LedgerError.UNKNOWN_ACCOUNT, report.getErrors().get(0).getError());
    }

    @Test
    void rejectsDuplicateEventId() {
        LedgerEvent e1 = event("E1", EventType.CREDIT, "ACC-001", "100.00", Day.DAY_1, null, null);
        LedgerReport report = engine().replay(List.of(e1, e1));

        assertEquals(1, report.getErrors().size());
        assertEquals(LedgerError.EVENT_ALREADY_EXISTS, report.getErrors().get(0).getError());
        assertEquals(Money.of("100.00", Currency.AED),
                report.getDailyBalances().get(Day.DAY_1).get("ACC-001").getLedgerBalance());
    }

    @Test
    void holdReducesAvailableBalanceNotLedgerBalance() {
        LedgerReport report = engine().replay(List.of(
                event("E1", EventType.CREDIT, "ACC-001", "200.00", Day.DAY_1, null, null),
                event("E2", EventType.AUTHORIZATION, "ACC-001", "50.00", Day.DAY_1, "Auth-A", null)));

        AccountBalance balance = report.getDailyBalances().get(Day.DAY_1).get("ACC-001");
        assertEquals(Money.of("200.00", Currency.AED), balance.getLedgerBalance());
        assertEquals(Money.of("50.00", Currency.AED), balance.getHeldAmount());
        assertEquals(Money.of("150.00", Currency.AED), balance.getAvailableBalance());
    }

    @Test
    void authorizationIsRejectedWhenAvailableBalanceWouldGoNegative() {
        LedgerReport report = engine().replay(List.of(
                event("E1", EventType.CREDIT, "ACC-001", "100.00", Day.DAY_1, null, null),
                event("E2", EventType.AUTHORIZATION, "ACC-001", "150.00", Day.DAY_1, "Auth-X", null)));

        AccountBalance balance = report.getDailyBalances().get(Day.DAY_1).get("ACC-001");
        assertEquals(Money.of("0.00", Currency.AED), balance.getHeldAmount());
        assertTrue(report.getErrors().stream().anyMatch(e -> e.getError() == LedgerError.INSUFFICIENT_BALANCE));
    }
}
