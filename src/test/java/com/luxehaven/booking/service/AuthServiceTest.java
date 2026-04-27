package com.luxehaven.booking.service;

import com.luxehaven.booking.dto.RegisterForm;
import com.luxehaven.booking.exception.BusinessException;
import com.luxehaven.booking.model.Role;
import com.luxehaven.booking.model.User;
import com.luxehaven.booking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserRepository userRepository;
    private BCryptPasswordEncoder encoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        encoder = new BCryptPasswordEncoder(4); // cost thấp để test nhanh
        authService = new AuthService(userRepository, encoder);
    }

    @Test
    void register_success() {
        RegisterForm form = new RegisterForm();
        form.setUsername("user1");
        form.setPassword("pass123");
        form.setConfirmPassword("pass123");
        form.setFullName("Test User");
        form.setEmail("a@b.com");
        form.setPhone("0900");

        when(userRepository.existsByUsername("user1")).thenReturn(false);
        when(userRepository.existsByEmail("a@b.com")).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        User saved = authService.register(form);
        assertNotNull(saved.getId());
        assertEquals(Role.CUSTOMER, saved.getRole());
        assertNotEquals("pass123", saved.getPassword(), "Mật khẩu phải được hash");
        assertTrue(encoder.matches("pass123", saved.getPassword()));
    }

    @Test
    void register_passwordMismatch() {
        RegisterForm form = new RegisterForm();
        form.setUsername("user1");
        form.setPassword("pass123");
        form.setConfirmPassword("pass456");
        form.setFullName("Test");
        form.setEmail("a@b.com");

        assertThrows(BusinessException.class, () -> authService.register(form));
    }

    @Test
    void register_duplicateUsername() {
        RegisterForm form = new RegisterForm();
        form.setUsername("user1");
        form.setPassword("pass123");
        form.setConfirmPassword("pass123");
        form.setFullName("Test");
        form.setEmail("a@b.com");

        when(userRepository.existsByUsername("user1")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.register(form));
        assertTrue(ex.getMessage().contains("đã tồn tại"));
    }

    @Test
    void authenticate_correctPassword_returnsUser() {
        String hashed = encoder.encode("secret");
        User u = User.builder()
                .id(1L).username("u").password(hashed)
                .fullName("U").email("u@u.com").role(Role.CUSTOMER).build();
        when(userRepository.findByUsername("u")).thenReturn(Optional.of(u));

        Optional<User> result = authService.authenticate("u", "secret");
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void authenticate_wrongPassword_returnsEmpty() {
        String hashed = encoder.encode("secret");
        User u = User.builder()
                .id(1L).username("u").password(hashed)
                .fullName("U").email("u@u.com").role(Role.CUSTOMER).build();
        when(userRepository.findByUsername("u")).thenReturn(Optional.of(u));

        Optional<User> result = authService.authenticate("u", "wrong");
        assertTrue(result.isEmpty());
    }
}
