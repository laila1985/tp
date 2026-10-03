Rejected acceptance criteria:

1. "After E9, all balances and fees return to their pre-E7 values."

Reason:
E9 reverses the principal effect of E7, but the overdraft fee assessed
because of E7 is a separate ledger entry and is not automatically
reversed.

2. "The three BHD instalments in E10 must each be BHD 3.334."

Reason:
BHD has 3 decimal places. 10.000 / 3 cannot be represented as three
equal amounts at that precision. 3.334 × 3 = 10.002, which exceeds
the original amount.

A deterministic rounding-allocation rule is required to ensure the
three instalments sum exactly to BHD 10.000.

3. "If the rounded daily interest accruals do not sum to the capitalized
   total, the remainder is discarded."

Reason:
This directly conflicts with the requirement that rounded daily
accruals must sum exactly to the capitalized total. Any rounding
remainder must be handled deterministically rather than discarded.


## Reversal for Authorization
I want it to include it but since authorisation can be settled before the reversal received and it need extra processing and controle I reject this idea 