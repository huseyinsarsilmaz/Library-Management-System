package com.huseyinsarsilmaz.lms.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.huseyinsarsilmaz.lms.model.entity.User;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private static final String NON_EXISTENT_EMAIL = "notexists@hotmail.com";

    private User activeUser;
    private User inactiveUser;

    private User createUser(String email, boolean isActive) {
        User user = new User();
        user.setEmail(email);
        user.setName("name");
        user.setSurname("surname");
        user.setPassword("12345678");
        user.setRoles("ROLE_PATRON");
        user.setPhoneNumber("5051112233");
        user.setIsActive(isActive);
        return userRepository.save(user);
    }

    @BeforeEach
    public void setUp() {
        activeUser = createUser("huseyinsarsilmaz@hotmail.com", true);
        inactiveUser = createUser("huseyinsarsilmaz2@hotmail.com", false);
    }

    @Test
    public void testFindById_whenUserFound() {
        Optional<User> optUser = userRepository.findById(activeUser.getId());

        assertTrue(optUser.isPresent());
        assertEquals(activeUser.getId(), optUser.get().getId());
    }

    @Test
    public void testFindById_whenUserNotFound() {
        Optional<User> optUser = userRepository.findById(Long.MAX_VALUE);

        assertFalse(optUser.isPresent());
    }

    @Test
    public void testFindByEmail_whenUserFound() {
        Optional<User> optUser = userRepository.findByEmail(activeUser.getEmail());

        assertTrue(optUser.isPresent());
        assertEquals(activeUser.getEmail(), optUser.get().getEmail());
    }

    @Test
    public void testFindByEmail_whenUserNotFound() {
        Optional<User> optUser = userRepository.findByEmail(NON_EXISTENT_EMAIL);

        assertFalse(optUser.isPresent());
    }

    @ParameterizedTest
    @CsvSource({
            "active, false",
            "inactive, true",
            "999, false"
    })
    public void testExistsByIdAndIsActiveFalse_param(String idType, boolean expectedExists) {
        Long idToCheck = switch (idType) {
            case "active" -> activeUser.getId();
            case "inactive" -> inactiveUser.getId();
            default -> Long.MAX_VALUE;
        };

        boolean exists = userRepository.existsByIdAndIsActiveFalse(idToCheck);
        assertEquals(expectedExists, exists);
    }

}
