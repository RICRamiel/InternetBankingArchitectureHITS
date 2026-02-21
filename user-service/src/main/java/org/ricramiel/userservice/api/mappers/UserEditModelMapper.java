package org.ricramiel.userservice.api.mappers;

import org.ricramiel.userservice.api.dtos.UserEditModelDto;
import org.ricramiel.userservice.domain.models.requests.UserEditRequestModel;
import org.springframework.stereotype.Component;

@Component
public class UserEditModelMapper {
    public UserEditModelDto toDto(UserEditRequestModel model) {
        return new UserEditModelDto(
                model.getName()
        );
    }
    public UserEditRequestModel toDomain(UserEditModelDto model) {
        return new UserEditRequestModel(
                model.getName()
        );
    }
}