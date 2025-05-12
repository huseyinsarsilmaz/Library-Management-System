package com.huseyinsarsilmaz.lms.model.mapper;

import com.huseyinsarsilmaz.lms.model.dto.request.RegisterRequest;
import com.huseyinsarsilmaz.lms.model.dto.request.UserUpdateRequest;
import com.huseyinsarsilmaz.lms.model.dto.response.UserDetailed;
import com.huseyinsarsilmaz.lms.model.dto.response.UserSimple;
import com.huseyinsarsilmaz.lms.model.entity.User;

import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mappings({
            @Mapping(target = "isActive", ignore = true),
            @Mapping(target = "roles", ignore = true),
    })
    User toEntity(RegisterRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "isActive", ignore = true),
            @Mapping(target = "password", ignore = true),
            @Mapping(target = "roles", ignore = true),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
    })
    void updateEntity(@MappingTarget User user, UserUpdateRequest req);

    @Named("toDtoSimple")
    UserSimple toDtoSimple(User user);

    @Named("toDtoDetailed")
    UserDetailed toDtoDetailed(User user);
}
