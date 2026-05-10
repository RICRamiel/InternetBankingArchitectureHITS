package org.ricramiel.creditservice.controller;

import lombok.RequiredArgsConstructor;
import org.ricramiel.creditservice.dto.PaymentHistoryRecordDTO;
import org.ricramiel.creditservice.mapper.PaymentHistoryRecordMapper;
import org.ricramiel.creditservice.service.PaymentHistoryRecordService;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/payment_history_record")
public class PaymentHistoryRecordController {

    private final PaymentHistoryRecordService paymentHistoryRecordService;

    @PreAuthorize("hasRole('WORKER') OR @accessChecker.isSelf(#userId)")
    @GetMapping("/{userId}/find_by_user_id")
    public List<PaymentHistoryRecordDTO> findByUserId(@PathVariable("userId") @Param("userId") UUID userId){
        return PaymentHistoryRecordMapper.toDtoList(paymentHistoryRecordService.getHistoryByUserId(userId));
    }

    @PreAuthorize("hasRole('WORKER') OR @accessChecker.isOwner(#cardAccountId)")
    @GetMapping("/{cardAccountId}/find_by_card_account_id")
    public List<PaymentHistoryRecordDTO> findByCardAccountId(@PathVariable("cardAccountId") @Param("cardAccountId") UUID cardAccountId){
        return PaymentHistoryRecordMapper.toDtoList(paymentHistoryRecordService.getHistoryByCardAccount(cardAccountId));
    }
}
