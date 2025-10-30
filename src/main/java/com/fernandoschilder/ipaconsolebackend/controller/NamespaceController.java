package com.fernandoschilder.ipaconsolebackend.controller;

import com.fernandoschilder.ipaconsolebackend.dto.NamespaceDTO;
import com.fernandoschilder.ipaconsolebackend.service.NamespaceService;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import org.springframework.http.ResponseEntity;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/namespaces")
@Validated
public class NamespaceController {

    private final NamespaceService namespaceService;

    public NamespaceController(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    @GetMapping
    public List<NamespaceDTO> list() {
        return namespaceService.listAll();
    }

    @PostMapping
    public ResponseEntity<NamespaceDTO> create(@RequestBody NamespaceDTO dto) {
        NamespaceDTO res = namespaceService.create(dto);
        return ResponseEntity.created(URI.create("/namespaces/" + res.id())).body(res);
    }
}
