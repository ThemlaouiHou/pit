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

    /**
     * Convenience overload that reuses the record-based request payload.
     */
    default Place create(CreatePlaceRequest req, Long userId) {
        return create(req.name(), req.description(), req.lat(), req.lng(), userId);
    }

    Place approve(Long id);
    Place reject(Long id);
    void delete(Long id);
}
