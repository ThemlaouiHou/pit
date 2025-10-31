package com.pit.service.impl;

import com.pit.domain.Place;
import com.pit.domain.PlaceStatus;
import com.pit.domain.User;
import com.pit.repository.PlaceRepository;
import com.pit.repository.UserRepository;
import com.pit.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceServiceImpl implements PlaceService {

    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;

    @Override
    public Place findById(Long id) {
        return placeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Place not found: " + id));
    }

    @Override
    public Page<Place> findAll(Pageable pageable) {
        return placeRepository.findAll(pageable);
    }

    @Override
    public Page<Place> findByStatus(PlaceStatus status, Pageable pageable) {
        if (status == null) {
            return findAll(pageable);
        }
        return placeRepository.findByStatus(status, pageable);
    }

    @Override
    public Page<Place> findApproved(Pageable pageable) {
        return placeRepository.findByStatus(PlaceStatus.APPROVED, pageable);
    }

    @Override
    @Transactional
    public Place create(String name, String description, double lat, double lng, Long userId) {
        // createdBy est un User → on charge l’entité
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Place p = new Place();
        p.setName(name);
        p.setDescription(description);
        p.setLat(lat);
        p.setLng(lng);
        p.setCreatedBy(author);
        p.setStatus(PlaceStatus.PENDING);

        return placeRepository.save(p);
    }

    @Override
    @Transactional
    public Place approve(Long id) {
        Place p = findById(id);
        if (p.getStatus() == PlaceStatus.APPROVED) {
            return p;
        }
        p.setStatus(PlaceStatus.APPROVED);
        return placeRepository.save(p);
    }

    @Override
    @Transactional
    public Place reject(Long id) {
        Place p = findById(id);
        if (p.getStatus() == PlaceStatus.REJECTED) {
            return p;
        }
        p.setStatus(PlaceStatus.REJECTED);
        return placeRepository.save(p);
    }
}
