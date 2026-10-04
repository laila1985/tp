package com.example.ledger.stream;

import com.example.ledger.domain.*;
import com.example.ledger.domain.model.LedgerEvent;
import com.example.ledger.domain.model.Money;

import java.math.BigDecimal;
import java.util.List;

public final class EventStream {

    public static List<LedgerEvent> create() {


       return  List.of(

        // Booked Event Details - E1 — Day 1 — CREDIT — ACC-001 AED 1,200.00 — value_date Day 1
        new LedgerEvent(
                        "E1",
                        Day.DAY_1,
                        EventType.CREDIT,
                        "ACC-001",
                        new Money(Currency.AED, new BigDecimal("1200.00")),
                        Day.DAY_1,
                        null,
                        null
                ),

        // E2 — Day 1 — DEBIT — ACC-001 AED 950.00 — value_date Day 1
        new LedgerEvent(
                        "E2",
                        Day.DAY_1,
                        EventType.DEBIT,
                        "ACC-001",
                        new Money(Currency.AED, new BigDecimal("950.00")),
                        Day.DAY_1,
                        null,
                        null
                ),

        // E3 — Day 2 — AUTHORIZATION — ACC-001 Auth-A hold AED 200.00 — value_date Day 2
        new LedgerEvent(
                        "E3",
                        Day.DAY_2,
                        EventType.AUTHORIZATION,
                        "ACC-001",
                        new Money(Currency.AED, new BigDecimal("200.00")),
                        Day.DAY_2,
                "Auth-A",
                        null
                ),

        // E4 — Day 3 — CREDIT — ACC-001 AED 400.00 — value_date Day 3
        new LedgerEvent(
                        "E4",
                        Day.DAY_3,
                        EventType.CREDIT,
                        "ACC-001",
                        new Money(Currency.AED, new BigDecimal("400.00")),
                        Day.DAY_3,
                        null,
                        null
                ),

        //E5 — Day 4 — SETTLEMENT — ACC-001 Auth-A settles for AED 185.00 — value_date Day 4
        new LedgerEvent(
                        "E5",
                        Day.DAY_4,
                        EventType.SETTLEMENT,
                        "ACC-001",
                        new Money(Currency.AED, new BigDecimal("185.00")),
                        Day.DAY_4, "Auth-A", null
                ),

        //E6 — Day 4 — SETTLEMENT — ACC-001 Auth-Z settles for AED 180.00 — value_date Day 4
        // (Auth-Z has no preceding authorization event)
        new LedgerEvent(
                        "E6",
                        Day.DAY_4,
                        EventType.SETTLEMENT,
                        "ACC-001",
                        new Money(Currency.AED, new BigDecimal("180.00")),
                        Day.DAY_4,
                        "Auth-Z",
                        null
                ),

        // E7 — Day 5 — DEBIT — ACC-001 AED 620.00 — value_date Day 2
        new LedgerEvent(
                        "E7",
                        Day.DAY_5,
                        EventType.DEBIT,
                        "ACC-001",
                        new Money(Currency.AED, new BigDecimal("620.00")),
                        Day.DAY_2,
                        null,
                        null
                ),

        // E8 — Day 5 — AUTHORIZATION — ACC-001 Auth-B hold AED 90.00
        new LedgerEvent(
                        "E8",
                        Day.DAY_5,
                        EventType.AUTHORIZATION,
                        "ACC-001",
                        new Money(Currency.AED, new BigDecimal("90.00")),
                        Day.DAY_5,
                        "Auth-B",null
                ),

        // E9 — Day 6 — REVERSAL — ACC-001 reverses E7 — value_date Day 2
        new LedgerEvent(
                        "E9",
                        Day.DAY_6,
                        EventType.REVERSAL,
                        "ACC-001",
                        new Money(Currency.AED, new BigDecimal("620.00")),
                        Day.DAY_2,
                        null,
                        "E7"
                ),

        // E10 — Day 5 — CREDIT — ACC-002 BHD 10.000
        // Posted as three equal instalments (3.333 + 3.333 + 3.334).
        new LedgerEvent(
                        "E10",
                        Day.DAY_5,
                        EventType.CREDIT,
                        "ACC-002",
                        new Money(Currency.BHD, new BigDecimal("10.000")),
                        Day.DAY_5,
                        null,
                        null,
                        3
                ));
    }
}
