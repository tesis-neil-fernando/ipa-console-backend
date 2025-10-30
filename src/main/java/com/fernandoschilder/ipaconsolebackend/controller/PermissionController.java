package com.fernandoschilder.ipaconsolebackend.controller;

import com.fernandoschilder.ipaconsolebackend.model.PermissionEntity;
import com.fernandoschilder.ipaconsolebackend.service.PermissionService;
import com.fernandoschilder.ipaconsolebackend.dto.PermissionDTO;
import com.fernandoschilder.ipaconsolebackend.mapper.PermissionMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import com.fernandoschilder.ipaconsolebackend.dto.AssignNamespacesDTO;
import com.fernandoschilder.ipaconsolebackend.dto.CreatePermissionDTO;
import java.util.List;

import org.springframework.http.ResponseEntity;
import java.net.URI;

@RestController
@RequestMapping("/permissions")
@Validated
public class PermissionController {

    private final PermissionService permissionService;
    private final PermissionMapper mapper;

    public PermissionController(PermissionService permissionService, PermissionMapper mapper) {
        this.permissionService = permissionService;
        this.mapper = mapper;
    }

    // Crear permiso (type único)
    @PostMapping
    public ResponseEntity<PermissionDTO> create(@RequestBody @Valid CreatePermissionDTO req) {
        PermissionEntity created = permissionService.createPermission(req.type());
        return ResponseEntity.created(URI.create("/permissions/" + created.getType())).body(mapper.toPermissionDto(created));
    }

    // Listar todos los permisos
    @GetMapping
    public List<PermissionDTO> list() {
        return permissionService.listPermissions().stream().map(mapper::toPermissionDto).toList();
    }

    // Obtener un permiso por type
    @GetMapping("/{type}")
    public PermissionDTO get(@PathVariable String type) {
        return mapper.toPermissionDto(permissionService.getByType(type));
    }
    @PutMapping("/{type}/namespaces")
    public PermissionDTO assignNamespacesToPermission(
            @PathVariable String type,
            @RequestBody AssignNamespacesDTO body) {
        return mapper.toPermissionDto(permissionService.setNamespaces(type, body.namespaces()));
    }

    // DTO moved to `com.fernandoschilder.ipaconsolebackend.dto.CreatePermissionDTO`.

    
}
