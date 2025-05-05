package com.huseyinsarsilmaz.lms.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.regex.Pattern;

public class IsbnValidator implements ConstraintValidator<Isbn, String> {

    private String entityName;
    private String fieldName;

    @Autowired
    private MessageSource messageSource;

    // Matches with ISBN-10 and ISBN-13 standarts
    // Hyphened form 978-3-16-148410-0 and compact form 9783161484100, both valid
    private static final Pattern ISBN_PATTERN = Pattern.compile(
            "^(?:\\d{9}[\\dX]|97[89](?:[- ]?\\d){10})$");

    @Override
    public void initialize(Isbn constraintAnnotation) {
        this.entityName = constraintAnnotation.entityName();
        this.fieldName = constraintAnnotation.fieldName();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        if (!ISBN_PATTERN.matcher(value).matches()) {
            context.disableDefaultConstraintViolation();

            String messageTemplate = messageSource.getMessage("fail.characters", null, LocaleContextHolder.getLocale());
            String message = String.format(messageTemplate, entityName, fieldName);

            context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
            return false;
        }

        return true;
    }
}
