package com.spcode.digitalwallet.controller;

import com.spcode.digitalwallet.dto.ReversalRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.spcode.digitalwallet.dto.TransferRequest;
import com.spcode.digitalwallet.dto.TransferResponse;
import com.spcode.digitalwallet.service.TransferService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
public class TransferController {
    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(transferService.transfer(request));
    }

    @PostMapping("/{id}/reverse")
    public ResponseEntity<TransferResponse> reverse(
            @PathVariable("id") UUID transactionId,
            @Valid @RequestBody ReversalRequest request) {
        return ResponseEntity.ok(transferService.reverse(transactionId, request.idempotencyKey()));
    }

}
