package com.huseyinsarsilmaz.lms.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.huseyinsarsilmaz.lms.model.entity.Book;

@Repository
public interface ReactiveBookRepository extends ReactiveCrudRepository<Book, Long> {

}
