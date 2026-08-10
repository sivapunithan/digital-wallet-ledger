package com.spcode.digitalwallet.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.spcode.digitalwallet.domain.Account;
import com.spcode.digitalwallet.domain.EntryType;
import com.spcode.digitalwallet.domain.LedgerEntry;
import com.spcode.digitalwallet.domain.Transaction;
import com.spcode.digitalwallet.domain.TransactionStatus;
import com.spcode.digitalwallet.dto.TransferRequest;
import com.spcode.digitalwallet.dto.TransferResponse;
import com.spcode.digitalwallet.repository.AccountRepository;
import com.spcode.digitalwallet.repository.LedgerEntryRepository;
import com.spcode.digitalwallet.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.var;

@Service
@RequiredArgsConstructor
public class TransferService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public TransferResponse transfer(TransferRequest request){
        var existing = transactionRepository.findByIdempotencyKey(request.idempotencyKey());
        if (existing.isPresent()) {
            return buildResponse(existing.get());
        }

        // Validate both account exists
        Account fromAccount = accountRepository.findById(request.fromAccountId())
                .orElseThrow(()-> new AccountNotFoundException(request.fromAccountId()));
        Account toAccount = accountRepository.findById(request.toAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.toAccountId()));

        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new InvalidTransferException("Cannot transfer to the same account");
        }

        BigDecimal fromBalance = ledgerEntryRepository.getBalanceForAccount(fromAccount.getId());
        if (fromBalance.compareTo(request.amount()) < 0) {
        throw new InsufficientBalanceException(fromAccount.getId(), fromBalance, request.amount());
        }

        // Build transactions + two balanced ledger rows
        Transaction transaction = new Transaction();
        transaction.setIdempotencyKey(request.idempotencyKey());
        transaction.setStatus(TransactionStatus.POSTED);

        LedgerEntry debit = new LedgerEntry();
        debit.setTransaction(transaction);
        debit.setAccount(fromAccount);
        debit.setType(EntryType.DEBIT);
        debit.setAmount(request.amount());

        LedgerEntry credit = new LedgerEntry();
        credit.setTransaction(transaction);
        credit.setAccount(toAccount);
        credit.setType(EntryType.CREDIT);
        debit.setAmount(request.amount());

        transaction.getEntries().add(credit);
        transaction.getEntries().add(debit);

        try{
            Transaction saved = transactionRepository.save(transaction);
            return buildResponse(saved);
        } catch(DataIntegrityViolationException e){
            // Race condition: two identical requests hit the unique constraint
            // Simultanously. Whichever losses re-reads the winners result
            Transaction winner = transactionRepository.findByIdempotencyKey(request.idempotencyKey())
                        .orElseThrow(() -> e);
            return buildResponse(winner);
        }
    }

    private TransferResponse buildResponse(Transaction transaction){
        List<LedgerEntry> entries = ledgerEntryRepository.findByTransaction_Id(transaction.getId());

        LedgerEntry debitEntry = entries.stream()
          .filter(e -> e.getType() == EntryType.DEBIT)
          .findFirst()
          .orElseThrow();

        LedgerEntry crediEntry = entries.stream()
          .filter(e -> e.getType() == EntryType.CREDIT)
          .findFirst()
          .orElseThrow();

        return new TransferResponse(
            transaction.getId(),
            debitEntry.getAccount().getId(),
            crediEntry.getAccount().getId(),
            debitEntry.getAmount(),
            transaction.getStatus().name(),
            transaction.getCreatedAt()
        );
            }

}
