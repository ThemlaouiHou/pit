package com.pit.service;
import com.pit.domain.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
// Application component.
public interface RatingService {
    Rating rate(Long placeId, Long userId, int score, String comment);
    Page<Rating> findByPlace(Long placeId, Pageable pageable);
    Optional<Rating> findByUserAndPlace(Long placeId, Long userId);
}
