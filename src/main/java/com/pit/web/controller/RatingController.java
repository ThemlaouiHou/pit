package com.pit.web.controller;

import com.pit.domain.Place;
import com.pit.domain.PlaceStatus;
import com.pit.service.AuthService;
import com.pit.service.PlaceService;
import com.pit.service.RatingService;
import com.pit.web.dto.RatingDto;
import com.pit.web.dto.RatingRequest;
import com.pit.web.mapper.RatingMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// REST endpoints for ratings.
@Tag(name = "rating-controller")
@RestController
@RequestMapping("/api/places/{placeId}/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;
    private final PlaceService placeService;
    private final AuthService authService;
    private final RatingMapper ratingMapper;

    @GetMapping
    // Handles list request operation
    public ResponseEntity<Page<RatingDto>> list(@PathVariable Long placeId,
                                                @ParameterObject Pageable pageable) {
        var permission = ensurePlaceVisible(placeId);
        if (permission.getStatusCode().isError()) {
            return ResponseEntity.status(permission.getStatusCode()).build();
        }
        Page<RatingDto> page = ratingService.findByPlace(placeId, pageable)
                .map(ratingMapper::toDto);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/me")
    // Handles my rating request operation
    public ResponseEntity<RatingDto> myRating(@PathVariable Long placeId) {
        Long userId = authService.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var permission = ensurePlaceVisible(placeId);
        if (permission.getStatusCode().isError()) {
            return ResponseEntity.status(permission.getStatusCode()).build();
        }
        return ratingService.findByUserAndPlace(placeId, userId)
                .map(rating -> ResponseEntity.ok(ratingMapper.toDto(rating)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping
    // Handles rate request operation
    public ResponseEntity<RatingDto> rate(@PathVariable Long placeId,
                                          @Valid @RequestBody RatingRequest request) {
        Long userId = authService.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            RatingDto dto = ratingMapper.toDto(
                    ratingService.rate(placeId, userId, request.score(), request.comment())
            );
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException ex) {
            String message = ex.getMessage() != null ? ex.getMessage() : "";
            if (message.startsWith("Place not found") || message.startsWith("User not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.badRequest().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    private ResponseEntity<Void> ensurePlaceVisible(Long placeId) {
        Place place;
        try {
            place = placeService.findById(placeId);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        if (place.getStatus() == PlaceStatus.APPROVED) {
            return ResponseEntity.ok().build();
        }
        Long currentUserId = authService.getCurrentUserId();
        boolean isAdmin = authService.isCurrentUserAdmin();
        boolean isOwner = currentUserId != null && place.getCreatedBy() != null
                && place.getCreatedBy().getId().equals(currentUserId);
        if (isAdmin || isOwner) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
