package com.samavya.backend.requirements;

public class InvalidRequirementStateException extends RuntimeException {

    public InvalidRequirementStateException() {
        super("Only open requirements can be cancelled");
    }
}
