package com.fernandoschilder.ipaconsolebackend.job;

import com.fernandoschilder.ipaconsolebackend.service.ExecutionSyncService;
import com.fernandoschilder.ipaconsolebackend.service.ExecutionSyncService.SyncSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExecutionsIngestJob {

    private static final Logger log = LoggerFactory.getLogger(ExecutionsIngestJob.class);

    private final ExecutionSyncService executionSyncService;

    public ExecutionsIngestJob(ExecutionSyncService executionSyncService) {
        this.executionSyncService = executionSyncService;
    }

    @Scheduled(cron = "${fernandoschilder.app.cron.retrieve-logs}", zone = "America/Lima")
    public void run() {
        long t0 = System.currentTimeMillis();
        try {
            SyncSummary summary = executionSyncService.pullAndSave();
            long ms = System.currentTimeMillis() - t0;
            log.info("n8n executions sync OK: scanned={}, created={}, stoppedOnExisting={}, time={}ms",
                    summary.total(), summary.created(), summary.stoppedOnExisting(), ms);
        } catch (Exception ex) {
            long ms = System.currentTimeMillis() - t0;
            log.error("n8n executions sync FAILED in {}ms: {}", ms, ex.getMessage(), ex);
        }
    }
}
