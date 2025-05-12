package com.huseyinsarsilmaz.lms.exception;

import org.springframework.http.HttpStatus;

public final class NotFoundException extends LmsException {
    public NotFoundException(String entity, String identifier) {
        super("not.found", HttpStatus.NOT_FOUND, entity, identifier);
    }
}