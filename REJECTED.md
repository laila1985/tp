# REJECTED.md

Acceptance criteria that are WRONG and are refused, with the reasoning, followed
by approaches abandoned mid-build.

Verdict per criterion: (1) correct, (2) REFUSED, (3) correct, (4) correct,
(5) correct, (6) REFUSED, (7) REFUSED, (8) REFUSED.

## Criterion 2 - REFUSED
> "E7 causes exactly one overdraft fee to be assessed, on Day 2."

E7 is a DEBIT of AED 620.00 with value date Day 2, booked on Day 5. Applied at
its value date, the closing ledger balances become:

```text
Day 1: +250.00   Day 2: -370.00   Day 3: +30.00
Day 4: -155.00   Day 5: -155.00   (Day 6 recovers to +390.00 after E9)
```

Days 2, 4 and 5 are ALL negative, and the rule assesses a fee for every negative
day ("once per day per account ... when that day's closing ledger balance ... is
negative"). The engine therefore assesses THREE fees of AED 25.00, on Days 2, 4
and 5 - see `OVERDRAFT FEE ASSESSMENTS` in the report. The criterion is wrong on
two counts: there is not "exactly one" fee, and E7 causes negative days beyond
Day 2. (Even under the alternative "fee only for the day that is closing"
reading, the single fee would fall on Day 5, not Day 2 - either way the criterion
is false. See AMBIGUITIES.md section E.)

## Criterion 6 - REFUSED
> "After E9, all balances and fees return to their pre-E7 values."

E9 reverses E7's principal (+620.00 at value Day 2), so the ledger balances do
return to their pre-E7 path. But the overdraft fee(s) assessed because of E7 are
separate, already-booked entries in an append-only ledger. E9 does not - and must
not - reverse them. The report shows three live AED 25.00 fees after E9, with Day
2 back in credit (225.00): the balance recovered, the fee did not.

## Criterion 7 - REFUSED
> "The three BHD instalments in E10 must each be BHD 3.334."

BHD has 3 decimals. 10.000 / 3 = 3.3333... cannot be written as three equal 3-dp
amounts that total 10.000. Three lots of 3.334 total 10.002, inventing 0.002 out
of nothing. The correct deterministic split is 3.333 + 3.333 + 3.334 = 10.000,
with the last instalment absorbing the rounding remainder (`InstallmentService`).

## Criterion 8 - REFUSED
> "If the rounded daily interest accruals do not sum to the capitalized total,
> the remainder is discarded."

This directly contradicts the non-negotiable rule that the rounded daily accruals
must sum EXACTLY to the capitalised total. For ACC-001 the rounded accruals are
0.10 + 0.09 + 0.25 + 0.17 + 0.16 + 0.16 = 0.93; the raw sum is 0.918, which would
round to 0.92. Discarding the 0.01 difference breaks the identity and loses money.
We define the capitalised total as the SUM of the rounded accruals (0.93), so
nothing is ever discarded (`InterestService.capitalizedTotal`).

## Criteria accepted (for completeness)
- (1) Day 2 closing at end of Day 5, pre-fee: AED -370.00 - correct, and the Day 2
  fee records exactly this triggering balance.
- (3) The Day 4 settlement of Auth-A is accepted (state becomes SETTLED).
- (4) A settlement of an authorisation that is not present (E6 / Auth-Z) is
  rejected and no funds move.
- (5) A hold reduces the available balance but not the ledger balance - a true
  statement about the mechanism. (Note: Auth-B is in fact REJECTED, so it places
  no hold at all; this is reported explicitly.)

## Approaches abandoned mid-build

- **Reversing authorizations.** Considered and dropped: an authorisation can be
  settled before a reversal arrives, which needs extra state and ordering rules
  the brief does not define. Reversals are limited to CREDIT/DEBIT (`ReversalService`).
- **Auto-creating unknown accounts.** The first draft silently created a
  zero-balance account on any unknown id, hiding data errors. Replaced with a hard
  UNKNOWN_ACCOUNT rejection and explicit up-front account registration.
- **A cached, self-mutating available balance.** An early `AccountBalance`
  recomputed `available = available - held` on every read and let holds reduce the
  ledger balance. Both bugs removed: available is derived (`ledger - held`) and
  holds never touch the ledger.
- **Per-day snapshots keyed by value date.** Snapshotting the report against each
  event's value date overwrote earlier days and produced a non-deterministic day
  order. Replaced by a recomputed, ordered Day 1..6 report.
- **A Spring Boot web stack for a program with no web layer.** Removed together
  with the Spring dependency; the suite is plain JUnit 5.
