package com.huseyinsarsilmaz.lms.util;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.huseyinsarsilmaz.lms.model.dto.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ResponseBuilder {
    private final MessageSource messageSource;

    public <T> ResponseEntity<ApiResponse<T>> success(String entity, String action, T data,
            HttpStatus status) {
        String message = messageSource.getMessage("success.message", new Object[] { entity, action },
                Locale.getDefault());
        ApiResponse<T> response = new ApiResponse<>(true, message, data);
        return new ResponseEntity<>(response, status);
    }

    public <T> ResponseEntity<ApiResponse<T>> fail(String type, String[] args, T data,
            HttpStatus status) {
        String message = messageSource.getMessage("fail." + type, args, Locale.getDefault());
        ApiResponse<T> response = new ApiResponse<>(false, message, data);
        return new ResponseEntity<>(response, status);
    }

}