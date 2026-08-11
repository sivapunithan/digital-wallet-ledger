package com.spcode.digitalwallet.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ReconcillationReport(
        boolean healthy,
        List<UUID> unbalanceTransactionIds,
        BigDecimal globalNetBalance
) {
}
