package com.fernandoschilder.ipaconsolebackend.controller;

import com.fernandoschilder.ipaconsolebackend.dto.ProcessCreateDto;
import com.fernandoschilder.ipaconsolebackend.dto.ProcessResponseDto;
import com.fernandoschilder.ipaconsolebackend.dto.ProcessUpdateDto;
import com.fernandoschilder.ipaconsolebackend.service.ProcessService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/processes")
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
    public ResponseEntity<List<ProcessResponseDto>> list() {
        return ResponseEntity.ok(processService.list());
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<?> start(@PathVariable Long id) {
        return processService.start(id);
    }

    @PatchMapping("/{id}")
    public ProcessResponseDto update(@PathVariable Long id, @RequestBody @Valid ProcessUpdateDto dto) {
        return processService.update(id, dto);
    }

}
