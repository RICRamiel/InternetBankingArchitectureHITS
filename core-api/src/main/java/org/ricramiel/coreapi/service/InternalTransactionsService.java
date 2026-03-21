package org.ricramiel.coreapi.service;

import lombok.RequiredArgsConstructor;
import org.ricramiel.common.dtos.EnrollDto;
import org.ricramiel.common.dtos.WithdrawDto;
import org.ricramiel.coreapi.model.TransferRequest;
import org.ricramiel.common.enums.TransactionStatus;
import org.ricramiel.common.enums.TransactionType;
import org.ricramiel.common.exceptions.status_code_exceptions.NotFoundException;
import org.ricramiel.coreapi.entity.CardAccount;
import org.ricramiel.coreapi.entity.TransactionOperation;
import org.ricramiel.coreapi.model.EnrollRequest;
import org.ricramiel.coreapi.model.TransferResult;
import org.ricramiel.coreapi.model.WithdrawRequest;
import org.ricramiel.coreapi.repository.CardAccountRepository;
import org.ricramiel.coreapi.repository.TransactionOperationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InternalTransactionsService {
    private final CardAccountRepository cardAccountRepository;
    private final TransactionOperationRepository transactionOperationRepository;

    @Value("${application.master_account.id}")
    private UUID masterAccountId;

    @Transactional
    public TransferResult withdrawToMasterAccount(WithdrawRequest withdrawDto, String action) {
        EnrollRequest enrollDto = EnrollRequest.builder()
                .cardAccountId(masterAccountId)
                .sum(withdrawDto.getSum())
                .currency(withdrawDto.getCurrency())
                .build();

        return transfer(enrollDto, withdrawDto, action, "Перевод от: " + withdrawDto.getCardAccountId());
    }

    @Transactional
    public TransferResult enrollFromMasterAccount(EnrollRequest enrollDto, String action) {
        WithdrawRequest withdrawDto = WithdrawRequest.builder()
                .cardAccountId(masterAccountId)
                .sum(enrollDto.getSum())
                .currency(enrollDto.getCurrency())
                .build();

        return transfer(enrollDto, withdrawDto, "Перевод на: " + enrollDto.getCardAccountId(), action);
    }

    @Transactional
    public TransferResult transfer(TransferRequest model) {
        return transfer(
                model,
                "Перевод на: " + model.getToCardAccountId(),
                "Перевод от: " + model.getFromCardAccountId()
        );
    }

    @Transactional
    public TransferResult transfer(TransferRequest model, String withdrawAction, String enrollAction) {
        WithdrawRequest withdrawDto = WithdrawRequest.builder()
                .cardAccountId(model.getFromCardAccountId())
                .sum(model.getSum())
                .currency(model.getCurrency())
                .build();

        EnrollRequest enrollDto = EnrollRequest.builder()
                .cardAccountId(model.getToCardAccountId())
                .sum(model.getSum())
                .currency(model.getCurrency())
                .build();

        return transfer(enrollDto, withdrawDto, withdrawAction,  enrollAction);
    }

    @Transactional
    public TransferResult transfer(EnrollRequest enrollDto, WithdrawRequest withdrawDto,String withdrawAction, String enrollAction) {
        TransactionOperation savedWithdrawal = withdraw(withdrawDto, withdrawAction);
        if (savedWithdrawal.getTransactionStatus() == TransactionStatus.COMPLETE){
            TransactionOperation savedEnrollment = enroll(enrollDto, enrollAction);
            return new TransferResult(savedWithdrawal, savedEnrollment);
        }
        return new TransferResult(savedWithdrawal, null);
    }

    public TransactionOperation enroll(EnrollRequest request, String action) {
        CardAccount account = cardAccountRepository.findById(request.getCardAccountId())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        account.setMoney(account.getMoney().add(request.getSum()));
        cardAccountRepository.save(account);

        TransactionOperation transactionOperation = new TransactionOperation();
        transactionOperation.setAccount(account);
        transactionOperation.setCurrency(request.getCurrency().toUpperCase());
        transactionOperation.setMoney(request.getSum());
        transactionOperation.setTransactionType(TransactionType.ENROLLMENT);
        transactionOperation.setTransactionStatus(TransactionStatus.COMPLETE);
        transactionOperation.setAction(action);
        transactionOperation.setDateTime(LocalDateTime.now());
        return transactionOperationRepository.save(transactionOperation);
    }

    public TransactionOperation withdraw(WithdrawRequest request, String action) {
        CardAccount account = cardAccountRepository.findById(request.getCardAccountId())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        TransactionOperation transactionOperation = new TransactionOperation();
        if (account.getMoney().compareTo(request.getSum()) >= 0) {
            account.setMoney(account.getMoney().subtract(request.getSum()));
            cardAccountRepository.save(account);
            transactionOperation.setTransactionStatus(TransactionStatus.COMPLETE);
        } else {
            transactionOperation.setTransactionStatus(TransactionStatus.DECLINED);
        }
        transactionOperation.setAccount(account);
        transactionOperation.setCurrency(request.getCurrency().toUpperCase());
        transactionOperation.setMoney(request.getSum());
        transactionOperation.setTransactionType(TransactionType.WITHDRAWAL);
        transactionOperation.setAction(action);
        transactionOperation.setDateTime(LocalDateTime.now());
        return transactionOperationRepository.save(transactionOperation);
    }
}
