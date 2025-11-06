package com.fernandoschilder.ipaconsolebackend.controller;

import com.fernandoschilder.ipaconsolebackend.dto.ExecutionResponseDto;
import com.fernandoschilder.ipaconsolebackend.dto.PageResponse;
import com.fernandoschilder.ipaconsolebackend.service.ExecutionsService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/executions")
@Validated
public class ExecutionsController {
    private final ExecutionsService executionsService;

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;

    public ExecutionsController(ExecutionsService executionsService) {
        this.executionsService = executionsService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<ExecutionResponseDto>> list(
            @RequestParam(required = false) Boolean includeData,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String workflowId,
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false, defaultValue = "100") Integer limit,
            @RequestParam(required = false) String cursor
    ) {
        int lim = (limit == null) ? DEFAULT_LIMIT : limit;
        if (lim <= 0) {
            throw new IllegalArgumentException("limit must be >= 1");
        }
        if (lim > MAX_LIMIT) {
            lim = MAX_LIMIT; // clamp to max instead of erroring for better UX
        }
        var res = executionsService.listExecutions(includeData, status, workflowId, projectId, lim, cursor);
        return ResponseEntity.ok(res);
    }
}