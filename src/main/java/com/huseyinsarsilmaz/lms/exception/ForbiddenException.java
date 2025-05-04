package com.huseyinsarsilmaz.lms.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends LmsException {

    public ForbiddenException() {
        super("forbidden", new String[] {}, HttpStatus.FORBIDDEN);
    }

}
