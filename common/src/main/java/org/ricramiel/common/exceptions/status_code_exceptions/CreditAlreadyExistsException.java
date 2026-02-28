package org.ricramiel.common.exceptions.status_code_exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CreditAlreadyExistsException extends RuntimeException {
    public CreditAlreadyExistsException(String message) {
        super(message);
    }
}