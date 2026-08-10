package com.spcode.digitalwallet.dto;

import java.time.Instant;
import java.util.UUID;

import com.spcode.digitalwallet.domain.Account;

public record AccountResponse(
    UUID id,
    String ownerRef,
    String currency,
    String type,
    Instant createdAt
) {
    public static AccountResponse from(Account account){
        return new AccountResponse(
            account.getId(),
            account.getOwnerRef(),
            account.getCurrency(),
            account.getType().name(),
            account.getCreatedAt());
    }
}
