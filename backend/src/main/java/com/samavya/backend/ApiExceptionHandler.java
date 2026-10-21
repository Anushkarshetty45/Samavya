package com.samavya.backend;

import com.samavya.backend.auth.EmailAlreadyExistsException;
import com.samavya.backend.auth.InvalidRegistrationException;
import com.samavya.backend.requirements.InvalidRequirementException;
import com.samavya.backend.requirements.InvalidRequirementStateException;
import com.samavya.backend.provider.DuplicateProviderOfferingException;
import com.samavya.backend.provider.InvalidGigStateException;
import com.samavya.backend.provider.InvalidProviderDataException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    ResponseEntity<ApiError> emailAlreadyExists() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("email_already_exists", "An account with this email already exists"));
    }

    @ExceptionHandler({InvalidRegistrationException.class, MethodArgumentNotValidException.class,
            ConstraintViolationException.class, InvalidRequirementException.class,
            HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            InvalidProviderDataException.class})
    ResponseEntity<ApiError> invalidRequest() {
        return ResponseEntity.badRequest()
                .body(new ApiError("invalid_request", "The request contains invalid input"));
    }

    @ExceptionHandler(InvalidRequirementStateException.class)
    ResponseEntity<ApiError> invalidRequirementState() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError(
                        "invalid_requirement_state",
                        "Only open requirements can be cancelled"));
    }

    @ExceptionHandler({InvalidGigStateException.class,
            DuplicateProviderOfferingException.class})
    ResponseEntity<ApiError> providerConflict(RuntimeException exception) {
        if (exception instanceof DuplicateProviderOfferingException) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiError(
                            "duplicate_provider_offering",
                            "An offering already exists for this community and category"));
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("invalid_gig_state", exception.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ApiError> invalidCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError("invalid_credentials", "Invalid email or password"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> accessDenied() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiError("forbidden", "Access to this resource is denied"));
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiError> responseStatus(ResponseStatusException exception) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        String code = switch (status) {
            case NOT_FOUND -> "not_found";
            case FORBIDDEN -> "forbidden";
            case BAD_REQUEST -> "invalid_request";
            default -> "request_failed";
        };
        String message = switch (status) {
            case NOT_FOUND -> "The requested resource was not found";
            case FORBIDDEN -> "Access to this resource is denied";
            case BAD_REQUEST -> "The request contains invalid input";
            default -> "The request could not be completed";
        };
        return ResponseEntity.status(status).body(new ApiError(code, message));
    }
}
