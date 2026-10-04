package com.example.ledger;

import com.example.ledger.domain.AuthorizationState;
import com.example.ledger.domain.Currency;
import com.example.ledger.domain.Day;
import com.example.ledger.domain.error.LedgerError;
import com.example.ledger.domain.model.AccountBalance;
import com.example.ledger.domain.model.Authorization;
import com.example.ledger.domain.model.Money;
import com.example.ledger.report.DailyReportPrinter;
import com.example.ledger.report.FeeAssessment;
import com.example.ledger.report.LedgerReport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Replays the fixed event stream once, prints the daily report (so the suite
 * itself exercises and prints the output), then asserts the reconciled numbers
 * and the verdict on every acceptance criterion.
 */
class EndToEndReplayTest {

    private static LedgerReport report;

    @BeforeAll
    static void replayOnce() {
        report = AccountLedgerCore.run();
        new DailyReportPrinter().print(report);
    }

    private static AccountBalance balance(Day day, String accountId) {
        return report.getDailyBalances().get(day).get(accountId);
    }

    private static Money ledger(Day day, String accountId) {
        return balance(day, accountId).getLedgerBalance();
    }

    private static FeeAssessment feeOn(Day day) {
        return report.getFees().stream().filter(f -> f.getDay() == day).findFirst().orElse(null);
    }

    private static Authorization authorization(String id) {
        return report.getAuthorizations().stream()
                .filter(a -> a.getAuthorizationId().equals(id))
                .findFirst().orElseThrow();
    }

    @Test
    void criterion1_day2ClosingAtEndOfDay5BeforeAnyFeeIsMinus370() {
        FeeAssessment day2Fee = feeOn(Day.DAY_2);
        assertNotNull(day2Fee, "Expected an overdraft fee on Day 2");
        assertEquals(Money.of("-370.00", Currency.AED), day2Fee.getBalanceBeforeFee());
    }

    @Test
    void closingBalancesPerDayAreCorrect() {
        assertEquals(Money.of("250.00", Currency.AED), ledger(Day.DAY_1, "ACC-001"));
        assertEquals(Money.of("225.00", Currency.AED), ledger(Day.DAY_2, "ACC-001"));
        assertEquals(Money.of("625.00", Currency.AED), ledger(Day.DAY_3, "ACC-001"));
        assertEquals(Money.of("415.00", Currency.AED), ledger(Day.DAY_4, "ACC-001"));
        assertEquals(Money.of("390.00", Currency.AED), ledger(Day.DAY_5, "ACC-001"));
        assertEquals(Money.of("390.93", Currency.AED), ledger(Day.DAY_6, "ACC-001"));

        assertEquals(Money.of("0.000", Currency.BHD), ledger(Day.DAY_4, "ACC-002"));
        assertEquals(Money.of("10.000", Currency.BHD), ledger(Day.DAY_5, "ACC-002"));
        assertEquals(Money.of("10.008", Currency.BHD), ledger(Day.DAY_6, "ACC-002"));
    }

    @Test
    void criterion2_e7CausesThreeFeesOnDays2And4And5NotExactlyOne() {
        List<Day> feeDays = report.getFees().stream().map(FeeAssessment::getDay).collect(Collectors.toList());
        assertEquals(3, report.getFees().size());
        assertEquals(List.of(Day.DAY_2, Day.DAY_4, Day.DAY_5), feeDays);
    }

    @Test
    void criterion3_day4SettlementOfAuthAIsAccepted() {
        assertEquals(AuthorizationState.SETTLED, authorization("Auth-A").getState());
        assertFalse(report.getErrors().stream().anyMatch(e -> "E5".equals(e.getEventId())));
    }

    @Test
    void criterion4_unknownAuthorizationSettlementIsRejectedAndFundsDoNotMove() {
        assertTrue(report.getErrors().stream()
                .anyMatch(e -> "E6".equals(e.getEventId()) && e.getError() == LedgerError.AUTHORIZATION_NOT_FOUND));
        // Had the AED 180.00 left the account, Day 4 would be 235.00.
        assertEquals(Money.of("415.00", Currency.AED), ledger(Day.DAY_4, "ACC-001"));
    }

    @Test
    void criterion5_authBIsRejectedSoItNeverPlacesAHold() {
        assertEquals(AuthorizationState.REJECTED, authorization("Auth-B").getState());
        assertEquals(Money.of("0.00", Currency.AED), balance(Day.DAY_5, "ACC-001").getHeldAmount());
    }

    @Test
    void criterion6_reversalRestoresLedgerButDoesNotUndoTheFee() {
        assertEquals(3, report.getFees().size());
        assertTrue(ledger(Day.DAY_2, "ACC-001").isPositive()); // E9 restored Day 2 ...
        assertNotNull(feeOn(Day.DAY_2));                       // ... but the fee remains.
    }

    @Test
    void criterion7_bhdInstalmentsAreThreeThreeThreeNotThreeThreeFour() {
        assertEquals(Money.of("10.000", Currency.BHD), ledger(Day.DAY_5, "ACC-002"));
        // 3.334 x 3 = 10.002, which would overshoot the original 10.000.
        assertEquals(Money.of("10.002", Currency.BHD).amount(),
                Money.of("3.334", Currency.BHD).amount().multiply(new BigDecimal("3")));
    }

    @Test
    void criterion8_roundedDailyAccrualsSumExactlyToCapitalisedTotal() {
        Map<Day, Money> accruals = report.getDailyInterest().get("ACC-001");
        Money sum = Money.zero(Currency.AED);
        for (Money accrual : accruals.values()) {
            sum = sum.add(accrual);
        }
        assertEquals(Money.of("0.93", Currency.AED), sum);
        assertEquals(Money.of("0.93", Currency.AED), report.getCapitalizedInterest().get("ACC-001"));

        // A naive "round the aggregate, discard the remainder" rule would give 0.92.
        Money naive = new Money(Currency.AED, new BigDecimal("0.918"));
        assertEquals(Money.of("0.92", Currency.AED), naive);
        assertNotEquals(naive, report.getCapitalizedInterest().get("ACC-001"));
    }
}
