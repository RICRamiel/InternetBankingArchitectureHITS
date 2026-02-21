package org.ricramiel.userservice.domain.services;

import jakarta.validation.Valid;
import lombok.NonNull;
import org.ricramiel.userservice.domain.models.entities.User;
import org.ricramiel.userservice.domain.models.requests.LoginRequestModel;
import org.ricramiel.userservice.domain.models.requests.RegisterRequestModel;
import org.ricramiel.userservice.domain.models.responses.JwtModel;

public interface AuthService {
    boolean isTokenValid(@NonNull String tokenValue);

    JwtModel login(@Valid @NonNull LoginRequestModel authRequest);

    void revoke(@NonNull String refreshTokenValue);

    JwtModel register(@Valid @NonNull RegisterRequestModel authRequest);

    JwtModel refreshAndRotate(@Valid @NonNull String refreshTokenValue);

    JwtModel createAndSaveJwtToken(@Valid @NonNull User user);
}
