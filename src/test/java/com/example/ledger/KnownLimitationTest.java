package com.example.ledger;

import com.example.ledger.domain.Currency;
import com.example.ledger.domain.Day;
import com.example.ledger.domain.model.Money;
import com.example.ledger.report.LedgerReport;
import com.example.ledger.service.InterestService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DELIBERATELY FAILING TEST - excluded from the default suite.
 * Run it explicitly with:  gradlew knownFailureTest
 *
 * <p><b>What it reveals.</b> The ledger computes each day's interest from that
 * day's FINAL closing balance, i.e. after every event has been replayed -
 * including the retroactive E9 reversal that lands on Day 6 with a Day 2 value
 * date. A "snapshot at each day's close" implementation would instead accrue
 * interest on the Day 2 balance as it stood when Day 2 closed (AED 250.00,
 * giving AED 0.10).</p>
 *
 * <p>By the end of the window the Day 2 closing balance is AED 225.00, so the
 * engine reports AED 0.09. Both figures are defensible; this test pins the
 * difference and fails on purpose so the choice is impossible to miss. See
 * AMBIGUITIES.md section D and REJECTED.md.</p>
 */
class KnownLimitationTest {

    @Test
    void day2InterestShouldUseTheEndOfDay2SnapshotButTheEngineUsesFinalBalances() {
        LedgerReport report = AccountLedgerCore.run();

        Money endOfDay2Snapshot = Money.of("250.00", Currency.AED);
        Money snapshotAccrual = new InterestService().calculateDailyAccrual(endOfDay2Snapshot);
        Money reportedAccrual = report.getDailyInterest().get("ACC-001").get(Day.DAY_2);

        assertEquals(Money.of("0.10", Currency.AED), snapshotAccrual);

        // Fails: the engine's Day 2 accrual is 0.09 (final balance 225.00), not 0.10.
        assertEquals(snapshotAccrual, reportedAccrual,
                "Interest is computed on final, retroactively-adjusted closing balances");
    }
}
