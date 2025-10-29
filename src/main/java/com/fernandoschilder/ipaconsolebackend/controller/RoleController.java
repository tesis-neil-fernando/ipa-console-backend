package com.fernandoschilder.ipaconsolebackend.controller;

import com.fernandoschilder.ipaconsolebackend.model.RoleEntity;
import com.fernandoschilder.ipaconsolebackend.service.RoleService;
import com.fernandoschilder.ipaconsolebackend.dto.RoleDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    // Crear rol
    @PostMapping
    public ResponseEntity<RoleDTO> create(@RequestBody @Valid CreateRoleDTO req) {
        RoleEntity created = roleService.createRole(req.name(), req.description());
        return ResponseEntity.created(URI.create("/roles/" + created.getName())).body(toDto(created));
    }

    // Listar roles
    @GetMapping
    public List<RoleDTO> list() {
        return roleService.listRoles().stream().map(this::toDto).toList();
    }

    // Obtener rol por name
    @GetMapping("/{name}")
    public RoleDTO get(@PathVariable String name) {
        return toDto(roleService.getByName(name));
    }

    // Reemplazar permisos del rol (sobrescribe el set completo)
    @PutMapping("/{name}/permissions")
    public RoleDTO setPermissions(@PathVariable String name, @RequestBody SetPermsDTO req) {
        return toDto(roleService.setPermissionsToRole(name, req.permissions()));
    }

    // Agregar permisos al rol (sin borrar los existentes)
    @PatchMapping("/{name}/permissions")
    public RoleDTO addPermissions(@PathVariable String name, @RequestBody SetPermsDTO req) {
        return toDto(roleService.addPermissionsToRole(name, req.permissions()));
    }

    // DTOs de entrada
    public record CreateRoleDTO(@NotBlank String name, String description) {}
    public record SetPermsDTO(List<String> permissions) {}

    private RoleDTO toDto(RoleEntity r) {
        return RoleDTO.builder()
                .id(r.getId())
                .name(r.getName())
                .description(r.getDescription())
                .permissions(r.getPermissions() == null ? java.util.Set.of() : r.getPermissions().stream().map(p -> p.getType()).collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new)))
                .build();
    }
}
