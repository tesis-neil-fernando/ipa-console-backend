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
public class RbacController {

    private final RbacService rbacService;

    @Autowired
    public RbacController(RbacService rbacService) {
        this.rbacService = rbacService;
    }

    @PostMapping("/namespaces")
    public ResponseEntity<NamespaceRbacDto> createNamespace(@RequestBody CreateNamespaceRequest req) {
        NamespaceRbacDto dto = rbacService.createNamespace(req.getName());
        return ResponseEntity.ok(dto);
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
    public ResponseEntity<RoleRbacDto> createRole(@RequestBody CreateRoleRequest req) {
        RoleRbacDto dto = rbacService.createRole(req.getName());
        return ResponseEntity.ok(dto);
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

}
