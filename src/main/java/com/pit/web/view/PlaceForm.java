package com.pit.web.view;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Application component.
public class PlaceForm {

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 1000)
    private String description;

    @NotNull
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double lat;

    @NotNull
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double lng;

    // Handles get name request operation
    public String getName() {
        return name;
    }

    // Handles set name request operation
    public void setName(String name) {
        this.name = name;
    }

    // Handles get description request operation
    public String getDescription() {
        return description;
    }

    // Handles set description request operation
    public void setDescription(String description) {
        this.description = description;
    }

    // Handles get lat request operation
    public Double getLat() {
        return lat;
    }

    // Handles set lat request operation
    public void setLat(Double lat) {
        this.lat = lat;
    }

    // Handles get lng request operation
    public Double getLng() {
        return lng;
    }

    // Handles set lng request operation
    public void setLng(Double lng) {
        this.lng = lng;
    }
}
