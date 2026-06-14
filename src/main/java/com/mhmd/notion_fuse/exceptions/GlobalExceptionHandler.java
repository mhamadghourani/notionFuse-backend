package com.mhmd.notion_fuse.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Handles the Notion Database column structural bug
    @ExceptionHandler(InvalidNotionParentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidNotionParent(InvalidNotionParentException ex) {
        ErrorResponse errorPayload = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorPayload, HttpStatus.BAD_REQUEST);
    }

    // 2. Handles bad logins (wrong password / user doesn't exist)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        ErrorResponse errorPayload = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Invalid username or password.",
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorPayload, HttpStatus.UNAUTHORIZED);
    }

    // 3. Handles expired JWTs or completely missing security context tokens
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        ErrorResponse errorPayload = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorPayload, HttpStatus.UNAUTHORIZED);
    }

    // 4. Fallback Catch-All: Prevents raw Java stack traces from leaking to your user interface
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        ErrorResponse errorPayload = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected server error occurred.",
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorPayload, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
