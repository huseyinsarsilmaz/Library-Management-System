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

    public static ResponseEntity<ApiResponse> successResponse(String entity, String action, Object data,
            HttpStatus status) {
        String message = messageSource.getMessage("success.message", new Object[] { entity, action },
                Locale.getDefault());
        ApiResponse response = new ApiResponse(true, message, data);
        return new ResponseEntity<>(response, status);
    }

    public static ResponseEntity<ApiResponse> failResponse(String type, String[] args, Object data, HttpStatus status) {
        String message = messageSource.getMessage("fail." + type, args, Locale.getDefault());
        ApiResponse response = new ApiResponse(false, message, data);
        return new ResponseEntity<>(response, status);
    }

}
