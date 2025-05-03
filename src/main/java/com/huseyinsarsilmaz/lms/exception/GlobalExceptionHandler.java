package com.huseyinsarsilmaz.lms.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.huseyinsarsilmaz.lms.model.dto.response.ApiResponse;
import com.huseyinsarsilmaz.lms.service.Utils;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LmsException.class)
    public ResponseEntity<ApiResponse> handleLmsException(LmsException ex) {
        return Utils.failResponse(ex.getType(), ex.getArgs(), ex.getMessage(), ex.getHttpStatus());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        return Utils.failResponse("invalid", new String[] { "Request" }, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return Utils.failResponse("invalid", new String[] { "Request" }, errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse> handleBadCredentialsException(BadCredentialsException ex) {
        return Utils.failResponse("failed", new String[] { "User login" }, ex.getMessage(), HttpStatus.UNAUTHORIZED);

    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<ApiResponse> handleInternalAuthenticationServiceException(
            InternalAuthenticationServiceException ex) {
        if (ex.getCause() instanceof NotFoundException) {
            // This case only occurs when the user enters a non-existent email in login
            return Utils.failResponse("not.found", new String[] { "User", "email" }, ex.getMessage(),
                    HttpStatus.NOT_FOUND);

        }
        return Utils.failResponse("general", new String[] {}, ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGeneralException(Exception ex) {
        return Utils.failResponse("general", new String[] {}, ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}