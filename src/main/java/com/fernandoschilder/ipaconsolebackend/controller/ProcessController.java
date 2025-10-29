package com.fernandoschilder.ipaconsolebackend.controller;
import com.fernandoschilder.ipaconsolebackend.dto.ProcessCreateDto;
import com.fernandoschilder.ipaconsolebackend.dto.ProcessResponseDto;
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

    private final ProcessService service;

    @PostMapping
    public ResponseEntity<ProcessResponseDto> create(@Valid @RequestBody ProcessCreateDto dto) {
        var res = service.create(dto);
        return ResponseEntity.created(URI.create("/processes/" + res.id())).body(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcessResponseDto> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping
    public ResponseEntity<List<ProcessResponseDto>> list() {
        return ResponseEntity.ok(service.list());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleNotFound(EntityNotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }
}
