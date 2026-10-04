package com.example.ledger;

import com.example.ledger.domain.Currency;
import com.example.ledger.domain.model.Money;
import com.example.ledger.service.InterestService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InterestServiceTest {

    private final InterestService interestService =
            new InterestService();

    @Test
    void shouldCalculateDailyInterestForPositiveBalance() {

        Money balance =
                Money.of("1200.00", Currency.AED);

        Money interest =
                interestService.calculateDailyAccrual(balance);

        assertEquals(
                Money.of("0.48", Currency.AED).amount(),
                interest.amount()
        );
    }

    @Test
    void shouldReturnZeroInterestForZeroBalance() {

        Money balance =
                Money.of("0.00", Currency.AED);

        Money interest =
                interestService.calculateDailyAccrual(balance);

        assertEquals(
                Money.zero(Currency.AED).amount(),
                interest.amount()
        );
    }

    @Test
    void shouldReturnZeroInterestForNegativeBalance() {

        Money balance =
                Money.of("-370.00", Currency.AED);

        Money interest =
                interestService.calculateDailyAccrual(balance);

        assertEquals(
                Money.zero(Currency.AED).amount(),
                interest.amount()
        );
    }

    @Test
    void shouldUseCurrencyPrecision() {

        Money balance =
                Money.of("100.000", Currency.BHD);

        Money interest =
                interestService.calculateDailyAccrual(balance);

        assertEquals(
                Money.of("0.040", Currency.BHD).amount(),
                interest.amount()
        );
    }
}

