// RatingMapper.java
package com.pit.web.mapper;
import com.pit.domain.Rating;
import com.pit.web.dto.RatingDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface RatingMapper {
    @Mapping(target="placeId", source="place.id")
    @Mapping(target="userId", source="user.id")
    RatingDto toDto(Rating rating);
}
