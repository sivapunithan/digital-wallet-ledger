package com.spcode.digitalwallet.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.spcode.digitalwallet.domain.Account;
import com.spcode.digitalwallet.domain.AccountType;
import com.spcode.digitalwallet.dto.AccountResponse;
import com.spcode.digitalwallet.dto.CreateAccountRequest;
import com.spcode.digitalwallet.repository.AccountRepository;
import com.spcode.digitalwallet.repository.LedgerEntryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {
    
    private final AccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public BigDecimal getBalance(UUID accountId){
        if (!accountRepository.existsById(accountId)) {
            throw new IllegalArgumentException("Account not exists ....");
        }
        return ledgerEntryRepository.getBalanceForAccount(accountId);
    }

    public AccountResponse createAccount(CreateAccountRequest request){
        AccountType type;

        try{
            type = AccountType.valueOf(request.type().toUpperCase());
        } catch (IllegalArgumentException e){
            throw new InvalidAccountTypeException(request.type());
        }

        Account account = new Account();
        account.setOwnerRef(request.ownerRef());
        account.setCurrency(request.currency());
        account.setType(type);

        Account saved = accountRepository.save(account);
        return AccountResponse.from(saved);
    }
}
