A. What happens to the remaining Auth-A hold?

Auth-A:

hold = 200

Settlement:

185

Does settlement:

release the entire 200 hold

or:

release 185 and leave 15 active?

The specification needs interpretation.

This should be documented.

B. How does the reversal work?

E9 says:

REVERSAL — reverses E7

You need to define precisely what reversal does.

Likely:

E7 = -620
E9 = +620

But the important question is whether the reversal also affects:

previously assessed fees
historical daily balances
interest calculations

You need a deterministic rule.

C. Retroactive value-date events

This is probably one of the biggest design challenges.

E7 arrives:

Day 5

but has:

value_date = Day 2

Therefore it changes the Day 2 balance.

Your replay engine must handle:

booking day
≠
value date

This is probably intentional.

D. Interest after retroactive events

This is another important ambiguity.

If E7 arrives on Day 5 but changes the Day 2 closing balance, does your Day 2 interest get recalculated?

Your implementation needs a clearly documented answer.

This is exactly the kind of question I expect during the defense.


## E10 — Three equal BHD instalments

The specification requires BHD 10.000 to be posted as three equal
instalments.

BHD supports three decimal places, therefore:

10.000 / 3 = 3.333333...

It is impossible to represent three exactly equal BHD amounts while
preserving the original total.

I chose the deterministic allocation:

- Instalment 1: BHD 3.333
- Instalment 2: BHD 3.333
- Instalment 3: BHD 3.334

The final instalment receives the rounding remainder so that the
three instalments always sum exactly to BHD 10.000.


