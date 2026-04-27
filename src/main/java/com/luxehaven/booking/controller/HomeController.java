package com.luxehaven.booking.controller;

import com.luxehaven.booking.config.AuthInterceptor;
import com.luxehaven.booking.exception.BusinessException;
import com.luxehaven.booking.model.RoomType;
import com.luxehaven.booking.model.User;
import com.luxehaven.booking.service.BookingService;
import com.luxehaven.booking.service.RoomService;
import com.luxehaven.booking.service.VoucherService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.Map;

@Controller
public class HomeController {

    private final RoomService roomService;
    private final VoucherService voucherService;
    private final BookingService bookingService;

    public HomeController(RoomService roomService, VoucherService voucherService, BookingService bookingService) {
        this.roomService = roomService;
        this.voucherService = voucherService;
        this.bookingService = bookingService;
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        model.addAttribute("rooms", roomService.findAllAvailable().stream()
                .sorted((a, b) -> Double.compare(b.getPricePerNight(), a.getPricePerNight()))
                .limit(9)
                .toList());

        User currentUser = (User) session.getAttribute(AuthInterceptor.SESSION_USER);
        var savedIds = currentUser != null
                ? voucherService.getSavedByUser(currentUser.getId()).stream()
                        .map(v -> v.getId()).collect(java.util.stream.Collectors.toSet())
                : java.util.Set.of();
        var usedIds = currentUser != null
                ? voucherService.getUsedVoucherIdsByUser(currentUser.getId())
                : java.util.Set.of();

        model.addAttribute("vouchers", voucherService.findAll().stream()
                .filter(v -> v.isUsable())
                .filter(v -> !savedIds.contains(v.getId()))
                .filter(v -> !usedIds.contains(v.getId()))
                .sorted((a, b) -> Integer.compare(b.getDiscountPercent(), a.getDiscountPercent()))
                .limit(6)
                .toList());
        model.addAttribute("roomTypes", RoomType.values());
        model.addAttribute("bookedRoomIds", bookingService.getBookedRoomIdsToday());
        return "home";
    }

    @GetMapping("/rooms")
    public String listRooms(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false) RoomType type,
            @RequestParam(required = false, defaultValue = "high") String sort,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            Model model) {
        var all = roomService.search(checkIn, checkOut, type);
        if (minPrice != null) all = all.stream().filter(r -> r.getPricePerNight() >= minPrice).toList();
        if (maxPrice != null) all = all.stream().filter(r -> r.getPricePerNight() <= maxPrice).toList();
        all = switch (sort) {
            case "low" -> all.stream().sorted((a, b) -> Double.compare(a.getPricePerNight(), b.getPricePerNight())).toList();
            default -> all.stream().sorted((a, b) -> Double.compare(b.getPricePerNight(), a.getPricePerNight())).toList();
        };
        int pageSize = 20;
        int totalPages = Math.max(1, (int) Math.ceil(all.size() / (double) pageSize));
        int currentPage = Math.min(Math.max(page, 1), totalPages);
        int from = Math.min((currentPage - 1) * pageSize, all.size());
        int to = Math.min(from + pageSize, all.size());
        model.addAttribute("rooms", all.subList(from, to));
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);
        model.addAttribute("type", type);
        model.addAttribute("sort", sort);
        model.addAttribute("page", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("roomTypes", RoomType.values());
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("bookedRoomIds", bookingService.getBookedRoomIdsToday());
        model.addAttribute("maxPrice", maxPrice);
        return "rooms";
    }

    @GetMapping("/rooms/{id}")
    public String roomDetail(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
        model.addAttribute("room", roomService.findById(id));
        return "room-detail";
    }

    @PostMapping("/vouchers/{id}/save")
    @ResponseBody
    public ResponseEntity<Map<String, String>> saveVoucher(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute(AuthInterceptor.SESSION_USER);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Bạn cần đăng nhập để lưu voucher"));
        }
        try {
            voucherService.saveForUser(id, user.getId());
            return ResponseEntity.ok(Map.of("message", "Đã lưu voucher vào kho!"));
        } catch (BusinessException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/services")
    public String services() {
        return "services";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
}
