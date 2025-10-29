package com.fernandoschilder.ipaconsolebackend.controller;

import com.fernandoschilder.ipaconsolebackend.dto.ProcessCreateDto;
import com.fernandoschilder.ipaconsolebackend.dto.ProcessResponseDto;
import com.fernandoschilder.ipaconsolebackend.dto.ProcessUpdateDto;
import com.fernandoschilder.ipaconsolebackend.service.ProcessService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/processes")
@RequiredArgsConstructor
public class ProcessController {

    private final ProcessService processService;

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

    @PatchMapping("/{id}")
    public ProcessResponseDto update(@PathVariable Long id, @RequestBody @Valid ProcessUpdateDto dto) {
        return processService.update(id, dto);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }
}
