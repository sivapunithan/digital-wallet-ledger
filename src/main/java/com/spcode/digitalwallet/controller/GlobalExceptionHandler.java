package com.spcode.digitalwallet.controller;

import javax.security.auth.login.AccountException;
import javax.security.auth.login.AccountNotFoundException;

import com.spcode.digitalwallet.service.TransactionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.spcode.digitalwallet.service.InsufficientBalanceException;
import com.spcode.digitalwallet.service.InvalidAccountTypeException;
import com.spcode.digitalwallet.service.InvalidTransferException;

public class GlobalExceptionHandler {
    
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<String> handleAccountNotFound(AccountException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidAccountTypeException.class)
    public ResponseEntity<String> handleInvalidAccountType(InvalidAccountTypeException ex) {
    return ResponseEntity.badRequest().body(ex.getMessage());
}

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationErrors(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getFieldErrors().stream()
        .map(err -> err.getField() + ": " + err.getDefaultMessage())
        .reduce((a, b) -> a + "; " + b)
        .orElse("Validation failed");
    return ResponseEntity.badRequest().body(message);
    }

   @ExceptionHandler(InvalidTransferException.class)
    public ResponseEntity<String> handleInvalidTransfer(InvalidTransferException ex) {
    return ResponseEntity.badRequest().body(ex.getMessage());
}
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<String> handleInsufficientBalance(InsufficientBalanceException ex) {
    return ResponseEntity.badRequest().body(ex.getMessage());
}
   @ExceptionHandler(TransactionNotFoundException.class)
   public ResponseEntity<String> handleTransactionNotFound(TransactionNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
   }
}
