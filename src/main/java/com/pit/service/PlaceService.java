package com.pit.service;

import com.pit.domain.Place;
import com.pit.domain.PlaceStatus;
import com.pit.web.dto.CreatePlaceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PlaceService {

    Page<Place> findAll(Pageable pageable);

    Page<Place> findByStatus(PlaceStatus status, Pageable pageable);

    Page<Place> findApproved(Pageable pageable);

    Place findById(Long id);

    Place create(String name, String description, double lat, double lng, Long userId);

    // pratique si tu veux réutiliser la Request plus tard
    default Place create(CreatePlaceRequest req, Long userId) {
        return create(req.name(), req.description(), req.lat(), req.lng(), userId);
    }

    Place approve(Long id);
    Place reject(Long id);
}
