package com.luxehaven.booking.service;

import com.luxehaven.booking.dto.BookingForm;
import com.luxehaven.booking.exception.BusinessException;
import com.luxehaven.booking.model.Booking;
import com.luxehaven.booking.model.BookingStatus;
import com.luxehaven.booking.model.PaymentMethod;
import com.luxehaven.booking.model.PaymentStatus;
import com.luxehaven.booking.model.Room;
import com.luxehaven.booking.model.RoomStatus;
import com.luxehaven.booking.model.RoomType;
import com.luxehaven.booking.repository.BookingRepository;
import com.luxehaven.booking.repository.RoomRepository;
import com.luxehaven.booking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    private BookingRepository bookingRepository;
    private RoomRepository roomRepository;
    private UserRepository userRepository;
    private VoucherService voucherService;
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        roomRepository = mock(RoomRepository.class);
        userRepository = mock(UserRepository.class);
        voucherService = mock(VoucherService.class);
        bookingService = new BookingService(bookingRepository, roomRepository, userRepository, voucherService);
    }

    private Room sampleRoom() {
        return Room.builder()
                .id(1L).roomNumber("101").type(RoomType.STANDARD)
                .pricePerNight(500_000).capacity(2)
                .description("test").status(RoomStatus.AVAILABLE)
                .build();
    }

    @Test
    void createBooking_calculatesTotalPrice() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(sampleRoom()));
        when(bookingRepository.hasOverlap(anyLong(), any(), any(), any())).thenReturn(false);
        when(bookingRepository.save(any())).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(99L);
            return b;
        });

        BookingForm form = new BookingForm();
        form.setRoomId(1L);
        form.setCheckIn(LocalDate.now().plusDays(1));
        form.setCheckOut(LocalDate.now().plusDays(4)); // 3 đêm
        form.setPaymentMethod(PaymentMethod.MOMO);
        form.setCustomerFullName("Nguyen Van A");
        form.setCustomerPhone("0900000000");
        form.setCustomerEmail("a@example.com");

        Booking saved = bookingService.createBooking(10L, form);

        assertEquals(99L, saved.getId());
        assertEquals(1_500_000.0, saved.getTotalPrice(), 0.001);
        assertEquals(BookingStatus.PENDING, saved.getStatus());
        assertEquals(PaymentStatus.UNPAID, saved.getPaymentStatus());
    }

    @Test
    void createBooking_failsIfCheckOutBeforeCheckIn() {
        BookingForm form = new BookingForm();
        form.setRoomId(1L);
        form.setCheckIn(LocalDate.now().plusDays(5));
        form.setCheckOut(LocalDate.now().plusDays(2));
        form.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        form.setCustomerFullName("Nguyen Van A");
        form.setCustomerPhone("0900000000");
        form.setCustomerEmail("a@example.com");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> bookingService.createBooking(10L, form));
        assertTrue(ex.getMessage().contains("sau ngày nhận phòng"));
    }

    @Test
    void createBooking_failsIfRoomAlreadyBooked() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(sampleRoom()));
        when(bookingRepository.hasOverlap(anyLong(), any(), any(), any())).thenReturn(true);

        BookingForm form = new BookingForm();
        form.setRoomId(1L);
        form.setCheckIn(LocalDate.now().plusDays(1));
        form.setCheckOut(LocalDate.now().plusDays(3));
        form.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        form.setCustomerFullName("Nguyen Van A");
        form.setCustomerPhone("0900000000");
        form.setCustomerEmail("a@example.com");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> bookingService.createBooking(10L, form));
        assertTrue(ex.getMessage().toLowerCase().contains("đã được đặt"));
    }

    @Test
    void pay_setsStatusPendingConfirmation() {
        Booking existing = Booking.builder()
                .id(5L).userId(10L).roomId(1L)
                .checkIn(LocalDate.now().plusDays(1))
                .checkOut(LocalDate.now().plusDays(2))
                .totalPrice(500_000)
                .status(BookingStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .build();
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Booking result = bookingService.pay(5L, 10L);

        assertEquals(BookingStatus.PENDING, result.getStatus());
        assertEquals(PaymentStatus.PENDING_CONFIRMATION, result.getPaymentStatus());
    }

    @Test
    void pay_failsIfNotOwner() {
        Booking existing = Booking.builder()
                .id(5L).userId(10L).roomId(1L)
                .checkIn(LocalDate.now().plusDays(1))
                .checkOut(LocalDate.now().plusDays(2))
                .totalPrice(500_000)
                .status(BookingStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .build();
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(existing));

        assertThrows(BusinessException.class, () -> bookingService.pay(5L, 999L));
    }

    @Test
    void cancel_changesStatusToCancelled() {
        Booking existing = Booking.builder()
                .id(5L).userId(10L).roomId(1L)
                .checkIn(LocalDate.now().plusDays(5))
                .checkOut(LocalDate.now().plusDays(7))
                .totalPrice(1_000_000)
                .status(BookingStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .build();
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Booking result = bookingService.cancel(5L, 10L);

        assertEquals(BookingStatus.CANCELLED, result.getStatus());
    }
}
