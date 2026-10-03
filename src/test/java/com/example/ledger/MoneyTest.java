package com.example.ledger;

import com.example.ledger.domain.Currency;
import com.example.ledger.domain.Money;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoneyTest {

    @Test
    void shouldApplyCreditAndDebit() {
        Money balance = Money.of("1200.00", Currency.AED);
        Money debit = Money.of("950.00", Currency.AED);
        Money result = balance.subtract(debit);
        assertEquals("AED 250.00", result.toString());
    }


}
