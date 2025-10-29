package com.fernandoschilder.ipaconsolebackend.controller;

import com.fernandoschilder.ipaconsolebackend.dto.NamespaceDTO;
import com.fernandoschilder.ipaconsolebackend.service.NamespaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/namespaces")
@RequiredArgsConstructor
public class NamespaceController {

    private final NamespaceService namespaceService;

    @GetMapping
    public List<NamespaceDTO> list() {
        return namespaceService.listAll();
    }

    @PostMapping
    public NamespaceDTO create(@RequestBody NamespaceDTO dto) {
        return namespaceService.create(dto);
    }
}
