package com.fernandoschilder.ipaconsolebackend.controller;

import com.fernandoschilder.ipaconsolebackend.model.RoleEntity;
import com.fernandoschilder.ipaconsolebackend.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    // Crear rol
    @PostMapping
    public RoleEntity create(@RequestBody CreateRoleDTO req) {
        return roleService.createRole(req.name(), req.description());
    }

    // Listar roles
    @GetMapping
    public List<RoleEntity> list() {
        return roleService.listRoles();
    }

    // Obtener rol por name
    @GetMapping("/{name}")
    public RoleEntity get(@PathVariable String name) {
        return roleService.getByName(name);
    }

    // Reemplazar permisos del rol (sobrescribe el set completo)
    @PutMapping("/{name}/permissions")
    public RoleEntity setPermissions(@PathVariable String name, @RequestBody SetPermsDTO req) {
        return roleService.setPermissionsToRole(name, req.permissions());
    }

    // Agregar permisos al rol (sin borrar los existentes)
    @PatchMapping("/{name}/permissions")
    public RoleEntity addPermissions(@PathVariable String name, @RequestBody SetPermsDTO req) {
        return roleService.addPermissionsToRole(name, req.permissions());
    }

    // DTOs de entrada
    public record CreateRoleDTO(String name, String description) {}
    public record SetPermsDTO(List<String> permissions) {}
}
