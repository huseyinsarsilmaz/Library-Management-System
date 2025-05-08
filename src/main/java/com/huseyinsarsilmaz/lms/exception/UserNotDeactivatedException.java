package com.huseyinsarsilmaz.lms.exception;

import org.springframework.http.HttpStatus;

public class UserNotDeactivatedException extends LmsException {

    public UserNotDeactivatedException() {
        super("user.not.deactivated", new String[] {}, HttpStatus.CONFLICT);
    }

}
