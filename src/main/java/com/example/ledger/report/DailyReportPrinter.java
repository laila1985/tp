package com.example.ledger.report;

import com.example.ledger.domain.model.AccountBalance;
import com.example.ledger.domain.Day;
import com.example.ledger.domain.error.LedgerErrorEntry;

import java.util.List;
import java.util.Map;

public final class DailyReportPrinter {


    public void print(List<LedgerErrorEntry> ledgerErrorEntries) {

        System.out.println();
        System.out.println("============================================================");
        System.out.println("                    LEDGER ERROR ENTRIES");
        System.out.println("============================================================");

        if (ledgerErrorEntries == null || ledgerErrorEntries.isEmpty()) {
            System.out.println();
            System.out.println(" No errors recorded.");
            System.out.println("============================================================");
            return;
        }

        System.out.println();
        System.out.printf(
                "%-15s %-25s %s%n",
                "Event ID",
                "Error Type",
                "Message"
        );

        System.out.println("------------------------------------------------------------");

        for (LedgerErrorEntry entry : ledgerErrorEntries) {
            System.out.printf(
                    "%-15s %-25s %s%n",
                    entry.getEventId(),
                    entry.getError(),
                    entry.getMessage()
            );
        }

        System.out.println("------------------------------------------------------------");
        System.out.printf("Total errors: %d%n", ledgerErrorEntries.size());
        System.out.println("============================================================");
    }

    public void print(Map<Day, Map<String, AccountBalance>> reports) {

        System.out.println();
        System.out.println("============================================================");
        System.out.println("                    DAILY LEDGER REPORT");
        System.out.println("============================================================");

        for (Map.Entry<Day, Map<String, AccountBalance>> dayEntry
                : reports.entrySet()) {

            Day day = dayEntry.getKey();
            Map<String, AccountBalance> balances = dayEntry.getValue();

            System.out.println();
            System.out.println("------------------------------------------------------------");
            System.out.println(" " + day);
            System.out.println("------------------------------------------------------------");

            System.out.printf(
                    "%-15s %-15s %-15s%n",
                    "ACCOUNT",
                    "Available Amount",
                    "Held Amount"
            );

            System.out.println("------------------------------------------------------------");

            for (Map.Entry<String, AccountBalance> accountEntry
                    : balances.entrySet()) {

                String accountId = accountEntry.getKey();
                AccountBalance balance = accountEntry.getValue();

                System.out.printf(
                        "%-15s %-15s %-15s%n",
                        accountId,
                        balance.getAvailableBalance(),
                        balance.getHeldAmount()
                );
            }

            System.out.println("------------------------------------------------------------");
        }

        System.out.println();
        System.out.println("============================================================");
        System.out.println("                    END OF REPORT");
        System.out.println("============================================================");
    }
}