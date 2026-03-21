package org.ricramiel.creditservice.infrastructure;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@AllArgsConstructor
public class AccessChecker {
    private final CurrentUserService userService;

    public boolean isSelf(@NonNull UUID userId) {
        UUID currentUserId = userService.getUserId();
        return userId.equals(currentUserId);
    }
}