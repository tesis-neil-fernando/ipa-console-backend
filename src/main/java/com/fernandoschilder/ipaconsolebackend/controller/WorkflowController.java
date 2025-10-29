package com.fernandoschilder.ipaconsolebackend.controller;

import com.fernandoschilder.ipaconsolebackend.dto.WorkflowResponseDto;
import com.fernandoschilder.ipaconsolebackend.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/workflows")
public class WorkflowController {

    private final WorkflowService workflowService;

    @GetMapping
    public ResponseEntity<List<WorkflowResponseDto>> findAll(
            @RequestParam(defaultValue = "false") boolean includeRaw) {
        return ResponseEntity.ok(workflowService.findAll(includeRaw));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkflowResponseDto> findById(
            @PathVariable String id,
            @RequestParam(defaultValue = "false") boolean includeRaw) {
        return ResponseEntity.ok(workflowService.findById(id, includeRaw));
    }
}