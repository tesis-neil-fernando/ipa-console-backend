package com.fernandoschilder.ipaconsolebackend.controller;

import com.fernandoschilder.ipaconsolebackend.service.N8nApiService;
import com.fernandoschilder.ipaconsolebackend.service.N8nApiService.ApiResponse;
import com.fernandoschilder.ipaconsolebackend.service.N8nWebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/n8n")
public class N8nController {

    @Autowired
    private N8nApiService n8nApiService;

    @Autowired
    private N8nWebhookService n8nWebhookService;

    @GetMapping("/executions")
    public ResponseEntity<ApiResponse<String>> list(
            @RequestParam(required = false) Boolean includeData,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String workflowId,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false, defaultValue = "100") Integer limit,
            @RequestParam(required = false) String cursor) {

        return n8nApiService.getExecutions(includeData, status, workflowId, projectId, limit, cursor);
    }

    @GetMapping("/workflows")
    public ResponseEntity<ApiResponse<String>> getWorkflows() {
        return n8nApiService.getWorkflowsRaw();
    }

    @PostMapping("/workflows/{id}/activate")
    public ResponseEntity<ApiResponse<String>> activateWorkflow(@PathVariable("id") String workflowId) {
        return n8nApiService.activateWorkflow(workflowId);
    }

    @PostMapping("/workflows/{id}/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivateWorkflow(@PathVariable("id") String workflowId) {
        return n8nApiService.deactivateWorkflow(workflowId);
    }

    @PostMapping("/webhook/{path}")
    public ResponseEntity<N8nApiService.ApiResponse<String>> forwardWebhook(
            @PathVariable String path,
            @RequestBody(required = false) Map<String, Object> body
    ) {
        return n8nWebhookService.postWebhook(path, body);
    }
}
