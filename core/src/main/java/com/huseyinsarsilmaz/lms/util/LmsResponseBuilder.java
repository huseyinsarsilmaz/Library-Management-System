package com.huseyinsarsilmaz.lms.util;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.huseyinsarsilmaz.lms.model.dto.response.LmsApiResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LmsResponseBuilder {
    private final MessageSource messageSource;

    public <T> ResponseEntity<LmsApiResponse<T>> success(String entity, String action, T data,
            HttpStatus status) {
        String message = messageSource.getMessage("success.message", new Object[] { entity, action },
                Locale.getDefault());
        LmsApiResponse<T> response = new LmsApiResponse<>(true, message, data);
        return new ResponseEntity<>(response, status);
    }

    public <T> ResponseEntity<LmsApiResponse<T>> fail(String type, String[] args, T data,
            HttpStatus status) {
        String message = messageSource.getMessage("fail." + type, args, Locale.getDefault());
        LmsApiResponse<T> response = new LmsApiResponse<>(false, message, data);
        return new ResponseEntity<>(response, status);
    }

}