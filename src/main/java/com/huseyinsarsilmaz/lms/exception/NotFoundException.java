package com.huseyinsarsilmaz.lms.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends LmsException {

    public NotFoundException(String entity, String identifier) {
        super("not.found", new String[] { entity, identifier }, HttpStatus.NOT_FOUND);
    }

}
