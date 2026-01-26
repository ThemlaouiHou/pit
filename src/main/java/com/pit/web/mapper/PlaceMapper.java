package com.pit.web.mapper;
import com.pit.domain.Place;
import com.pit.web.dto.PlaceDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


// Application component.
@Mapper(componentModel = "spring")
public interface PlaceMapper {
    @Mapping(target="status", expression = "java(place.getStatus().name())")
    PlaceDto toDto(Place place);
}
