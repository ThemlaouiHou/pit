package com.pit.service;

import com.pit.domain.Place;
import com.pit.domain.PlaceStatus;
import com.pit.domain.Rating;
import com.pit.domain.User;
import com.pit.repository.PlaceRepository;
import com.pit.repository.RatingRepository;
import com.pit.repository.UserRepository;
import com.pit.service.impl.RatingServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceImplTest {

    @Mock RatingRepository ratingRepository;
    @Mock PlaceRepository placeRepository;
    @Mock UserRepository userRepository;
    @InjectMocks RatingServiceImpl service;

    @Test
    void rateRejectsOutOfRangeScore() {
        assertThatThrownBy(() -> service.rate(1L, 2L, 6, "bad"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.rate(1L, 2L, 0, "bad"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rateRejectsPendingPlace() {
        Place pending = new Place();
        pending.setId(1L);
        pending.setStatus(PlaceStatus.PENDING);

        when(placeRepository.findById(1L)).thenReturn(Optional.of(pending));

        assertThatThrownBy(() -> service.rate(1L, 2L, 3, "note"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rateCreatesOrUpdatesRatingAndRefreshesMetrics() {
        Place approved = new Place();
        approved.setId(1L);
        approved.setStatus(PlaceStatus.APPROVED);

        User user = new User();
        user.setId(2L);

        when(placeRepository.findById(1L)).thenReturn(Optional.of(approved));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(ratingRepository.findByUserIdAndPlaceId(2L, 1L)).thenReturn(Optional.empty());
        when(ratingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(ratingRepository.countByPlaceId(1L)).thenReturn(1L);
        when(ratingRepository.findAverageScoreByPlaceId(1L)).thenReturn(4.0);
        when(placeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Rating saved = service.rate(1L, 2L, 4, "ok");

        assertThat(saved.getScore()).isEqualTo(4);
        assertThat(saved.getComment()).isEqualTo("ok");
        verify(ratingRepository).save(any(Rating.class));
        verify(placeRepository).save(approved);
    }
}
