package com.pit.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

// Application component.
public record RatingRequest(@Min(1) @Max(5) int score,
                            String comment) {
}
