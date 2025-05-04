package com.github.yozdemir.dto.mapper;

import com.github.yozdemir.dto.response.TransactionResponse;
import com.github.yozdemir.domain.entity.Transaction;
import com.github.yozdemir.common.Constants;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Mapper used for mapping TransactionResponse fields.
 */
@Mapper(componentModel = "spring")
public interface TransactionResponseMapper {

    Transaction toTransaction(TransactionResponse dto);

    @Mapping(target = "createdAt", ignore = true)
    TransactionResponse toTransactionResponse(Transaction entity);

    @AfterMapping
    default void formatCreatedAt(@MappingTarget TransactionResponse dto, Transaction entity) {
        LocalDateTime datetime = LocalDateTime.ofInstant(entity.getCreatedAt(), ZoneOffset.UTC);
        dto.setCreatedAt(DateTimeFormatter.ofPattern(Constants.DATE_TIME_FORMAT).format(datetime));
    }
}
