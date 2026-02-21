package org.ricramiel.userservice.domain.services;

import org.ricramiel.userservice.domain.models.entities.User;

import java.util.UUID;

public interface CurrentUserService {
    User getUser();

    String getEmail();

    UUID getId();
}
