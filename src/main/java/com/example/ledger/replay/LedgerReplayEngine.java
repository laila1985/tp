package com.example.ledger.replay;

import com.example.ledger.domain.Currency;
import com.example.ledger.domain.Day;
import com.example.ledger.domain.error.LedgerError;
import com.example.ledger.domain.error.LedgerErrorEntry;
import com.example.ledger.domain.model.Account;
import com.example.ledger.domain.model.AccountBalance;
import com.example.ledger.domain.model.Authorization;
import com.example.ledger.domain.model.LedgerEvent;
import com.example.ledger.domain.model.Money;
import com.example.ledger.exception.LedgerException;
import com.example.ledger.report.FeeAssessment;
import com.example.ledger.report.LedgerReport;
import com.example.ledger.service.AuthorizationService;
import com.example.ledger.service.BalanceServiceImp;
import com.example.ledger.service.FeeService;
import com.example.ledger.service.InstallmentService;
import com.example.ledger.service.InterestService;
import com.example.ledger.service.LedgerEventStore;
import com.example.ledger.service.ReversalService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Replays the fixed event stream (E1..E10) and produces a {@link LedgerReport}.
 *
 * <p>Modelling decisions (also documented in AMBIGUITIES.md):</p>
 * <ul>
 *   <li>Holds never change the ledger balance, only the available balance.</li>
 *   <li>Closing balances are value-date based: the closing balance of Day d is
 *       the sum of every posted entry whose value date is &le; d.</li>
 *   <li>Overdraft fees are assessed once per account per day, at the value date
 *       of the day whose closing balance first turns negative. A retroactive
 *       (back-valued) event can therefore trigger a fee for a past day, and a
 *       fee is never reversed by later events.</li>
 *   <li>Interest is evaluated on the final closing balances (after every event,
 *       including retroactive ones) and capitalised as a single Day 6 credit
 *       equal to the sum of the rounded daily accruals.</li>
 * </ul>
 */
public final class LedgerReplayEngine {

    private static final int WINDOW_DAYS = 6;

    private final Map<String, Account> accounts = new LinkedHashMap<>();

    private final BalanceServiceImp balanceService = new BalanceServiceImp();
    private final AuthorizationService authorizationService = new AuthorizationService();
    private final FeeService feeService = new FeeService();
    private final InterestService interestService = new InterestService();
    private final InstallmentService installmentService = new InstallmentService();
    private final ReversalService reversalService = new ReversalService();
    private final LedgerEventStore eventStore = new LedgerEventStore();

    // Append-only, value-dated history.
    private final Map<String, List<Posting>> postingsByAccount = new LinkedHashMap<>();
    private final Map<String, List<Posting>> holdChangesByAccount = new LinkedHashMap<>();

    private final Map<String, Map<Day, FeeAssessment>> feesByAccount = new LinkedHashMap<>();
    private final Map<String, Map<Day, Money>> interestByAccount = new LinkedHashMap<>();
    private final Map<String, Money> capitalizedInterest = new LinkedHashMap<>();
    private final Map<Day, List<String>> authorizationNotesByDay = new LinkedHashMap<>();
    private final Map<Day, List<LedgerEvent>> eventsByBookingDay = new LinkedHashMap<>();
    private final Map<Day, List<LedgerErrorEntry>> errorsByDay = new LinkedHashMap<>();
    private final List<LedgerErrorEntry> errors = new ArrayList<>();

    private int maxBookingDay = 0;

    public void registerAccount(Account account) {
        accounts.put(account.getAccountId(), account);
        balanceService.getOrCreate(account.getAccountId(), account.getCurrency());
    }

    public LedgerReport replay(List<LedgerEvent> events) {
        for (LedgerEvent event : events) {
            try {
                process(event);
            } catch (LedgerException e) {
                LedgerErrorEntry entry = new LedgerErrorEntry(event.getEventId(), e.getError(), e.getMessage());
                errors.add(entry);
                errorsByDay.computeIfAbsent(event.getBookingDay(), d -> new ArrayList<>()).add(entry);
            }
        }
        accrueAndCapitaliseInterest();
        return buildReport();
    }

