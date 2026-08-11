package com.spcode.digitalwallet.dto;

import jakarta.validation.constraints.NotBlank;

public record ReversalRequest(
        @NotBlank(message = "Idempotency is required")
        String idempotencyKey
) {
}
