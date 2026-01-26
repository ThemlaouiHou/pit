package com.pit.repository;
import com.pit.domain.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
// Application component.
public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByUserIdAndPlaceId(Long userId, Long placeId);
    Page<Rating> findByPlaceId(Long placeId, Pageable pageable);

    long countByPlaceId(Long placeId);

    @Query("select avg(r.score) from Rating r where r.place.id = :placeId")
    Double findAverageScoreByPlaceId(@Param("placeId") Long placeId);
}
