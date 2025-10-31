package com.fernandoschilder.ipaconsolebackend.service.rbac;

import com.fernandoschilder.ipaconsolebackend.dto.rbac.*;

import java.util.List;

public interface RbacService {
    NamespaceRbacDto createNamespace(String name);

    void assignProcessToNamespace(Long processId, Long namespaceId);

    void removeProcessFromNamespace(Long processId, Long namespaceId);

    RoleRbacDto createRole(String name);

    void assignPermissionToRole(Long roleId, Long permissionId);

    void removePermissionFromRole(Long roleId, Long permissionId);

    void assignRoleToUser(Long userId, Long roleId);

    void removeRoleFromUser(Long userId, Long roleId);

    UserRbacDto getUserById(Long id);

    RoleRbacDto getRoleById(Long id);

    NamespaceRbacDto getNamespaceById(Long id);

    List<UserRbacDto> listUsers();

    List<RoleRbacDto> listRoles();

    List<NamespaceRbacDto> listNamespaces();

    List<ProcessRbacDto> listProcesses();
}
