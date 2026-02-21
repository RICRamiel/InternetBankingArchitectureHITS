package org.ricramiel.userservice.unit.infrastructure.services;

import jakarta.persistence.EntityNotFoundException;
import org.ricramiel.common.enums.Role;
import org.ricramiel.userservice.domain.models.entities.User;
import org.ricramiel.userservice.domain.models.requests.UserEditRequestModel;
import org.ricramiel.userservice.infrastructure.repositories.UserRepository;
import org.ricramiel.userservice.infrastructure.services.UsersServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UsersServiceImplTests {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UsersServiceImpl userService;

    private UUID testId;
    private User testUser;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testUser = User.builder()
                .id(testId)
                .email("test@ricramiel.com")
                .name("Test User")
                .roles(Set.of(Role.USER))
                .isActive(true)
                .build();
    }

    @Test
    public void getUserById_ShouldReturnUser() {
        when(userRepository.findById(testId)).thenReturn(Optional.of(testUser));

        User result = userService.getUserById(testId);

        assertThat(result)
                .hasFieldOrPropertyWithValue("id", testId)
                .hasFieldOrPropertyWithValue("email", "test@ricramiel.com");
        verify(userRepository, times(1)).findById(testId);
    }

    @Test
    public void deleteUserById_ShouldSetActiveToFalse() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.<User>getArgument(0));
        when(userRepository.findById(testId)).thenReturn(Optional.of(testUser));

        User result = userService.deleteUserById(testId);

        assertThat(result)
                .hasFieldOrPropertyWithValue("isActive", false);
        verify(userRepository, times(1)).findById(testId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void editUserById_ShouldReturnEditedUser() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.<User>getArgument(0));
        when(userRepository.findById(testId)).thenReturn(Optional.of(testUser));

        User result = userService.editUserById(testId, new UserEditRequestModel("newName"));

        assertThat(result)
                .hasFieldOrPropertyWithValue("id", testId)
                .hasFieldOrPropertyWithValue("name", "newName");
        verify(userRepository, times(1)).findById(testId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void getUser_WithNonExistingUser_ThrowsException() {
        assertThatThrownBy(() -> userService.getUserById(UUID.randomUUID()))
                .isInstanceOf(EntityNotFoundException.class);
    }
}