package com.huseyinsarsilmaz.lmsr.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.huseyinsarsilmaz.lmsr.model.entity.Book;

public interface BookRepository extends ReactiveCrudRepository<Book, Long> {

}