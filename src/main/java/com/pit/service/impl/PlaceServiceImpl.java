package com.pit.service.impl;

import com.pit.domain.Place;
import com.pit.domain.PlaceStatus;
import com.pit.domain.User;
import com.pit.repository.PlaceRepository;
import com.pit.repository.UserRepository;
import com.pit.service.PlaceService;
import com.pit.web.dto.PlaceNotificationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceServiceImpl implements PlaceService {

    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

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
        // Load the author entity so the relationship is managed.
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Place p = new Place();
        p.setName(name);
        p.setDescription(description);
        p.setLat(lat);
        p.setLng(lng);
        p.setCreatedBy(author);
        p.setStatus(PlaceStatus.PENDING);

        Place saved = placeRepository.save(p);
        notifyAdminsOfNewPlace(saved);
        return saved;
    }

    @Override
    @Transactional
    public Place approve(Long id) {
        Place p = findById(id);
        if (p.getStatus() == PlaceStatus.APPROVED) {
            return p;
        }
        p.setStatus(PlaceStatus.APPROVED);
        Place saved = placeRepository.save(p);
        notifyCreator(saved, "APPROVED", "Votre lieu est en ligne.");
        return saved;
    }

    @Override
    @Transactional
    public Place reject(Long id) {
        Place p = findById(id);
        if (p.getStatus() == PlaceStatus.REJECTED) {
            return p;
        }
        p.setStatus(PlaceStatus.REJECTED);
        Place saved = placeRepository.save(p);
        notifyCreator(saved, "REJECTED", "Votre lieu a été refusé.");
        return saved;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Place p = findById(id);
        placeRepository.delete(p);
    }

    private void notifyCreator(Place place, String status, String message) {
        if (place.getCreatedBy() == null || place.getCreatedBy().getEmail() == null) {
            return;
        }
        PlaceNotificationDto dto = new PlaceNotificationDto(place.getId(), status, message);
        messagingTemplate.convertAndSendToUser(place.getCreatedBy().getEmail(), "/queue/places", dto);
    }

    private void notifyAdminsOfNewPlace(Place place) {
        String author = place.getCreatedBy() != null && place.getCreatedBy().getEmail() != null
                ? place.getCreatedBy().getEmail()
                : "un utilisateur";
        PlaceNotificationDto dto = new PlaceNotificationDto(place.getId(), "PENDING",
                "Nouvelle proposition par " + author + ".");
        messagingTemplate.convertAndSend("/topic/admin/places", dto);
    }
}
