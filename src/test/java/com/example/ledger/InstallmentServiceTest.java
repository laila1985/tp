package com.example.ledger;

import com.example.ledger.domain.Currency;
import com.example.ledger.domain.model.Money;
import com.example.ledger.service.InstallmentService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InstallmentServiceTest {

    private final InstallmentService service =
            new InstallmentService();

    @Test
    void shouldSplitBhdAmountIntoThreeInstallments() {

        Money total =
                Money.of("10.000", Currency.BHD);

        List<Money> installments =
                service.splitIntoEqualInstallments(total, 3);

        assertEquals(3, installments.size());

        assertEquals(
                Money.of("3.333", Currency.BHD).amount(),
                installments.get(0).amount()
        );

        assertEquals(
                Money.of("3.333", Currency.BHD).amount(),
                installments.get(1).amount()
        );

        assertEquals(
                Money.of("3.334", Currency.BHD).amount(),
                installments.get(2).amount()
        );
    }

    @Test
    void installmentsMustSumToOriginalAmount() {

        Money total =
                Money.of("10.000", Currency.BHD);

        List<Money> installments =
                service.splitIntoEqualInstallments(total, 3);

        Money sum = Money.zero(Currency.BHD);

        for (Money installment : installments) {
            sum = sum.add(installment);
        }

        assertEquals(
                total.amount(),
                sum.amount()
        );
    }
}
