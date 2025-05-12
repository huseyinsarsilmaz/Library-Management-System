package com.huseyinsarsilmaz.lms.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.huseyinsarsilmaz.lms.model.dto.request.BookCreateRequest;
import com.huseyinsarsilmaz.lms.model.dto.request.BookUpdateRequest;
import com.huseyinsarsilmaz.lms.model.entity.Book;

public interface BookService {

    public Book create(BookCreateRequest req);

    public void isIsbnTaken(String isbn);

    public Book getById(long id);

    public Book update(Book book, BookUpdateRequest req);

    public void delete(Book book);

    public Page<Book> searchBooks(Book.SearchType searchType, String query, Pageable pageable);

    public Book updateAvailability(Book book, boolean newAvailability);

    public void checkAvailability(Book book);
}
