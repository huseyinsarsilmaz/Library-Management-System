package com.huseyinsarsilmaz.lms.exception;

import org.springframework.http.HttpStatus;

public final class UserNotDeactivatedException extends LmsException {
    public UserNotDeactivatedException() {
        super("user.not.deactivated", HttpStatus.CONFLICT);
    }
}
