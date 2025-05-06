package com.huseyinsarsilmaz.lms.service;

import com.huseyinsarsilmaz.lms.model.dto.request.BookCreateRequest;
import com.huseyinsarsilmaz.lms.model.entity.Book;

public interface BookService {

    public Book create(BookCreateRequest req);

    public void isIsbnTaken(String isbn);

    public Book getById(long id);
}