    /**
     * Processes a single event. Throws {@link LedgerException} when the event is
     * rejected; a rejected event never changes the ledger.
     */
    public void process(LedgerEvent event) {
        if (!accounts.containsKey(event.getAccountId())) {
            throw new LedgerException(LedgerError.UNKNOWN_ACCOUNT, "Unknown account: " + event.getAccountId());
        }
        if (eventStore.find(event.getEventId()) != null) {
            throw new LedgerException(LedgerError.EVENT_ALREADY_EXISTS, "Event already exists: " + event.getEventId());
        }

        switch (event.getType()) {
            case CREDIT -> processCredit(event);
            case DEBIT -> processDebit(event);
            case AUTHORIZATION -> processAuthorization(event);
            case SETTLEMENT -> processSettlement(event);
            case REVERSAL -> processReversal(event);
        }

        eventStore.add(event);
        eventsByBookingDay.computeIfAbsent(event.getBookingDay(), d -> new ArrayList<>()).add(event);
        maxBookingDay = Math.max(maxBookingDay, event.getBookingDay().getNumber());
        assessOverdraftFees();
    }

    private void processCredit(LedgerEvent event) {
        int count = Math.max(1, event.getInstallmentCount());
        List<Money> parts = count > 1
                ? installmentService.splitIntoEqualInstallments(event.getAmount(), count)
                : List.of(event.getAmount());
        for (Money part : parts) {
            post(event.getAccountId(), event.getValueDate(), part, event.getEventId());
        }
    }

    private void processDebit(LedgerEvent event) {
        // Debits are always posted, even into overdraft: overdrafting is what
        // triggers the overdraft fee. Insufficient-funds rejection only applies
        // to authorizations.
        post(event.getAccountId(), event.getValueDate(), event.getAmount().negate(), event.getEventId());
    }

    private void processAuthorization(LedgerEvent event) {
        AccountBalance balance = balanceService.get(event.getAccountId());
        Money availableAfterHold = balance.getAvailableBalance().subtract(event.getAmount());

        if (availableAfterHold.isNegative()) {
            authorizationService.createRejected(event.getAuthorizationId(), event.getAccountId(), event.getAmount());
            noteAuthorization(event.getBookingDay(), event.getAuthorizationId()
                    + ": REJECTED (available " + balance.getAvailableBalance()
                    + " < hold " + event.getAmount() + ")");
            throw new LedgerException(
                    LedgerError.INSUFFICIENT_BALANCE,
                    "Authorization " + event.getAuthorizationId() + " rejected: available balance "
                            + balance.getAvailableBalance() + " would fall below zero");
        }

        Authorization authorization = authorizationService.create(
                event.getAuthorizationId(), event.getAccountId(), event.getAmount());
        changeHold(event.getAccountId(), event.getValueDate(), event.getAmount(), event.getEventId());
        noteAuthorization(event.getBookingDay(), authorization.getAuthorizationId()
                + ": HOLD (hold " + event.getAmount() + ")");
    }

    private void processSettlement(LedgerEvent event) {
        Authorization authorization = authorizationService.settle(
                event.getAuthorizationId(), event.getAccountId(), event.getAmount());

        // Release the full original hold on the settlement value date ...
        changeHold(authorization.getAccountId(), event.getValueDate(),
                authorization.getHoldAmount().negate(), event.getEventId());

        // ... and post the actual settled amount as a debit.
        post(authorization.getAccountId(), event.getValueDate(),
                event.getAmount().negate(), event.getEventId());

        noteAuthorization(event.getBookingDay(), authorization.getAuthorizationId()
                + ": SETTLED (settled " + event.getAmount() + ")");
    }

    private void processReversal(LedgerEvent event) {
        LedgerEvent original = eventStore.find(event.getReversedEventId());
        if (original == null) {
            throw new LedgerException(LedgerError.EVENT_NOT_FOUND,
                    "Original event not found: " + event.getReversedEventId());
        }

        reversalService.validate(event, original);

        List<Posting> toReverse = new ArrayList<>();
        for (Posting posting : postingsByAccount.getOrDefault(original.getAccountId(), List.of())) {
            if (posting.label.equals(original.getEventId())) {
                toReverse.add(posting);
            }
        }
        for (Posting posting : toReverse) {
            post(original.getAccountId(), posting.valueDate, posting.amount.negate(), event.getEventId());
        }
    }

    private void assessOverdraftFees() {
        for (String accountId : accounts.keySet()) {
            Map<Day, FeeAssessment> assessed = feesByAccount.computeIfAbsent(accountId, k -> new LinkedHashMap<>());
            for (int n = 1; n <= maxBookingDay; n++) {
                Day day = Day.of(n);
                if (assessed.containsKey(day)) {
                    continue;
                }
                Money closing = closingBalance(accountId, day);
                Money fee = feeService.calculateOverdraftFee(closing, false);
                if (fee.isPositive()) {
                    assessed.put(day, new FeeAssessment(accountId, day, fee, closing,
                            "closing ledger balance " + closing + " was negative"));
                    post(accountId, day, fee.negate(), "FEE-" + accountId + "-" + day);
                }
            }
        }
    }

