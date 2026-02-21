package org.ricramiel.userservice.api.mappers;

import org.ricramiel.common.dtos.UserDto;
import org.ricramiel.userservice.domain.models.entities.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDto toDto(User model) {
        return new UserDto(
                model.getId(),
                model.getName(),
                model.getEmail(),
                model.isActive(),
                model.getRoles()
        );
    }
}