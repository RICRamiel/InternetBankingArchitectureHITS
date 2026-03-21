package org.ricramiel.creditservice.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import lombok.SneakyThrows;
import org.ricramiel.common.dtos.EventTransactionDto;
import org.ricramiel.common.dtos.TransactionKafkaDto;
import org.ricramiel.common.enums.TransactionStatus;
import org.ricramiel.common.enums.TransactionType;
import org.ricramiel.common.exceptions.status_code_exceptions.CreditAlreadyExistsException;
import org.ricramiel.common.exceptions.status_code_exceptions.NotFoundException;
import org.ricramiel.creditservice.dto.CreditCreateModelDto;
import org.ricramiel.creditservice.model.Credit;
import org.ricramiel.creditservice.model.CreditRating;
import org.ricramiel.creditservice.model.CreditRule;
import org.ricramiel.creditservice.model.OutboxEvent;
import org.ricramiel.creditservice.repository.CreditRatingRepository;
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
    private final CreditRatingRepository creditRatingRepository;

    @Value("${app.kafka.topics.enroll}")
    private String ENROLL_TRANSACTION_TOPIC;

    @Value("${type.enroll}")
    private String TYPE;

    @Override
    @Transactional
    public Credit createCredit(CreditCreateModelDto creditDTO) {
        CreditRule rule = creditRuleRepository.findById(creditDTO.getCreditRuleId())
                .orElseThrow(() -> new NotFoundException("Credit rule not found"));

        if(creditRepository.existsByCardAccount(creditDTO.getCardAccount())){
            throw new CreditAlreadyExistsException("Credit on this card account already exists");
        }

        Credit credit = Credit.builder()
                .creditRule(rule)
                .initialDebt(creditDTO.getTotalDebt())
                .currentDebtSum(BigDecimal.ZERO)
                .interestDebtSum(BigDecimal.ZERO)
                .currency(creditDTO.getCurrency())
                .cardAccount(creditDTO.getCardAccount())
                .userId(creditDTO.getUserId())
                .lastInterestUpdate(LocalDateTime.now())
                .build();
        eventCreateCredit(credit);

        CreditRating creditRating = new CreditRating();

        creditRating.setRating(BigDecimal.valueOf(100));
        creditRating.setUserId(credit.getUserId());
        creditRatingRepository.save(creditRating);

        return creditRepository.save(credit);
    }

    @SneakyThrows
    private void eventCreateCredit(Credit credit){

        TransactionKafkaDto transactionKafkaDto = new TransactionKafkaDto(
                null,
                credit.getCardAccount(),
                LocalDateTime.now(),
                TransactionType.ENROLLMENT,
                TransactionStatus.IN_PROGRESS,
                "ENROLL",
                credit.getInitialDebt(),
                credit.getCurrency());

        EventTransactionDto eventTransactionDto = new EventTransactionDto();
        eventTransactionDto.setDestination(TYPE);
        eventTransactionDto.setData(transactionKafkaDto);
        eventTransactionDto.setId(UUID.randomUUID());
        eventTransactionDto.setCreationDate(LocalDateTime.now());

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setOutboxTopic(ENROLL_TRANSACTION_TOPIC);
        outboxEvent.setPayload(objectMapper.writeValueAsString(eventTransactionDto));
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
        //остаток в случае, если сумма которую пользователь отправил на погашение долга превышает сумму, накаповшую с процентов
        BigDecimal remainings = BigDecimal.ZERO;

        if(credit.getInterestDebtSum().compareTo(money) < 0){
            remainings = money.subtract(credit.getInterestDebtSum());
            money = credit.getInterestDebtSum();
        }

        credit.setInterestDebtSum(credit.getInterestDebtSum().subtract(money));

        credit.setCurrentDebtSum(credit.getCurrentDebtSum().subtract(remainings));

        creditRepository.save(credit);
        return creditRepository.findByCardAccount(cardAccountId);
    }
}
