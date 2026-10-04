### In-Memory Account Ledger Core
## Overview

This project implements an in-memory account ledger core.

The system replays a fixed stream of ledger events for six days, from Day 1 through Day 6, and produces a daily report containing:

Closing ledger balance per account
Overdraft fee assessments
Authorization states
Errors and rejected events
Interest accruals and final capitalization

## Implementation :
The Current Project process the ledger event , do calculation and generate report with daily balance available and held .
This is so far what has been implemented .
And it will display the rejected Event with reason and id 

to run the application please run the Main in the AccountLedgerCore.

The Generated Report will be :
============================================================
                    DAILY LEDGER REPORT
============================================================

------------------------------------------------------------
DAY_3
------------------------------------------------------------
ACCOUNT         Available Amount Held Amount
------------------------------------------------------------
ACC-001         AED 250.00      AED 200.00
------------------------------------------------------------

------------------------------------------------------------
DAY_5
------------------------------------------------------------
ACCOUNT         Available Amount Held Amount
------------------------------------------------------------
ACC-001         AED 85.00       AED 90.00      
ACC-002         BHD 10.000      BHD 0.000
------------------------------------------------------------

------------------------------------------------------------
DAY_1
------------------------------------------------------------
ACCOUNT         Available Amount Held Amount
------------------------------------------------------------
ACC-001         AED 250.00      AED 0.00
------------------------------------------------------------

------------------------------------------------------------
DAY_2
------------------------------------------------------------
ACCOUNT         Available Amount Held Amount
------------------------------------------------------------
ACC-001         AED 85.00       AED 90.00
------------------------------------------------------------

------------------------------------------------------------
DAY_4
------------------------------------------------------------
ACCOUNT         Available Amount Held Amount
------------------------------------------------------------
ACC-001         AED 265.00      AED 0.00
------------------------------------------------------------

============================================================
END OF REPORT
============================================================

============================================================
LEDGER ERROR ENTRIES
============================================================

Event ID        Error Type                Message
------------------------------------------------------------
E6              AUTHORIZATION_NOT_FOUND   Authorisation not found: Auth-Z
------------------------------------------------------------
Total errors: 1
============================================================
## The core challenge is not "build a banking system." it s : 

             EVENT STREAM
                  │
                  ▼
           ┌──────────────┐
           │ Replay Engine│
           └──────┬───────┘
                  │
       ┌──────────┼───────────┐
       ▼          ▼           ▼
    Ledger      Holds       Errors
    Balance     / Auth
       │
       ├── Fees
       │
       ├── Interest
       │
       └── Daily Output




### Core domain

Ledger
│
├── Account ACC-001
│   ├── Currency: AED
│   ├── Opening balance: 0.00
│   └── Entries / Events
│
└── Account ACC-002
   ├── Currency: BHD
   ├── Opening balance: 0.000
   └── Entries / Events

Then an event model:

Event
├── eventId
├── eventType
├── accountId
├── amount
├── currency
├── valueDate
└── event-specific data

### Event Types

The supplied event stream contains the following event types:

CREDIT
DEBIT
AUTHORIZATION
SETTLEMENT
REVERSAL


## Scope

The implementation focuses on:

Event replay
Account ledger balance calculation
Value-date processing
Authorization holds
Settlement processing
Reversals
Overdraft fee assessment
Daily interest calculation
Currency-specific precision and rounding
Error handling
Deterministic replay
Daily reporting

## Rules : 
- opening balance 0.000Non-negotiable rules:Overdraft fee: AED 25.00,
- assessed once per day per account when that day's closing ledger balance (all entries with value_date ≤ that day) is negative
- Booked with value_date equal to the day assessed
- Daily interest: 0.04% per day on the closing ledger balance, positive balances only.
- Accruals capitalize as a single credit at end of Day 6.
- The rounded daily accruals must sum exactly to the capitalized total
- AED is 2 decimal places, BHD is 3.
- Amounts stored and rounded to their own precision.The ledger is append-only.
- No event record is ever mutated or deleted
- An authorization is approved only if the account's available balance — ledger balance minus active holds — remains at or above zero after the hold is applied.

### Daily Interest
Daily interest is: 0.04% per day
Interest is calculated only on positive closing ledger balances.
Negative and zero balances do not accrue positive daily interest.
The daily rate is: 0.04% = 0.0004
Daily interest is calculated using the applicable closing ledger balance.

### Ledger Balance
The ledger balance represents posted ledger entries.
Authorization holds do not directly change the ledger balance.
The available balance is calculated separately.
Available Balance = Ledger Balance - Active Holds
An authorization is approved only if the available balance remains greater than or equal to zero after applying the new hold.
available balance after hold >= 0
If this condition is not satisfied, the authorization is rejected.

