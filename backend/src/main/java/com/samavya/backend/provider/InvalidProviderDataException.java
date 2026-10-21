package com.samavya.backend.provider;

public class InvalidProviderDataException extends RuntimeException {

    public InvalidProviderDataException(String message) {
        super(message);
    }
}
