package com.example.ledger.report;

import com.example.ledger.domain.Day;
import com.example.ledger.domain.error.LedgerErrorEntry;
import com.example.ledger.domain.model.Account;
import com.example.ledger.domain.model.AccountBalance;
import com.example.ledger.domain.model.Authorization;
import com.example.ledger.domain.model.LedgerEvent;
import com.example.ledger.domain.model.Money;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Renders a {@link LedgerReport} to stdout: per day it prints the closing
 * ledger balance, fee assessments, authorization states and errors, followed by
 * whole-window summaries for authorizations, fees, interest and errors.
 */
public final class DailyReportPrinter {

    private static final String LINE = "============================================================";
    private static final String THIN = "------------------------------------------------------------";

    public void print(LedgerReport report) {

        System.out.println();
        System.out.println(LINE);
        System.out.println("            IN-MEMORY ACCOUNT LEDGER - DAILY REPORT");
        System.out.println(LINE);
        System.out.println("Window   : Day 1 .. Day 6");
        System.out.println("Accounts :");
        for (Account account : report.getAccounts()) {
            System.out.printf("    %-8s %-4s opening balance %s%n",
                    account.getAccountId(),
                    account.getCurrency(),
                    account.getOpeningBalance().amount());
        }

        for (Day day : report.getDays()) {
            printDay(report, day);
        }

        printAuthorizations(report);
        printFees(report);
        printInterest(report);
        printErrors(report);

        System.out.println();
        System.out.println(LINE);
        System.out.println("                        END OF REPORT");
        System.out.println(LINE);
    }

    private void printDay(LedgerReport report, Day day) {

        System.out.println();
        System.out.println(LINE);
        System.out.println("DAY " + day.getNumber());
        System.out.println(LINE);
        System.out.printf("%-12s %-18s %-16s %-16s%n", "ACCOUNT", "CLOSING LEDGER", "HELD", "AVAILABLE");
        System.out.println(THIN);

        Map<String, AccountBalance> balances = report.getDailyBalances().get(day);
        for (Map.Entry<String, AccountBalance> entry : balances.entrySet()) {
            AccountBalance balance = entry.getValue();
            System.out.printf("%-12s %-18s %-16s %-16s%n",
                    entry.getKey(),
                    balance.getLedgerBalance(),
                    balance.getHeldAmount(),
                    balance.getAvailableBalance());
        }
        System.out.println(THIN);

        List<FeeAssessment> dayFees = new ArrayList<>();
        for (FeeAssessment fee : report.getFees()) {
            if (fee.getDay() == day) {
                dayFees.add(fee);
            }
        }
        if (dayFees.isEmpty()) {
            System.out.println("Fee assessed  : none");
        } else {
            for (FeeAssessment fee : dayFees) {
                System.out.println("Fee assessed  : " + fee.getAccountId() + " " + fee.getAmount()
                        + " (value date Day " + fee.getDay().getNumber() + "; " + fee.getReason() + ")");
            }
        }

        List<String> notes = report.getAuthorizationNotesByDay().getOrDefault(day, List.of());
        System.out.println("Auth states   : " + (notes.isEmpty() ? "none" : String.join(" | ", notes)));

        List<LedgerEvent> events = report.getEventsByBookingDay().getOrDefault(day, List.of());
        System.out.println("Events booked : " + (events.isEmpty() ? "none" : joinEventIds(events)));

        List<LedgerErrorEntry> dayErrors = report.getErrorsByDay().getOrDefault(day, List.of());
        if (dayErrors.isEmpty()) {
            System.out.println("Errors        : none");
        } else {
            for (LedgerErrorEntry error : dayErrors) {
                System.out.println("Errors        : " + error.getEventId() + " "
                        + error.getError() + " - " + error.getMessage());
            }
        }
    }

    private void printAuthorizations(LedgerReport report) {
        System.out.println();
        System.out.println(LINE);
        System.out.println("AUTHORIZATION STATES");
        System.out.println(LINE);
        if (report.getAuthorizations().isEmpty()) {
            System.out.println("No authorizations recorded.");
            System.out.println(THIN);
            return;
        }
        System.out.printf("%-12s %-12s %-18s %-12s%n", "AUTH ID", "ACCOUNT", "HOLD", "STATE");
        System.out.println(THIN);
        for (Authorization authorization : report.getAuthorizations()) {
            System.out.printf("%-12s %-12s %-18s %-12s%n",
                    authorization.getAuthorizationId(),
                    authorization.getAccountId(),
                    authorization.getHoldAmount(),
                    authorization.getState());
        }
        System.out.println(THIN);
    }

    private void printFees(LedgerReport report) {
        System.out.println();
        System.out.println(LINE);
        System.out.println("OVERDRAFT FEE ASSESSMENTS");
        System.out.println(LINE);
        if (report.getFees().isEmpty()) {
            System.out.println("No overdraft fees assessed.");
            System.out.println(THIN);
            return;
        }
        System.out.printf("%-12s %-8s %-16s %-22s%n", "ACCOUNT", "DAY", "FEE", "BALANCE BEFORE FEE");
        System.out.println(THIN);
        for (FeeAssessment fee : report.getFees()) {
            System.out.printf("%-12s %-8s %-16s %-22s%n",
                    fee.getAccountId(),
                    "Day " + fee.getDay().getNumber(),
                    fee.getAmount(),
                    fee.getBalanceBeforeFee());
        }
        System.out.println(THIN);
        System.out.println("Total fee assessments: " + report.getFees().size());
    }

    private void printInterest(LedgerReport report) {
        System.out.println();
        System.out.println(LINE);
        System.out.println("DAILY INTEREST (0.04% per day, positive closing balances)");
        System.out.println(LINE);
        for (Map.Entry<String, Map<Day, Money>> entry : report.getDailyInterest().entrySet()) {
            String accountId = entry.getKey();
            System.out.println("Account " + accountId);
            Money sum = null;
            for (Map.Entry<Day, Money> accrual : entry.getValue().entrySet()) {
                System.out.printf("    %-6s accrual %s%n", "Day " + accrual.getKey().getNumber(), accrual.getValue());
                sum = (sum == null) ? accrual.getValue() : sum.add(accrual.getValue());
            }
            Money capitalized = report.getCapitalizedInterest().get(accountId);
            System.out.println("    Sum of rounded daily accruals : " + sum);
            System.out.println("    Capitalised on Day 6          : " + capitalized);
            System.out.println("    Accruals == capitalised total : " + sum.equals(capitalized));
            System.out.println(THIN);
        }
    }

    private void printErrors(LedgerReport report) {
        System.out.println();
        System.out.println(LINE);
        System.out.println("LEDGER ERRORS / REJECTED EVENTS");
        System.out.println(LINE);
        if (report.getErrors().isEmpty()) {
            System.out.println("No errors recorded.");
            System.out.println(THIN);
            return;
        }
        System.out.printf("%-8s %-30s %s%n", "EVENT", "ERROR TYPE", "MESSAGE");
        System.out.println(THIN);
        for (LedgerErrorEntry error : report.getErrors()) {
            System.out.printf("%-8s %-30s %s%n", error.getEventId(), error.getError(), error.getMessage());
        }
        System.out.println(THIN);
        System.out.println("Total errors: " + report.getErrors().size());
    }

    private String joinEventIds(List<LedgerEvent> events) {
        StringBuilder builder = new StringBuilder();
        for (LedgerEvent event : events) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(event.getEventId());
        }
        return builder.toString();
    }
}
