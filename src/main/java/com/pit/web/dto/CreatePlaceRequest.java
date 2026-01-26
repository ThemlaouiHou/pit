package com.pit.web.dto;
import jakarta.validation.constraints.*;
// Application component.
public record CreatePlaceRequest(@NotBlank String name,
                                 String description,
                                 @DecimalMin("-90.0") @DecimalMax("90.0") double lat,
                                 @DecimalMin("-180.0") @DecimalMax("180.0") double lng) {}
