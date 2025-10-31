package com.pit.web.controller;

import com.pit.domain.Place;
import com.pit.domain.PlaceStatus;
import com.pit.service.AuthService;
import com.pit.service.PlaceService;
import com.pit.web.dto.CreatePlaceRequest;
import com.pit.web.dto.PlaceDto;
import com.pit.web.mapper.PlaceMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Tag(name = "place-controller")
@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;
    private final PlaceMapper placeMapper;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<Page<PlaceDto>> list(@ParameterObject Pageable pageable,
                                               @RequestParam(defaultValue = "APPROVED") String status) {
        boolean isAdmin = authService.isCurrentUserAdmin();
        Page<PlaceDto> page;
        try {
            if ("ALL".equalsIgnoreCase(status)) {
                if (!isAdmin) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
                page = placeService.findAll(pageable).map(placeMapper::toDto);
            } else {
                PlaceStatus filter = PlaceStatus.valueOf(status.toUpperCase());
                if (!isAdmin && filter != PlaceStatus.APPROVED) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
                page = placeService.findByStatus(filter, pageable).map(placeMapper::toDto);
            }
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaceDto> get(@PathVariable Long id) {
        Place place;
        try {
            place = placeService.findById(id);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        if (place.getStatus() != PlaceStatus.APPROVED) {
            Long currentUserId = authService.getCurrentUserId();
            boolean isAdmin = authService.isCurrentUserAdmin();
            boolean isOwner = currentUserId != null && place.getCreatedBy() != null
                    && place.getCreatedBy().getId().equals(currentUserId);
            if (!isAdmin && !isOwner) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }
        return ResponseEntity.ok(placeMapper.toDto(place));
    }

    @PostMapping
    public ResponseEntity<PlaceDto> create(@Valid @RequestBody CreatePlaceRequest req) {
        Long userId = authService.getCurrentUserId();
        if (userId == null) return ResponseEntity.status(401).build();
        var saved = placeService.create(req.name(), req.description(), req.lat(), req.lng(), userId);
        return ResponseEntity.created(URI.create("/api/places/" + saved.getId())).body(placeMapper.toDto(saved));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}:approve")
    public ResponseEntity<Void> approve(@PathVariable Long id) {
        try {
            placeService.approve(id);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}:reject")
    public ResponseEntity<Void> reject(@PathVariable Long id) {
        try {
            placeService.reject(id);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.noContent().build();
    }
}
