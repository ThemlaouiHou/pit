package com.pit.repository;
import com.pit.domain.Place;
import com.pit.domain.PlaceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Collection;
public interface PlaceRepository extends JpaRepository<Place, Long> {
    Page<Place> findByStatus(PlaceStatus status, Pageable pageable);
    Page<Place> findByStatusIn(Collection<PlaceStatus> statuses, Pageable pageable);
}
