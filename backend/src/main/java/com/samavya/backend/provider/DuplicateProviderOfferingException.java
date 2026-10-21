package com.samavya.backend.provider;

public class DuplicateProviderOfferingException extends RuntimeException {

    public DuplicateProviderOfferingException() {
        super("An offering already exists for this community and category");
    }
}
