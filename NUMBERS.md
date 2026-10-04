# NUMBERS.md - every constant, and why that value and not half of it

All money is `BigDecimal`; no `double`/`float` is used anywhere on the money
path. `Money` stores a `Currency` plus a `BigDecimal` scaled to that currency.

## Specification constants (fixed by the brief)

### OVERDRAFT_FEE = AED 25.00
`FeeService.AED_OVERDRAFT_FEE = Money.of("25.00", Currency.AED)`.
Why 25.00 and not 12.50? The brief states "AED 25.00" verbatim and it is a
non-negotiable rule. It is a flat, per-day, per-account regulatory-style charge,
not a percentage, so there is nothing to scale; halving it would under-recover
and contradict the brief for no benefit.

### INTEREST_RATE = 0.04% per day = 0.0004
`InterestService.DAILY_RATE = new BigDecimal("0.0004")`.
Why 0.0004 and not 0.0002? 0.04% = 0.04/100 = 0.0004. Halving it (0.02%) halves
every accrual. It is written as a decimal string, never computed as `0.04 / 100`,
so it is exact - and 0.0004 is exactly representable as a decimal.

## Currency precision

- AED: 2 decimal places (`Currency.AED(2)`).
- BHD: 3 decimal places (`Currency.BHD(3)`).
Every amount is stored and rounded to its own currency's scale at construction
(`Money`). A BHD amount is never forced to 2 dp and vice versa.

## Rounding

### RoundingMode.HALF_UP, everywhere
`Money` rounds to the currency scale with `HALF_UP` ("away from zero on a tie").
Rounding is applied the moment a value becomes money (rate x balance, instalment
split, fee), never to a value that is still being summed at full precision.
Why not HALF_EVEN? Banker's rounding would produce a different page of numbers
(e.g. 0.165 -> 0.16 instead of 0.17) and is not what a customer sees on a
statement. Why not truncate (DOWN)? It would silently short-change credits.

## Window

### WINDOW_DAYS = 6
`LedgerReplayEngine.WINDOW_DAYS`. Day 1 .. Day 6 inclusive - exactly the window
the brief defines. Not 3 (half) and not 12: every day is evaluated, and interest
capitalises at the very end of the window.

## Fee trigger

### Negative == strictly below zero, on the CLOSING LEDGER balance
A fee is assessed when the closing balance signum < 0. A zero balance is not an
overdraft and does not trigger a fee. The balance used excludes holds, because
holds are not ledger entries (available balance is a separate quantity).

### Fee currency = AED
The brief only defines an AED fee, so `FeeService` charges it to AED accounts
only. A BHD account would need its own (unspecified) rule; ACC-002 never goes
negative here anyway.

### At most one fee per account per day
Enforced by tracking which (account, day) pairs have been assessed, so a
persistently negative day is charged once, not once per event.

## Interest

### Positive == strictly above zero
Interest accrues only on strictly positive closing balances. Zero and negative
balances accrue nothing (a negative balance earns nothing and is already priced
by the fee).

### Capitalised on Day 6, equal to the sum of the rounded accruals
A single credit valued Day 6. The capitalised total is defined as the sum of the
six rounded daily accruals, so no rounding remainder can ever be discarded.
For ACC-001 the accruals are 0.10, 0.09, 0.25, 0.17, 0.16, 0.16 -> 0.93 (the raw
sum 0.918 would round to 0.92). The credit does not itself accrue interest inside
the window.

## Instalments

### E10: BHD 10.000 split into 3
Base = `10.000 / 3` floored to 3 dp = `3.333`; the remainder
(`10.000 - 3 x 3.333 = 0.001`) goes to the last instalment -> `3.333, 3.333,
3.334`. Last-gets-the-remainder is deterministic and preserves the total exactly.
Why not 3.334 each? 3 x 3.334 = 10.002; it would invent 0.002 from nowhere.

## Opening balances

- ACC-001 = AED 0.00, ACC-002 = BHD 0.000 (given). Accounts are registered with
  these explicitly; unknown ids are rejected rather than auto-created.
