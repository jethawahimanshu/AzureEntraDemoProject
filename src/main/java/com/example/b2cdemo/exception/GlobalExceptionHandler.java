package com.example.b2cdemo.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler that produces RFC 7807 {@link ProblemDetail} responses.
 *
 * <p>Spring Boot 3.x has native support for {@code ProblemDetail} — no extra dependency needed.
 * Error details (type, title, status, detail) follow the RFC 7807 specification.
 *
 * <p>IMPORTANT: Never expose internal stack traces or implementation details to clients.
 * The generic 500 handler intentionally returns a vague message.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String BASE_TYPE = "https://example.com/errors";

    // ---- 400: Bean validation failures ----
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationFailure(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "invalid",
                        (first, second) -> first
                ));

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "One or more request fields are invalid");
        pd.setType(URI.create(BASE_TYPE + "/validation-failed"));
        pd.setTitle("Validation Failed");
        pd.setProperty("timestamp", Instant.now());
        pd.setProperty("errors", fieldErrors);
        return pd;
    }

    // ---- 401: Invalid / expired JWT (structurally valid but semantically wrong) ----
    @ExceptionHandler(InvalidBearerTokenException.class)
    public ProblemDetail handleInvalidBearerToken(InvalidBearerTokenException ex) {
        log.warn("Rejected bearer token: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, "The provided token is invalid or has expired");
        pd.setType(URI.create(BASE_TYPE + "/invalid-token"));
        pd.setTitle("Invalid Token");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    // ---- 403: Insufficient scope or missing role ----
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN, "Insufficient permissions to access this resource");
        pd.setType(URI.create(BASE_TYPE + "/access-denied"));
        pd.setTitle("Access Denied");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    // ---- 404: Resource not found ----
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setType(URI.create(BASE_TYPE + "/not-found"));
        pd.setTitle("Not Found");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    // ---- 409: Conflict ----
    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setType(URI.create(BASE_TYPE + "/conflict"));
        pd.setTitle("Conflict");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    // ---- 500: Catch-all — never expose internal details ----
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again or contact support.");
        pd.setType(URI.create(BASE_TYPE + "/internal-error"));
        pd.setTitle("Internal Server Error");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }
}
