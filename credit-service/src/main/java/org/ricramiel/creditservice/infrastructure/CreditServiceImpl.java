package org.ricramiel.creditservice.infrastructure;

import lombok.RequiredArgsConstructor;
import org.ricramiel.common.exceptions.status_code_exceptions.NotFoundException;
import org.ricramiel.creditservice.dto.CreditCreateModelDto;
import org.ricramiel.creditservice.dto.CreditDTO;
import org.ricramiel.creditservice.mapper.CreditMapper;
import org.ricramiel.creditservice.model.Credit;
import org.ricramiel.creditservice.model.CreditRule;
import org.ricramiel.creditservice.repository.CreditRepository;
import org.ricramiel.creditservice.repository.CreditRuleRepository;
import org.ricramiel.creditservice.service.CreditRuleService;
import org.ricramiel.creditservice.service.CreditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreditServiceImpl implements CreditService {

    private final CreditRepository creditRepository;
    private final CreditRuleRepository creditRuleRepository;

    @Override
    @Transactional
    public Credit createCredit(CreditCreateModelDto creditDTO) {
        CreditRule rule = creditRuleRepository.findById(creditDTO.getCreditRuleId())
                .orElseThrow(() -> new NotFoundException("Credit rule not found"));

        Credit credit = Credit.builder()
                .creditRule(rule)
                .totalDebt(creditDTO.getTotalDebt())
                .debt(BigDecimal.ZERO)
                .cardAccount(creditDTO.getCardAccount())
                .build();
        return creditRepository.save(credit);
    }

    @Override
    @Transactional
    public void deleteCredit(UUID creditId) {
        creditRepository.deleteById(creditId);
    }

    @Override
    public List<Credit> getByUserId(UUID userId) {
        return creditRepository.findByUserId(userId);
    }

    @Override
    public Page<Credit> findAllPageable(int pageNumber, int size) {
        return creditRepository.findAll(PageRequest.of(pageNumber, size));
    }

    @Override
    public Credit getByCardAccountId(UUID cardAccountId) {
        return creditRepository.findByCardAccount(cardAccountId);
    }

    @Override
    @Transactional
    public Credit makeEnrollment(UUID cardAccountId, BigDecimal money) {
        Credit credit = getByCardAccountId(cardAccountId);
        credit.setDebt(credit.getDebt().subtract(money));
        creditRepository.save(credit);
        return creditRepository.findByCardAccount(cardAccountId);
    }
}
