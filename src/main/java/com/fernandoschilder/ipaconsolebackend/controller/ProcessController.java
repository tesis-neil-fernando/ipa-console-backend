package com.fernandoschilder.ipaconsolebackend.controller;

import com.fernandoschilder.ipaconsolebackend.dto.ProcessCreateDto;
import com.fernandoschilder.ipaconsolebackend.dto.ProcessResponseDto;
import com.fernandoschilder.ipaconsolebackend.dto.ProcessUpdateDto;
import com.fernandoschilder.ipaconsolebackend.service.ProcessService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/processes")
@Validated
public class ProcessController {

    private final ProcessService processService;

    public ProcessController(ProcessService processService) {
        this.processService = processService;
    }

    @PostMapping
    public ResponseEntity<ProcessResponseDto> create(@Valid @RequestBody ProcessCreateDto dto) {
        var res = processService.create(dto);
        return ResponseEntity.created(URI.create("/processes/" + res.id())).body(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcessResponseDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(processService.get(id));
    }

    @GetMapping
    public ResponseEntity<List<ProcessResponseDto>> list(
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean archived
    ) {
        return ResponseEntity.ok(processService.list(tags, active, archived));
    }

    /**
     * List processes visible to the current authenticated user, scoped by namespaces where the
     * user has the given permission (default: "view").
     */
    @GetMapping("/me")
    public ResponseEntity<List<ProcessResponseDto>> listForCurrentUser(
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean archived,
            @RequestParam(required = false, defaultValue = "view") String permissionType
    ) {
        return ResponseEntity.ok(processService.listForCurrentUser(tags, active, archived, permissionType));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<?> start(@PathVariable Long id) {
        return processService.start(id);
    }

    @PatchMapping("/{id}")
    public ProcessResponseDto update(@PathVariable Long id, @RequestBody @Valid ProcessUpdateDto dto) {
        return processService.update(id, dto);
    }

    @DeleteMapping("/{processId}/parameters/{parameterId}")
    public ResponseEntity<?> deleteParameter(@PathVariable Long processId, @PathVariable Long parameterId) {
        return processService.deleteParameter(processId, parameterId);
    }

}
