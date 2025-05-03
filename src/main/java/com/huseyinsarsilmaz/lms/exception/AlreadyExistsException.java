package com.huseyinsarsilmaz.lms.exception;

import org.springframework.http.HttpStatus;

public class AlreadyExistsException extends LmsException {

    public AlreadyExistsException(String entity, String identifier) {
        super("already.exists", new String[] { entity, identifier }, HttpStatus.CONFLICT);
    }

}
