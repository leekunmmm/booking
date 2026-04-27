package com.luxehaven.booking.controller;

import com.luxehaven.booking.config.AuthInterceptor;
import com.luxehaven.booking.dto.LoginForm;
import com.luxehaven.booking.dto.RegisterForm;
import com.luxehaven.booking.exception.BusinessException;
import com.luxehaven.booking.model.User;
import com.luxehaven.booking.repository.BookingRepository;
import com.luxehaven.booking.repository.UserRepository;
import com.luxehaven.booking.service.AuthService;
import com.luxehaven.booking.service.BookingService;
import com.luxehaven.booking.service.VoucherService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;



@Controller
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final VoucherService voucherService;

    public AuthController(AuthService authService, UserRepository userRepository,
                          BookingRepository bookingRepository, BookingService bookingService,
                          VoucherService voucherService) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.bookingService = bookingService;
        this.voucherService = voucherService;
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new LoginForm());
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute LoginForm form,
                        @RequestParam(required = false) String redirect,
                        HttpSession session,
                        Model model) {
        Optional<User> opt = authService.authenticate(form.getUsername(), form.getPassword());
        if (opt.isEmpty()) {
            model.addAttribute("error", "Sai tên đăng nhập hoặc mật khẩu");
            model.addAttribute("loginForm", form);
            return "login";
        }
        session.setAttribute(AuthInterceptor.SESSION_USER, opt.get());
        if (redirect != null && !redirect.isBlank()) return "redirect:" + redirect;
        return opt.get().isAdmin() ? "redirect:/admin" : "redirect:/";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new RegisterForm());
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute RegisterForm form, Model model) {
        try {
            authService.register(form);
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("registerForm", form);
            return "register";
        }
        model.addAttribute("loginForm", new LoginForm());
        model.addAttribute("success", "Đăng ký thành công! Hãy đăng nhập để bắt đầu.");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // ===== Profile =====
    @GetMapping("/profile")
    public String profilePage(HttpSession session, Model model) {
        User user = (User) session.getAttribute(AuthInterceptor.SESSION_USER);
        if (user == null) return "redirect:/login";
        User fresh = userRepository.findById(user.getId()).orElse(user);
        double totalSpent = bookingRepository.sumPaidByUserId(fresh.getId());
        int bookingCount = bookingRepository.countByUserId(fresh.getId());
        model.addAttribute("profileUser", fresh);
        model.addAttribute("totalSpent", totalSpent);
        model.addAttribute("bookingCount", bookingCount);
        String memberTier = getMemberTier(totalSpent);
        model.addAttribute("memberTier", memberTier);
        model.addAttribute("memberTierLabel", getMemberTierLabel(memberTier));
        model.addAttribute("memberBenefit", getMemberBenefit(memberTier));
        model.addAttribute("memberDiscountPercent", getMemberDiscountPercent(memberTier));
        model.addAttribute("memberTier", memberTier);
        model.addAttribute("myVouchers", voucherService.getSavedByUser(fresh.getId()));
        model.addAttribute("myBookings", bookingService.findMyBookings(fresh.getId()));
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String fullName,
                                @RequestParam String email,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String avatarUrl,
                                HttpSession session,
                                RedirectAttributes ra) {
        User user = (User) session.getAttribute(AuthInterceptor.SESSION_USER);
        if (user == null) return "redirect:/login";
        userRepository.updateProfile(user.getId(), fullName, email, phone, avatarUrl);
        User updated = userRepository.findById(user.getId()).orElse(user);
        session.setAttribute(AuthInterceptor.SESSION_USER, updated);
        ra.addFlashAttribute("success", "Đã cập nhật thông tin thành công");
        return "redirect:/profile";
    }

    @PostMapping("/profile/upload-avatar")
    @ResponseBody
    public ResponseEntity<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File trống"));
        }
        try {
            String original = file.getOriginalFilename();
            String ext = ".png";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf('.'));
            }
            String filename = "avatar-" + UUID.randomUUID() + ext;
            Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);
            Files.copy(file.getInputStream(), uploadDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return ResponseEntity.ok(Map.of("url", "/uploads/" + filename));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Không thể tải ảnh lên"));
        }
    }

    private String getMemberTier(double totalSpent) {
        if (totalSpent >= 100_000_000) return "VIP";
        if (totalSpent >=  50_000_000) return "DIAMOND";
        if (totalSpent >=  20_000_000) return "GOLD";
        if (totalSpent >=   5_000_000) return "SILVER";
        return "BRONZE";
    }

    private String getMemberTierLabel(String tier) {
        return switch (tier) {
            case "SILVER"  -> "Silver";
            case "GOLD"    -> "Gold";
            case "DIAMOND" -> "Diamond";
            case "VIP"     -> "VIP";
            default        -> "Bronze";
        };
    }

    private String getMemberBenefit(String tier) {
        return switch (tier) {
            case "SILVER"  -> "5% discount on services, priority support";
            case "GOLD"    -> "10% discount, priority early check-in";
            case "DIAMOND" -> "15% discount, room upgrade when available";
            case "VIP"     -> "20% discount, dedicated concierge & exclusive perks";
            default        -> "Basic benefits, earn membership points";
        };
    }

    private int getMemberDiscountPercent(String tier) {
        return switch (tier) {
            case "SILVER"  -> 5;
            case "GOLD"    -> 10;
            case "DIAMOND" -> 15;
            case "VIP"     -> 20;
            default        -> 0;
        };
    }
}
