package org.ricramiel.coreapi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ricramiel.coreapi.entity.TransactionOperation;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketPushService {

    private final SimpMessagingTemplate messagingTemplate;

    public void pushToAccount(UUID accountId, TransactionOperation transactionOperation) {
        String destination = "/topic/account/" + accountId + "/transactions";
        messagingTemplate.convertAndSend(destination, transactionOperation);
        log.info("Pushed {} transaction to account {}", transactionOperation.getId(), accountId);
    }
}