package com.fernandoschilder.ipaconsolebackend.job;

import com.fernandoschilder.ipaconsolebackend.service.WorkflowSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class N8nJob {
    @Autowired
    WorkflowSyncService workflowSyncService;

    @Scheduled(cron = "${fernandoschilder.app.cron.retrieve-workflows}", zone = "America/Lima")
    public void retriveWorklows() {
        int size = workflowSyncService.pullAndSave();
        System.out.println("Retrieve worklfows size : " + size);
    }

//    @Scheduled(cron = "${fernandoschilder.app.cron.retrieve-logs}", zone = "America/Lima")
//    public void retriveLogs() {
//        System.out.println("Retrieving logs: "+neightnService.getAllWorkflows().toString());
//    }
}

