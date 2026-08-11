package com.spcode.digitalwallet.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spcode.digitalwallet.domain.LedgerEntry;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID>{
    
    @Query(value = """
            SELECT COALESCE(SUM(
            CASE
                WHEN type = 'CREDIT' THEN amount
                WHEN type = 'DEBIT' THEN -amount
            END
            ), 0)
            FROM ledger_entries
            WHERE account_id = :accountId
            """, nativeQuery = true)
    BigDecimal getBalanceForAccount(@Param("accountId") UUID accountId);

    List<LedgerEntry> findByTransaction_Id(UUID transactionId);

    @Query(value = """
    SELECT t.id
    FROM transactions t
    JOIN ledger_entries le ON le.transaction_id = t.id
    GROUP BY t.id
    HAVING SUM(CASE WHEN le.type = 'CREDIT' THEN le.amount ELSE -le.amount END) != 0
    """, nativeQuery = true)
    List<UUID> findUnbalancedTransactionIds();

    @Query(value = """
    SELECT COALESCE(SUM(
        CASE WHEN type = 'CREDIT' THEN amount ELSE -amount END
    ), 0)
    FROM ledger_entries
    """, nativeQuery = true)
    BigDecimal getGlobalNetBalance();
}
