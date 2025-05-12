package com.huseyinsarsilmaz.lms.exception;

import org.springframework.http.HttpStatus;

public final class AlreadyExistsException extends LmsException {
    public AlreadyExistsException(String entity, String identifier) {
        super("already.exists", HttpStatus.CONFLICT, entity, identifier);
    }
}
