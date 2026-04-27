package com.luxehaven.booking.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {
    private Long id;
    private String roomNumber;
    private RoomType type;
    private double pricePerNight;
    private int capacity;
    private String description;
    private RoomStatus status;
    private String imagesRaw;    // comma-separated URLs stored in DB
    private String amenitiesRaw; // comma-separated Amenity names stored in DB

    public List<String> getImages() {
        if (imagesRaw == null || imagesRaw.isBlank()) return Collections.emptyList();
        return List.of(imagesRaw.split(",")).stream()
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    public List<Amenity> getAmenities() {
        if (amenitiesRaw == null || amenitiesRaw.isBlank()) return Collections.emptyList();
        return List.of(amenitiesRaw.split(",")).stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    try { return Amenity.valueOf(s); } catch (Exception e) { return null; }
                })
                .filter(a -> a != null)
                .toList();
    }

    public String getFirstImage() {
        List<String> imgs = getImages();
        return imgs.isEmpty()
                ? "https://images.unsplash.com/photo-1611892440504-42a792e24d32?w=800&q=80"
                : imgs.get(0);
    }
}
