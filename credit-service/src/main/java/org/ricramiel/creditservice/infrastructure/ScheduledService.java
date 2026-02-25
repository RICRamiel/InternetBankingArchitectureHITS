package org.ricramiel.creditservice.infrastructure;

import lombok.RequiredArgsConstructor;
import org.ricramiel.creditservice.enums.PercentageStrategy;
import org.ricramiel.creditservice.model.Credit;
import org.ricramiel.creditservice.model.CreditRule;
import org.ricramiel.creditservice.repository.CreditRepository;
import org.ricramiel.creditservice.service.CreditService;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@EnableScheduling
public class ScheduledService {

    private final CreditService creditService;
    private final CreditRepository creditRepository;

    @Scheduled(fixedRate = 60_000)
    @Transactional
    private void interestUpdater() {
        int size = 100;
        int offset = 0;
        Page<Credit> credits = creditService.findAllPageable(size, offset);
        while (!credits.isEmpty()) {
            offset += size;
            credits = creditService.findAllPageable(size, offset);
            credits.forEach(credit -> {
                counter(credit);
                creditRepository.save(credit);
            });
        }
    }

    private void counter(Credit credit) {
        BigDecimal debt = credit.getDebt();
        CreditRule creditRule = credit.getCreditRule();
        if (creditRule.getPercentageStrategy().equals(PercentageStrategy.FROM_REMAINING_DEBT)) {
            credit.setDebt(debt.add(debt.multiply(creditRule.getPercentage())));
        }
    }
}
