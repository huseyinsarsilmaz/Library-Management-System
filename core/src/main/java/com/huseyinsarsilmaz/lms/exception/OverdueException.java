package com.huseyinsarsilmaz.lms.exception;

import org.springframework.http.HttpStatus;

public final class OverdueException extends LmsException {
    public OverdueException() {
        super("overdue", HttpStatus.FORBIDDEN);
    }
}
