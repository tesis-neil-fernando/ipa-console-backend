package com.fernandoschilder.ipaconsolebackend.service;

import com.fernandoschilder.ipaconsolebackend.model.PermissionEntity;
import com.fernandoschilder.ipaconsolebackend.model.RoleEntity;
import com.fernandoschilder.ipaconsolebackend.repository.PermissionRepository;
import com.fernandoschilder.ipaconsolebackend.repository.RoleRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final com.fernandoschilder.ipaconsolebackend.service.PermissionService permissionService;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository, com.fernandoschilder.ipaconsolebackend.service.PermissionService permissionService) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.permissionService = permissionService;
    }

    public RoleEntity createRole(String name) {
        if (roleRepository.existsByName(name)) {
            throw new EntityExistsException("Role already exists: " + name);
        }
        RoleEntity r = new RoleEntity(name);
        return roleRepository.save(r);
    }

    public List<RoleEntity> listRoles() {
        return roleRepository.findAll();
    }

    public RoleEntity getByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + name));
    }

    /**
     * Asigna (o reemplaza) los permisos del rol indicado.
     * Si quieres “agregar sin reemplazar”, ajusta la lógica a addAll() sobre el set existente.
     */
    @Transactional
    public RoleEntity setPermissionsToRole(String roleName, List<String> permissionTypes) {
        RoleEntity role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleName));

        Set<PermissionEntity> newPermissions = new HashSet<>();
        for (String type : permissionTypes) {
            PermissionEntity perm = permissionRepository.findByType(type)
                    .orElseThrow(() -> new EntityNotFoundException("Permission not found: " + type));
            newPermissions.add(perm);
        }

        role.setPermissions(newPermissions); // reemplaza todo el set
        return roleRepository.save(role);
    }

    /**
     * Replace permissions of a role. Each permission DTO may include namespaces which will be set
     * on the Permission entity (replacing its namespaces).
     */
    @Transactional
    public RoleEntity setPermissionsToRole(String roleName, List<com.fernandoschilder.ipaconsolebackend.dto.PermissionWithNamespacesDTO> permissionDtos) {
        RoleEntity role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleName));

        Set<PermissionEntity> newPermissions = new HashSet<>();
        if (permissionDtos != null) {
            for (var dto : permissionDtos) {
                String type = dto.type();
                PermissionEntity perm = permissionRepository.findByType(type)
                        .orElseThrow(() -> new EntityNotFoundException("Permission not found: " + type));

                // Set the namespaces for this permission using PermissionService (will validate namespaces)
                if (dto.namespaces() != null) {
                    permissionService.setNamespaces(type, dto.namespaces());
                    // refresh perm reference
                    perm = permissionRepository.findByType(type).orElse(perm);
                }

                newPermissions.add(perm);
            }
        }

        role.setPermissions(newPermissions);
        return roleRepository.save(role);
    }

    /**
     * Variante para AGREGAR permisos sin perder los existentes.
     */
    @Transactional
    public RoleEntity addPermissionsToRole(String roleName, List<String> permissionTypes) {
        RoleEntity role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleName));

        Set<PermissionEntity> current = role.getPermissions() != null ? role.getPermissions() : new HashSet<>();
        for (String type : permissionTypes) {
            PermissionEntity perm = permissionRepository.findByType(type)
                    .orElseThrow(() -> new EntityNotFoundException("Permission not found: " + type));
            current.add(perm);
        }
        role.setPermissions(current);
        return roleRepository.save(role);
    }

    /**
     * Replace role properties completely: optionally rename, update description and permissions.
     * If newName differs and already exists -> EntityExistsException.
     */
    @Transactional
    public RoleEntity replaceRole(String currentName, String newName, List<String> permissionTypes) {
        RoleEntity role = roleRepository.findByName(currentName)
                .orElseThrow(() -> new EntityNotFoundException("Role not found: " + currentName));

        if (newName != null && !newName.isBlank() && !currentName.equals(newName)) {
            if (roleRepository.existsByName(newName)) {
                throw new EntityExistsException("Role already exists: " + newName);
            }
            role.setName(newName);
        }
    // description removed: nothing to update here
        if (permissionTypes != null) {
            Set<PermissionEntity> newPermissions = new HashSet<>();
            for (String type : permissionTypes) {
                PermissionEntity perm = permissionRepository.findByType(type)
                        .orElseThrow(() -> new EntityNotFoundException("Permission not found: " + type));
                newPermissions.add(perm);
            }
            role.setPermissions(newPermissions);
        }

        return roleRepository.save(role);
    }
}
