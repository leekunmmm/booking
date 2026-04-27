package com.luxehaven.booking.service;

import com.luxehaven.booking.dto.RegisterForm;
import com.luxehaven.booking.exception.BusinessException;
import com.luxehaven.booking.model.Role;
import com.luxehaven.booking.model.User;
import com.luxehaven.booking.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterForm form) {
        if (form.getUsername() == null || form.getUsername().trim().length() < 3) {
            throw new BusinessException("Tên đăng nhập phải có ít nhất 3 ký tự");
        }
        if (form.getPassword() == null || form.getPassword().length() < 6) {
            throw new BusinessException("Mật khẩu phải có ít nhất 6 ký tự");
        }
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new BusinessException("Mật khẩu xác nhận không khớp");
        }
        if (form.getEmail() == null || !form.getEmail().contains("@")) {
            throw new BusinessException("Email không hợp lệ");
        }
        if (form.getFullName() == null || form.getFullName().trim().isEmpty()) {
            throw new BusinessException("Họ tên không được để trống");
        }
        if (userRepository.existsByUsername(form.getUsername())) {
            throw new BusinessException("Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmail(form.getEmail())) {
            throw new BusinessException("Email đã được sử dụng");
        }

        User user = User.builder()
                .username(form.getUsername().trim())
                .password(passwordEncoder.encode(form.getPassword()))
                .fullName(form.getFullName().trim())
                .email(form.getEmail().trim())
                .phone(form.getPhone())
                .role(Role.CUSTOMER)
                .build();
        return userRepository.save(user);
    }

    public Optional<User> authenticate(String username, String password) {
        Optional<User> opt = userRepository.findByUsername(username);
        if (opt.isEmpty()) return Optional.empty();
        User u = opt.get();
        if (!passwordEncoder.matches(password, u.getPassword())) return Optional.empty();
        return Optional.of(u);
    }
}
