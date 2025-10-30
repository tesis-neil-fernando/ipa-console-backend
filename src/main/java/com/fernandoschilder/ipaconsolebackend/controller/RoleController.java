package com.fernandoschilder.ipaconsolebackend.controller;

import com.fernandoschilder.ipaconsolebackend.model.RoleEntity;
import com.fernandoschilder.ipaconsolebackend.service.RoleService;
import com.fernandoschilder.ipaconsolebackend.dto.RoleDTO;
import com.fernandoschilder.ipaconsolebackend.mapper.RoleMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.ResponseEntity;
import java.net.URI;
import java.util.List;
import com.fernandoschilder.ipaconsolebackend.dto.CreateRoleDTO;
import com.fernandoschilder.ipaconsolebackend.dto.SetPermsDTO;

@RestController
@RequestMapping("/roles")
@Validated
public class RoleController {

    private final RoleService roleService;
    private final RoleMapper mapper;

    public RoleController(RoleService roleService, RoleMapper mapper) {
        this.roleService = roleService;
        this.mapper = mapper;
    }

    // Crear rol
    @PostMapping
    public ResponseEntity<RoleDTO> create(@RequestBody @Valid CreateRoleDTO req) {
        RoleEntity created = roleService.createRole(req.name(), req.description());
        return ResponseEntity.created(URI.create("/roles/" + created.getName())).body(mapper.toRoleDto(created));
    }

    // Listar roles
    @GetMapping
    public List<RoleDTO> list() {
        return roleService.listRoles().stream().map(mapper::toRoleDto).toList();
    }

    // Obtener rol por name
    @GetMapping("/{name}")
    public RoleDTO get(@PathVariable String name) {
        return mapper.toRoleDto(roleService.getByName(name));
    }

    // Reemplazar permisos del rol (sobrescribe el set completo)
    @PutMapping("/{name}/permissions")
    public RoleDTO setPermissions(@PathVariable String name, @RequestBody SetPermsDTO req) {
        return mapper.toRoleDto(roleService.setPermissionsToRole(name, req.permissions()));
    }

    // Agregar permisos al rol (sin borrar los existentes)
    @PatchMapping("/{name}/permissions")
    public RoleDTO addPermissions(@PathVariable String name, @RequestBody SetPermsDTO req) {
        return mapper.toRoleDto(roleService.addPermissionsToRole(name, req.permissions()));
    }

    // DTOs moved to `com.fernandoschilder.ipaconsolebackend.dto` package:
    // - CreateRoleDTO
    // - SetPermsDTO

    
}
