package com.huseyinsarsilmaz.lms.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.huseyinsarsilmaz.lms.exception.AlreadyBorrowedException;
import com.huseyinsarsilmaz.lms.exception.AlreadyReturnedBorrowingException;
import com.huseyinsarsilmaz.lms.exception.BorrowingNotExcusableException;
import com.huseyinsarsilmaz.lms.exception.ForbiddenException;
import com.huseyinsarsilmaz.lms.exception.HasActiveBorrowingsException;
import com.huseyinsarsilmaz.lms.exception.NotFoundException;
import com.huseyinsarsilmaz.lms.exception.OverdueException;

import com.huseyinsarsilmaz.lms.model.dto.request.BorrowRequest;
import com.huseyinsarsilmaz.lms.model.entity.Book;
import com.huseyinsarsilmaz.lms.model.entity.Borrowing;
import com.huseyinsarsilmaz.lms.model.entity.User;
import com.huseyinsarsilmaz.lms.model.mapper.BorrowingMapper;
import com.huseyinsarsilmaz.lms.repository.BorrowingRepository;
import com.huseyinsarsilmaz.lms.service.impl.BorrowingServiceImpl;

@ExtendWith(MockitoExtension.class)
public class BorrowingServiceTest {

    @Mock
    private BorrowingRepository borrowingRepository;
    @Mock
    private UserService userService;
    @Mock
    private BookService bookService;
    @Mock
    private BorrowingMapper borrowingMapper;

    @InjectMocks
    private BorrowingServiceImpl borrowingService;

    private User user;
    private Book book;
    private Borrowing borrowing;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setIsActive(true);

        book = new Book();
        book.setId(1L);
        book.setIsAvailable(true);

        borrowing = Borrowing.builder()
                .borrower(user)
                .book(book)
                .borrowDate(LocalDate.now().minusDays(10))
                .dueDate(LocalDate.now().minusDays(3))
                .status(Borrowing.Status.BORROWED)
                .build();

