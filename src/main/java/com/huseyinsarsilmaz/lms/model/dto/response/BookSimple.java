package com.huseyinsarsilmaz.lms.model.dto.response;

import java.time.LocalDate;

import com.huseyinsarsilmaz.lms.model.entity.Book;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BookSimple {

    private long id;
    private String title;
    private String author;
    private String description;
    private String isbn;
    private LocalDate publicationDate;
    private Book.Genre genre;

    public BookSimple(Book book) {
        this.id = book.getId();
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.description = book.getDescription();
        this.isbn = book.getIsbn();
        this.publicationDate = book.getPublicationDate();
        this.genre = book.getGenre();

    }
}