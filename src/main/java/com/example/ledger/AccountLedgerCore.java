package com.example.ledger;

import com.example.ledger.domain.LedgerEvent;
import com.example.ledger.replay.LedgerReplayEngine;
import com.example.ledger.report.DailyReport;
import com.example.ledger.report.DailyReportPrinter;
import com.example.ledger.stream.EventStream;

import java.util.List;

public class AccountLedgerCore {

    public static void main(String[] args) {
        List<LedgerEvent> events = EventStream.create();

        LedgerReplayEngine engine = new LedgerReplayEngine();

        List<DailyReport> reports = engine.replay(events);

        DailyReportPrinter printer = new DailyReportPrinter();

        printer.print(reports);
    }
}