### Currency Precision

Different currencies use different decimal precision.

AED
2 decimal places

Examples:

AED 25.00
AED 1200.00
AED 0.01
BHD
3 decimal places

Examples:

BHD 10.000
BHD 3.333
BHD 0.001

All monetary calculations must respect the precision of the account currency.

Floating-point arithmetic should not be used for financial amounts where it could introduce precision errors.


### Daily Closing Balance

For each day, the closing ledger balance is calculated using all applicable ledger entries whose:

value_date <= current day

The report must show the resulting balance for each account.

For example:

Day 2
ACC-001
Closing ledger balance: ...

The calculation must include applicable credits, debits, fees, settlements, and reversals.

Authorization holds are not included directly in the ledger balance.


## Ledger Model

The ledger is append-only.

An event that has been recorded is never modified or deleted.

The current ledger state is derived by replaying the event stream.

Conceptually:

Event Stream
|
v
Replay Engine
|
v
Ledger State
|
+-- Ledger balances
+-- Active authorization holds
+-- Authorization states
+-- Fees
+-- Errors
+-- Interest accruals

The same event stream must produce the same result when replayed again.


### Event stream, replayed in this order:
Booked Event Details - E1 — Day 1 — CREDIT — ACC-001 AED 1,200.00 — value_date Day 1

E2 — Day 1 — DEBIT — ACC-001 AED 950.00 — value_date Day 1
E3 — Day 2 — AUTHORIZATION — ACC-001 Auth-A hold AED 200.00 — value_date Day 2
E4 — Day 3 — CREDIT — ACC-001 AED 400.00 — value_date Day 3
E5 — Day 4 — SETTLEMENT — ACC-001 Auth-A settles for AED 185.00 — value_date Day 4
E6 — Day 4 — SETTLEMENT — ACC-001 Auth-Z settles for AED 180.00 — value_date Day 4 (Auth-Z has no preceding authorization event)
E7 — Day 5 — DEBIT — ACC-001 AED 620.00 — value_date Day 2
E8 — Day 5 — AUTHORIZATION — ACC-001 Auth-B hold AED 90.00 — value_date Day 5
E9 — Day 6 — REVERSAL — ACC-001 reverses E7 — value_date Day 2
E10 — Day 5 — CREDIT — ACC-002 BHD 10.000, posted as three equal instalments — value_date Day 5Auth-B is never settled inside the window.


Event	Booking Day	Type	    Account	    Amount / Details	Value Date
E1	    Day 1	CREDIT	        ACC-001	    AED                 1,200.00	        Day 1
E2	    Day 1	DEBIT	        ACC-001	    AED                 950.00	            Day 1
E3	    Day 2	AUTHORIZATION	ACC-001	    Auth-A hold AED     200.00	            Day 2
E4	    Day 3	CREDIT	        ACC-001	    AED                 400.00	            Day 3
E5	    Day 4	SETTLEMENT	    ACC-001	    Auth-A settles AED  185.00	            Day 4
E6	    Day 4	SETTLEMENT	    ACC-001	    Auth-Z settles AED  180.00	            Day 4
E7	    Day 5	DEBIT	        ACC-001	    AED                 620.00	            Day 2
E8	    Day 5	AUTHORIZATION	ACC-001	    Auth-B hold AED     90.00	            Day 5
E9	    Day 6	REVERSAL	    ACC-001	    Reverses E7	                            Day 2
E10	    Day 5	CREDIT	        ACC-002 	BHD 10.000 in three equal instalments	Day 5

## Add a runnable suite/script that:

Replays E1 → E10 in the specified order
Applies the ledger rules
Produces daily results for Day 1 → Day 6
Shows:
- closing ledger balance.
- fee assessments
- authorization states
- errors

So your program should essentially be:
Event stream
↓
Replay engine
↓
Day 1
Day 2
Day 3
Day 4
Day 5
Day 6
↓
Daily report


The most important part: replay


## Error Handling: 
The implementation must handle invalid events without corrupting the ledger.

Examples include:

Account does not exist
Invalid amount
Insufficient available balance
Duplicate event / transaction
Settlement references an unknown authorization
Invalid reversal reference
Invalid authorization
Currency mismatch, where applicable

Rejected events must not incorrectly change the account ledger.

Errors should be visible in the daily output.


## Expected Design Principles

The implementation is intentionally small, but the following properties are important.

### Deterministic

The same event stream must always produce the same result.

### Append-only

Existing events are never modified or deleted.

### Currency-safe

Monetary calculations respect currency-specific precision.

### Replayable

Account state can be reconstructed by replaying the event history.

### Explicit

Important business rules should be visible in the domain logic rather than hidden in arbitrary calculations.

### Testable

Ledger rules should be testable independently from the command-line/test runner.