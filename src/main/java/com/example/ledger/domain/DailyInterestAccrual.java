package com.example.ledger.domain;

public record DailyInterestAccrual(
        Day day,
        Money amount
) {
}
