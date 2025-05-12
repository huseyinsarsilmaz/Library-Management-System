package com.huseyinsarsilmaz.lms.model.mapper;

import com.huseyinsarsilmaz.lms.model.dto.request.BookCreateRequest;
import com.huseyinsarsilmaz.lms.model.dto.request.BookUpdateRequest;
import com.huseyinsarsilmaz.lms.model.dto.response.BookSimple;
import com.huseyinsarsilmaz.lms.model.entity.Book;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BookMapper {

    @Mapping(target = "isAvailable", ignore = true)
    Book toEntity(BookCreateRequest req);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "updatedAt", ignore = true),
            @Mapping(target = "isAvailable", ignore = true)
    })

    void updateEntity(@MappingTarget Book book, BookUpdateRequest req);

    @Named("toDtoSimple")
    BookSimple toDtoSimple(Book book);
}
