package org.ricramiel.userservice.api.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ricramiel.common.dtos.UserDto;
import org.ricramiel.userservice.api.dtos.*;
import org.ricramiel.userservice.api.dtos.UserEditModelDto;
import org.ricramiel.userservice.api.mappers.UserEditModelMapper;
import org.ricramiel.userservice.api.mappers.UserMapper;
import org.ricramiel.userservice.domain.services.CurrentUserService;
import org.ricramiel.userservice.domain.services.UsersService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("account")
@RequiredArgsConstructor
@Tag(name = "Account")
public class CurrentUserController {
    private final CurrentUserService currentUserService;
    private final UserMapper userMapper;
    private final UserEditModelMapper userEditModelMapper;
    private final UsersService usersService;

    @GetMapping()
    public UserDto getUser() {
        return userMapper.toDto(currentUserService.getUser());
    }

    @PreAuthorize("hasRole('WORKER')")
    @PutMapping("edit")
    public void editUser(@Valid @RequestBody UserEditModelDto model) {
        usersService.editUserById(currentUserService.getId(), userEditModelMapper.toDomain(model));
    }
}