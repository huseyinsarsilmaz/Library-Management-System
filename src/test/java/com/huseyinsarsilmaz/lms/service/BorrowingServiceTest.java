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

    @InjectMocks
    private BorrowingServiceImpl borrowingService;

    private User user;
    private Book book;
    private Borrowing borrowing;

    @BeforeEach
    public void setUp() {
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
                .returnDate(null)
                .status(Borrowing.Status.BORROWED)
                .build();

        borrowing.setId(1L);
    }

    @Test
    public void testCreate_whenNotAlreadyBorrowed() {
        BorrowRequest req = new BorrowRequest();
        req.setBorrowerId(1L);
        req.setBookId(1L);

        when(borrowingRepository.existsByBorrowerIdAndBookIdAndStatusIn(anyLong(), anyLong(), anyList()))
                .thenReturn(false);
        when(userService.getById(1L)).thenReturn(user);
        when(bookService.getById(1L)).thenReturn(book);
        doNothing().when(bookService).checkAvailability(book);
        when(bookService.changeAvailability(book, false)).thenReturn(book);
        when(borrowingRepository.save(any(Borrowing.class))).thenAnswer(inv -> inv.getArgument(0));

        Borrowing result = borrowingService.create(req);

        assertNotNull(result);
        assertEquals(user, result.getBorrower());
        assertEquals(book, result.getBook());
        assertEquals(Borrowing.Status.BORROWED, result.getStatus());
    }

    @Test
    public void testCreate_whenAlreadyBorrowed() {
        BorrowRequest req = new BorrowRequest();
        req.setBorrowerId(1L);
        req.setBookId(1L);

        when(borrowingRepository.existsByBorrowerIdAndBookIdAndStatusIn(anyLong(), anyLong(), anyList()))
                .thenReturn(true);

        assertThrows(AlreadyBorrowedException.class, () -> borrowingService.create(req));
    }

    @Test
    public void testGetById_whenExists() {
        when(borrowingRepository.findByIdWithBookAndBorrower(1L)).thenReturn(Optional.of(borrowing));

        Borrowing found = borrowingService.getById(1L);

        assertNotNull(found);
        assertEquals(1L, found.getId());
    }

    @Test
    public void testGetById_whenNotExists() {
        when(borrowingRepository.findByIdWithBookAndBorrower(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> borrowingService.getById(Long.MAX_VALUE));
    }

    @Test
    public void testCheckOwnership_whenOwner() {

        assertDoesNotThrow(() -> borrowingService.checkOwnership(user, borrowing));
    }

    @Test
    public void testCheckOwnership_whenNotOwner() {
        User otherUser = new User();
        otherUser.setId(2L);

        assertThrows(ForbiddenException.class, () -> borrowingService.checkOwnership(otherUser, borrowing));
    }

    @Test
    public void testCheckReturnable_whenAlreadyReturned() {
        borrowing.setStatus(Borrowing.Status.RETURNED_TIMELY);

        assertThrows(AlreadyReturnedBorrowingException.class, () -> borrowingService.checkReturnable(borrowing));
    }

    @Test
    public void testCheckReturnable_whenNotReturned() {

        assertDoesNotThrow(() -> borrowingService.checkReturnable(borrowing));
    }

    @Test
    public void testReturnBorrowing_whenTimely() {
        borrowing.setDueDate(LocalDate.now().plusDays(1));

        when(bookService.changeAvailability(book, true)).thenReturn(book);
        when(borrowingRepository.save(borrowing)).thenReturn(borrowing);

        Borrowing returned = borrowingService.returnBorrowing(borrowing);

        assertNotNull(returned.getReturnDate());
        assertEquals(Borrowing.Status.RETURNED_TIMELY, returned.getStatus());
        assertTrue(book.getIsAvailable());
    }

    @Test
    public void testReturnBorrowing_whenOverdueAndNotSuspended() {
        when(borrowingRepository.countByBorrowerIdAndStatus(user.getId(), Borrowing.Status.OVERDUE)).thenReturn(1L);
        when(bookService.changeAvailability(book, true)).thenReturn(book);
        when(borrowingRepository.save(borrowing)).thenReturn(borrowing);

        Borrowing returned = borrowingService.returnBorrowing(borrowing);

        assertEquals(Borrowing.Status.RETURNED_OVERDUE, returned.getStatus());
        verify(userService, never()).changeActive(user, false);
    }

    @Test
    public void testReturnBorrowing_whenOverdueAndSuspended() {
        book.setIsAvailable(false);
        when(borrowingRepository.countByBorrowerIdAndStatus(user.getId(), Borrowing.Status.OVERDUE)).thenReturn(2L);
        when(bookService.changeAvailability(book, true)).thenAnswer(inv -> {
            book.setIsAvailable(true);
            return book;
        });
        when(borrowingRepository.save(borrowing)).thenReturn(borrowing);

        Borrowing returned = borrowingService.returnBorrowing(borrowing);

        assertEquals(Borrowing.Status.RETURNED_OVERDUE, returned.getStatus());
        verify(userService).changeActive(user, false);
        assertNotNull(returned.getReturnDate());
        assertTrue(book.getIsAvailable());

    }

    @Test
    public void testGetByBorrowerId() {
        when(borrowingRepository.findAllByBorrowerIdWithBook(1L)).thenReturn(List.of(borrowing));

        List<Borrowing> list = borrowingService.getByBorrowerId(1L);

        assertEquals(1, list.size());
        assertEquals(borrowing, list.get(0));
    }

    @Test
    public void testCheckUserHasActiveBorrowings_whenHas() {
        when(borrowingRepository.countByBorrowerIdAndStatusIn(anyLong(), anyList())).thenReturn(1L);

        assertThrows(HasActiveBorrowingsException.class, () -> borrowingService.checkUserHasActiveBorrowings(user));
    }

    @Test
    public void testCheckUserHasActiveBorrowings_whenHasNot() {
        when(borrowingRepository.countByBorrowerIdAndStatusIn(anyLong(), anyList())).thenReturn(0L);

        assertDoesNotThrow(() -> borrowingService.checkUserHasActiveBorrowings(user));
    }

    @Test
    public void testGetOverdueByBorrowerId() {
        Page<Borrowing> page = new PageImpl<>(List.of(borrowing));

        when(borrowingRepository.findAllByBorrowerIdAndStatusNotIn(eq(1L), anyList(), any(Pageable.class)))
                .thenReturn(page);

        Page<Borrowing> result = borrowingService.getOverdueByBorrowerId(1L, Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testGetAllOverdue() {
        Page<Borrowing> page = new PageImpl<>(List.of(borrowing));

        when(borrowingRepository.findAllByStatusNotIn(anyList(), any(Pageable.class))).thenReturn(page);

        Page<Borrowing> result = borrowingService.getAllOverdue(Pageable.unpaged());

        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void testCheckBorrowableByBorrowerId_whenOverdue() {
        when(borrowingRepository.existsByBorrowerIdAndStatus(anyLong(), eq(Borrowing.Status.OVERDUE))).thenReturn(true);

        assertThrows(OverdueException.class, () -> borrowingService.checkBorrowableByBorrowerId(1L));
    }

    @Test
    public void testCheckBorrowableByBorrowerId_whenBorrowable() {
        when(borrowingRepository.existsByBorrowerIdAndStatus(anyLong(), eq(Borrowing.Status.OVERDUE)))
                .thenReturn(false);

        assertDoesNotThrow(() -> borrowingService.checkBorrowableByBorrowerId(1L));
    }

    @Test
    public void testExcuseReturnedOverdueBorrowings() {
        borrowing.setStatus(Borrowing.Status.RETURNED_OVERDUE);

        when(borrowingRepository.findByBorrowerIdAndStatus(1L, Borrowing.Status.RETURNED_OVERDUE))
                .thenReturn(List.of(borrowing));

        borrowingService.excuseReturnedOverdueBorrowings(user);

        assertEquals(Borrowing.Status.RETURNED_EXCUSED, borrowing.getStatus());
        verify(borrowingRepository).saveAll(anyList());
    }

    @Test
    public void testExcuseBorrowing() {
        borrowing.setStatus(Borrowing.Status.RETURNED_OVERDUE);

        when(borrowingRepository.save(borrowing)).thenReturn(borrowing);

        Borrowing result = borrowingService.excuseBorrowing(borrowing);

        assertEquals(Borrowing.Status.RETURNED_EXCUSED, result.getStatus());
    }

    @Test
    public void testCheckExcusable_whenNotExcusable() {
        borrowing.setStatus(Borrowing.Status.RETURNED_TIMELY);

        assertThrows(BorrowingNotExcusableException.class, () -> borrowingService.checkExcusable(borrowing));
    }

    @Test
    public void testCheckExcusable_whenExcusable() {
        borrowing.setStatus(Borrowing.Status.RETURNED_OVERDUE);

        assertDoesNotThrow(() -> borrowingService.checkExcusable(borrowing));
    }

    @Test
    public void testMarkOverdueBorrowings() {
        when(borrowingRepository.findPastDueByStatus(Borrowing.Status.BORROWED, LocalDate.now()))
                .thenReturn(List.of(borrowing));

        borrowingService.markOverdueBorrowings();

        assertEquals(Borrowing.Status.OVERDUE, borrowing.getStatus());
        verify(borrowingRepository).saveAll(anyList());
    }
}
