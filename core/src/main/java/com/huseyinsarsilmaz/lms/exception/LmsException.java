package com.huseyinsarsilmaz.lms.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class LmsException extends RuntimeException {
    private final String type;
    private final String[] args;
    private final HttpStatus httpStatus;

    public LmsException(String messageKey, HttpStatus httpStatus, String... args) {
        super(messageKey);
        this.type = messageKey;
        this.args = args;
        this.httpStatus = httpStatus;
    }
}