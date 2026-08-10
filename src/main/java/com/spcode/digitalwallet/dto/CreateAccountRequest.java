package com.spcode.digitalwallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateAccountRequest (
    
    @NotBlank(message = "owner ref is required")
    String ownerRef,

    @NotBlank(message = "currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be a 3-letter ISO code, e.g. INR")
    String currency,

    @NotBlank(message = "type is required")
    String type
    ){}
