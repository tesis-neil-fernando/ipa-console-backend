package com.fernandoschilder.ipaconsolebackend.controller;

import com.fernandoschilder.ipaconsolebackend.model.N8nWorkflowEntity;
import com.fernandoschilder.ipaconsolebackend.response.N8nWorkflowsResponse;
import com.fernandoschilder.ipaconsolebackend.service.N8nService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/n8n")
public class N8nController {
    @Autowired
    private N8nService service;

    @GetMapping("/executions")
    public ResponseEntity<String> list(
            @RequestParam(required = false) Boolean includeData,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String workflowId,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false, defaultValue = "100") Integer limit,
            @RequestParam(required = false) String cursor) {

        try {
            return service.getExecutions(includeData, status, workflowId, projectId, limit, cursor);
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body("{\"error\":\"Error al obtener ejecuciones de n8n\"}");
        }
    }
}