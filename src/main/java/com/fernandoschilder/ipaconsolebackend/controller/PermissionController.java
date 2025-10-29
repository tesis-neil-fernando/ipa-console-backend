package com.fernandoschilder.ipaconsolebackend.controller;

import com.fernandoschilder.ipaconsolebackend.model.PermissionEntity;
import com.fernandoschilder.ipaconsolebackend.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.fernandoschilder.ipaconsolebackend.dto.AssignNamespacesDTO;
import java.util.List;

import org.springframework.http.ResponseEntity;
import java.net.URI;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    // Crear permiso (type único)
    @PostMapping
    public ResponseEntity<PermissionEntity> create(@RequestBody CreatePermissionDTO req) {
        PermissionEntity created = permissionService.createPermission(req.type());
        return ResponseEntity.created(URI.create("/permissions/" + created.getType())).body(created);
    }

    // Listar todos los permisos
    @GetMapping
    public List<PermissionEntity> list() {
        return permissionService.listPermissions();
    }

    // Obtener un permiso por type
    @GetMapping("/{type}")
    public PermissionEntity get(@PathVariable String type) {
        return permissionService.getByType(type);
    }
    @PutMapping("/{type}/namespaces")
    public PermissionEntity assignNamespacesToPermission(
            @PathVariable String type,
            @RequestBody AssignNamespacesDTO body) {
        return permissionService.setNamespaces(type, body.getNamespaces());
    }

    // DTO de entrada
    public record CreatePermissionDTO(String type) {}
}
