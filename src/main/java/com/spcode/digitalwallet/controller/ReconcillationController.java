package com.spcode.digitalwallet.controller;

import com.spcode.digitalwallet.dto.ReconcillationReport;
import com.spcode.digitalwallet.service.ReconcillationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reconciliation")
@RequiredArgsConstructor
public class ReconcillationController {
    private final ReconcillationService reconcillationService;

    @GetMapping("/check")
    public ReconcillationReport check(){
        return reconcillationService.runCheck();
    }
}
