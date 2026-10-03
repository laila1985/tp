2026-10-03 11:15
to get on the subject i start first thinking about simple design with account and user and how the Ledger will handle the case 
than i reading the requirement , and discussing it with chatgpt to understand the wanted design

2026-10-03 11:40
Reviewed assessment requirements and identified event replay,
value-date handling, authorization holds, overdraft fees,
interest and currency precision as core areas.

2026-10-03 12:00
Identified incorrect acceptance criteria related to E9,
BHD instalment rounding and discarded interest remainder.


2026-10-03 04:00
Implement Authorization, AuthorizationState , Money, EventType, Currency, DailyInterestAccrual,LedgerEvent , EventStream
LedgerService, FeeService

and UT : 
FeeServiceTest, InstallmentServiceTest, InterestServiceTest, FeeServiceTest, MoneyTest

2026-10-03 08:15
define AuthorizationService



