package com.huseyinsarsilmaz.lmsr.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "books")
public class Book {

    @Id
    private Long id;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String title;
    private String author;

    private String description;

    private String isbn;
    private LocalDate publicationDate;

    @Column("genre")
    private Genre genre;

    @Builder.Default
    private Boolean isAvailable = true;

    public enum Genre {
        FICTION,
        DRAMA,
        MYSTERY,
        THRILLER,
        HORROR,
        ROMANCE,
        FANTASY,
        BIOGRAPHY,
        POETRY,
        ADVENTURE,
        EROTICA,
        SPIRITUAL,
        TRAVEL,
        OTHER
    }

    public enum SearchType {
        TITLE,
        AUTHOR,
        ISBN,
        GENRE
    }
}
