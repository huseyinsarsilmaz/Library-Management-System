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
import com.huseyinsarsilmaz.lms.model.dto.response.LmsApiResponse;
import com.huseyinsarsilmaz.lms.model.dto.response.BookSimple;
import com.huseyinsarsilmaz.lms.model.dto.response.PagedResponse;
import com.huseyinsarsilmaz.lms.model.entity.Book;
import com.huseyinsarsilmaz.lms.model.mapper.BookMapper;
import com.huseyinsarsilmaz.lms.service.BookService;
import com.huseyinsarsilmaz.lms.util.LmsResponseBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final LmsResponseBuilder responseBuilder;
    private final BookMapper bookMapper;

    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    @PostMapping
    @Operation(summary = "Create a new book", description = "Creates a new book in the library system. Only librarians can create books.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Book successfully created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookSimple.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Validation failed", content = @Content)
    })
    public ResponseEntity<LmsApiResponse<BookSimple>> create(@Valid @RequestBody BookCreateRequest req) {

        Book newBook = bookService.create(req);
        return responseBuilder.success("Book", "created", bookMapper.toDtoSimple(newBook), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book by ID", description = "Fetches a book by its unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookSimple.class))),
            @ApiResponse(responseCode = "404", description = "Not Found - Book not found with the provided ID", content = @Content)
    })
    public ResponseEntity<LmsApiResponse<BookSimple>> getById(@PathVariable long id) {
        Book book = bookService.getById(id);
        return responseBuilder.success("Book", "fetched", bookMapper.toDtoSimple(book), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_LIBRARIAN')")
    @PutMapping("/{id}")
    @Operation(summary = "Update book by ID", description = "Updates an existing book's details by its ID. Only librarians can update books.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book successfully updated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookSimple.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Validation failed or ISBN mismatch", content = @Content),
            @ApiResponse(responseCode = "404", description = "Not Found - Book not found with the provided ID", content = @Content)
    })
    public ResponseEntity<LmsApiResponse<BookSimple>> update(
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
    @Operation(summary = "Delete book by ID", description = "Deletes a book by its ID. Only librarians can delete books.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book successfully deleted", content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookSimple.class))),
            @ApiResponse(responseCode = "404", description = "Not Found - Book not found with the provided ID", content = @Content)
    })
    public ResponseEntity<LmsApiResponse<BookSimple>> delete(@PathVariable long id) {

        Book book = bookService.getById(id);
        bookService.delete(book);

        return responseBuilder.success("Book", "deleted", bookMapper.toDtoSimple(book), HttpStatus.OK);
    }

    @GetMapping("/search")
    @Operation(summary = "Search books", description = "Searches for books based on different search criteria (title, author, ISBN, genre).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Books successfully fetched", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request - Invalid search type or query", content = @Content)
    })
    public ResponseEntity<LmsApiResponse<PagedResponse<BookSimple>>> search(
            @RequestParam Book.SearchType type,
            @RequestParam String query,
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {

        Page<BookSimple> results = bookService.searchBooks(type, query, pageable)
                .map(bookMapper::toDtoSimple);

        return responseBuilder.success("Books", "fetched", new PagedResponse<>(results), HttpStatus.OK);
    }

}
