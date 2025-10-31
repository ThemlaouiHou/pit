package com.pit.service.impl;

import com.pit.domain.Place;
import com.pit.domain.PlaceStatus;
import com.pit.domain.Rating;
import com.pit.repository.PlaceRepository;
import com.pit.repository.RatingRepository;
import com.pit.repository.UserRepository;
import com.pit.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepo;
    private final PlaceRepository placeRepo;
    private final UserRepository userRepo;

    @Override
    public Rating rate(Long placeId, Long userId, int score, String comment) {
        if (score < 1 || score > 5) {
            throw new IllegalArgumentException("score must be between 1 and 5");
        }
        Place place = placeRepo.findById(placeId)
                .orElseThrow(() -> new IllegalArgumentException("Place not found: " + placeId));
        if (place.getStatus() != PlaceStatus.APPROVED) {
            throw new IllegalStateException("ratings are allowed only on approved places");
        }
        var user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Rating rating = ratingRepo.findByUserIdAndPlaceId(userId, placeId)
                .orElseGet(() -> {
                    Rating created = new Rating();
                    created.setPlace(place);
                    created.setUser(user);
                    return created;
                });

        rating.setScore(score);
        rating.setComment(comment);

        Rating saved = ratingRepo.save(rating);
        refreshPlaceMetrics(place);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Rating> findByPlace(Long placeId, Pageable pageable) {
        return ratingRepo.findByPlaceId(placeId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Rating> findByUserAndPlace(Long placeId, Long userId) {
        return ratingRepo.findByUserIdAndPlaceId(userId, placeId);
    }

    private void refreshPlaceMetrics(Place place) {
        long count = ratingRepo.countByPlaceId(place.getId());
        Double avg = ratingRepo.findAverageScoreByPlaceId(place.getId());
        place.setRatingsCount(Math.toIntExact(count));
        place.setAvgRating(avg != null ? avg : 0.0);
        placeRepo.save(place);
    }
}
