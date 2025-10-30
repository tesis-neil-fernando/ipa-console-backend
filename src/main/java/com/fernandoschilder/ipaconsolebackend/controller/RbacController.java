package com.fernandoschilder.ipaconsolebackend.controller;

import com.fernandoschilder.ipaconsolebackend.dto.*;
import com.fernandoschilder.ipaconsolebackend.mapper.RoleMapper;
import com.fernandoschilder.ipaconsolebackend.model.RoleEntity;
import com.fernandoschilder.ipaconsolebackend.service.NamespaceService;
import com.fernandoschilder.ipaconsolebackend.service.PermissionService;
import com.fernandoschilder.ipaconsolebackend.service.RoleService;
import com.fernandoschilder.ipaconsolebackend.service.ProcessService;
import com.fernandoschilder.ipaconsolebackend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/rbac")
@Validated
public class RbacController {

    private final UserService userService;
    private final NamespaceService namespaceService;
    private final PermissionService permissionService;
    private final RoleService roleService;
    private final RoleMapper roleMapper;
    private final ProcessService processService;

    public RbacController(UserService userService, NamespaceService namespaceService, PermissionService permissionService, RoleService roleService, RoleMapper roleMapper, ProcessService processService) {
        this.userService = userService;
        this.namespaceService = namespaceService;
        this.permissionService = permissionService;
        this.roleService = roleService;
        this.roleMapper = roleMapper;
        this.processService = processService;
    }

    // Create user (no bindings)
    @PostMapping("/users")
    public ResponseEntity<RbacResponse<UserViewDTO>> createUser(@RequestBody @Valid UserCreateDto body) {
        UserViewDTO created = userService.createUser(body);
        return ResponseEntity.created(URI.create("/users/" + created.username()))
                .body(new RbacResponse<>(created, "User created"));
    }

    // Create namespace and ensure default permissions (view, exec, edit) are present and linked
    @PostMapping("/namespaces")
    public ResponseEntity<RbacResponse<NamespaceDTO>> createNamespace(@RequestBody @Valid NamespaceDTO dto) {
        NamespaceDTO created = namespaceService.create(dto);

        // ensure the canonical permissions exist and are attached to this namespace
        List.of("view", "exec", "edit").forEach(type -> {
            try {
                permissionService.createPermission(type);
            } catch (Exception ignored) {
                // if exists, ignore; we'll attach namespace below
            }
            permissionService.addNamespaceToPermission(type, created.name());
        });

        return ResponseEntity.created(URI.create("/namespaces/" + created.id()))
                .body(new RbacResponse<>(created, "Namespace created and default permissions attached"));
    }

    // Create role with permissions specified
    @PostMapping("/roles")
    public ResponseEntity<RbacResponse<RoleDTO>> createRole(@RequestBody @Valid CreateRoleWithPermsDTO req) {
    RoleEntity created = roleService.createRole(req.name());
        if (req.permissions() != null && !req.permissions().isEmpty()) {
            roleService.setPermissionsToRoleByTypes(created.getName(), req.permissions());
            created = roleService.getByName(created.getName());
        }
        RoleDTO resp = roleMapper.toRoleDto(created);
        return ResponseEntity.created(URI.create("/roles/" + resp.name()))
                .body(new RbacResponse<>(resp, "Role created"));
    }

    // Get all users and their roles
    @GetMapping("/users")
    public List<UserViewDTO> listUsers() {
        return userService.listAllUsers();
    }

    // Get all roles and their permissions
    @GetMapping("/roles")
    public List<RoleDTO> listRoles() {
        return roleService.listRoles().stream().map(roleMapper::toRoleDto).toList();
    }

    // Get all namespaces and their processes
    @GetMapping("/namespaces")
    public List<NamespaceWithProcessesDTO> listNamespacesWithProcesses() {
        return namespaceService.listAllWithProcesses();
    }

    // Get all processes (mirror of /processes)
    @GetMapping("/processes")
    public java.util.List<com.fernandoschilder.ipaconsolebackend.dto.ProcessResponseDto> listProcesses(
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean archived
    ) {
        return processService.list(tags, active, archived);
    }

    @GetMapping("/processes/{id}")
    public com.fernandoschilder.ipaconsolebackend.dto.ProcessResponseDto getProcess(@PathVariable Long id) {
        return processService.get(id);
    }

    // Patch a user (update its roles)
    @PatchMapping("/users/{username}/roles")
    public UserViewDTO patchUserRoles(@PathVariable String username, @RequestBody Set<String> roles) {
        return userService.setUserRoles(username, roles);
    }

    // Patch a role (set its permissions)
    @PatchMapping("/roles/{name}/permissions")
    public RoleDTO patchRolePermissions(@PathVariable String name, @RequestBody SetPermsDTO body) {
        return roleMapper.toRoleDto(roleService.setPermissionsToRoleByTypes(name, body.permissions()));
    }

    // Patch a role with namespaces per permission
    @PatchMapping("/roles/{name}/permissions/namespaces")
    public RoleDTO patchRolePermissionsWithNamespaces(@PathVariable String name, @RequestBody SetPermsWithNamespacesDTO body) {
        return roleMapper.toRoleDto(roleService.setPermissionsToRoleWithNamespaces(name, body.permissions()));
    }

    // Replace (PUT) a role completely: name/description/permissions
    @PutMapping("/roles/{name}")
    public RoleDTO replaceRole(@PathVariable String name, @RequestBody @Valid CreateRoleWithPermsDTO req) {
        RoleEntity updated = roleService.replaceRole(name, req.name(), req.permissions());
        return roleMapper.toRoleDto(updated);
    }

    // Replace (PUT) a namespace completely (rename)
    @PutMapping("/namespaces/{name}")
    public ResponseEntity<RbacResponse<NamespaceDTO>> replaceNamespace(@PathVariable String name, @RequestBody @Valid NamespaceDTO dto) {
        NamespaceDTO updated = namespaceService.replaceNamespace(name, dto);
        return ResponseEntity.ok(new RbacResponse<>(updated, "Namespace replaced"));
    }

    // Replace (PUT) a user completely (username, password, roles)
    @PutMapping("/users/{username}")
    public UserViewDTO replaceUser(@PathVariable String username, @RequestBody @Valid UserReplaceDto body) {
        return userService.replaceUser(username, body);
    }
}
