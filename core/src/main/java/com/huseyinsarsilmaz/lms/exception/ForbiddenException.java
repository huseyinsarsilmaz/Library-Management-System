package com.huseyinsarsilmaz.lms.exception;

import org.springframework.http.HttpStatus;

public final class ForbiddenException extends LmsException {
    public ForbiddenException() {
        super("forbidden", HttpStatus.FORBIDDEN);
    }
}