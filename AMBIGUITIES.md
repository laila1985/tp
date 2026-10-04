# AMBIGUITIES.md

Every ambiguity found and how it was resolved. This file is deliberately long;
the retroactive value dates and the fee/interest timing raise several genuine
questions, and a near-empty file would be a fail.

## A. Settlement of a hold larger than the settled amount (E5)
Auth-A held AED 200.00; E5 settles it for AED 185.00. Does the AED 15.00 stay on
hold?
**Resolution:** settlement releases the ENTIRE original hold and posts the
settled amount as a debit. Available balance therefore rises by the 15.00
difference. Rationale: a settlement closes the authorisation; keeping a residual
hold on a closed authorisation is a dangling state. Outcome: Day 4 held = 0.00,
ledger 415.00, available 415.00.

## B. What a reversal does (E9)
**Resolution:** a reversal appends the exact opposite of the original posting(s)
at the ORIGINAL value date. E7 was a DEBIT of 620.00 value Day 2, so E9 appends a
CREDIT of 620.00 value Day 2. A reversal does not delete or mutate the original,
does not touch fees already assessed, and is itself just another append-only
record. Reversals are limited to CREDIT/DEBIT; AUTHORIZATION, SETTLEMENT and FEE
cannot be reversed.

## C. Retroactive (back-valued) events (E7, E9)
E7 is booked on Day 5 but valued Day 2, so it rewrites the historical Day 2 (and
Day 4 and Day 5) closing balances.
**Resolution:** every day's closing balance is recomputed from the complete set
of value-dated entries, so a back-valued event changes past days. This is visible
in the output: the Day 2 fee records a triggering balance of -370.00.

## D. Interest after retroactive events
If E7 changes Day 2's balance on Day 5, is Day 2's interest recomputed?
**Resolution:** YES. Interest is evaluated once, on the FINAL closing balance of
each day (after E9 as well). Day 2 therefore accrues on 225.00 (0.09), not on the
250.00 it closed with. The opposite choice - snapshot at each day's close - is
defensible; `KnownLimitationTest` pins the difference and fails on purpose, and
it is the point of that test.

## E. When a fee is "assessed", and for which day
**Resolution adopted:** after every processed event, for each account and each
day d up to the highest booking day seen, if closing(d) < 0 and no fee has yet
been assessed for (account, d), assess one fee booked at value date d.
Consequences:
- E7 (value Day 2, booked Day 5) makes Days 2, 4 and 5 negative, so THREE fees
  are assessed; the Day 2 fee is the first and its pre-fee balance is -370.00.
- A fee is assessed at most once per account per day.
- E9 never reverses a fee.
**Alternative considered:** assess a fee only for the day that is closing (the
booking day). That yields a single fee on Day 5 and no Day 2 fee. The value-date
reading was chosen because the rule says "that day's closing ledger balance (all
entries with value_date <= that day)" and "booked with value_date equal to the
day assessed", and because criterion 1 speaks of Day 2 being evaluated at the end
of Day 5. Either reading makes criterion 2 false (three fees / wrong day).

## F. Authorization approval timing
An authorisation is approved iff the available balance at the moment it is
booked - current ledger minus CURRENT holds - stays >= 0 after the hold. Auth-B
(90.00, Day 5) is tested against the then-current available balance of -155.00
-> rejected. The authorisation's value date is used only for the daily report,
not for the approval check.

## G. Debits that overdraw are allowed
E7 drives ACC-001 negative. The brief lists "insufficient available balance" as
an error case, but that governs authorizations. A posted debit is always posted;
the resulting overdraft is then priced by the fee - otherwise E7 could never
trigger the very fee the task is built around.

## H. Unknown account / unknown authorization
An event for an unregistered account is rejected (UNKNOWN_ACCOUNT). A settlement
referencing an authorization with no prior authorization event (E6 / Auth-Z) is
rejected (AUTHORIZATION_NOT_FOUND) and no funds move. Rejected events are not
added to the event store, so they cannot later be reversed, and change no balance.

## I. Replay order vs. booking order
The stream is replayed strictly in the given order (E1..E10). Note E10 is a Day 5
event listed AFTER the Day 6 event E9; we do not re-sort by booking day. Because
E10 only touches ACC-002, this does not change any ACC-001 figure.

## J. Interest applies to BHD too
No currency is excluded, so ACC-002 accrues 0.04%/day on its positive balances:
Day 5 and Day 6 at BHD 0.004 each, capitalising to BHD 0.008.

## K. Capitalised-total definition
"The rounded daily accruals must sum exactly to the capitalised total." We make
this true by construction: the capitalised total IS the sum of the rounded daily
accruals (AED 0.93 for ACC-001). Were it instead `round(sum of raw accruals)` it
would be AED 0.92 and 0.01 would be lost. Criterion 8 (discard the remainder) is
therefore refused (see REJECTED.md).

## L. Day 6 closing balance vs. the interest credit
Day 6's closing balance is reported BEFORE the interest credit (390.00) and the
credit (0.93) is reported separately; the post-capitalisation balance is 390.93.
The Day 6 accrual is computed on the pre-credit balance, i.e. interest does not
compound on Day 6.

## M. Error visibility
Rejected events are surfaced twice: inline in their booking day's section, and in
the global `LEDGER ERRORS / REJECTED EVENTS` list. Errors carry the event id and
an error code so the daily output is self-explanatory.
