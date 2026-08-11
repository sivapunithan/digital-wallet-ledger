package com.spcode.digitalwallet.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spcode.digitalwallet.domain.Account;
import com.spcode.digitalwallet.domain.AccountType;
import com.spcode.digitalwallet.domain.EntryType;
import com.spcode.digitalwallet.domain.LedgerEntry;
import com.spcode.digitalwallet.domain.Transaction;
import com.spcode.digitalwallet.domain.TransactionStatus;
import com.spcode.digitalwallet.dto.DepositRequest;
import com.spcode.digitalwallet.dto.TransferRequest;
import com.spcode.digitalwallet.dto.TransferResponse;
import com.spcode.digitalwallet.repository.AccountRepository;
import com.spcode.digitalwallet.repository.LedgerEntryRepository;
import com.spcode.digitalwallet.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransferService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    private static final UUID SYSTEM_FUNDING_ACCOUNT_ID =
            UUID.fromString("99999999-9999-9999-9999-999999999999");

    @Transactional
    public TransferResponse transfer(TransferRequest request){
        var existing = transactionRepository.findByIdempotencyKey(request.idempotencyKey());
        if (existing.isPresent()) {
            return buildResponse(existing.get());
        }

        // Locking accounts in the consistent orders (by UUID comparision) to prevent deadlocks
        UUID firstId = request.fromAccountId().compareTo(request.toAccountId()) < 0
                       ? request.fromAccountId() : request.toAccountId();
        UUID secondId = request.fromAccountId().compareTo(request.toAccountId()) < 0
                       ? request.toAccountId() : request.fromAccountId();

        Account first = accountRepository.findByIdForUpdate(firstId)
                       .orElseThrow(()-> new AccountNotFoundException(firstId));
        Account second = accountRepository.findByIdForUpdate(secondId)
                       .orElseThrow(() -> new AccountNotFoundException(secondId));

        // Validate both account exists
        Account fromAccount = first.getId().equals(request.fromAccountId()) ? first : second;
        Account toAccount =second.getId().equals(request.toAccountId()) ? second : first;

        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new InvalidTransferException("Cannot transfer to the same account");
        }

        if (fromAccount.getType() != AccountType.SYSTEM) {
          BigDecimal fromBalance = ledgerEntryRepository.getBalanceForAccount(fromAccount.getId());
        if (fromBalance.compareTo(request.amount()) < 0) {
        throw new InsufficientBalanceException(fromAccount.getId(), fromBalance, request.amount());
        }  
        }

//        try{
//            Thread.sleep(1000);
//        }catch (InterruptedException e){
//            Thread.currentThread().interrupt();
//        }

        // Build transactions + two balanced ledger rows
        Transaction transaction = new Transaction();
        transaction.setIdempotencyKey(request.idempotencyKey());
        transaction.setStatus(TransactionStatus.POSTED);

        LedgerEntry debit = new LedgerEntry(transaction, fromAccount, EntryType.DEBIT, request.amount());
        LedgerEntry credit = new LedgerEntry(transaction, toAccount, EntryType.CREDIT, request.amount());

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

    @Transactional
    public TransferResponse reverse(UUID originalTransactionId, String idempotencyKey){
        // Idempotency Check for reversal itself
        var existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()){
            return buildResponse(existing.get());
        }

        Transaction original = transactionRepository.findById(originalTransactionId)
                .orElseThrow(() -> new TransactionNotFoundException(originalTransactionId));

        if (original.getStatus() == TransactionStatus.REVERSED){
            throw new InvalidTransferException("Transaction "+ originalTransactionId +"is already reversed");
        }

        List<LedgerEntry> originalEntries = ledgerEntryRepository.findByTransaction_Id(originalTransactionId);

        LedgerEntry originalDebit = originalEntries.stream()
                .filter(e -> e.getType() == EntryType.DEBIT)
                .findFirst()
                .orElseThrow(()-> new IllegalStateException("Original transaction has no debit entry"));

        LedgerEntry originalCredit = originalEntries.stream()
                .filter(e -> e.getType() == EntryType.CREDIT)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Original transaction has not credit entry "));

        // Lock both accounts in consistent order same as transfer
        UUID acctA = originalDebit.getAccount().getId();
        UUID acctB = originalCredit.getAccount().getId();
        UUID firstId = acctA.compareTo(acctB) < 0 ? acctA : acctB;
        UUID secondId = acctA.compareTo(acctB) < 0 ? acctB : acctA;

        accountRepository.findByIdForUpdate(firstId)
                .orElseThrow(()-> new AccountNotFoundException(firstId));
        accountRepository.findByIdForUpdate(secondId)
                .orElseThrow(()-> new AccountNotFoundException(secondId));

        // Build the reversal: entries are swapped - money flows back
        Transaction reversal = new Transaction();
        reversal.setIdempotencyKey(idempotencyKey);
        reversal.setStatus(TransactionStatus.POSTED);

        LedgerEntry reversalDebit = new LedgerEntry(
                reversal, originalCredit.getAccount(), EntryType.DEBIT, originalCredit.getAmount()
        );
        LedgerEntry reversalCredit = new LedgerEntry(
                reversal, originalDebit.getAccount(), EntryType.CREDIT, originalDebit.getAmount()
        );
        reversal.getEntries().add(reversalDebit);
        reversal.getEntries().add(reversalCredit);

        try {
            Transaction saved = transactionRepository.save(reversal);

            // Mark original as REVERSED - status only, entries stay untouched
            original.setStatus(TransactionStatus.REVERSED);
            transactionRepository.save(original);

            return buildResponse(saved);
        } catch (DataIntegrityViolationException e){
            Transaction winner = transactionRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(()-> e);
            return buildResponse(winner);
        }

    }

    private TransferResponse buildResponse(Transaction transaction){
        List<LedgerEntry> entries = ledgerEntryRepository.findByTransaction_Id(transaction.getId());

        LedgerEntry debitEntry = entries.stream()
          .filter(e -> e.getType() == EntryType.DEBIT)
          .findFirst()
          .orElseThrow();

        LedgerEntry creditEntry = entries.stream()
          .filter(e -> e.getType() == EntryType.CREDIT)
          .findFirst()
          .orElseThrow();

        return new TransferResponse(
            transaction.getId(),
            debitEntry.getAccount().getId(),
            creditEntry.getAccount().getId(),
            debitEntry.getAmount(),
            transaction.getStatus().name(),
            transaction.getCreatedAt()
        );
            }

    @Transactional
    public TransferResponse deposit(DepositRequest request){
        TransferRequest asTransfer = new TransferRequest(
            SYSTEM_FUNDING_ACCOUNT_ID, 
            request.toAccountId(), 
            request.amount(),
            request.idempotencyKey());

            return transfer(asTransfer);
    }

}
