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

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public RoleEntity createRole(String name, String description) {
        if (roleRepository.existsByName(name)) {
            throw new EntityExistsException("Role already exists: " + name);
        }
        RoleEntity r = new RoleEntity(name, description);
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
}
