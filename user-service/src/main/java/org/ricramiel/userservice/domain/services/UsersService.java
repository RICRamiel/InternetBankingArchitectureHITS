package org.ricramiel.userservice.domain.services;

import jakarta.validation.Valid;
import lombok.NonNull;
import org.ricramiel.userservice.domain.models.entities.User;
import org.ricramiel.userservice.domain.models.requests.UserEditRequestModel;

import java.util.UUID;

public interface UsersService {
    User getUserById(@NonNull UUID id);

    User editUserById(@NonNull UUID id, @Valid @NonNull UserEditRequestModel userEditModel);

    User deleteUserById(@NonNull UUID id);
}
