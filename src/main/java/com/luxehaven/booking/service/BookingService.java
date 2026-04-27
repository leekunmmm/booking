package com.luxehaven.booking.service;

import com.luxehaven.booking.dto.BookingForm;
import com.luxehaven.booking.exception.BusinessException;
import com.luxehaven.booking.model.Booking;
import com.luxehaven.booking.model.BookingStatus;
import com.luxehaven.booking.model.PaymentMethod;
import com.luxehaven.booking.model.PaymentStatus;
import com.luxehaven.booking.model.Room;
import com.luxehaven.booking.model.RoomStatus;
import com.luxehaven.booking.model.Voucher;
import com.luxehaven.booking.repository.BookingRepository;
import com.luxehaven.booking.repository.RoomRepository;
import com.luxehaven.booking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final VoucherService voucherService;

    public BookingService(BookingRepository bookingRepository,
                          RoomRepository roomRepository,
                          UserRepository userRepository,
                          VoucherService voucherService) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.voucherService = voucherService;
    }

    @Transactional
    public Booking createBooking(Long userId, BookingForm form) {
        if (form.getRoomId() == null) throw new BusinessException("Thiếu thông tin phòng");
        if (form.getCheckIn() == null || form.getCheckOut() == null) {
            throw new BusinessException("Vui lòng chọn ngày nhận phòng và trả phòng");
        }
        if (!form.getCheckOut().isAfter(form.getCheckIn())) {
            throw new BusinessException("Ngày trả phòng phải sau ngày nhận phòng");
        }
        if (form.getCheckIn().isBefore(LocalDate.now())) {
            throw new BusinessException("Ngày nhận phòng không được ở quá khứ");
        }
        if (form.getPaymentMethod() == null) {
            throw new BusinessException("Vui lòng chọn phương thức thanh toán");
        }
        if (form.getPaymentMethod() != PaymentMethod.MOMO
                && form.getPaymentMethod() != PaymentMethod.BANK_TRANSFER) {
            throw new BusinessException("Phương thức thanh toán không hợp lệ");
        }
        if (form.getCustomerFullName() == null || form.getCustomerFullName().trim().isEmpty()) {
            throw new BusinessException("Vui lòng nhập họ tên khách hàng");
        }
        if (form.getCustomerPhone() == null || form.getCustomerPhone().trim().isEmpty()) {
            throw new BusinessException("Vui lòng nhập số điện thoại khách hàng");
        }
        if (form.getCustomerEmail() == null || form.getCustomerEmail().trim().isEmpty() || !form.getCustomerEmail().contains("@")) {
            throw new BusinessException("Vui lòng nhập email khách hàng hợp lệ");
        }

        Room room = roomRepository.findById(form.getRoomId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy phòng"));

        if (bookingRepository.hasOverlap(room.getId(), form.getCheckIn(), form.getCheckOut(), null)) {
            throw new BusinessException("Phòng đã được đặt trong khoảng thời gian này");
        }

        long nights = ChronoUnit.DAYS.between(form.getCheckIn(), form.getCheckOut());
        double totalPrice = nights * room.getPricePerNight();

        // Apply voucher if provided
        Voucher voucher = null;
        String voucherCode = form.getVoucherCode();
        if (voucherCode != null && !voucherCode.isBlank()) {
            voucher = voucherService.validateAndGet(voucherCode.trim(), userId);
            totalPrice = totalPrice * (1.0 - voucher.getDiscountPercent() / 100.0);
        }

        Booking b = Booking.builder()
                .userId(userId)
                .roomId(room.getId())
                .checkIn(form.getCheckIn())
                .checkOut(form.getCheckOut())
                .totalPrice(totalPrice)
                .status(BookingStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .paymentMethod(form.getPaymentMethod())
                .customerFullName(form.getCustomerFullName().trim())
                .customerPhone(form.getCustomerPhone().trim())
                .customerEmail(form.getCustomerEmail().trim())
                .customerNote(form.getCustomerNote() == null ? null : form.getCustomerNote().trim())
                .voucherCode(voucher != null ? voucher.getCode() : null)
                .build();
        Booking saved = bookingRepository.save(b);

        if (voucher != null) {
            voucherService.use(voucher, userId);
            voucherService.removeSavedForUser(voucher.getId(), userId);
        }
        return saved;
    }

    public List<Booking> findMyBookings(Long userId) {
        List<Booking> list = bookingRepository.findByUserId(userId);
        attachRoomInfo(list);
        return list;
    }

    public List<Booking> findAllForAdmin(BookingStatus filter) {
        return findAllForAdmin(filter, null, null);
    }

    public List<Booking> findAllForAdmin(BookingStatus statusFilter, PaymentStatus paymentFilter, String search) {
        List<Booking> list = (statusFilter == null)
                ? bookingRepository.findAll()
                : bookingRepository.findByStatus(statusFilter);
        attachRoomInfo(list);
        attachUserInfo(list);
        if (paymentFilter != null) {
            list = list.stream().filter(b -> b.getPaymentStatus() == paymentFilter).toList();
        }
        if (search != null && !search.isBlank()) {
            String q = search.trim().toLowerCase();
            list = list.stream().filter(b ->
                    (b.getCustomerFullName() != null && b.getCustomerFullName().toLowerCase().contains(q)) ||
                    (b.getUser() != null && b.getUser().getFullName().toLowerCase().contains(q)) ||
                    String.valueOf(b.getId()).contains(q)
            ).toList();
        }
        return list;
    }

    public Booking findById(Long id) {
        Booking b = bookingRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy booking"));
        roomRepository.findById(b.getRoomId()).ifPresent(b::setRoom);
        userRepository.findById(b.getUserId()).ifPresent(b::setUser);
        return b;
    }

    @Transactional
    public Booking pay(Long bookingId, Long userId) {
        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy booking"));
        if (!b.getUserId().equals(userId)) {
            throw new BusinessException("Bạn không có quyền thao tác booking này");
        }
        if (b.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("Booking đã huỷ, không thể thanh toán");
        }
        if (b.getPaymentStatus() == PaymentStatus.PAID || b.getPaymentStatus() == PaymentStatus.PENDING_CONFIRMATION) {
            throw new BusinessException("Booking đã được gửi thanh toán");
        }
        b.setPaymentStatus(PaymentStatus.PENDING_CONFIRMATION);
        return bookingRepository.save(b);
    }

    @Transactional
    public Booking adminConfirmPayment(Long bookingId) {
        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy booking"));
        if (b.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("Booking đã hủy, không thể xác nhận thanh toán");
        }
        if (b.getPaymentStatus() == PaymentStatus.PAID) {
            return b;
        }
        b.setPaymentStatus(PaymentStatus.PAID);
        b.setStatus(BookingStatus.CONFIRMED);
        return bookingRepository.save(b);
    }

    @Transactional
    public Booking cancel(Long bookingId, Long userId) {
        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy booking"));
        if (!b.getUserId().equals(userId)) {
            throw new BusinessException("Bạn không có quyền huỷ booking này");
        }
        if (b.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException("Booking đã được huỷ trước đó");
        }
        if (b.getStatus() == BookingStatus.COMPLETED) {
            throw new BusinessException("Booking đã hoàn tất, không thể huỷ");
        }
        if (!b.getCheckIn().isAfter(LocalDate.now()) && b.getStatus() == BookingStatus.CONFIRMED) {
            throw new BusinessException("Đã đến ngày nhận phòng, không thể huỷ");
        }
        b.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(b);

        // Release room if no other active bookings
        if (!bookingRepository.hasActiveBookingsExcluding(b.getRoomId(), bookingId)) {
            roomRepository.updateStatus(b.getRoomId(), RoomStatus.AVAILABLE);
        }
        return saved;
    }

    @Transactional
    public Booking adminUpdateStatus(Long bookingId, BookingStatus newStatus) {
        Booking b = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy booking"));
        b.setStatus(newStatus);
        if (newStatus == BookingStatus.COMPLETED) {
            b.setPaymentStatus(PaymentStatus.PAID);
            // Room available again
            roomRepository.updateStatus(b.getRoomId(), RoomStatus.AVAILABLE);
        } else if (newStatus == BookingStatus.CONFIRMED) {
            b.setPaymentStatus(PaymentStatus.PAID);
            // Keep room AVAILABLE for future-date bookings.
            roomRepository.updateStatus(b.getRoomId(), RoomStatus.AVAILABLE);
        } else if (newStatus == BookingStatus.CANCELLED) {
            if (!bookingRepository.hasActiveBookingsExcluding(b.getRoomId(), bookingId)) {
                roomRepository.updateStatus(b.getRoomId(), RoomStatus.AVAILABLE);
            }
        }
        return bookingRepository.save(b);
    }

    public java.util.Set<Long> getBookedRoomIdsToday() {
        return bookingRepository.findBookedRoomIdsOnDate(LocalDate.now());
    }

    public int countByStatus(BookingStatus status) {
        return bookingRepository.countByStatus(status);
    }

    public double totalRevenue() {
        return bookingRepository.totalRevenue();
    }

    private void attachRoomInfo(List<Booking> bookings) {
        for (Booking b : bookings) {
            roomRepository.findById(b.getRoomId()).ifPresent(b::setRoom);
        }
    }

    private void attachUserInfo(List<Booking> bookings) {
        for (Booking b : bookings) {
            userRepository.findById(b.getUserId()).ifPresent(b::setUser);
        }
    }
}
