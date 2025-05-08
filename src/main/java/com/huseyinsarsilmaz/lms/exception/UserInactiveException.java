package com.huseyinsarsilmaz.lms.exception;

import org.springframework.http.HttpStatus;

public class UserInactiveException extends LmsException {

    public UserInactiveException() {
        super("inactive", new String[] {}, HttpStatus.FORBIDDEN);
    }

}
