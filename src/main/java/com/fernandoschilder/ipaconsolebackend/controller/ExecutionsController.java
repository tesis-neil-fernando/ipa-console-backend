package com.fernandoschilder.ipaconsolebackend.controller;

import com.fernandoschilder.ipaconsolebackend.dto.ExecutionsListResponseDto;
import com.fernandoschilder.ipaconsolebackend.service.ExecutionsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/executions")
public class ExecutionsController {
    private final ExecutionsService executionsService;

    public ExecutionsController(ExecutionsService executionsService) {
        this.executionsService = executionsService;
    }

    @GetMapping
    public ResponseEntity<ExecutionsListResponseDto> list(
            @RequestParam(required = false) Boolean includeData,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String workflowId,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false, defaultValue = "100") Integer limit,
            @RequestParam(required = false) String cursor
    ) {
        var res = executionsService.listExecutions(includeData, status, workflowId, projectId, limit, cursor);
        return ResponseEntity.ok(res);
    }
}