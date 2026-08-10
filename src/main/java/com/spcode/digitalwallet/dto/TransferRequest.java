package com.spcode.digitalwallet.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransferRequest(

    @NotNull(message = "fromAccountId is required")
    UUID fromAccountId,

    @NotNull(message = "toAccountId is required")
    UUID toAccountId,

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be positive")
    BigDecimal amount,

    @NotBlank(message = "idempotencyKey is required")
    String idempotencyKey
) {
    
}
