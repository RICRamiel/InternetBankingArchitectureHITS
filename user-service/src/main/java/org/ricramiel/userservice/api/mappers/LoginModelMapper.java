package org.ricramiel.userservice.api.mappers;

import org.ricramiel.userservice.api.dtos.LoginModelDto;
import org.ricramiel.userservice.domain.models.requests.LoginRequestModel;
import org.springframework.stereotype.Component;

@Component
public class LoginModelMapper {
    public LoginRequestModel toDomain(LoginModelDto model) {
        return new LoginRequestModel(
                model.getEmail(),
                model.getPassword()
        );
    }
    public LoginModelDto toDto(LoginRequestModel model) {
        return new LoginModelDto(
                model.getEmail(),
                model.getPassword()
        );
    }
}