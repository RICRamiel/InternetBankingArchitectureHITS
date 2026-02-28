package org.ricramiel.creditservice.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
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
            credits.forEach(credit -> {
                CreditRule creditRule = credit.getCreditRule();

                long iterationsAmount = Duration.between(LocalDateTime.now(), credit.getLastInterestUpdate()).getSeconds() / creditRule.getCollectionPeriodSeconds();

                if (creditRule.getPercentageStrategy().equals(PercentageStrategy.FROM_REMAINING_DEBT)) {
                    for (int i = 0; i < iterationsAmount; i++) {
                        credit.setLastInterestUpdate(LocalDateTime.now());
                        BigDecimal interest = calcInterest(credit.getTotalDebt(), creditRule.getPercentage());
                        credit.setDebt(credit.getDebt().add(interest));
                        credit.setTotalDebt(credit.getTotalDebt().subtract(interest));
                    }
                }

                creditRepository.save(credit);
            });
            pageNumber++;
            credits = creditService.findAllPageable(pageNumber, size);
        }
    }

    @Scheduled(fixedRate = 60_000)
    @Transactional
    protected void moneyCall() {
        int size = 100;
        int pageNumber = 0;
        Page<Credit> credits = creditService.findAllPageable(pageNumber, size);

        while (!credits.isEmpty()) {
            credits.forEach(credit -> {

                if(!credit.getDebt().equals(BigDecimal.ZERO)){
                    withdraw(credit.getCardAccount(), credit.getDebt());
                }
            });
            pageNumber++;
            credits = creditService.findAllPageable(pageNumber, size);
        }
    }

    private BigDecimal calcInterest(BigDecimal to, BigDecimal percentage) {
        return to.multiply(percentage).divide(new BigDecimal(100));
    }

    @Transactional
    public void withdraw(UUID cardAccountId, BigDecimal money) {
        WithdrawDto withdrawDto = new WithdrawDto();
        withdrawDto.setCardAccountId(cardAccountId);
        withdrawDto.setSum(money);
        withdrawDto.setDestination("credit");
        coreClient.askForWithdraw(withdrawDto);
    }
}
