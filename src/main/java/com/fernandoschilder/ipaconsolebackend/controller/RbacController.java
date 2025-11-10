package com.fernandoschilder.ipaconsolebackend.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

import com.fernandoschilder.ipaconsolebackend.service.rbac.RbacService;
import com.fernandoschilder.ipaconsolebackend.dto.rbac.*;

import java.util.List;


@RestController
@RequestMapping("/rbac")
@Validated
@org.springframework.security.access.prepost.PreAuthorize("hasAuthority('ADMIN')")
public class RbacController {

    private final RbacService rbacService;

    @Autowired
    public RbacController(RbacService rbacService) {
        this.rbacService = rbacService;
    }

    @PostMapping("/namespaces")
    public ResponseEntity<CreateResponse> createNamespace(@RequestBody CreateNamespaceRequest req) {
        NamespaceRbacDto dto = rbacService.createNamespace(req.getName());
        CreateResponse resp = new CreateResponse(true, dto.getId());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/namespaces/{nsId}/processes/{processId}")
    public ResponseEntity<Void> assignProcessToNamespace(@PathVariable Long nsId, @PathVariable Long processId) {
        rbacService.assignProcessToNamespace(processId, nsId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/namespaces/{nsId}/processes/{processId}")
    public ResponseEntity<Void> removeProcessFromNamespace(@PathVariable Long nsId, @PathVariable Long processId) {
        rbacService.removeProcessFromNamespace(processId, nsId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/roles")
    public ResponseEntity<CreateResponse> createRole(@RequestBody CreateRoleRequest req) {
        RoleRbacDto dto = rbacService.createRole(req.getName());
        CreateResponse resp = new CreateResponse(true, dto.getId());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/users")
    public ResponseEntity<CreateUserResponse> createUser(@RequestBody CreateUserRequest req) {
        // accept optional name; newly created users are enabled by default
        // Delegate password generation and user creation to the service
        CreateUserResponse resp = rbacService.createUser(req.getUsername(), req.getName());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/roles/{roleId}/permissions/{permissionId}")
    public ResponseEntity<Void> assignPermissionToRole(@PathVariable Long roleId, @PathVariable Long permissionId) {
        rbacService.assignPermissionToRole(roleId, permissionId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
    public ResponseEntity<Void> removePermissionFromRole(@PathVariable Long roleId, @PathVariable Long permissionId) {
        rbacService.removePermissionFromRole(roleId, permissionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}/roles/{roleId}")
    public ResponseEntity<Void> assignRoleToUser(@PathVariable Long userId, @PathVariable Long roleId) {
        rbacService.assignRoleToUser(userId, roleId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{userId}/roles/{roleId}")
    public ResponseEntity<Void> removeRoleFromUser(@PathVariable Long userId, @PathVariable Long roleId) {
        rbacService.removeRoleFromUser(userId, roleId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserRbacDto> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(rbacService.getUserById(id));
    }

    @GetMapping("/roles/{id}")
    public ResponseEntity<RoleRbacDto> getRole(@PathVariable Long id) {
        return ResponseEntity.ok(rbacService.getRoleById(id));
    }

    @GetMapping("/namespaces/{id}")
    public ResponseEntity<NamespaceRbacDto> getNamespace(@PathVariable Long id) {
        return ResponseEntity.ok(rbacService.getNamespaceById(id));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserRbacDto>> listUsers() {
        return ResponseEntity.ok(rbacService.listUsers());
    }

    @GetMapping("/roles")
    public ResponseEntity<List<RoleRbacDto>> listRoles() {
        return ResponseEntity.ok(rbacService.listRoles());
    }

    @GetMapping("/namespaces")
    public ResponseEntity<List<NamespaceRbacDto>> listNamespaces() {
        return ResponseEntity.ok(rbacService.listNamespaces());
    }

    @GetMapping("/processes")
    public ResponseEntity<List<ProcessRbacDto>> listProcesses() {
        return ResponseEntity.ok(rbacService.listProcesses());
    }

    @PutMapping("/users/password")
    public ResponseEntity<Void> updatePassword(@RequestBody UpdatePasswordRequest req) {
        // Request contains the user id and the new password (admin operation)
        rbacService.updatePassword(req.getId(), req.getPassword());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/enabled")
    public ResponseEntity<Void> updateUserEnabled(@PathVariable Long id, @RequestBody UpdateUserEnabledRequest req) {
        rbacService.updateUserEnabled(id, req.getEnabled());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/processes/{id}")
    public ResponseEntity<Void> updateProcess(@PathVariable Long id, @RequestBody UpdateProcessRequest req) {
        rbacService.updateProcess(id, req.getName(), req.getDescription());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/name")
    public ResponseEntity<Void> updateUserName(@PathVariable Long id, @RequestBody UpdateNameRequest req) {
        rbacService.updateUserName(id, req.getName());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/namespaces/{id}")
    public ResponseEntity<Void> updateNamespaceName(@PathVariable Long id, @RequestBody UpdateNameRequest req) {
        rbacService.updateNamespaceName(id, req.getName());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/roles/{id}")
    public ResponseEntity<Void> updateRoleName(@PathVariable Long id, @RequestBody UpdateNameRequest req) {
        rbacService.updateRoleName(id, req.getName());
        return ResponseEntity.ok().build();
    }

}
