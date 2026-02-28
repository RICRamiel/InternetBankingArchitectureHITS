package org.ricramiel.creditservice.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import lombok.SneakyThrows;
import org.ricramiel.common.dtos.EnrollDto;
import org.ricramiel.common.dtos.EventEnrollDto;
import org.ricramiel.common.exceptions.status_code_exceptions.NotFoundException;
import org.ricramiel.creditservice.dto.CreditCreateModelDto;
import org.ricramiel.creditservice.model.Credit;
import org.ricramiel.creditservice.model.CreditRule;
import org.ricramiel.creditservice.model.OutboxEvent;
import org.ricramiel.creditservice.repository.CreditRepository;
import org.ricramiel.creditservice.repository.CreditRuleRepository;
import org.ricramiel.creditservice.repository.OutboxRepository;
import org.ricramiel.creditservice.service.CreditService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CreditServiceImpl implements CreditService {

    private final CreditRepository creditRepository;
    private final CreditRuleRepository creditRuleRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    @Value("${app.kafka.destination}")
    private String destination;
    @Value("${type.enroll}")
    private String TYPE_ENROLL;
    @Value("${app.kafka.topics.enroll}")
    private String ENROLL_TRANSACTION_TOPIC;

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
                .userId(creditDTO.getUserId())
                .build();
        eventCreateCredit(credit);
        return creditRepository.save(credit);
    }

    @SneakyThrows
    private void eventCreateCredit(Credit credit){
        EnrollDto enrollDto = new EnrollDto(credit.getCardAccount(), credit.getTotalDebt(), destination);
        EventEnrollDto eventEnrollDto = new EventEnrollDto(UUID.randomUUID(), enrollDto, LocalDateTime.now(), TYPE_ENROLL);
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setOutboxTopic(ENROLL_TRANSACTION_TOPIC + "_" + enrollDto.getDestination());
        outboxEvent.setPayload(objectMapper.writeValueAsString(eventEnrollDto));
        outboxRepository.save(outboxEvent);
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
