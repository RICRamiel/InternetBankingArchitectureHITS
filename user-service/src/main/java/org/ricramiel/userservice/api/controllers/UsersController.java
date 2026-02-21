package org.ricramiel.userservice.api.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.ricramiel.common.dtos.UserDto;
import org.ricramiel.userservice.api.mappers.UserMapper;
import org.ricramiel.userservice.domain.services.UsersService;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("users")
@RequiredArgsConstructor
@Tag(name = "Users")
public class UsersController {
    private final UserMapper userMapper;
    private final UsersService usersService;

    @GetMapping("{id}")
    @PreAuthorize("hasRole('ADMIN') OR @accessChecker.isSelf(#id)")
    public UserDto getUserById(@PathVariable("id") @Param("id") UUID id) {
        return userMapper.toDto(usersService.getUserById(id));
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasRole('ADMIN') OR @accessChecker.isSelf(#id)")
    public void deleteUserById(@PathVariable("id") @Param("id") UUID id) {
        usersService.deleteUserById(id);
    }
}