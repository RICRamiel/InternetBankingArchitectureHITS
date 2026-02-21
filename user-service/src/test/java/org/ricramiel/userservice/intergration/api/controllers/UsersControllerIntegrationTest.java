package org.ricramiel.userservice.intergration.api.controllers;

import org.ricramiel.userservice.infrastructure.services.JwtAccessTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.ricramiel.common.enums.Role;
import org.ricramiel.userservice.domain.models.entities.User;
import org.ricramiel.userservice.infrastructure.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integrationtest")
class UsersControllerIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtAccessTokenUtil jwtAccessTokenUtil;

    private User adminUser;
    private String adminToken;

    @BeforeEach
    void setUpAdminUser(){
        adminUser = userRepository.save(
                User.builder()
                        .email("admin@admin.com")
                        .name("admin")
                        .roles(Set.of(Role.USER, Role.ADMIN))
                        .isActive(true)
                        .password("adminpass")
                        .build()
        );
        adminToken = jwtAccessTokenUtil.generateToken(adminUser);
    }

    @Test
    void getUser_ShouldReturnUser(){
        User savedUser = userRepository.save(
                User.builder()
                        .email("test@ricramiel.com")
                        .name("test")
                        .roles(Set.of(Role.USER))
                        .isActive(true)
                        .password("password")
                        .build()
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        ResponseEntity<User> response = restTemplate.exchange(
                "/users/" + savedUser.getId(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                User.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getEmail()).isEqualTo("test@ricramiel.com");
    }
}