    private void accrueAndCapitaliseInterest() {
        for (String accountId : accounts.keySet()) {
            Currency currency = accounts.get(accountId).getCurrency();
            Map<Day, Money> daily = new LinkedHashMap<>();
            List<Money> accruals = new ArrayList<>();
            for (int n = 1; n <= WINDOW_DAYS; n++) {
                Money accrual = interestService.calculateDailyAccrual(closingBalance(accountId, Day.of(n)));
                daily.put(Day.of(n), accrual);
                accruals.add(accrual);
            }
            interestByAccount.put(accountId, daily);

            Money capitalized = interestService.capitalizedTotal(currency, accruals);
            capitalizedInterest.put(accountId, capitalized);
            if (capitalized.isPositive()) {
                post(accountId, Day.DAY_6, capitalized, "INTEREST-CAPITALIZED");
            }
        }
    }

    private void post(String accountId, Day valueDate, Money signedAmount, String label) {
        postingsByAccount.computeIfAbsent(accountId, k -> new ArrayList<>())
                .add(new Posting(valueDate, signedAmount, label));
        if (signedAmount.isNegative()) {
            balanceService.debit(accountId, signedAmount.negate());
        } else if (signedAmount.isPositive()) {
            balanceService.credit(accountId, signedAmount);
        }
    }

    private void changeHold(String accountId, Day valueDate, Money signedAmount, String label) {
        holdChangesByAccount.computeIfAbsent(accountId, k -> new ArrayList<>())
                .add(new Posting(valueDate, signedAmount, label));
        if (signedAmount.isPositive()) {
            balanceService.placeHold(accountId, signedAmount);
        } else if (signedAmount.isNegative()) {
            balanceService.releaseHold(accountId, signedAmount.negate());
        }
    }

    private Money closingBalance(String accountId, Day day) {
        Money total = Money.zero(accounts.get(accountId).getCurrency());
        for (Posting posting : postingsByAccount.getOrDefault(accountId, List.of())) {
            if (posting.valueDate.getNumber() <= day.getNumber()) {
                total = total.add(posting.amount);
            }
        }
        return total;
    }

    private Money heldAsOf(String accountId, Day day) {
        Money total = Money.zero(accounts.get(accountId).getCurrency());
        for (Posting hold : holdChangesByAccount.getOrDefault(accountId, List.of())) {
            if (hold.valueDate.getNumber() <= day.getNumber()) {
                total = total.add(hold.amount);
            }
        }
        return total;
    }

    private void noteAuthorization(Day day, String note) {
        authorizationNotesByDay.computeIfAbsent(day, d -> new ArrayList<>()).add(note);
    }

    private LedgerReport buildReport() {
        List<Day> days = new ArrayList<>();
        for (int n = 1; n <= WINDOW_DAYS; n++) {
            days.add(Day.of(n));
        }

        Map<Day, Map<String, AccountBalance>> dailyBalances = new LinkedHashMap<>();
        for (Day day : days) {
            Map<String, AccountBalance> perAccount = new LinkedHashMap<>();
            for (String accountId : accounts.keySet()) {
                perAccount.put(accountId, new AccountBalance(
                        closingBalance(accountId, day),
                        heldAsOf(accountId, day)));
            }
            dailyBalances.put(day, perAccount);
        }

        List<FeeAssessment> fees = new ArrayList<>();
        for (Map<Day, FeeAssessment> byDay : feesByAccount.values()) {
            fees.addAll(byDay.values());
        }

        return new LedgerReport(
                List.copyOf(accounts.values()),
                List.copyOf(days),
                dailyBalances,
                List.copyOf(fees),
                List.copyOf(authorizationService.getAll()),
                authorizationNotesByDay,
                List.copyOf(errors),
                errorsByDay,
                interestByAccount,
                capitalizedInterest,
                eventsByBookingDay);
    }

    public List<LedgerEvent> events() {
        return eventStore.findAll();
    }

    private static final class Posting {
        private final Day valueDate;
        private final Money amount;
        private final String label;

        private Posting(Day valueDate, Money amount, String label) {
            this.valueDate = valueDate;
            this.amount = amount;
            this.label = label;
        }
    }
}
