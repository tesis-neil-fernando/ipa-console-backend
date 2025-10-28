package com.fernandoschilder.ipaconsolebackend.controller;

import com.fernandoschilder.ipaconsolebackend.model.PermissionEntity;
import com.fernandoschilder.ipaconsolebackend.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    // Crear permiso (type único)
    @PostMapping
    public PermissionEntity create(@RequestBody CreatePermissionDTO req) {
        return permissionService.createPermission(req.type());
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

    // DTO de entrada
    public record CreatePermissionDTO(String type) {}
}
