package com.example.ledger;

import com.example.ledger.domain.Currency;
import com.example.ledger.domain.model.Account;
import com.example.ledger.domain.model.LedgerEvent;
import com.example.ledger.domain.model.Money;
import com.example.ledger.report.DailyReportPrinter;
import com.example.ledger.report.LedgerReport;
import com.example.ledger.replay.LedgerReplayEngine;
import com.example.ledger.stream.EventStream;

import java.util.List;

/**
 * Entry point: replays the fixed event stream (E1..E10) and prints the daily
 * report. Run with {@code gradlew run}.
 */
public final class AccountLedgerCore {

    public static void main(String[] args) {
        DailyReportPrinter printer = new DailyReportPrinter();
        printer.print(run());
    }

    /** Registers the accounts, replays the fixed stream and returns the report. */
    public static LedgerReport run() {
        LedgerReplayEngine engine = new LedgerReplayEngine();
        engine.registerAccount(new Account("ACC-001", Money.of("0.00", Currency.AED)));
        engine.registerAccount(new Account("ACC-002", Money.of("0.000", Currency.BHD)));

        List<LedgerEvent> events = EventStream.create();
        return engine.replay(events);
    }
}
