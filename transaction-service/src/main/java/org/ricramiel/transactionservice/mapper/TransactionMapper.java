package org.ricramiel.transactionservice.mapper;

import org.ricramiel.common.dtos.EnrollDto;
import org.ricramiel.common.dtos.TransactionKafkaDto;
import org.ricramiel.common.dtos.WithdrawDto;
import org.ricramiel.common.enums.TransactionStatus;
import org.ricramiel.common.enums.TransactionType;
import org.ricramiel.transactionservice.entity.OperationHistory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class TransactionMapper {
    public static TransactionKafkaDto toTransactionKafkaDto(WithdrawDto dto, String action) {
        return new TransactionKafkaDto(
                UUID.randomUUID(),
                dto.getCardAccountId(),
                LocalDateTime.now(),
                TransactionType.WITHDRAWAL,
                TransactionStatus.IN_PROGRESS,
                action, //maybe dto.getDestination()?
                dto.getSum(),
                dto.getCurrency().toUpperCase()
        );
    }

    public static TransactionKafkaDto toTransactionKafkaDto(EnrollDto dto, String action) {
        return new TransactionKafkaDto(
                UUID.randomUUID(),
                dto.getCardAccountId(),
                LocalDateTime.now(),
                TransactionType.ENROLLMENT,
                TransactionStatus.IN_PROGRESS,
                action, //maybe dto.getDestination()?
                dto.getSum(),
                dto.getCurrency().toUpperCase()
        );
    }

    public static OperationHistory fromTransactionKafkaDto(TransactionKafkaDto dto) {
        return new OperationHistory(
                dto.getSourceId(),
                dto.getTransactionType(),
                dto.getMoney(),
                dto.getAction(),
                dto.getTransactionStatus(),
                dto.getAccountId()
        );
    }
}
