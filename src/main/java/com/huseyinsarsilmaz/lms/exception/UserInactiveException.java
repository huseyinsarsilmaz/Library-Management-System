package com.huseyinsarsilmaz.lms.exception;

import org.springframework.http.HttpStatus;

public final class UserInactiveException extends LmsException {
    public UserInactiveException() {
        super("inactive", HttpStatus.FORBIDDEN);
    }
}
