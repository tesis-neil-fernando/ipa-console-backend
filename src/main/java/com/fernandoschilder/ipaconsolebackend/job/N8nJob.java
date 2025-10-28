package com.fernandoschilder.ipaconsolebackend.job;

import com.fernandoschilder.ipaconsolebackend.service.WorkflowSyncService;
import com.fernandoschilder.ipaconsolebackend.service.WorkflowSyncService.SyncSummary;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class N8nJob {

    private static final Logger log = LoggerFactory.getLogger(N8nJob.class);

    private final WorkflowSyncService workflowSyncService;

    @Scheduled(cron = "${fernandoschilder.app.cron.retrieve-workflows}", zone = "America/Lima")
    public void retrieveWorkflows() {
        long t0 = System.currentTimeMillis();
        try {
            SyncSummary summary = workflowSyncService.pullAndSave();
            long ms = System.currentTimeMillis() - t0;

            log.info("n8n sync OK: total={}, created={}, updated={}, time={}ms",
                    summary.getTotal(), summary.getCreated(), summary.getUpdated(), ms);

        } catch (Exception ex) {
            long ms = System.currentTimeMillis() - t0;
            log.error("n8n sync FAILED in {}ms: {}", ms, ex.getMessage(), ex);
        }
    }
}
