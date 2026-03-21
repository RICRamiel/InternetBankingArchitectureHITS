package org.ricramiel.userservice.domain.models.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.ricramiel.common.enums.Role;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
public class UserEditRequestModel {
    private Set<Role> roles;
}