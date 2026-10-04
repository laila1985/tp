package com.example.ledger.report;

import com.example.ledger.domain.Day;
import com.example.ledger.domain.error.LedgerErrorEntry;
import com.example.ledger.domain.model.Account;
import com.example.ledger.domain.model.AccountBalance;
import com.example.ledger.domain.model.Authorization;
import com.example.ledger.domain.model.LedgerEvent;
import com.example.ledger.domain.model.Money;

import java.util.List;
import java.util.Map;

/**
 * Immutable snapshot of everything the daily report needs to print:
 * per-day closing balances, fee assessments, authorization states, errors and
 * interest accruals/capitalization.
 */
public final class LedgerReport {

    private final List<Account> accounts;
    private final List<Day> days;
    private final Map<Day, Map<String, AccountBalance>> dailyBalances;
    private final List<FeeAssessment> fees;
    private final List<Authorization> authorizations;
    private final Map<Day, List<String>> authorizationNotesByDay;
    private final List<LedgerErrorEntry> errors;
    private final Map<Day, List<LedgerErrorEntry>> errorsByDay;
    private final Map<String, Map<Day, Money>> dailyInterest;
    private final Map<String, Money> capitalizedInterest;
    private final Map<Day, List<LedgerEvent>> eventsByBookingDay;

    public LedgerReport(
            List<Account> accounts,
            List<Day> days,
            Map<Day, Map<String, AccountBalance>> dailyBalances,
            List<FeeAssessment> fees,
            List<Authorization> authorizations,
            Map<Day, List<String>> authorizationNotesByDay,
            List<LedgerErrorEntry> errors,
            Map<Day, List<LedgerErrorEntry>> errorsByDay,
            Map<String, Map<Day, Money>> dailyInterest,
            Map<String, Money> capitalizedInterest,
            Map<Day, List<LedgerEvent>> eventsByBookingDay) {
        this.accounts = accounts;
        this.days = days;
        this.dailyBalances = dailyBalances;
        this.fees = fees;
        this.authorizations = authorizations;
        this.authorizationNotesByDay = authorizationNotesByDay;
        this.errors = errors;
        this.errorsByDay = errorsByDay;
        this.dailyInterest = dailyInterest;
        this.capitalizedInterest = capitalizedInterest;
        this.eventsByBookingDay = eventsByBookingDay;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public List<Day> getDays() {
        return days;
    }

    public Map<Day, Map<String, AccountBalance>> getDailyBalances() {
        return dailyBalances;
    }

    public List<FeeAssessment> getFees() {
        return fees;
    }

    public List<Authorization> getAuthorizations() {
        return authorizations;
    }

    public Map<Day, List<String>> getAuthorizationNotesByDay() {
        return authorizationNotesByDay;
    }

    public List<LedgerErrorEntry> getErrors() {
        return errors;
    }

    public Map<Day, List<LedgerErrorEntry>> getErrorsByDay() {
        return errorsByDay;
    }

    public Map<String, Map<Day, Money>> getDailyInterest() {
        return dailyInterest;
    }

    public Map<String, Money> getCapitalizedInterest() {
        return capitalizedInterest;
    }

    public Map<Day, List<LedgerEvent>> getEventsByBookingDay() {
        return eventsByBookingDay;
    }
}
