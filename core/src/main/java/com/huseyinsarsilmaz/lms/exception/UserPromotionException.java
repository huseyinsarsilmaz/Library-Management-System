package com.huseyinsarsilmaz.lms.exception;

import org.springframework.http.HttpStatus;

public final class UserPromotionException extends LmsException {
    public UserPromotionException() {
        super("user.promotion", HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
