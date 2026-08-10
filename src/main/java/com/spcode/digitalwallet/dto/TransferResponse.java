package com.spcode.digitalwallet.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
    UUID transactionId,
    UUID fromAccountId,
    UUID toAccountId,
    BigDecimal amount,
    String status,
    Instant createdAt
) {
    
}
