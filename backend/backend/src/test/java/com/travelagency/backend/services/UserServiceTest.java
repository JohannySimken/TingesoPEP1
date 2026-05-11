package com.travelagency.backend.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.travelagency.backend.entities.UserEntity;
import com.travelagency.backend.repositories.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
            .id(1L)
            .name("John Doe")
            .email("john@test.com")
            .password("password123")
            .role(UserEntity.Role.CLIENT)
            .status(UserEntity.Status.ACTIVE)
            .failedLoginAttempts(0)
            .build();
    }

    // Covered cases: register, login, findById, findAll, update, deactivateUser

    // -----------------------------------------------------------------------------

    // --- register ---
    @Test
    void register_success() {
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        UserEntity result = userService.register(user);

        assertEquals(UserEntity.Role.CLIENT, result.getRole());
        assertEquals(UserEntity.Status.ACTIVE, result.getStatus());
        assertEquals(0, result.getFailedLoginAttempts());
        verify(userRepository).save(user);
    }

    @Test
    void register_duplicateEmail_throwsException() {
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            userService.register(user)
        );
        assertTrue(ex.getMessage().contains("Email ya registrado"));
    }

    // --- login ---
    @Test
    void login_success() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(
            Optional.of(user)
        );
        when(userRepository.save(any())).thenReturn(user);

        UserEntity result = userService.login(user.getEmail(), "password123");

        assertEquals(0, result.getFailedLoginAttempts());
    }

    @Test
    void login_userNotFound_throwsException() {
        when(userRepository.findByEmail("notexist@test.com")).thenReturn(
            Optional.empty()
        );

        assertThrows(RuntimeException.class, () ->
            userService.login("notexist@test.com", "pass")
        );
    }

    @Test
    void login_inactiveUser_throwsException() {
        user.setStatus(UserEntity.Status.INACTIVE);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(
            Optional.of(user)
        );

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            userService.login(user.getEmail(), "password123")
        );
        assertTrue(ex.getMessage().contains("bloqueado"));
    }

    @Test
    void login_wrongPassword_incrementsFailedAttempts() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(
            Optional.of(user)
        );
        when(userRepository.save(any())).thenReturn(user);

        assertThrows(RuntimeException.class, () ->
            userService.login(user.getEmail(), "wrongpass")
        );
        verify(userRepository).save(user);
    }

    @Test
    void login_tooManyFailedAttempts_deactivatesUser() {
        user.setFailedLoginAttempts(3);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(
            Optional.of(user)
        );
        when(userRepository.save(any())).thenReturn(user);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            userService.login(user.getEmail(), "wrongpass")
        );
        assertTrue(ex.getMessage().contains("intentos"));
        assertEquals(UserEntity.Status.INACTIVE, user.getStatus());
    }

    // --- findById ---
    @Test
    void findById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        assertEquals(user, userService.findById(1L));
    }

    @Test
    void findById_notFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.findById(99L));
    }

    // --- findAll ---
    @Test
    void findAll_returnsList() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        assertEquals(1, userService.findAll().size());
    }

    // --- update ---
    @Test
    void update_success() {
        UserEntity updated = UserEntity.builder()
            .name("New Name")
            .phone("123456789")
            .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        UserEntity result = userService.update(1L, updated);
        assertEquals("New Name", user.getName());
        assertEquals("123456789", user.getPhone());
    }

    @Test
    void update_notFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () ->
            userService.update(99L, user)
        );
    }

    // --- deactivateUser ---
    @Test
    void deactivateUser_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        userService.deactivateUser(1L);
        assertEquals(UserEntity.Status.INACTIVE, user.getStatus());
        verify(userRepository).save(user);
    }

    @Test
    void deactivateUser_notFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () ->
            userService.deactivateUser(99L)
        );
    }

    @Test
    void update_withNationalityAndDocument_coversRemainingBranches() {
        UserEntity patch = UserEntity.builder()
            .nationality("Chilena")
            .identityDocument("12.345.678-9")
            .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        userService.update(1L, patch);

        assertEquals("Chilena", user.getNationality());
        assertEquals("12.345.678-9", user.getIdentityDocument());
        assertEquals("John Doe", user.getName());
        assertNull(user.getPhone());
    }
}
