package com.pit.service;

import com.pit.domain.Place;
import com.pit.domain.PlaceStatus;
import com.pit.domain.Role;
import com.pit.domain.User;
import com.pit.repository.PlaceRepository;
import com.pit.repository.UserRepository;
import com.pit.service.impl.PlaceServiceImpl;
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
class PlaceServiceImplTest {

    @Mock PlaceRepository placeRepository;
    @Mock UserRepository userRepository;
    @InjectMocks PlaceServiceImpl service;

    @Test
    void createSetsPendingStatusAndAuthor() {
        User author = new User();
        author.setId(7L);
        author.setRole(Role.USER);

        when(userRepository.findById(7L)).thenReturn(Optional.of(author));
        when(placeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Place created = service.create("Test", "Desc", 1.0, 2.0, 7L);

        assertThat(created.getStatus()).isEqualTo(PlaceStatus.PENDING);
        assertThat(created.getCreatedBy()).isEqualTo(author);
        assertThat(created.getName()).isEqualTo("Test");
    }

    @Test
    void createFailsWhenUserMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create("Name", "Desc", 1.0, 2.0, 99L))
                .isInstanceOf(IllegalArgumentException.class);
        verify(placeRepository, never()).save(any());
    }

    @Test
    void approveUpdatesStatusWhenPending() {
        Place place = new Place();
        place.setId(1L);
        place.setStatus(PlaceStatus.PENDING);

        when(placeRepository.findById(1L)).thenReturn(Optional.of(place));
        when(placeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Place updated = service.approve(1L);

        assertThat(updated.getStatus()).isEqualTo(PlaceStatus.APPROVED);
        verify(placeRepository).save(place);
    }

    @Test
    void approveNoopWhenAlreadyApproved() {
        Place place = new Place();
        place.setId(1L);
        place.setStatus(PlaceStatus.APPROVED);

        when(placeRepository.findById(1L)).thenReturn(Optional.of(place));

        Place updated = service.approve(1L);

        assertThat(updated.getStatus()).isEqualTo(PlaceStatus.APPROVED);
        verify(placeRepository, never()).save(any());
    }

    @Test
    void deleteRemovesExistingPlace() {
        Place place = new Place();
        place.setId(5L);
        when(placeRepository.findById(5L)).thenReturn(Optional.of(place));

        service.delete(5L);

        verify(placeRepository).delete(place);
    }
}
