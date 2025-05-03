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

}
