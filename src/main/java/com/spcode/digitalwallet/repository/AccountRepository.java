package com.spcode.digitalwallet.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spcode.digitalwallet.domain.Account;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    
}
