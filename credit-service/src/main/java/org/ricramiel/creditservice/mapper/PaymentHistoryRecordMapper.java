package org.ricramiel.creditservice.mapper;


import org.ricramiel.creditservice.dto.PaymentHistoryRecordDTO;
import org.ricramiel.creditservice.model.PaymentHistoryRecord;

import java.util.List;
import java.util.stream.Collectors;

public class PaymentHistoryRecordMapper {

    public static PaymentHistoryRecordDTO toDto(PaymentHistoryRecord paymentHistoryRecord){

        if(paymentHistoryRecord == null){
            return null;
        }

        PaymentHistoryRecordDTO paymentHistoryRecordDTO = new PaymentHistoryRecordDTO();
        paymentHistoryRecordDTO.setId(paymentHistoryRecord.getId());
        paymentHistoryRecordDTO.setDate(paymentHistoryRecord.getDate());
        paymentHistoryRecordDTO.setCardAccount(paymentHistoryRecord.getCardAccount());
        paymentHistoryRecordDTO.setUserId(paymentHistoryRecord.getUserId());
        paymentHistoryRecordDTO.setSum(paymentHistoryRecord.getSum());

        return paymentHistoryRecordDTO;
    }

    public static List<PaymentHistoryRecordDTO> toDtoList(List<PaymentHistoryRecord> paymentHistoryRecords){

        if(paymentHistoryRecords == null){
            return null;
        }

        return  paymentHistoryRecords.stream().map(PaymentHistoryRecordMapper::toDto).collect(Collectors.toList());
    }
}
