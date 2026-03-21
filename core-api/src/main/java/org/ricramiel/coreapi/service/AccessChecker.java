package org.ricramiel.coreapi.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.ricramiel.coreapi.entity.CardAccount;
import org.ricramiel.coreapi.repository.CardAccountRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@AllArgsConstructor
public class AccessChecker {
    private final CurrentUserService userService;
    private final CardAccountRepository cardAccountRepository;

    public boolean isSelf(@NonNull UUID userId) {
        UUID currentUserId = userService.getUserId();
        return userId.equals(currentUserId);
    }

    public boolean isCardAccountOwner(UUID cardAccountId) {
        CardAccount cardAccount = cardAccountRepository.findById(cardAccountId)
                .orElseThrow(() -> new EntityNotFoundException("Card account not found with id: " + cardAccountId));

        return cardAccount.getUserId().equals(userService.getUserId());
    }
}