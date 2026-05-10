package org.ricramiel.userservice.api.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ricramiel.common.dtos.UserDto;
import org.ricramiel.userservice.api.dtos.UserEditModelDto;
import org.ricramiel.userservice.api.mappers.UserEditModelMapper;
import org.ricramiel.userservice.api.mappers.UserMapper;
import org.ricramiel.userservice.domain.services.UsersService;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UsersController {
    private final UserEditModelMapper userEditModelMapper;
    private final UserMapper userMapper;
    private final UsersService usersService;

    @GetMapping("{id}")
    @PreAuthorize("hasRole('WORKER') OR @accessChecker.isSelf(#id)")
    public UserDto getUserById(@PathVariable("id") @Param("id") UUID id) {
        return userMapper.toDto(usersService.getUserById(id));
    }

    @GetMapping("{id}/isActive")
    @PreAuthorize("hasRole('WORKER') OR @accessChecker.isSelf(#id)")
    public Boolean isUserActiveById(@PathVariable("id") @Param("id") UUID id) {
        return usersService.getUserById(id).isActive();
    }

    @GetMapping
    public List<UserDto> getAllUsers() {
        return usersService.getAllUsers().stream().map(userMapper::toDto).toList();
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('WORKER')")
    public void deleteUserById(@PathVariable("id") @Param("id") UUID id) {
        usersService.deleteUserById(id);
    }

    @PreAuthorize("hasRole('WORKER')")
    @PutMapping("{id}/edit")
    public void editUser(
            @PathVariable("id") @Param("id") UUID id,
            @Valid @RequestBody UserEditModelDto model
    ) {
        usersService.editUserById(id, userEditModelMapper.toDomain(model));
    }

    @GetMapping("/by-email/{email}")
    public UserDto getUserByEmail(
            @PathVariable("email") @Param("email") String email
    ) {
        return userMapper.toDto(usersService.getUserByEmail(email));
    }
}