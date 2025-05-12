package com.huseyinsarsilmaz.lms.model.dto.response;

import java.time.LocalDate;

import com.huseyinsarsilmaz.lms.model.entity.Book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookSimple {

    private long id;
    private String title;
    private String author;
    private String description;
    private String isbn;
    private LocalDate publicationDate;
    private Book.Genre genre;
    private Boolean isAvailable;

}