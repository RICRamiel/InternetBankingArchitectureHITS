package org.ricramiel.creditservice.infrastructure;

import lombok.RequiredArgsConstructor;
import org.ricramiel.common.exceptions.status_code_exceptions.NotFoundException;
import org.ricramiel.creditservice.dto.CreditRuleDTO;
import org.ricramiel.creditservice.mapper.CreditRuleMapper;
import org.ricramiel.creditservice.model.CreditRule;
import org.ricramiel.creditservice.repository.CreditRuleRepository;
import org.ricramiel.creditservice.service.CreditRuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreditRuleServiceImpl implements CreditRuleService {

    private final CreditRuleRepository creditRuleRepository;

    @Override
    @Transactional
    public CreditRule createCreditRule(CreditRuleDTO creditRuleDTO) {
        return creditRuleRepository.save(CreditRuleMapper.toCreditRule(creditRuleDTO));
    }

    @Override
    @Transactional
    public CreditRule editCreditRule(CreditRuleDTO creditRuleDTO, UUID creditRuleId) {
        return creditRuleRepository.save(CreditRuleMapper.toCreditRule(creditRuleDTO));
    }

    @Override
    @Transactional
    public void deleteCreditRule(UUID creditRuleId) {
        creditRuleRepository.deleteById(creditRuleId);
    }

    @Override
    public List<CreditRule> getAllCreditRules() {
        return creditRuleRepository.findAll();
    }

    @Override
    public CreditRule getCreditRuleById(UUID creditRuleId) {
        return creditRuleRepository.findById(creditRuleId).orElseThrow(() -> new NotFoundException("Credit rule not found"));
    }
}
