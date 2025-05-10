package com.huseyinsarsilmaz.lms.service;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.huseyinsarsilmaz.lms.model.dto.response.ApiResponse;

@Component
public class Utils {

    private static MessageSource messageSource;

    @Autowired
    public void setMessageSource(MessageSource messageSource) {
        Utils.messageSource = messageSource;
    }

    public static <T> ResponseEntity<ApiResponse<T>> successResponse(String entity, String action, T data,
            HttpStatus status) {
        String message = messageSource.getMessage("success.message", new Object[] { entity, action },
                Locale.getDefault());
        ApiResponse<T> response = new ApiResponse<>(true, message, data);
        return new ResponseEntity<>(response, status);
    }

    public static <T> ResponseEntity<ApiResponse<T>> failResponse(String type, String[] args, T data,
            HttpStatus status) {
        String message = messageSource.getMessage("fail." + type, args, Locale.getDefault());
        ApiResponse<T> response = new ApiResponse<>(false, message, data);
        return new ResponseEntity<>(response, status);
    }

}
