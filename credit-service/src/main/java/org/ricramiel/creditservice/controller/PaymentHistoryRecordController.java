package org.ricramiel.creditservice.controller;

import lombok.RequiredArgsConstructor;
import org.ricramiel.creditservice.dto.PaymentHistoryRecordDTO;
import org.ricramiel.creditservice.mapper.PaymentHistoryRecordMapper;
import org.ricramiel.creditservice.service.PaymentHistoryRecordService;
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

    @GetMapping("/{userId}/find_by_user_id")
    public List<PaymentHistoryRecordDTO> findByUserId(@PathVariable UUID userId){
        return PaymentHistoryRecordMapper.toDtoList(paymentHistoryRecordService.getHistoryByUserId(userId));
    }

    @GetMapping("/{cardAccountId}/find_by_card_account_id")
    public List<PaymentHistoryRecordDTO> findByCardAccountId(@PathVariable UUID cardAccountId){
        return PaymentHistoryRecordMapper.toDtoList(paymentHistoryRecordService.getHistoryByCardAccount(cardAccountId));
    }
}
