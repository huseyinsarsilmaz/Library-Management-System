package com.huseyinsarsilmaz.lms.exception;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.huseyinsarsilmaz.lms.model.dto.response.ApiResponse;
import com.huseyinsarsilmaz.lms.util.ResponseBuilder;

import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ResponseBuilder responseBuilder;

    @ExceptionHandler(LmsException.class)
    public ResponseEntity<ApiResponse<String>> handleLmsException(LmsException ex) {
        return responseBuilder.fail(ex.getType(), ex.getArgs(), ex.getMessage(), ex.getHttpStatus());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<String>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {
        return responseBuilder.fail("invalid", new String[] { "Request" }, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return responseBuilder.fail("invalid", new String[] { "Request" }, errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<String>> handleBadCredentialsException(BadCredentialsException ex) {
        return responseBuilder.fail("failed", new String[] { "User login" }, ex.getMessage(), HttpStatus.UNAUTHORIZED);

    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<String>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {
        String name = ex.getName();

        Object valueObj = ex.getValue();
        String value = (valueObj != null) ? valueObj.toString() : "null";

        Class<?> requiredTypeClass = ex.getRequiredType();
        String type = "unknown";
        String validValueList = "";

        if (requiredTypeClass != null) {
            type = requiredTypeClass.getSimpleName();

            if (requiredTypeClass.isEnum()) {
                Object[] constants = requiredTypeClass.getEnumConstants();
                String validValues = Arrays.stream(constants)
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));
                validValueList = "Valid Values: " + validValues;
            }
        }

        return responseBuilder.fail("invalid.type", new String[] { name, type, value, validValueList }, ex.getMessage(),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<ApiResponse<String>> handleInternalAuthenticationServiceException(
            InternalAuthenticationServiceException ex) {
        if (ex.getCause() instanceof NotFoundException) {
            // This case only occurs when the user enters a non-existent email in login
            return responseBuilder.fail("not.found", new String[] { "User", "email" }, ex.getMessage(),
                    HttpStatus.NOT_FOUND);

        }
        return responseBuilder.fail("general", new String[] {}, ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGeneralException(Exception ex) {
        return responseBuilder.fail("general", new String[] {}, ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}