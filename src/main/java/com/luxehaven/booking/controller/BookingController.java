package com.luxehaven.booking.controller;

import com.luxehaven.booking.config.AuthInterceptor;
import com.luxehaven.booking.dto.BookingForm;
import com.luxehaven.booking.exception.BusinessException;
import com.luxehaven.booking.model.PaymentMethod;
import com.luxehaven.booking.model.User;
import com.luxehaven.booking.service.BookingService;
import com.luxehaven.booking.service.RoomService;
import com.luxehaven.booking.service.VoucherService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class BookingController {

    private final BookingService bookingService;
    private final RoomService roomService;
    private final VoucherService voucherService;
    @Value("${payment.bank.qr-image-url:/images/payments/bank-qr.png}")
    private String bankQrImageUrl;
    @Value("${payment.bank.name:Vietcombank}")
    private String bankName;
    @Value("${payment.bank.account-number:0123456789}")
    private String bankAccountNumber;
    @Value("${payment.bank.account-name:CONG TY LUXEHAVEN}")
    private String bankAccountName;
    @Value("${payment.bank.transfer-prefix:BOOKING}")
    private String bankTransferPrefix;
    @Value("${payment.momo.qr-image-url:/images/qrmomo.png}")
    private String momoQrImageUrl;
    @Value("${payment.momo.account-name:LUXEHAVEN}")
    private String momoAccountName;

    public BookingController(BookingService bookingService, RoomService roomService,
                             VoucherService voucherService) {
        this.bookingService = bookingService;
        this.roomService = roomService;
        this.voucherService = voucherService;
    }

    @GetMapping("/bookings/new")
    public String newBookingForm(
            @RequestParam Long roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            HttpSession session,
            Model model) {
        BookingForm form = new BookingForm();
        form.setRoomId(roomId);
        form.setCheckIn(checkIn);
        form.setCheckOut(checkOut);
        form.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        User user = (User) session.getAttribute(AuthInterceptor.SESSION_USER);
        if (user != null) {
            form.setCustomerFullName(user.getFullName());
            form.setCustomerPhone(user.getPhone());
            form.setCustomerEmail(user.getEmail());
        }
        model.addAttribute("bookingForm", form);
        model.addAttribute("room", roomService.findById(roomId));
        model.addAttribute("paymentMethods", PaymentMethod.values());
        if (user != null) {
            model.addAttribute("savedVouchers", voucherService.getSavedByUser(user.getId())
                    .stream().filter(v -> v.isUsable()).toList());
        }
        return "booking-form";
    }

    @PostMapping("/bookings")
    public String createBooking(@ModelAttribute BookingForm form,
                                HttpSession session,
                                RedirectAttributes ra,
                                Model model) {
        User user = (User) session.getAttribute(AuthInterceptor.SESSION_USER);
        try {
            var created = bookingService.createBooking(user.getId(), form);
            ra.addFlashAttribute("success", "Đặt phòng thành công. Vui lòng thanh toán ở bước 3.");
            return "redirect:/bookings/" + created.getId() + "/payment";
        } catch (BusinessException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("bookingForm", form);
            model.addAttribute("room", roomService.findById(form.getRoomId()));
            model.addAttribute("paymentMethods", PaymentMethod.values());
            model.addAttribute("savedVouchers", voucherService.getSavedByUser(user.getId())
                    .stream().filter(v -> v.isUsable()).toList());
            return "booking-form";
        }
    }

    @GetMapping("/bookings/{id}/payment")
    public String paymentStep(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes ra) {
        User user = (User) session.getAttribute(AuthInterceptor.SESSION_USER);
        try {
            var booking = bookingService.findById(id);
            if (!booking.getUserId().equals(user.getId())) {
                ra.addFlashAttribute("error", "Bạn không có quyền xem booking này");
                return "redirect:/my-bookings";
            }
            model.addAttribute("booking", booking);
            model.addAttribute("bankQrImageUrl", bankQrImageUrl);
            model.addAttribute("bankName", bankName);
            model.addAttribute("bankAccountNumber", bankAccountNumber);
            model.addAttribute("bankAccountName", bankAccountName);
            model.addAttribute("bankTransferPrefix", bankTransferPrefix);
            model.addAttribute("momoQrImageUrl", momoQrImageUrl);
            model.addAttribute("momoAccountName", momoAccountName);
            model.addAttribute("customerPhone", user.getPhone() != null ? user.getPhone().trim() : "");
            if (booking.getVoucherCode() != null) {
                voucherService.findByCode(booking.getVoucherCode())
                        .ifPresent(v -> model.addAttribute("voucherDiscountPercent", v.getDiscountPercent()));
            }
            return "booking-payment";
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/my-bookings";
        }
    }

    @GetMapping("/my-bookings")
    public String myBookings(HttpSession session, Model model) {
        User user = (User) session.getAttribute(AuthInterceptor.SESSION_USER);
        model.addAttribute("bookings", bookingService.findMyBookings(user.getId()));
        model.addAttribute("bankQrImageUrl", bankQrImageUrl);
        model.addAttribute("bankName", bankName);
        model.addAttribute("bankAccountNumber", bankAccountNumber);
        model.addAttribute("bankAccountName", bankAccountName);
        model.addAttribute("bankTransferPrefix", bankTransferPrefix);
        model.addAttribute("momoQrImageUrl", momoQrImageUrl);
        model.addAttribute("momoAccountName", momoAccountName);
        model.addAttribute("customerPhone", user.getPhone() != null ? user.getPhone().trim() : "");
        return "my-bookings";
    }

    @PostMapping("/bookings/{id}/pay")
    public String pay(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        User user = (User) session.getAttribute(AuthInterceptor.SESSION_USER);
        try {
            bookingService.pay(id, user.getId());
            ra.addFlashAttribute("success", "Đã ghi nhận thanh toán. Vui lòng chờ admin xác nhận.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/my-bookings";
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancel(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        User user = (User) session.getAttribute(AuthInterceptor.SESSION_USER);
        try {
            bookingService.cancel(id, user.getId());
            ra.addFlashAttribute("success", "Đã huỷ booking.");
        } catch (BusinessException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/my-bookings";
    }
}
