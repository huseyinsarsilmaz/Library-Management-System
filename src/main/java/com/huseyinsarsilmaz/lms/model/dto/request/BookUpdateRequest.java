package com.huseyinsarsilmaz.lms.model.dto.request;

import java.time.LocalDate;

import com.huseyinsarsilmaz.lms.model.entity.Book;
import com.huseyinsarsilmaz.lms.validation.Isbn;
import com.huseyinsarsilmaz.lms.validation.RequiredField;
import com.huseyinsarsilmaz.lms.validation.StrSize;

import jakarta.validation.constraints.PastOrPresent;

public class BookUpdateRequest {
    @RequiredField(entityName = "Book", fieldName = "title")
    @StrSize(entityName = "Book", fieldName = "title", min = 1, max = 255)
    private String title;

    @RequiredField(entityName = "Book", fieldName = "author")
    @StrSize(entityName = "Book", fieldName = "author", min = 1, max = 255)
    private String author;

    @RequiredField(entityName = "Book", fieldName = "description")
    @StrSize(entityName = "Book", fieldName = "description", min = 8, max = 2048)
    private String description;

    @RequiredField(entityName = "Book", fieldName = "isbn")
    @Isbn(entityName = "Book", fieldName = "isbn")
    private String isbn;

    @RequiredField(entityName = "Book", fieldName = "publication date")
    @PastOrPresent(message = "{fail.date.past.present}")
    private LocalDate publicationDate;

    @RequiredField(entityName = "Book", fieldName = "genre")
    private Book.Genre genre;
}
