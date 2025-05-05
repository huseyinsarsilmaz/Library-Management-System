package com.huseyinsarsilmaz.lms.model.dto.response;

import java.time.LocalDate;

import com.huseyinsarsilmaz.lms.model.entity.Book;

import lombok.Data;

@Data
public class BookSimple {

    private final long id;
    private final String title;
    private final String author;
    private final String description;
    private final String isbn;
    private final LocalDate publicationDate;
    private final Book.Genre genre;

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