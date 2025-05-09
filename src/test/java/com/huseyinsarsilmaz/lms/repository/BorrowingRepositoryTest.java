package com.huseyinsarsilmaz.lms.repository;

import jakarta.transaction.Transactional;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import com.huseyinsarsilmaz.lms.model.entity.Book;
import com.huseyinsarsilmaz.lms.model.entity.Borrowing;
import com.huseyinsarsilmaz.lms.model.entity.User;

@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DataJpaTest
public class BorrowingRepositoryTest {

    @Autowired
    private BorrowingRepository borrowingRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    private Borrowing borrowing1;
    private User user1;
    private User user2;
    private User user3;
    private Book book1;
    private Book book2;

    private List<Borrowing.Status> activeStatuses = List.of(Borrowing.Status.BORROWED, Borrowing.Status.OVERDUE);
    private List<Borrowing.Status> returnedStatuses = List.of(Borrowing.Status.RETURNED_EXCUSED,
            Borrowing.Status.RETURNED_OVERDUE, Borrowing.Status.RETURNED_TIMELY);

    private User createUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setIsActive(true);
        return userRepository.save(user);
    }

    private Book createBook(String title) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor("Hüseyin Sarsilmaz");
        book.setIsbn(UUID.randomUUID().toString());
        book.setGenre(Book.Genre.FICTION);
        return bookRepository.save(book);
    }

    private Borrowing createBorrowing(User user, Book book, Borrowing.Status status, LocalDate dueDate) {
        Borrowing borrowing1 = new Borrowing();
        borrowing1.setBorrower(user);
        borrowing1.setBook(book);
        borrowing1.setStatus(status);
        borrowing1.setDueDate(dueDate);
        return borrowingRepository.save(borrowing1);
    }

    @BeforeEach
    public void setUp() {
        user1 = createUser("huseyinsarsilmaz@hotmail.com");
        user2 = createUser("huseyinsarsilmaz2@hotmail.com");
        user3 = createUser("huseyinsarsilmaz3@hotmail.com");
        book1 = createBook("How to write code");
        book2 = createBook("How to write code2");
        borrowing1 = createBorrowing(user1, book1, Borrowing.Status.BORROWED, LocalDate.now().plusDays(7));
        createBorrowing(user1, createBook("Not Late Book"), Borrowing.Status.BORROWED, LocalDate.now().plusDays(7));
        createBorrowing(user1, createBook("Late Book"), Borrowing.Status.BORROWED,
                LocalDate.now().minusDays(1));
        createBorrowing(user1, createBook("Late Book2"), Borrowing.Status.BORROWED,
                LocalDate.now().minusDays(1));
        createBorrowing(user2, createBook("Late Book 3"), Borrowing.Status.BORROWED,
                LocalDate.now().minusDays(1));
    }

    @Test
    public void testFindById() {
        Optional<Borrowing> result = borrowingRepository.findById(borrowing1.getId());
        assertTrue(result.isPresent());
        assertEquals(borrowing1.getId(), result.get().getId());
    }

    @Test
    public void testFindById_whenNotFound() {
        Optional<Borrowing> result = borrowingRepository.findById(Long.MAX_VALUE);
        assertFalse(result.isPresent());
    }

    @Test
    public void testFindByBorrowerIdAndBookId() {
        Optional<Borrowing> result = borrowingRepository.findByBorrowerIdAndBookId(user1.getId(), book1.getId());
        assertTrue(result.isPresent());
        assertEquals(borrowing1.getId(), result.get().getId());
    }

    @Test
    public void testFindByBorrowerIdAndBookId_whenNotFound() {
        Optional<Borrowing> result = borrowingRepository.findByBorrowerIdAndBookId(user1.getId(), book2.getId());
        assertFalse(result.isPresent());
    }

    @Test
    public void testExistsByBorrowerIdAndBookIdAndStatusIn() {
        boolean exists = borrowingRepository.existsByBorrowerIdAndBookIdAndStatusIn(
                user1.getId(), book1.getId(), activeStatuses);
        assertTrue(exists);
    }

    @Test
    public void testExistsByBorrowerIdAndBookIdAndStatusIn_whenNotFound() {
        boolean exists = borrowingRepository.existsByBorrowerIdAndBookIdAndStatusIn(
                user1.getId(), book1.getId(), returnedStatuses);
        assertFalse(exists);
    }

    @Test
    public void testExistsByBorrowerIdAndStatus() {
        boolean exists = borrowingRepository.existsByBorrowerIdAndStatus(user1.getId(), Borrowing.Status.BORROWED);
        assertTrue(exists);
    }

    @Test
    public void testExistsByBorrowerIdAndStatus_whenNotFound() {
        boolean exists = borrowingRepository.existsByBorrowerIdAndStatus(user1.getId(),
                Borrowing.Status.RETURNED_TIMELY);
        assertFalse(exists);
    }

    @Test
    public void testCountByBorrowerIdAndStatus() {
        long count = borrowingRepository.countByBorrowerIdAndStatus(user1.getId(), Borrowing.Status.BORROWED);
        assertEquals(4, count);
    }

    @Test
    public void testCountByBorrowerIdAndStatus_whenNotFound() {
        long count = borrowingRepository.countByBorrowerIdAndStatus(user1.getId(), Borrowing.Status.RETURNED_TIMELY);
        assertEquals(0, count);
    }

    @Test
    public void testCountByBorrowerIdAndStatusIn() {
        long count = borrowingRepository.countByBorrowerIdAndStatusIn(user1.getId(),
                activeStatuses);
        assertEquals(4, count);
    }

    @Test
    public void testCountByBorrowerIdAndStatusIn_whenNotFound() {
        long count = borrowingRepository.countByBorrowerIdAndStatusIn(user1.getId(),
                returnedStatuses);
        assertEquals(0, count);
    }

    @Test
    public void testFindByBorrowerIdAndStatus() {
        List<Borrowing> result = borrowingRepository.findByBorrowerIdAndStatus(user1.getId(),
                Borrowing.Status.BORROWED);
        assertEquals(4, result.size());
        assertTrue(result.stream().allMatch(borrowing -> borrowing.getStatus().equals(Borrowing.Status.BORROWED)));
    }

    @Test
    public void testFindByBorrowerIdAndStatus_whenNotFound() {
        List<Borrowing> result = borrowingRepository.findByBorrowerIdAndStatus(user1.getId(),
                Borrowing.Status.RETURNED_TIMELY);
        assertEquals(0, result.size());
    }

    @Test
    public void testFindPastDueByStatus() {
        List<Borrowing> result = borrowingRepository.findPastDueByStatus(Borrowing.Status.BORROWED, LocalDate.now());
        assertEquals(3, result.size());
        boolean isBorrowed = result.stream()
                .allMatch(borrowing -> borrowing.getStatus().equals(Borrowing.Status.BORROWED));
        boolean isOverdue = result.stream()
                .allMatch(borrowing -> LocalDate.now().isAfter(borrowing.getDueDate()));

        assertTrue(isBorrowed && isOverdue);
    }

    @Test
    public void testFindByIdWithBookAndBorrower() {
        Optional<Borrowing> result = borrowingRepository.findByIdWithBookAndBorrower(borrowing1.getId());
        assertTrue(result.isPresent());
        assertEquals(user1.getId(), result.get().getBorrower().getId());
        assertEquals(book1.getId(), result.get().getBook().getId());
    }

    @Test
    public void testFindByIdWithBookAndBorrower_whenNotFound() {
        Optional<Borrowing> result = borrowingRepository.findByIdWithBookAndBorrower(Long.MAX_VALUE);
        assertFalse(result.isPresent());
    }

    @Test
    public void testFindAllByBorrowerIdWithBook() {
        List<Borrowing> result = borrowingRepository.findAllByBorrowerIdWithBook(user1.getId());
        assertEquals(4, result.size());
        assertTrue(result.stream().allMatch(borrowing -> borrowing.getStatus().equals(Borrowing.Status.BORROWED)));
    }

    @Test
    public void testFindAllByBorrowerIdWithBook_whenNotFound() {
        List<Borrowing> result = borrowingRepository.findAllByBorrowerIdWithBook(user3.getId());
        assertEquals(0, result.size());
    }

    @Test
    public void testFindAllByBorrowerIdAndStatusNotIn() {
        Page<Borrowing> result = borrowingRepository.findAllByBorrowerIdAndStatusNotIn(user1.getId(),
                returnedStatuses, Pageable.ofSize(10));
        assertEquals(4, result.getContent().size());
    }

    @Test
    public void testFindAllByBorrowerIdAndStatusNotIn_whenNotFound() {
        Page<Borrowing> result = borrowingRepository.findAllByBorrowerIdAndStatusNotIn(user1.getId(),
                activeStatuses, Pageable.ofSize(10));
        assertEquals(0, result.getContent().size());
    }

    @Test
    public void testFindAllByStatusNotIn() {

        Page<Borrowing> result = borrowingRepository.findAllByStatusNotIn(returnedStatuses,
                Pageable.ofSize(10));
        assertEquals(5, result.getContent().size());
    }

    @Test
    public void testFindAllByStatusNotIn_whenNotFound() {

        Page<Borrowing> result = borrowingRepository.findAllByStatusNotIn(activeStatuses,
                Pageable.ofSize(10));
        assertEquals(0, result.getContent().size());
    }
}
