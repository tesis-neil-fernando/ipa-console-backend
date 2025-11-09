package com.fernandoschilder.ipaconsolebackend.controller;

import com.fernandoschilder.ipaconsolebackend.service.WorkflowSchedulerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/scheduler")
public class AdminSchedulerController {

    private final WorkflowSchedulerService workflowSchedulerService;

    public AdminSchedulerController(WorkflowSchedulerService workflowSchedulerService) {
        this.workflowSchedulerService = workflowSchedulerService;
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh() {
        workflowSchedulerService.refreshSchedules();
        return ResponseEntity.ok().body("scheduler refreshed");
    }
}
