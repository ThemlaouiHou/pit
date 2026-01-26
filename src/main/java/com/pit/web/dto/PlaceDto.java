package com.pit.web.dto;

import java.time.Instant;

// Application component.
public record PlaceDto(Long id, String name, String description, double lat, double lng,
                       String status, double avgRating, int ratingsCount, Instant createdAt) {}
