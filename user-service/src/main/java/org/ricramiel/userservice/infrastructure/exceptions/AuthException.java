package org.ricramiel.userservice.infrastructure.exceptions;

import org.ricramiel.common.exceptions.status_code_exceptions.UnauthorizedException;

public class AuthException extends UnauthorizedException {
    public AuthException(String message) {
        super(message);
    }
}