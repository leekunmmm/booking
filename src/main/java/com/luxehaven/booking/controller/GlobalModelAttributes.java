package com.luxehaven.booking.controller;

import com.luxehaven.booking.config.AuthInterceptor;
import com.luxehaven.booking.model.User;
import com.luxehaven.booking.repository.BookingRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final BookingRepository bookingRepository;

    public GlobalModelAttributes(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @ModelAttribute("currentUser")
    public User currentUser(HttpSession session) {
        User user = (User) session.getAttribute(AuthInterceptor.SESSION_USER);
        if (user != null) {
            user.setMemberTier(getMemberTier(user.getId()));
        }
        return user;
    }

    private String getMemberTier(Long userId) {
        if (userId == null) return "BRONZE";
        double totalSpent = bookingRepository.sumPaidByUserId(userId);
        if (totalSpent >= 100_000_000) return "VIP";
        if (totalSpent >=  50_000_000) return "DIAMOND";
        if (totalSpent >=  20_000_000) return "GOLD";
        if (totalSpent >=   5_000_000) return "SILVER";
        return "BRONZE";
    }
}
