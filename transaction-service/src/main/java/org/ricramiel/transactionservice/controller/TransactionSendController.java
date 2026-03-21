package org.ricramiel.transactionservice.controller;

import lombok.RequiredArgsConstructor;
import org.ricramiel.common.dtos.EnrollDto;
import org.ricramiel.common.dtos.WithdrawDto;
import org.ricramiel.transactionservice.service.TransactionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionSendController {
    private final TransactionService transactionService;

    @PostMapping("/enroll")
    public void enrollMoney(@RequestBody EnrollDto enrollDto) {
        transactionService.enroll(enrollDto);
    }

    @PostMapping("/withdraw")
    public void withdrawMoney(@RequestBody WithdrawDto withdrawDto) {
        transactionService.withdraw(withdrawDto);
    }

}
