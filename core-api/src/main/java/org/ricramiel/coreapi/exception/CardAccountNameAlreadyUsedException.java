package org.ricramiel.coreapi.exception;

public class CardAccountNameAlreadyUsedException extends RuntimeException {
    public CardAccountNameAlreadyUsedException(String message) {
        super(message);
    }
}
