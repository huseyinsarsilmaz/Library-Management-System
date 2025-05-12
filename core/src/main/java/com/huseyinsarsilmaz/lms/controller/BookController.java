package com.huseyinsarsilmaz.lms.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.huseyinsarsilmaz.lms.model.dto.request.BookCreateRequest;
import com.huseyinsarsilmaz.lms.model.dto.request.BookUpdateRequest;
import com.huseyinsarsilmaz.lms.model.dto.response.ApiResponse;
import com.huseyinsarsilmaz.lms.model.dto.response.BookSimple;
import com.huseyinsarsilmaz.lms.model.dto.response.PagedResponse;
import com.huseyinsarsilmaz.lms.model.entity.Book;
import com.huseyinsarsilmaz.lms.model.mapper.BookMapper;
import com.huseyinsarsilmaz.lms.service.BookService;
import com.huseyinsarsilmaz.lms.util.ResponseBuilder;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final ResponseBuilder responseBuilder;
    private final BookMapper bookMapper;

    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    @PostMapping
    public ResponseEntity<ApiResponse<BookSimple>> create(@Valid @RequestBody BookCreateRequest req) {

        Book newBook = bookService.create(req);
        return responseBuilder.success("Book", "created", bookMapper.toDtoSimple(newBook), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookSimple>> getById(@PathVariable long id) {
        Book book = bookService.getById(id);
        return responseBuilder.success("Book", "fetched", bookMapper.toDtoSimple(book), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookSimple>> update(
            @Valid @RequestBody BookUpdateRequest req,
            @PathVariable long id) {

        Book book = bookService.getById(id);

        if (!book.getIsbn().equals(req.getIsbn())) {
            bookService.isIsbnTaken(req.getIsbn());
        }

        Book updatedBook = bookService.update(book, req);
        return responseBuilder.success("Book", "updated", bookMapper.toDtoSimple(updatedBook), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<BookSimple>> delete(@PathVariable long id) {

        Book book = bookService.getById(id);
        bookService.delete(book);

        return responseBuilder.success("Book", "deleted", bookMapper.toDtoSimple(book), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<BookSimple>>> search(
            @RequestParam Book.SearchType type,
            @RequestParam String query,
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {

        Page<BookSimple> results = bookService.searchBooks(type, query, pageable)
                .map(bookMapper::toDtoSimple);

        return responseBuilder.success("Books", "fetched", new PagedResponse<>(results), HttpStatus.OK);
    }

}
