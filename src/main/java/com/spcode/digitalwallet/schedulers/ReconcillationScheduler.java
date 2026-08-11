package com.spcode.digitalwallet.schedulers;

import com.spcode.digitalwallet.service.ReconcillationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReconcillationScheduler {
    private final ReconcillationService reconcillationService;

    @Scheduled(fixedRate = 60000) // Every 60 seconds
    public void scheduledReconcillation(){
        reconcillationService.runCheck();
    }
}
