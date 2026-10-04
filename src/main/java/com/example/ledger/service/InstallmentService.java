package com.example.ledger.service;

import com.example.ledger.domain.model.Money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class InstallmentService {

    public List<Money> splitIntoEqualInstallments(
            Money total,
            int numberOfInstallments) {

        if (numberOfInstallments <= 0) {
            throw new IllegalArgumentException(
                    "Number of installments must be greater than zero"
            );
        }

        BigDecimal baseAmount = total.amount()
                .divide(
                        BigDecimal.valueOf(numberOfInstallments),
                        total.getCurrency().getScale(),
                        java.math.RoundingMode.DOWN
                );

        List<Money> installments = new ArrayList<>();

        for (int i = 0; i < numberOfInstallments; i++) {
            installments.add(
                    new Money(
                            total.getCurrency(),
                            baseAmount
                    )
            );
        }

        BigDecimal allocated = baseAmount
                .multiply(BigDecimal.valueOf(numberOfInstallments));

        BigDecimal remainder = total.amount()
                .subtract(allocated);

        if (remainder.signum() != 0) {
            Money lastInstallment = installments.get(
                    installments.size() - 1
            );

            installments.set(
                    installments.size() - 1,
                    lastInstallment.add(
                            new Money(
                                    total.getCurrency(),
                                    remainder
                            )
                    )
            );
        }

        return installments;
    }
}
