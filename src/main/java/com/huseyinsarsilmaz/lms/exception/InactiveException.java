package com.huseyinsarsilmaz.lms.exception;

import org.springframework.http.HttpStatus;

public class InactiveException extends LmsException {

    public InactiveException() {
        super("inactive", new String[] {}, HttpStatus.FORBIDDEN);
    }

}
