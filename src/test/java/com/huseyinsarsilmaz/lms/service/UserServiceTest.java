package com.huseyinsarsilmaz.lms.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
import com.huseyinsarsilmaz.lms.model.dto.request.RegisterRequest;
import com.huseyinsarsilmaz.lms.model.dto.request.UserUpdateRequest;
import com.huseyinsarsilmaz.lms.model.entity.User;
import com.huseyinsarsilmaz.lms.repository.UserRepository;
import com.huseyinsarsilmaz.lms.security.JwtService;
import com.huseyinsarsilmaz.lms.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("huseyinsarsilmaz@hotmail.com");
        user.setPassword("MTIzNDU2Nzg=");
        user.setRoles("ROLE_PATRON");
        user.setName("Hüseyin");
        user.setSurname("Sarsılmaz");
    }

    @Test
    public void testIsEmailTaken_whenEmailExists() {
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(user));

        assertThrows(AlreadyExistsException.class, () -> userService.isEmailTaken("huseyinsarsilmaz@hotmail.com"));
    }

    @Test
    public void testIsEmailTaken_whenEmailNotExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> userService.isEmailTaken("testnotexist@hotmail.com"));
    }

    @Test
    public void testRegister() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("huseyinsarsilmaz@hotmail.com");
        req.setPassword("12345678");
        req.setName("Hüseyin");
        req.setSurname("Sarsılmaz");

        when(passwordEncoder.encode(anyString())).thenReturn("MTIzNDU2Nzg=");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User savedUser = userService.register(req);
        assertNotNull(savedUser);

        assertEquals(req.getEmail(), savedUser.getEmail());
        assertEquals(req.getName(), savedUser.getName());
        assertEquals(req.getSurname(), savedUser.getSurname());

    }

    @Test
    public void testGetByEmail_whenUserExists() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        User result = userService.getByEmail("huseyinsarsilmaz@hotmail.com");
        assertNotNull(result);
        assertEquals("huseyinsarsilmaz@hotmail.com", result.getEmail());
    }

    @Test
    public void testGetByEmail_whenUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getByEmail("testnotexist@hotmail.com"));
    }

    @Test
    public void testGetById_whenUserExists() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

        User result = userService.getById(1);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    public void testGetById_whenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getById(Long.MAX_VALUE));
    }

    @Test
    public void testPromote_whenNewRoleIsAdmin() {

        assertThrows(ForbiddenException.class, () -> userService.promote(user, User.Role.ROLE_ADMIN));
        verify(userRepository, never()).save(any(User.class));

    }

    @Test
    public void testPromote_whenNewRoleIsPatron() {

        User promotedUser = userService.promote(user, User.Role.ROLE_PATRON);
        assertEquals(user, promotedUser);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testPromote_whenRoleAlreadyExists() {

        user.setRoles("ROLE_PATRON,ROLE_LIBRARIAN");

        User promotedUser = userService.promote(user, User.Role.ROLE_LIBRARIAN);
        assertEquals(user, promotedUser);
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
    public void testGetFromToken() {
        when(jwtService.extractEmail(anyString())).thenReturn("huseyinsarsilmaz@hotmail.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        User result = userService.getFromToken("Bearer token");
        assertNotNull(result);
        assertEquals("huseyinsarsilmaz@hotmail.com", result.getEmail());
    }

    @Test
    public void testCheckRole_whenRoleMatches() {
        user.setRoles("ROLE_PATRON,ROLE_LIBRARIAN");

        assertDoesNotThrow(() -> userService.checkRole(user, User.Role.ROLE_PATRON));
    }

    @Test
    public void testCheckRole_whenRoleDoesNotMatch() {
        user.setRoles("ROLE_PATRON");

        assertThrows(ForbiddenException.class, () -> userService.checkRole(user, User.Role.ROLE_LIBRARIAN));
    }

    @Test
    public void testUpdate() {
        UserUpdateRequest req = new UserUpdateRequest();
        req.setEmail("huseyinsarsilmaz2@hotmail.com");
        req.setName("Hüseyin2");
        req.setSurname("Sarsılmaz2");
        req.setPhoneNumber("123456789");

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
        doNothing().when(userRepository).delete(any(User.class));

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
        when(userRepository.existsByIdAndIsActiveFalse(user.getId())).thenReturn(true);

        assertDoesNotThrow(() -> userService.checkDeactivated(user));
        verify(userRepository).existsByIdAndIsActiveFalse(user.getId());
    }

    @Test
    public void testCheckDeactivated_whenUserIsNotDeactivated() {
        when(userRepository.existsByIdAndIsActiveFalse(user.getId())).thenReturn(false);

        assertThrows(UserNotDeactivatedException.class, () -> userService.checkDeactivated(user));
        verify(userRepository).existsByIdAndIsActiveFalse(user.getId());
    }

}
