package com.samavya.backend;

import com.samavya.backend.auth.EmailAlreadyExistsException;
import com.samavya.backend.auth.InvalidRegistrationException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
            ConstraintViolationException.class})
    ResponseEntity<ApiError> invalidRequest() {
        return ResponseEntity.badRequest()
                .body(new ApiError("invalid_request", "The request contains invalid input"));
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
