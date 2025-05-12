package com.huseyinsarsilmaz.lms.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.huseyinsarsilmaz.lms.exception.AlreadyExistsException;
import com.huseyinsarsilmaz.lms.exception.ForbiddenException;
import com.huseyinsarsilmaz.lms.exception.NotFoundException;
import com.huseyinsarsilmaz.lms.exception.UserNotDeactivatedException;
import com.huseyinsarsilmaz.lms.exception.UserPromotionException;
import com.huseyinsarsilmaz.lms.model.dto.request.RegisterRequest;
import com.huseyinsarsilmaz.lms.model.dto.request.UserUpdateRequest;
import com.huseyinsarsilmaz.lms.model.entity.User;
import com.huseyinsarsilmaz.lms.model.mapper.UserMapper;
import com.huseyinsarsilmaz.lms.repository.UserRepository;
import com.huseyinsarsilmaz.lms.security.JwtService;
import com.huseyinsarsilmaz.lms.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    private static final String EMAIL = "huseyinsarsilmaz@hotmail.com";
    private static final String ENCODED_PASSWORD = "MTIzNDU2Nzg=";
    private static final Long USER_ID = 1L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(USER_ID);
        user.setEmail(EMAIL);
        user.setPassword(ENCODED_PASSWORD);
        user.setRoles("ROLE_PATRON");
        user.setName("Hüseyin");
        user.setSurname("Sarsılmaz");
    }

    @Test
    public void testIsEmailTaken_whenEmailExists() {
        when(userRepository.findByEmail(eq(EMAIL))).thenReturn(Optional.of(user));
        assertThrows(AlreadyExistsException.class, () -> userService.isEmailTaken(EMAIL));
    }

    @Test
    public void testIsEmailTaken_whenEmailNotExists() {
        when(userRepository.findByEmail(eq("testnotexist@hotmail.com"))).thenReturn(Optional.empty());
        assertDoesNotThrow(() -> userService.isEmailTaken("testnotexist@hotmail.com"));
    }

    @Test
    public void testRegister() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail(EMAIL);
        req.setPassword("12345678");
        req.setName("Hüseyin");
        req.setSurname("Sarsılmaz");

        User mappedUser = new User();
        mappedUser.setEmail(req.getEmail());
        mappedUser.setName(req.getName());
        mappedUser.setSurname(req.getSurname());
        mappedUser.setPassword(ENCODED_PASSWORD);

        when(userMapper.toEntity(eq(req))).thenReturn(mappedUser);

        when(passwordEncoder.encode(anyString())).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User savedUser = userService.register(req);

        assertNotNull(savedUser);
        assertEquals(req.getEmail(), savedUser.getEmail());
        assertEquals(req.getName(), savedUser.getName());
        assertEquals(req.getSurname(), savedUser.getSurname());
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testGetByEmail_whenUserExists() {
        when(userRepository.findByEmail(eq(EMAIL))).thenReturn(Optional.of(user));

        User result = userService.getByEmail(EMAIL);
        assertNotNull(result);
        assertEquals(EMAIL, result.getEmail());
    }

    @Test
    public void testGetByEmail_whenUserNotFound() {
        when(userRepository.findByEmail(eq("testnotexist@hotmail.com"))).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getByEmail("testnotexist@hotmail.com"));
    }

    @Test
    public void testGetById_whenUserExists() {
        when(userRepository.findById(eq(USER_ID))).thenReturn(Optional.of(user));

        User result = userService.getById(USER_ID);
        assertNotNull(result);
        assertEquals(USER_ID, result.getId());
    }

    @Test
    public void testGetById_whenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getById(Long.MAX_VALUE));
    }

    @Test
    public void testPromote_whenNewRoleIsAdmin() {
        assertThrows(UserPromotionException.class, () -> userService.promote(user, User.Role.ROLE_ADMIN));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testPromote_whenNewRoleIsPatron() {
        assertThrows(UserPromotionException.class, () -> userService.promote(user, User.Role.ROLE_PATRON));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testPromote_whenRoleAlreadyExists() {
        user.setRoles("ROLE_PATRON,ROLE_LIBRARIAN");

        assertThrows(UserPromotionException.class, () -> userService.promote(user, User.Role.ROLE_LIBRARIAN));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testPromote_whenNewRoleAddedSuccessfully() {
        user.setRoles("ROLE_PATRON");

        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User promotedUser = userService.promote(user, User.Role.ROLE_LIBRARIAN);

        assertTrue(promotedUser.getRoles().contains("ROLE_PATRON"));
        assertTrue(promotedUser.getRoles().contains("ROLE_LIBRARIAN"));
        verify(userRepository).save(user);
    }

    @Test
    public void testCheckHasRole_whenRoleMatches() {
        user.setRoles("ROLE_PATRON,ROLE_LIBRARIAN");
        assertDoesNotThrow(() -> userService.checkHasRole(user, User.Role.ROLE_PATRON));
    }

    @Test
    public void testCheckHasRole_whenRoleDoesNotMatch() {
        user.setRoles("ROLE_PATRON");
        assertThrows(ForbiddenException.class, () -> userService.checkHasRole(user, User.Role.ROLE_LIBRARIAN));
    }

    @Test
    public void testUpdate() {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setEmail("huseyinsarsilmaz2@hotmail.com");
        req.setName("Hüseyin2");
        req.setSurname("Sarsılmaz2");
        req.setPhoneNumber("123456789");

        doAnswer(invocation -> {
            User userArg = invocation.getArgument(0);
            UserUpdateRequest reqArg = invocation.getArgument(1);
            userArg.setEmail(reqArg.getEmail());
            userArg.setName(reqArg.getName());
            userArg.setSurname(reqArg.getSurname());
            userArg.setPhoneNumber(reqArg.getPhoneNumber());
            return null;
        }).when(userMapper).updateEntity(eq(user), eq(req));

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userService.update(user, req);

        assertEquals(req.getEmail(), updatedUser.getEmail());
        assertEquals(req.getName(), updatedUser.getName());
        assertEquals(req.getSurname(), updatedUser.getSurname());
        assertEquals(req.getPhoneNumber(), updatedUser.getPhoneNumber());
        verify(userRepository).save(user);
    }

    @Test
    public void testDelete() {
        doNothing().when(userRepository).delete(eq(user));

        assertDoesNotThrow(() -> userService.delete(user));
        verify(userRepository).delete(user);
    }

    @Test
    public void testChangeActive() {
        user.setIsActive(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userService.changeActive(user, true);

        assertTrue(updatedUser.getIsActive());
        verify(userRepository).save(user);
    }

    @Test
    public void testCheckDeactivated_whenUserIsDeactivated() {
        when(userRepository.existsByIdAndIsActiveFalse(eq(USER_ID))).thenReturn(true);

        assertDoesNotThrow(() -> userService.checkDeactivated(user));
        verify(userRepository).existsByIdAndIsActiveFalse(USER_ID);
    }

    @Test
    public void testCheckDeactivated_whenUserIsNotDeactivated() {
        when(userRepository.existsByIdAndIsActiveFalse(eq(USER_ID))).thenReturn(false);

        assertThrows(UserNotDeactivatedException.class, () -> userService.checkDeactivated(user));
        verify(userRepository).existsByIdAndIsActiveFalse(USER_ID);
    }
}
