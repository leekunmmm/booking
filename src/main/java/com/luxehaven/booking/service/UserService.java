package com.luxehaven.booking.service;

import com.luxehaven.booking.model.MemberTier;
import com.luxehaven.booking.model.User;
import com.luxehaven.booking.repository.BookingRepository;
import com.luxehaven.booking.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public UserService(UserRepository userRepository, BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    public double totalSpent(Long userId) {
        return bookingRepository.sumPaidByUserId(userId);
    }

    public MemberTier calculateTier(Long userId) {
        double spent = totalSpent(userId);
        if (spent >= 100_000_000) return MemberTier.VIP;
        if (spent >=  50_000_000) return MemberTier.DIAMOND;
        if (spent >=  20_000_000) return MemberTier.GOLD;
        if (spent >=   5_000_000) return MemberTier.SILVER;
        return MemberTier.BRONZE;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public long countCustomers() {
        return userRepository.findAll().stream()
                .filter(u -> !u.isAdmin())
                .count();
    }
}