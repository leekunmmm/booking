package com.luxehaven.booking.service;

import com.luxehaven.booking.dto.RoomForm;
import com.luxehaven.booking.exception.BusinessException;
import com.luxehaven.booking.model.Room;
import com.luxehaven.booking.model.RoomStatus;
import com.luxehaven.booking.model.RoomType;
import com.luxehaven.booking.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    public List<Room> findFiltered(RoomType type, RoomStatus status, String search) {
        return roomRepository.findFiltered(type, status, search);
    }

    public List<Room> findAllAvailable() {
        return roomRepository.findAllAvailable();
    }

    public Room findById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy phòng"));
    }

    public List<Room> search(LocalDate checkIn, LocalDate checkOut, RoomType type) {
        if (checkIn == null || checkOut == null) {
            List<Room> all = roomRepository.findAll().stream()
                    .filter(r -> r.getStatus() == RoomStatus.AVAILABLE || r.getStatus() == RoomStatus.OCCUPIED)
                    .toList();
            return type == null ? all : all.stream().filter(r -> r.getType() == type).toList();
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new BusinessException("Ngày trả phòng phải sau ngày nhận phòng");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new BusinessException("Ngày nhận phòng không được ở quá khứ");
        }
        return roomRepository.findAvailableInRange(checkIn, checkOut, type);
    }

    public Room create(RoomForm form) {
        validate(form);
        if (roomRepository.existsByRoomNumber(form.getRoomNumber())) {
            throw new BusinessException("Số phòng đã tồn tại");
        }
        Room r = Room.builder()
                .roomNumber(form.getRoomNumber().trim())
                .type(form.getType())
                .pricePerNight(form.getPricePerNight())
                .capacity(form.getCapacity())
                .description(form.getDescription())
                .status(form.getStatus() != null ? form.getStatus() : RoomStatus.AVAILABLE)
                .imagesRaw(normalizeImages(form.getImagesRaw()))
                .amenitiesRaw(form.getAmenities() != null ? String.join(",", form.getAmenities()) : null)
                .build();
        return roomRepository.save(r);
    }

    public Room update(Long id, RoomForm form) {
        validate(form);
        Room existing = findById(id);
        if (!existing.getRoomNumber().equals(form.getRoomNumber())
                && roomRepository.existsByRoomNumber(form.getRoomNumber())) {
            throw new BusinessException("Số phòng đã tồn tại");
        }
        existing.setRoomNumber(form.getRoomNumber().trim());
        existing.setType(form.getType());
        existing.setPricePerNight(form.getPricePerNight());
        existing.setCapacity(form.getCapacity());
        existing.setDescription(form.getDescription());
        existing.setStatus(form.getStatus());
        existing.setImagesRaw(normalizeImages(form.getImagesRaw()));
        existing.setAmenitiesRaw(form.getAmenities() != null ? String.join(",", form.getAmenities()) : null);
        return roomRepository.save(existing);
    }

    public void delete(Long id) {
        Room r = findById(id);
        if (roomRepository.hasActiveBookings(r.getId())) {
            throw new BusinessException("Không thể xoá phòng đang có booking PENDING/CONFIRMED");
        }
        roomRepository.deleteById(r.getId());
    }

    private String normalizeImages(String raw) {
        if (raw == null || raw.isBlank()) return null;
        // accept both newline and comma separated, normalise to comma separated
        return String.join(",", raw.replace("\r", "").split("[\n,]+")).strip();
    }

    private void validate(RoomForm f) {
        if (f.getRoomNumber() == null || f.getRoomNumber().trim().isEmpty()) {
            throw new BusinessException("Số phòng không được để trống");
        }
        if (f.getType() == null) {
            throw new BusinessException("Loại phòng không được để trống");
        }
        if (f.getPricePerNight() <= 0) {
            throw new BusinessException("Giá/đêm phải lớn hơn 0");
        }
        if (f.getCapacity() <= 0) {
            throw new BusinessException("Sức chứa phải lớn hơn 0");
        }
    }
}
