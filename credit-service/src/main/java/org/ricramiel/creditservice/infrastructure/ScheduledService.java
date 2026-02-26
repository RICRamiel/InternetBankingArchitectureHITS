package org.ricramiel.creditservice.infrastructure;

import lombok.RequiredArgsConstructor;
import org.ricramiel.common.dtos.WithdrawDto;
import org.ricramiel.creditservice.client.CoreClient;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScheduledService {

    private final CreditService creditService;
    private final CreditRepository creditRepository;
    private final CoreClient coreClient;

    @Scheduled(fixedRate = 120_000)
    @Transactional
    protected void interestUpdater() {
        int size = 100;
        int pageNumber = 0;
        Page<Credit> credits = creditService.findAllPageable(pageNumber, size);
        while (!credits.isEmpty()) {
            pageNumber++;
            credits = creditService.findAllPageable(pageNumber, size);
            credits.forEach(credit -> {
                counter(credit);
                creditRepository.save(credit);
            });
        }
    }

    @Scheduled(fixedRate = 60_000)
    @Transactional
    protected void moneyCall(){
        int size = 100;
        int pageNumber = 0;
        Page<Credit> credits = creditService.findAllPageable(pageNumber, size);
        while (!credits.isEmpty()) {
            pageNumber++;
            credits = creditService.findAllPageable(pageNumber, size);
            credits.forEach(credit -> {
                CreditRule creditRule = credit.getCreditRule();
                withdraw(credit.getCardAccount(), credit.getDebt().add(creditRule.getPercentage().multiply(credit.getDebt().subtract(credit.getDebt()))));
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

    @Transactional
    protected void withdraw(UUID cardAccountId, BigDecimal money){
        WithdrawDto withdrawDto = new WithdrawDto();
        withdrawDto.setCardAccountId(cardAccountId);
        withdrawDto.setSum(money);
        coreClient.askForWithdraw(withdrawDto);
    }
}
