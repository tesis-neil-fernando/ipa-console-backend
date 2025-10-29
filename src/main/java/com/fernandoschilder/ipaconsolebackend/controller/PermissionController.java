package com.fernandoschilder.ipaconsolebackend.controller;

import com.fernandoschilder.ipaconsolebackend.model.PermissionEntity;
import com.fernandoschilder.ipaconsolebackend.service.PermissionService;
import com.fernandoschilder.ipaconsolebackend.dto.PermissionDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
    public ResponseEntity<PermissionDTO> create(@RequestBody @Valid CreatePermissionDTO req) {
        PermissionEntity created = permissionService.createPermission(req.type());
        return ResponseEntity.created(URI.create("/permissions/" + created.getType())).body(toDto(created));
    }

    // Listar todos los permisos
    @GetMapping
    public List<PermissionDTO> list() {
        return permissionService.listPermissions().stream().map(this::toDto).toList();
    }

    // Obtener un permiso por type
    @GetMapping("/{type}")
    public PermissionDTO get(@PathVariable String type) {
        return toDto(permissionService.getByType(type));
    }
    @PutMapping("/{type}/namespaces")
    public PermissionDTO assignNamespacesToPermission(
            @PathVariable String type,
            @RequestBody AssignNamespacesDTO body) {
        return toDto(permissionService.setNamespaces(type, body.getNamespaces()));
    }

    // DTO de entrada
    public record CreatePermissionDTO(@NotBlank String type) {}

    private PermissionDTO toDto(PermissionEntity p) {
        return PermissionDTO.builder()
                .id(p.getId())
                .type(p.getType())
                .namespaces(p.getPermission_namespaces() == null ? java.util.Set.of() : p.getPermission_namespaces().stream().map(n -> n.getName()).collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new)))
                .build();
    }
}
