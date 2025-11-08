package com.fernandoschilder.ipaconsolebackend.job;

import com.fernandoschilder.ipaconsolebackend.service.WorkflowSyncService;
import com.fernandoschilder.ipaconsolebackend.service.WorkflowSyncService.SyncSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WorkflowsIngestJob {

    private static final Logger log = LoggerFactory.getLogger(WorkflowsIngestJob.class);

    private final WorkflowSyncService workflowSyncService;

    public WorkflowsIngestJob(WorkflowSyncService workflowSyncService) {
        this.workflowSyncService = workflowSyncService;
    }

    @Scheduled(cron = "${fernandoschilder.app.cron.retrieve-workflows}", zone = "America/Lima")
    public void run() {
        long t0 = System.currentTimeMillis();
        try {
            SyncSummary summary = workflowSyncService.pullAndSave();
            long ms = System.currentTimeMillis() - t0;

        log.info("n8n sync OK: total={}, created={}, updated={}, time={}ms",
            summary.total(), summary.created(), summary.updated(), ms);

        } catch (Exception ex) {
            long ms = System.currentTimeMillis() - t0;
            log.error("n8n sync FAILED in {}ms: {}", ms, ex.getMessage(), ex);
        }
    }
}
