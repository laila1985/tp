# WORKLOG.md

Timestamped work log. Newest session last.

## 2026-10-03 (initial build)

11:15 - to get on the subject i start first thinking about simple design with
account and how the ledger will handle the case, then i read the requirement and
discuss it to understand the wanted design.

11:40 - reviewed assessment requirements and identified event replay, value-date
handling, authorization holds, overdraft fees, interest and currency precision as
core areas.

12:00 - identified incorrect acceptance criteria related to E9, BHD instalment
rounding and the discarded interest remainder.

16:00 - implemented Authorization, AuthorizationState, Money, EventType,
Currency, LedgerEvent, EventStream, LedgerService, FeeService and unit tests
(FeeServiceTest, InstallmentServiceTest, InterestServiceTest, MoneyTest).

18:15 - defined AuthorizationService.

19:15 - cleaned the code, processed the events, verified the balance, added daily
balance (held + available), cleaned the replay engine, updated DailyReportPrinter
to display a daily report.

19:55 - cleaned unused code.

## 2026-10-04 (review + fix session)

09:00 - started a full code review against the brief. Built and ran the existing
suite: it compiled, but only "empty" placeholder tests plus one Spring context
test (which failed because there is no @SpringBootConfiguration). No test
replayed the event stream end to end.

09:20 - found the core defects: accounts were never registered in
AccountLedgerCore; the engine never assessed fees, never accrued interest and
never split E10; `AccountBalance.getAvailableBalance()` mutated state on every
read; `placeHold`/`releaseHold` wrongly changed the ledger balance; the
`DailyReportPrinter` printed only available/held (no ledger balance, fees, auth
states or errors); the day map was an unordered HashMap; the settlement of an
unknown authorisation created a phantom HOLD; and the build still pulled Spring
Boot for a program with no web layer.

09:40 - hand-computed the expected numbers for the whole window (see NUMBERS.md
and the report) and settled the semantics: fee assessed once per account per day
at the day's value date, retroactive events reopen past days, fees are not
reversed by E9, interest computed on final balances and capitalised on Day 6 as
the sum of the rounded accruals.

10:00 - implemented the fixes: rewrote LedgerReplayEngine with value-dated
postings/holds, correct hold semantics, fee assessment, E10 instalment splitting
and interest capitalisation; fixed AccountBalance, AuthorizationService,
InterestService, ReversalService, Account, Day (Day.of), Money (negate) and
LedgerEvent (installmentCount); added LedgerReport and rewrote
DailyReportPrinter.

10:30 - replaced build.gradle (dropped Spring Boot, added the application plugin,
JUnit 5, and a `knownFailureTest` task that excludes the deliberately failing
test from the default suite); removed the stray nested `demo` Spring project, the
Spring `application.yaml`, `DemoApplicationTests`, the unused `LedgerEntry`, the
empty placeholder tests and the mis-named, non-compiling `LedgerReplayEngineTest`
file.

11:00 - wrote the tests: EndToEndReplayTest (replays, prints and asserts every
number and every criterion), LedgerReplayEngineTest, EventStreamTest, plus
executed the existing unit tests. Added KnownLimitationTest - the single
deliberately failing test (interest uses final, retroactive-adjusted balances).

11:20 - ran `gradlew test`: 36 tests, 0 failures. Ran `gradlew knownFailureTest`:
1 test, 1 failure (as designed).

11:40 - ran `gradlew run` and reconciled the printed report against the hand
calculations: closing balances 250.00/225.00/625.00/415.00/390.00/390.93 (AED),
10.000/10.008 (BHD), three fees of 25.00 on Days 2/4/5, interest 0.93 / 0.008.

12:00 - wrote README.md, NUMBERS.md, AMBIGUITIES.md, REJECTED.md (added refused
criterion 2 alongside 6, 7 and 8) and this log.
