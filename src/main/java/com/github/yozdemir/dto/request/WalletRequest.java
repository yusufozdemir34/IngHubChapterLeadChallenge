package com.github.yozdemir.dto.request;

import com.github.yozdemir.validator.ValidIban;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for Wallet request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WalletRequest {

    private Long id;

    @ValidIban(message = "{validation.iban.format}")
    @NotBlank(message = "{validation.iban.required}")
    private String iban;

    @Size(min = 3, max = 50, message = "{validation.field.name.length}")
    @NotBlank(message = "{validation.field.name.required}")
    private String name;
    @NotBlank(message = "{validation.field.currency.required}")
    @Size(min = 3, max = 3, message = "{validation.field.currency.length}") // ISO 4217
    private String currency;

    @NotNull(message = "{validation.field.activeForShopping.required}")
    private Boolean activeForShopping;

    @NotNull(message = "{validation.field.activeForWithdraw.required}")
    private Boolean activeForWithdraw;

    @NotNull(message = "{validation.field.balance.required}")
    @DecimalMin(value = "0.0", inclusive = true, message = "{validation.field.balance.positive}")
    private BigDecimal balance;

    @NotNull(message = "{validation.field.usableBalance.required}")
    @DecimalMin(value = "0.0", inclusive = true, message = "{validation.field.usableBalance.positive}")
    private BigDecimal usableBalance;


    @NotNull(message = "{validation.field.user.required}")
    private Long userId;
}
