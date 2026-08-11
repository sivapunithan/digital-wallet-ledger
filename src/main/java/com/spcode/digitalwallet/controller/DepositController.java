package com.spcode.digitalwallet.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spcode.digitalwallet.dto.DepositRequest;
import com.spcode.digitalwallet.dto.TransferResponse;
import com.spcode.digitalwallet.service.TransferService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/deposits")
@RestController
@RequiredArgsConstructor
public class DepositController {
    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<TransferResponse> deposit(@Valid @RequestBody DepositRequest request){
        return ResponseEntity.ok(transferService.deposit(request));
    }
}
