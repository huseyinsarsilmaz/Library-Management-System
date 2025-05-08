package com.huseyinsarsilmaz.lms.exception;

import org.springframework.http.HttpStatus;

public class OverdueException extends LmsException {

    public OverdueException() {
        super("overdue", new String[] {}, HttpStatus.FORBIDDEN);
    }

}
