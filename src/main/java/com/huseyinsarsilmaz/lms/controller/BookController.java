package com.huseyinsarsilmaz.lms.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.huseyinsarsilmaz.lms.model.dto.request.BookCreateRequest;
import com.huseyinsarsilmaz.lms.model.dto.request.BookUpdateRequest;
import com.huseyinsarsilmaz.lms.model.dto.response.ApiResponse;
import com.huseyinsarsilmaz.lms.model.dto.response.BookSimple;
import com.huseyinsarsilmaz.lms.model.dto.response.PagedResponse;
import com.huseyinsarsilmaz.lms.model.entity.Book;
import com.huseyinsarsilmaz.lms.model.entity.User;
import com.huseyinsarsilmaz.lms.service.BookService;
import com.huseyinsarsilmaz.lms.service.UserService;
import com.huseyinsarsilmaz.lms.service.Utils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookSimple>> createBook(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody BookCreateRequest req) {

        User myUser = userService.getFromToken(token);
        userService.checkRole(myUser, User.Role.ROLE_LIBRARIAN);

        bookService.isIsbnTaken(req.getIsbn());

        Book newBook = bookService.create(req);

        return Utils.successResponse(Book.class.getSimpleName(), "created", new BookSimple(newBook),
                HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookSimple>> getBook(@PathVariable("id") long id) {

        Book book = bookService.getById(id);

        return Utils.successResponse(Book.class.getSimpleName(), "acquired", new BookSimple(book), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BookSimple>> updateBook(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody BookUpdateRequest req,
            @PathVariable("id") long id) {

        User myUser = userService.getFromToken(token);
        userService.checkRole(myUser, User.Role.ROLE_LIBRARIAN);

        Book book = bookService.getById(id);

        if (!book.getIsbn().equals(req.getIsbn())) {
            bookService.isIsbnTaken(req.getIsbn());
        }

        book = bookService.update(book, req);

        return Utils.successResponse(Book.class.getSimpleName(), "updated", new BookSimple(book), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<BookSimple>> deleteBook(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") long id) {

        User myUser = userService.getFromToken(token);
        userService.checkRole(myUser, User.Role.ROLE_LIBRARIAN);

        Book book = bookService.getById(id);
        bookService.delete(book);

        return Utils.successResponse(Book.class.getSimpleName(), "deleted", new BookSimple(book), HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<BookSimple>>> searchBooks(
            @RequestParam Book.SearchType type,
            @RequestParam String query,
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {

        Page<BookSimple> books = bookService.searchBooks(type, query, pageable)
                .map(BookSimple::new);

        return Utils.successResponse(Book.class.getSimpleName() + "s", "acquired", new PagedResponse<BookSimple>(books),
                HttpStatus.OK);
    }

}