        borrowing.setId(1L);
    }

    @Test
    void testCreate_whenNotAlreadyBorrowed() {
        BorrowRequest req = new BorrowRequest();
        req.setBorrowerId(1L);
        req.setBookId(1L);

        when(borrowingRepository.existsByBorrowerIdAndBookIdAndStatusIn(anyLong(), anyLong(), anyList()))
                .thenReturn(false);
        when(userService.getById(1L)).thenReturn(user);
        when(bookService.getById(1L)).thenReturn(book);
        doNothing().when(bookService).checkAvailability(book);
        when(bookService.updateAvailability(book, false)).thenReturn(book);
        when(borrowingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Borrowing result = borrowingService.create(req);

        assertNotNull(result);
        assertEquals(user, result.getBorrower());
        assertEquals(book, result.getBook());
        assertEquals(Borrowing.Status.BORROWED, result.getStatus());
    }

    @Test
    void testCreate_whenAlreadyBorrowed() {
        BorrowRequest req = new BorrowRequest();
        req.setBorrowerId(1L);
        req.setBookId(1L);

        when(borrowingRepository.existsByBorrowerIdAndBookIdAndStatusIn(anyLong(), anyLong(), anyList()))
                .thenReturn(true);

        assertThrows(AlreadyBorrowedException.class, () -> borrowingService.create(req));
    }

    @Test
    void testGetById_whenExists() {
        when(borrowingRepository.findByIdWithBookAndBorrower(1L)).thenReturn(Optional.of(borrowing));

        Borrowing result = borrowingService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetById_whenNotExists() {
        when(borrowingRepository.findByIdWithBookAndBorrower(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> borrowingService.getById(99L));
    }

    @Test
    void testCheckOwnership_whenOwner() {
        assertDoesNotThrow(() -> borrowingService.checkOwnership(user, borrowing));
    }

    @Test
    void testCheckOwnership_whenNotOwner() {
        User other = new User();
        other.setId(99L);

        assertThrows(ForbiddenException.class, () -> borrowingService.checkOwnership(other, borrowing));
    }

    @Test
    void testCheckReturnable_whenAlreadyReturned() {
        borrowing.setStatus(Borrowing.Status.RETURNED_TIMELY);

        assertThrows(AlreadyReturnedBorrowingException.class, () -> borrowingService.checkReturnable(borrowing));
    }

    @Test
    void testCheckReturnable_whenNotReturned() {
        assertDoesNotThrow(() -> borrowingService.checkReturnable(borrowing));
    }

    @Test
    void testReturnBorrowing_whenTimely() {
        borrowing.setDueDate(LocalDate.now().plusDays(1));

        when(bookService.updateAvailability(book, true)).thenReturn(book);
        when(borrowingRepository.save(borrowing)).thenReturn(borrowing);

        Borrowing result = borrowingService.returnBorrowing(borrowing);

        assertNotNull(result.getReturnDate());
        assertEquals(Borrowing.Status.RETURNED_TIMELY, result.getStatus());
        assertTrue(book.getIsAvailable());
    }

    @Test
    void testReturnBorrowing_whenOverdueAndNotSuspended() {
        when(borrowingRepository.countByBorrowerIdAndStatus(user.getId(), Borrowing.Status.OVERDUE)).thenReturn(1L);
        when(bookService.updateAvailability(book, true)).thenReturn(book);
        when(borrowingRepository.save(borrowing)).thenReturn(borrowing);

        Borrowing result = borrowingService.returnBorrowing(borrowing);

        assertEquals(Borrowing.Status.RETURNED_OVERDUE, result.getStatus());
        verify(userService, never()).changeActive(user, false);
    }

    @Test
    void testReturnBorrowing_whenOverdueAndSuspended() {
        book.setIsAvailable(false);

        when(borrowingRepository.countByBorrowerIdAndStatus(user.getId(), Borrowing.Status.OVERDUE)).thenReturn(2L);
        when(bookService.updateAvailability(book, true)).thenAnswer(inv -> {
            book.setIsAvailable(true);
            return book;
        });
        when(borrowingRepository.save(borrowing)).thenReturn(borrowing);

        Borrowing result = borrowingService.returnBorrowing(borrowing);

        assertEquals(Borrowing.Status.RETURNED_OVERDUE, result.getStatus());
        assertTrue(book.getIsAvailable());
        assertNotNull(result.getReturnDate());
        verify(userService).changeActive(user, false);
    }

    @Test
    void testGetByBorrowerId() {
        Page<Borrowing> page = new PageImpl<>(List.of(borrowing));
        Pageable pageable = Pageable.ofSize(10);

        when(borrowingRepository.findAllByBorrowerIdWithBook(1L, pageable)).thenReturn(page);

        Page<Borrowing> result = borrowingService.getByBorrowerId(1L, pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(borrowing, result.getContent().get(0));
    }

    @Test
    void testCheckUserHasActiveBorrowings_whenHas() {
        when(borrowingRepository.countByBorrowerIdAndStatusIn(anyLong(), anyList())).thenReturn(1L);

        assertThrows(HasActiveBorrowingsException.class, () -> borrowingService.checkUserHasActiveBorrowings(user));
    }

    @Test
    void testCheckUserHasActiveBorrowings_whenHasNot() {
        when(borrowingRepository.countByBorrowerIdAndStatusIn(anyLong(), anyList())).thenReturn(0L);

        assertDoesNotThrow(() -> borrowingService.checkUserHasActiveBorrowings(user));
    }

    @Test
    void testGetOverdueByBorrowerId() {
        Page<Borrowing> page = new PageImpl<>(List.of(borrowing));

        when(borrowingRepository.findAllByBorrowerIdAndStatusNotIn(eq(1L), anyList(), any())).thenReturn(page);

        Page<Borrowing> result = borrowingService.getOverdueByBorrowerId(1L, Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetAllOverdue() {
        Page<Borrowing> page = new PageImpl<>(List.of(borrowing));

        when(borrowingRepository.findAllByStatusNotIn(anyList(), any())).thenReturn(page);

        Page<Borrowing> result = borrowingService.getAllOverdue(Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testCheckBorrowableByBorrowerId_whenOverdue() {
        when(borrowingRepository.existsByBorrowerIdAndStatus(1L, Borrowing.Status.OVERDUE)).thenReturn(true);

        assertThrows(OverdueException.class, () -> borrowingService.checkBorrowableByBorrowerId(1L));
    }

    @Test
    void testCheckBorrowableByBorrowerId_whenBorrowable() {
        when(borrowingRepository.existsByBorrowerIdAndStatus(1L, Borrowing.Status.OVERDUE)).thenReturn(false);

        assertDoesNotThrow(() -> borrowingService.checkBorrowableByBorrowerId(1L));
    }

    @Test
    void testExcuseReturnedOverdueBorrowings() {
        borrowing.setStatus(Borrowing.Status.RETURNED_OVERDUE);

        when(borrowingRepository.findByBorrowerIdAndStatus(1L, Borrowing.Status.RETURNED_OVERDUE))
                .thenReturn(List.of(borrowing));

        borrowingService.excuseReturnedOverdueBorrowings(user);

        assertEquals(Borrowing.Status.RETURNED_EXCUSED, borrowing.getStatus());
        verify(borrowingRepository).saveAll(anyList());
    }

    @Test
    void testExcuseBorrowing() {
        borrowing.setStatus(Borrowing.Status.RETURNED_OVERDUE);

        when(borrowingRepository.save(borrowing)).thenReturn(borrowing);

        Borrowing result = borrowingService.excuseBorrowing(borrowing);

        assertEquals(Borrowing.Status.RETURNED_EXCUSED, result.getStatus());
    }

    @Test
    void testCheckExcusable_whenNotExcusable() {
        borrowing.setStatus(Borrowing.Status.RETURNED_TIMELY);

        assertThrows(BorrowingNotExcusableException.class, () -> borrowingService.checkExcusable(borrowing));
    }

    @Test
    void testCheckExcusable_whenExcusable() {
        borrowing.setStatus(Borrowing.Status.RETURNED_OVERDUE);

        assertDoesNotThrow(() -> borrowingService.checkExcusable(borrowing));
    }

    @Test
    void testMarkOverdueBorrowings() {
        when(borrowingRepository.findPastDueByStatus(Borrowing.Status.BORROWED, LocalDate.now()))
                .thenReturn(List.of(borrowing));

        borrowingService.markOverdueBorrowings();

        assertEquals(Borrowing.Status.OVERDUE, borrowing.getStatus());
        verify(borrowingRepository).saveAll(anyList());
    }
}
