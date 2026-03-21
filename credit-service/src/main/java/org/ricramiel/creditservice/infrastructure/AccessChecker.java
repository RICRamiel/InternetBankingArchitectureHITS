package org.ricramiel.creditservice.infrastructure;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.ricramiel.creditservice.model.Credit;
import org.ricramiel.creditservice.repository.CreditRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@AllArgsConstructor
public class AccessChecker {
    private final CurrentUserService userService;
    private final CreditRepository creditRepository;

    public boolean isSelf(@NonNull UUID userId) {
        UUID currentUserId = userService.getUserId();
        return userId.equals(currentUserId);
    }

    public boolean isCreditOwner(UUID creditId) {
        Credit credit = creditRepository.findById(creditId)
                .orElseThrow(() -> new EntityNotFoundException("Credit not found with id: " + creditId));

        return credit.getUserId().equals(userService.getUserId());
    }

    public boolean isAccountOwner(UUID cardAccountId){
        Credit credit = creditRepository.findByCardAccount(cardAccountId);

        return credit.getUserId().equals(userService.getUserId());
    }
}