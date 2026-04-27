package com.luxehaven.booking.dto;

import com.luxehaven.booking.model.RoomStatus;
import com.luxehaven.booking.model.RoomType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RoomForm {
    private Long id;
    private String roomNumber;
    private RoomType type;
    private double pricePerNight;
    private int capacity;
    private String description;
    private RoomStatus status = RoomStatus.AVAILABLE;
    private String imagesRaw;          // textarea: one URL per line
    private List<String> amenities = new ArrayList<>(); // checkbox values
}
