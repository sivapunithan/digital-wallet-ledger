package com.spcode.digitalwallet.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spcode.digitalwallet.domain.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>{
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
}
