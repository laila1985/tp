package com.example.ledger.replay;

import com.example.ledger.domain.*;
import com.example.ledger.domain.error.LedgerErrorEntry;
import com.example.ledger.domain.model.Account;
import com.example.ledger.domain.model.AccountBalance;
import com.example.ledger.exception.LedgerException;
import com.example.ledger.service.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public final class LedgerReplayEngine {

    private static final Logger LOGGER =
            Logger.getLogger(LedgerReplayEngine.class.getName());

    private final Map<String, Account> accounts = new HashMap<>();
    Map<String, Money> balances = new HashMap<>();
    private final FeeService feeService;
    private final InterestService interestService;

    private final AuthorizationService authorizationService;
    private final BalanceServiceImp balanceService;
    private final ReversalService reversalService;
    private final LedgerEventStore eventStore;

    private final Map<String, AccountBalance> snapshot = new HashMap<>();

    private Map<Day, Map<String, AccountBalance>> dailyBalances;
    private List<LedgerErrorEntry> failedEvents;

    public LedgerReplayEngine(Map<Day, Map<String, AccountBalance>> dailyBalances, List<LedgerErrorEntry> failedEvents ) {
        this.feeService = new FeeService();
        this.interestService = new InterestService();
        this.authorizationService = new AuthorizationService();
        this.balanceService = new BalanceServiceImp();
        this.reversalService = new ReversalService();
        this.eventStore = new LedgerEventStore();
        this.dailyBalances = dailyBalances;
        this.failedEvents = failedEvents;
    }

    public LedgerReplayEngine(FeeService feeService, InterestService interestService, AuthorizationService authorizationService, BalanceServiceImp balanceService, ReversalService reversalService) {
        this.feeService = feeService;
        this.interestService = interestService;
        this.authorizationService = authorizationService;
        this.balanceService = balanceService;
        this.reversalService = reversalService;
        this.eventStore = new LedgerEventStore();
    }


    public void registerAccount(Account account) {
        accounts.put(account.getAccountId(), account);
    }


    public void replay(List<LedgerEvent> events) {

        for (LedgerEvent event : events) {
            try {
                process(event);
                snapshotDay(event.getValueDate());
            } catch (LedgerException e) {
                LOGGER.warning(e.getMessage());
                failedEvents.add(new LedgerErrorEntry(event.getEventId(), e.getError(),e.getMessage() ));
            }
        }
    }


    public void process(LedgerEvent event) {

        Account account = accounts.get(event.getAccountId());
        if (account == null){
            account = new Account(event.getAccountId(),feeService.calculateOverdraftFee(new Money(Currency.AED, new BigDecimal(0)),false));
            registerAccount(account);

        }

        if (account == null) {
            throw new LedgerException(
                    LedgerError.UNKNOWN_ACCOUNT,
                    "Unknown account: " + event.getAccountId()
            );
        }


        if (eventStore.find(event.getEventId()) != null) {
            throw new LedgerException(
                    LedgerError.EVENT_ALREADY_EXISTS,
                    "Event already exists: "
                            + event.getEventId()
            );
        }

        switch (event.getType()) {

            case AUTHORIZATION -> processAuthorization(event);
            case SETTLEMENT -> processSettlement(event);
            case REVERSAL -> processReversal(event);
            case CREDIT -> processCredit(event);
            case DEBIT -> processDebit(event);
            default -> throw new LedgerException(
                        LedgerError.UNSUPPORTED_EVENT_TYPE,
                        "Unsupported event type: "
                                + event.getType()
                );
            }
            // store successful event
            eventStore.add(event);

    }

    private void snapshotDay(Day day) {

        for (String accountId : accounts.keySet()) {
            snapshot.put(
                    accountId,
                    new AccountBalance(
                            balanceService.get(accountId)
                    )
            );
        }

        dailyBalances.put(
                day,
                Map.copyOf(snapshot)
        );
    }

    private void processAuthorization(LedgerEvent event) {

        Authorization authorization = authorizationService.create(
                event.getAuthorizationId(),
                event.getAccountId(),
                event.getAmount()
        );
        balanceService.placeHold(
                authorization.getAccountId(),
                authorization.getHoldAmount()
        );

    }

    private void processSettlement(LedgerEvent event) {
        Authorization authorization = authorizationService.settle(event.getAuthorizationId(),
                event.getAccountId(),
                event.getAmount());
        // Release the original hold
        balanceService.releaseHold(
                authorization.getAccountId(),
                authorization.getHoldAmount()
        );

        // Apply the actual settled amount
        balanceService.debit(
                authorization.getAccountId(),
                event.getAmount()
        );
    }

    private void processReversal(LedgerEvent event) {

        LedgerEvent originalEvent =
                eventStore.find(event.getReversedEventId());

        if (originalEvent == null) {
            throw new LedgerException(
                    LedgerError.EVENT_NOT_FOUND,
                    "Original event not found: "
                            + event.getReversedEventId()
            );
        }

        // if Amount Debited :
        reversalService.validate(event, originalEvent);
        if (originalEvent.getType() == EventType.CREDIT){
            balanceService.debit(
                    originalEvent.getAccountId(),
                    originalEvent.getAmount()
            );
        }
        // if Amount Credited :
        if (originalEvent.getType() == EventType.DEBIT){
            balanceService.credit(
                    originalEvent.getAccountId(),
                    originalEvent.getAmount()
            );
        }
    }

    private void processCredit(LedgerEvent event) {
        balanceService.credit(
                event.getAccountId(),
                event.getAmount()
        );
    }

    private void processDebit(LedgerEvent event) {
        balanceService.debit(
                event.getAccountId(),
                event.getAmount()
        );
    }

    public List<LedgerEvent> events() {
        return eventStore.findAll();
    }

}

