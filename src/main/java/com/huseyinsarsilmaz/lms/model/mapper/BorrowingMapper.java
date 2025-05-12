package com.huseyinsarsilmaz.lms.model.mapper;

import com.huseyinsarsilmaz.lms.model.dto.response.BorrowingDetailed;
import com.huseyinsarsilmaz.lms.model.dto.response.BorrowingSimple;
import com.huseyinsarsilmaz.lms.model.entity.Borrowing;

import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = { UserMapper.class, BookMapper.class })
public interface BorrowingMapper {

    @Mapping(target = "borrower", qualifiedByName = "toDtoSimple")
    @Mapping(target = "book", qualifiedByName = "toDtoSimple")
    BorrowingSimple toDtoSimple(Borrowing borrowing);

    @Mapping(target = "borrower", qualifiedByName = "toDtoSimple")
    @Mapping(target = "book", qualifiedByName = "toDtoSimple")
    BorrowingDetailed toDtoDetailed(Borrowing borrowing);
}
