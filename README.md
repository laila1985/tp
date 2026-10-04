# In-Memory Account Ledger Core

A small, dependency-light Java 21 ledger core. It replays a fixed six-day event
stream (E1..E10) and prints, **per day**: the closing ledger balance of every
account, the overdraft-fee assessments, the authorization states and the errors.
There is no web layer, no persistence, no database and no UI.

## How to run

Requires a JDK 21. The Gradle wrapper pins Gradle 9.7.1.

```text
gradlew.bat test              # run the suite (Windows)     -> ./gradlew test        (macOS/Linux)
gradlew.bat run               # replay the stream and print the daily report
gradlew.bat knownFailureTest  # run the one, deliberately failing test
```

The runnable script required by the task is the test suite itself: it replays
the fixed event stream, prints the daily report, and asserts every number.

## How to read the output

`gradlew run` (and `EndToEndReplayTest`) prints, in order:

1. A `DAY n` section for every day, Day 1 .. Day 6, containing
   - a balances table: `CLOSING LEDGER`, `HELD`, `AVAILABLE`;
   - `Fee assessed`  - any overdraft fee whose value date is that day;
   - `Auth states`   - authorization state changes booked that day;
   - the events booked that day, and any errors for that day.
2. `AUTHORIZATION STATES`          - every authorization and its final state.
3. `OVERDRAFT FEE ASSESSMENTS`     - each fee with the balance that triggered it.
4. `DAILY INTEREST`                - the six rounded daily accruals, their sum
   and the capitalised Day 6 credit, per account.
5. `LEDGER ERRORS / REJECTED EVENTS` - every rejected event.

### Expected headline numbers

| Quantity | Value |
| --- | --- |
| ACC-001 closing ledger, Day 1..6 | 250.00, 225.00, 625.00, 415.00, 390.00, 390.93 AED |
| ACC-002 closing ledger, Day 5 / Day 6 | 10.000 / 10.008 BHD |
| Day 2 closing balance, at end of Day 5, before any fee | -370.00 AED |
| Overdraft fees | 3 x AED 25.00, on Days 2, 4 and 5 |
| Capitalised interest | AED 0.93 (ACC-001), BHD 0.008 (ACC-002) |
| Rejected events | E6 (unknown authorisation Auth-Z), E8 (Auth-B, insufficient available balance) |

## Model in one paragraph

Events are replayed in stream order. CREDIT/DEBIT post value-dated entries that
add to the ledger balance; AUTHORIZATION places a hold (which affects only the
available balance); SETTLEMENT releases the full original hold and posts the
settled amount; REVERSAL appends the opposite of the original entry at the
original value date. The closing ledger balance of Day `d` is the sum of every
entry whose value date is `<= d`. An overdraft fee of AED 25.00 is assessed once
per account per day whenever that day's closing balance is negative, booked with
that day as its value date; a retroactive event can therefore trigger a fee for
a past day, and a fee is never undone by a later event. Interest of 0.04%/day is
computed on each day's positive closing balance and capitalised as a single Day 6
credit equal to the sum of the rounded daily accruals.

## Project layout

```text
src/main/java/com/example/ledger/
  AccountLedgerCore.java         entry point (register accounts, replay, print)
  domain/                        Day, Currency, EventType, AuthorizationState, error model
  domain/model/                  Money, Account, AccountBalance, Authorization, LedgerEvent
  stream/EventStream.java        the fixed E1..E10 stream
  replay/LedgerReplayEngine.java the replay engine
  report/                        LedgerReport, FeeAssessment, DailyReportPrinter
  service/                       Fee, Interest, Installment, Authorization, Reversal, ...
src/test/java/com/example/ledger/
  EndToEndReplayTest.java        replays + prints + asserts every number and criterion
  LedgerReplayEngineTest.java    engine mechanics (holds, rejections)
  EventStreamTest.java           the shape of the fixed stream
  KnownLimitationTest.java       the one deliberately failing test (excluded by default)
  *ServiceTest, MoneyTest        focused unit tests
```

## Deliverables

- `README.md`      - this file.
- `NUMBERS.md`     - every constant chosen, why that value and not half of it.
- `AMBIGUITIES.md` - every ambiguity found and how it was resolved.
- `REJECTED.md`    - refused acceptance criteria and abandoned approaches.
- `KnownLimitationTest.java` - the failing test, inline-annotated with what it reveals.
- `WORKLOG.md`     - timestamped work log.
