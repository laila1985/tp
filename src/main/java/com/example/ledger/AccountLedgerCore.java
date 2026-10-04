package com.example.ledger;

import com.example.ledger.domain.model.AccountBalance;
import com.example.ledger.domain.Day;
import com.example.ledger.domain.error.LedgerErrorEntry;
import com.example.ledger.domain.model.LedgerEvent;
import com.example.ledger.report.DailyReportPrinter;
import com.example.ledger.replay.LedgerReplayEngine;
import com.example.ledger.stream.EventStream;

import java.util.*;

public class AccountLedgerCore {

    private static final Map<Day, Map<String, AccountBalance>> dailyBalances =
            new HashMap<>();

    private static final List<LedgerErrorEntry> failedEvents = new ArrayList<>();

    public static void main(String[] args) {
        List<LedgerEvent> events = EventStream.create();

        LedgerReplayEngine engine = new LedgerReplayEngine(dailyBalances, failedEvents);

        engine.replay(events);

        DailyReportPrinter printer = new DailyReportPrinter();

        printer.print(dailyBalances);
        printer.print(failedEvents);
    }
}
