package com.example.ledger;

import com.example.ledger.domain.Currency;
import com.example.ledger.domain.model.Money;
import com.example.ledger.service.FeeService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FeeServiceTest {

    private final FeeService feeService = new FeeService();

    @Test
    void shouldAssessOverdraftFeeWhenBalanceIsNegative() {

        Money balance = Money.of("-370.00", Currency.AED);

        Money fee = feeService.calculateOverdraftFee(
                balance,
                false
        );

        assertEquals(
                Money.of("25.00", Currency.AED).amount(),
                fee.amount()
        );
    }

    @Test
    void shouldNotAssessFeeTwice() {

        Money balance = Money.of("-370.00", Currency.AED);

        Money fee = feeService.calculateOverdraftFee(
                balance,
                true
        );

        assertTrue(fee.isZero());
    }

    @Test
    void shouldNotAssessFeeForPositiveBalance() {

        Money balance = Money.of("250.00", Currency.AED);

        Money fee = feeService.calculateOverdraftFee(
                balance,
                false
        );

        assertTrue(fee.isZero());
    }
}
