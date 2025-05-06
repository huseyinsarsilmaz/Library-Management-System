package com.huseyinsarsilmaz.lms.model.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "books")
public class Book extends LmsEntity {

    private String title;
    private String author;

    @Column(length = 2048)
    private String description;

    private String isbn;
    private LocalDate publicationDate;

    @Enumerated(EnumType.STRING)
    private Genre genre;

    public enum Genre {
        FICTION,
        DRAMA,
        MYSTERY,
        THRILLER,
        HORROR,
        ROMANCE,
        FANTASY,
        SCI_FI,
        BIOGRAPHY,
        SELF_HELP,
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
