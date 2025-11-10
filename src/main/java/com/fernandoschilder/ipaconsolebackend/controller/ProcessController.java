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
    private final com.fernandoschilder.ipaconsolebackend.security.RbacSecurity rbacSecurity;

    public ProcessController(ProcessService processService, com.fernandoschilder.ipaconsolebackend.security.RbacSecurity rbacSecurity) {
        this.processService = processService;
        this.rbacSecurity = rbacSecurity;
    }

    @PostMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ProcessResponseDto> create(@Valid @RequestBody ProcessCreateDto dto) {
        var res = processService.create(dto);
        return ResponseEntity.created(URI.create("/processes/" + res.id())).body(res);
    }

    @GetMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("@rbacSecurity.canViewProcess(authentication.name, #id)")
    public ResponseEntity<ProcessResponseDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(processService.get(id));
    }

    @GetMapping
    public ResponseEntity<List<ProcessResponseDto>> list(
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean archived
    ) {
        // If the user is admin, return full list; otherwise return scoped list for current user
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : null;
        if (username != null && rbacSecurity.isAdmin(username)) {
            return ResponseEntity.ok(processService.list(tags, active, archived));
        }
        return ResponseEntity.ok(processService.listForCurrentUser(tags, active, archived, "view"));
    }

    @PostMapping("/{id}/start")
    @org.springframework.security.access.prepost.PreAuthorize("@rbacSecurity.canExecuteProcess(authentication.name, #id)")
    public ResponseEntity<?> start(@PathVariable Long id) {
        return processService.start(id);
    }

    @PatchMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("@rbacSecurity.canEditProcess(authentication.name, #id)")
    public ProcessResponseDto update(@PathVariable Long id, @RequestBody @Valid ProcessUpdateDto dto) {
        return processService.update(id, dto);
    }

    @DeleteMapping("/{processId}/parameters/{parameterId}")
    @org.springframework.security.access.prepost.PreAuthorize("@rbacSecurity.canEditProcess(authentication.name, #processId)")
    public ResponseEntity<?> deleteParameter(@PathVariable Long processId, @PathVariable Long parameterId) {
        return processService.deleteParameter(processId, parameterId);
    }

}
