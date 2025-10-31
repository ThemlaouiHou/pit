// RatingDto.java
package com.pit.web.dto;

import java.time.Instant;

public record RatingDto(Long id, Long placeId, Long userId, int score, String comment, Instant createdAt) {}
