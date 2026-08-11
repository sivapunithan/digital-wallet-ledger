package com.spcode.digitalwallet.service;

import com.spcode.digitalwallet.dto.ReconcillationReport;
import com.spcode.digitalwallet.repository.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class ReconcillationService {

    private static final Logger log = LoggerFactory.getLogger(ReconcillationService.class);
    private final LedgerEntryRepository ledgerEntryRepository;

    public ReconcillationReport runCheck(){
        List<UUID> unbalancedTransactions = ledgerEntryRepository.findUnbalancedTransactionIds();
        BigDecimal globalNetBalance = ledgerEntryRepository.getGlobalNetBalance();

        boolean isHealthy = unbalancedTransactions.isEmpty() && globalNetBalance.compareTo(BigDecimal.ZERO) == 0;

        if (!isHealthy) {
            log.error("LEDGER INTEGRITY VIOLATION — unbalancedTransactions={}, globalNetBalance={}",
                    unbalancedTransactions, globalNetBalance);
        } else {
            log.info("Reconciliation check passed. globalNetBalance={}", globalNetBalance);
        }

        return new ReconcillationReport(
                isHealthy, unbalancedTransactions, globalNetBalance
        );
    }
}
