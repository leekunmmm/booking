package com.luxehaven.booking.controller;

import com.luxehaven.booking.dto.RoomForm;
import com.luxehaven.booking.dto.VoucherForm;
import com.luxehaven.booking.exception.BusinessException;
import com.luxehaven.booking.model.Amenity;
import com.luxehaven.booking.model.BookingStatus;
import com.luxehaven.booking.model.MemberTier;
import com.luxehaven.booking.model.PaymentStatus;
import com.luxehaven.booking.model.Room;
import com.luxehaven.booking.model.RoomStatus;
import com.luxehaven.booking.model.RoomType;
import com.luxehaven.booking.model.User;
import com.luxehaven.booking.model.Voucher;
import com.luxehaven.booking.service.BookingService;
import com.luxehaven.booking.service.RoomService;
import com.luxehaven.booking.service.UserService;
import com.luxehaven.booking.service.VoucherService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final RoomService roomService;
    private final BookingService bookingService;
    private final VoucherService voucherService;
    private final UserService userService;

    public AdminController(RoomService roomService, BookingService bookingService, VoucherService voucherService, UserService userService) {
        this.roomService = roomService;
        this.bookingService = bookingService;
        this.voucherService = voucherService;
        this.userService = userService;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalRooms", roomService.findAll().size());
        model.addAttribute("totalCustomers", userService.countCustomers());
        model.addAttribute("pendingBookings", bookingService.countByStatus(BookingStatus.PENDING));
        model.addAttribute("confirmedBookings", bookingService.countByStatus(BookingStatus.CONFIRMED));
        model.addAttribute("completedBookings", bookingService.countByStatus(BookingStatus.COMPLETED));
        model.addAttribute("totalRevenue", bookingService.totalRevenue());
        model.addAttribute("recentBookings", bookingService.findAllForAdmin(null).stream().limit(5).toList());
        return "admin/dashboard";
    }

    // ===== Rooms =====
    @GetMapping("/rooms")
    public String rooms(@RequestParam(required = false) RoomType type,
                        @RequestParam(required = false) RoomStatus status,
                        @RequestParam(required = false) String search,
                        Model model) {
        model.addAttribute("rooms", roomService.findFiltered(type, status, search));
        model.addAttribute("roomTypes", RoomType.values());
        model.addAttribute("roomStatuses", RoomStatus.values());
        model.addAttribute("filterType", type);
        model.addAttribute("filterStatus", status);
        model.addAttribute("filterSearch", search);
        return "admin/rooms";
    }

    @GetMapping("/rooms/new")
    public String newRoomForm(Model model) {
        model.addAttribute("roomForm", new RoomForm());
        model.addAttribute("roomTypes", RoomType.values());
        model.addAttribute("roomStatuses", RoomStatus.values());
        model.addAttribute("allAmenities", Amenity.values());
        model.addAttribute("editing", false);
        return "admin/room-form";
    }

    @PostMapping("/rooms")
    public String createRoom(@ModelAttribute RoomForm form, RedirectAttributes ra, Model model) {
        try {
            roomService.create(form);
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("roomForm", form);
            model.addAttribute("roomTypes", RoomType.values());
            model.addAttribute("roomStatuses", RoomStatus.values());
            model.addAttribute("allAmenities", Amenity.values());
            model.addAttribute("editing", false);
            return "admin/room-form";
        }
        ra.addFlashAttribute("success", "Đã thêm phòng mới");
        return "redirect:/admin/rooms";
    }

    @GetMapping("/rooms/{id}/edit")
    public String editRoomForm(@PathVariable Long id, Model model) {
        Room r = roomService.findById(id);
        RoomForm form = new RoomForm();
        form.setId(r.getId());
        form.setRoomNumber(r.getRoomNumber());
        form.setType(r.getType());
        form.setPricePerNight(r.getPricePerNight());
        form.setCapacity(r.getCapacity());
        form.setDescription(r.getDescription());
        form.setStatus(r.getStatus());
        form.setImagesRaw(r.getImagesRaw() != null ? r.getImagesRaw().replace(",", "\n") : "");
        if (r.getAmenitiesRaw() != null && !r.getAmenitiesRaw().isBlank()) {
            form.setAmenities(Arrays.asList(r.getAmenitiesRaw().split(",")));
        }
        model.addAttribute("roomForm", form);
        model.addAttribute("roomTypes", RoomType.values());
        model.addAttribute("roomStatuses", RoomStatus.values());
        model.addAttribute("allAmenities", Amenity.values());
        model.addAttribute("editing", true);
        return "admin/room-form";
    }

    @PostMapping("/rooms/{id}/edit")
    public String updateRoom(@PathVariable Long id, @ModelAttribute RoomForm form,
                             RedirectAttributes ra, Model model) {
        try {
            roomService.update(id, form);
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("roomForm", form);
            model.addAttribute("roomTypes", RoomType.values());
            model.addAttribute("roomStatuses", RoomStatus.values());
            model.addAttribute("allAmenities", Amenity.values());
            model.addAttribute("editing", true);
            return "admin/room-form";
        }
        ra.addFlashAttribute("success", "Đã cập nhật phòng");
        return "redirect:/admin/rooms";
    }

    @PostMapping("/rooms/{id}/delete")
    public String deleteRoom(@PathVariable Long id, RedirectAttributes ra) {
        try {
            roomService.delete(id);
            ra.addFlashAttribute("success", "Đã xoá phòng");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/rooms";
    }

    // ===== Bookings =====
    @GetMapping("/bookings")
    public String bookings(@RequestParam(required = false) BookingStatus status,
                           @RequestParam(required = false) PaymentStatus paymentStatus,
                           @RequestParam(required = false) String search,
                           Model model) {
        model.addAttribute("bookings", bookingService.findAllForAdmin(status, paymentStatus, search));
        model.addAttribute("statuses", BookingStatus.values());
        model.addAttribute("paymentStatuses", PaymentStatus.values());
        model.addAttribute("filterStatus", status);
        model.addAttribute("filterPaymentStatus", paymentStatus);
        model.addAttribute("filterSearch", search);
        return "admin/bookings";
    }

    @PostMapping("/bookings/{id}/status")
    public String updateBookingStatus(@PathVariable Long id, @RequestParam BookingStatus status,
                                      RedirectAttributes ra) {
        try {
            bookingService.adminUpdateStatus(id, status);
            ra.addFlashAttribute("success", "Đã cập nhật trạng thái booking");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/bookings";
    }

    @PostMapping("/bookings/{id}/confirm-payment")
    public String confirmPayment(@PathVariable Long id, RedirectAttributes ra) {
        try {
            bookingService.adminConfirmPayment(id);
            ra.addFlashAttribute("success", "Đã xác nhận thanh toán cho booking #" + id);
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/bookings";
    }

    // ===== Vouchers =====
    @GetMapping("/vouchers")
    public String vouchers(@RequestParam(required = false) String search,
                           @RequestParam(required = false) String active,
                           @RequestParam(required = false) String tier,
                           Model model) {
        List<Voucher> vouchers = voucherService.findAll();
        if (search != null && !search.isBlank()) {
            String q = search.trim().toUpperCase();
            vouchers = vouchers.stream().filter(v -> v.getCode().contains(q)).toList();
        }
        if ("true".equals(active)) {
            vouchers = vouchers.stream().filter(Voucher::isUsable).toList();
        } else if ("false".equals(active)) {
            vouchers = vouchers.stream().filter(v -> !v.isUsable()).toList();
        }
        if (tier != null && !tier.isBlank()) {
            vouchers = vouchers.stream().filter(v -> tier.equals(v.getTargetTier())).toList();
        }
        model.addAttribute("vouchers", vouchers);
        model.addAttribute("memberTiers", MemberTier.values());
        model.addAttribute("filterSearch", search);
        model.addAttribute("filterActive", active);
        model.addAttribute("filterTier", tier);
        return "admin/vouchers";
    }

    @GetMapping("/vouchers/new")
    public String newVoucherForm(Model model) {
        model.addAttribute("voucherForm", new VoucherForm());
        model.addAttribute("memberTiers", MemberTier.values());
        model.addAttribute("editing", false);
        return "admin/voucher-form";
    }

    @PostMapping("/vouchers")
    public String createVoucher(@ModelAttribute VoucherForm form, RedirectAttributes ra, Model model) {
        try {
            voucherService.create(form);
            ra.addFlashAttribute("success", "Đã tạo voucher " + form.getCode().toUpperCase());
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("voucherForm", form);
            model.addAttribute("editing", false);
            return "admin/voucher-form";
        }
        return "redirect:/admin/vouchers";
    }

    @GetMapping("/vouchers/{id}/edit")
    public String editVoucherForm(@PathVariable Long id, Model model) {
        Voucher v = voucherService.findById(id);
        VoucherForm form = new VoucherForm();
        form.setId(v.getId());
        form.setCode(v.getCode());
        form.setDiscountPercent(v.getDiscountPercent());
        form.setValidUntil(v.getValidUntil());
        form.setMaxUses(v.getMaxUses());
        form.setActive(v.isActive());
        form.setTargetTier(v.getTargetTier());
        model.addAttribute("voucherForm", form);
        model.addAttribute("memberTiers", MemberTier.values());
        model.addAttribute("editing", true);
        return "admin/voucher-form";
    }

    @PostMapping("/vouchers/{id}/edit")
    public String updateVoucher(@PathVariable Long id, @ModelAttribute VoucherForm form,
                                RedirectAttributes ra, Model model) {
        try {
            voucherService.update(id, form);
            ra.addFlashAttribute("success", "Đã cập nhật voucher");
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("voucherForm", form);
            model.addAttribute("editing", true);
            return "admin/voucher-form";
        }
        return "redirect:/admin/vouchers";
    }

    @PostMapping("/vouchers/{id}/delete")
    public String deleteVoucher(@PathVariable Long id, RedirectAttributes ra) {
        voucherService.delete(id);
        ra.addFlashAttribute("success", "Đã xoá voucher");
        return "redirect:/admin/vouchers";
    }

    // ===== Customers =====
    @GetMapping("/customers")
    public String customers(@RequestParam(required = false) String search,
                            @RequestParam(required = false) String tier,
                            Model model) {
        List<User> allUsers = userService.findAll();

        Map<Long, MemberTier> tierMap = new HashMap<>();
        Map<Long, Double> spendingMap = new HashMap<>();
        for (User u : allUsers) {
            if (!u.isAdmin()) {
                tierMap.put(u.getId(), userService.calculateTier(u.getId()));
                spendingMap.put(u.getId(), userService.totalSpent(u.getId()));
            }
        }

        List<User> filtered = allUsers;
        if (search != null && !search.isBlank()) {
            String q = search.trim().toLowerCase();
            filtered = filtered.stream()
                    .filter(u -> u.getFullName().toLowerCase().contains(q) ||
                                 u.getUsername().toLowerCase().contains(q) ||
                                 (u.getEmail() != null && u.getEmail().toLowerCase().contains(q)))
                    .toList();
        }
        if (tier != null && !tier.isBlank()) {
            try {
                MemberTier mt = MemberTier.valueOf(tier);
                filtered = filtered.stream()
                        .filter(u -> !u.isAdmin() && mt.equals(tierMap.get(u.getId())))
                        .toList();
            } catch (IllegalArgumentException ignored) {}
        }

        model.addAttribute("customers", filtered);
        model.addAttribute("tierMap", tierMap);
        model.addAttribute("spendingMap", spendingMap);
        model.addAttribute("memberTiers", MemberTier.values());
        model.addAttribute("filterSearch", search);
        model.addAttribute("filterTier", tier);
        return "admin/customers";
    }

    @PostMapping("/customers/{id}/delete")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes ra) {
        userService.delete(id);
        ra.addFlashAttribute("success", "Đã xoá khách hàng");
        return "redirect:/admin/customers";
    }

    // ===== Image Upload =====
    @PostMapping("/upload-image")
    @ResponseBody
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "File trống"));
        try {
            String ext = "";
            String original = file.getOriginalFilename();
            if (original != null && original.contains("."))
                ext = original.substring(original.lastIndexOf('.'));
            String filename = UUID.randomUUID() + ext;
            Path uploadDir = Paths.get("uploads").toAbsolutePath();
            Files.createDirectories(uploadDir);
            Files.copy(file.getInputStream(), uploadDir.resolve(filename));
            return ResponseEntity.ok(Map.of("url", "/uploads/" + filename));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
