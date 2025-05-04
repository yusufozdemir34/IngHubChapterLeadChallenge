package com.github.yozdemir.dto.mapper;

import com.github.yozdemir.dto.request.TransactionRequest;
import com.github.yozdemir.domain.entity.Transaction;
import com.github.yozdemir.service.TypeService;
import com.github.yozdemir.service.WalletService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

/**
 * Mapper used for mapping TransactionRequest fields.
 */
@Mapper(componentModel = "spring",
        uses = {WalletService.class, TypeService.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public abstract class TransactionRequestMapper {

    private WalletService walletService;
    private TypeService typeService;

    @Autowired
    public void setWalletService(@Lazy WalletService walletService) {
        this.walletService = walletService;
    }

    @Autowired
    public void setTypeService(TypeService typeService) {
        this.typeService = typeService;
    }

    // set default value of the status field as Status.APPROVED
    @Mapping(source = "status",target = "status")
    @Mapping(target = "referenceNumber", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(source = "createdAt", target = "createdAt", defaultExpression = "java(java.time.Instant.now())")
    @Mapping(target = "fromWallet", ignore = true)
    @Mapping(target = "toWallet", ignore = true)
    @Mapping(target = "type", ignore = true)
    public abstract Transaction toTransaction(TransactionRequest dto);

    public abstract TransactionRequest toTransactionRequest(Transaction entity);

    @AfterMapping
    void setToEntityFields(@MappingTarget Transaction entity, TransactionRequest dto) {
        entity.setFromWallet(walletService.getByIban(dto.getFromWalletIban()));
        entity.setToWallet(walletService.getByIban(dto.getToWalletIban()));
        entity.setType(typeService.getReferenceById(dto.getTypeId()));
    }
}
