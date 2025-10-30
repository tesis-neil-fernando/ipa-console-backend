package com.fernandoschilder.ipaconsolebackend.service;

import com.fernandoschilder.ipaconsolebackend.model.NamespaceEntity;
import com.fernandoschilder.ipaconsolebackend.model.PermissionEntity;
import com.fernandoschilder.ipaconsolebackend.repository.NamespaceRepository;
import com.fernandoschilder.ipaconsolebackend.repository.PermissionRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final NamespaceRepository namespaceRepository; // ← nuevo

    public PermissionService(PermissionRepository permissionRepository, NamespaceRepository namespaceRepository) {
        this.permissionRepository = permissionRepository;
        this.namespaceRepository = namespaceRepository;
    }

    /**
     * Attach an existing namespace to an existing permission.
     * Throws EntityNotFoundException if either does not exist.
     */
    @jakarta.transaction.Transactional
    public PermissionEntity addNamespaceToPermission(String permissionType, String namespaceName) {
        PermissionEntity perm = permissionRepository.findByType(permissionType)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Permission not found: " + permissionType));

        NamespaceEntity ns = namespaceRepository.findByName(namespaceName)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Namespace not found: " + namespaceName));

        perm.addNamespace(ns);
        return permissionRepository.save(perm);
    }

    /* ======================== CRUD PERMISO ======================== */

    public PermissionEntity createPermission(String type) {
        String normalized = normalizeType(type);
        if (permissionRepository.existsByType(normalized)) {
            throw new EntityExistsException("Permission already exists: " + normalized);
        }
        PermissionEntity p = new PermissionEntity(normalized);
        return permissionRepository.save(p);
    }

    private String normalizeType(String type) {
        if (type == null) throw new IllegalArgumentException("Permission type cannot be null");
        String t = type.trim().toLowerCase();
        // Accept only canonical permission types
        if (Set.of("view", "exec", "edit", "admin").contains(t)) return t;
        throw new IllegalArgumentException("Invalid permission type: " + type + ". Allowed: view, exec, edit, admin");
    }

    public List<PermissionEntity> listPermissions() {
        return permissionRepository.findAll();
    }

    public PermissionEntity getByType(String type) {
        return permissionRepository.findByType(type)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found: " + type));
    }

    /* ============== ASIGNAR NAMESPACES A UN PERMISO ============== */

    /**
     * Reemplaza completamente los namespaces asociados a un permiso.
     * @param permissionType p. ej.: "visualizar", "ejecutar"
     * @param namespaceNames p. ej.: ["inteligencia_comercial","marketing"]
     * @return PermissionEntity actualizado
     */
    @Transactional
    public PermissionEntity setNamespaces(String permissionType, Set<String> namespaceNames) {
        PermissionEntity perm = permissionRepository.findByType(permissionType)
                .orElseThrow(() -> new EntityNotFoundException("Permission not found: " + permissionType));

        // Busca los namespaces por nombre; si alguno no existe, lanza error (comportamiento recomendado)
        Set<NamespaceEntity> namespaces = namespaceNames.stream()
                .map(name -> namespaceRepository.findByName(name)
                        .orElseThrow(() -> new EntityNotFoundException("Namespace not found: " + name)))
                .collect(Collectors.toSet());

    perm.setNamespaces(namespaces);
        return permissionRepository.save(perm);
    }

    /* ------------ Variante opcional: autocrear namespaces ---------- */
    // @Transactional
    // public PermissionEntity upsertNamespaces(String permissionType, Set<String> namespaceNames) {
    //     PermissionEntity perm = permissionRepository.findByType(permissionType)
    //             .orElseThrow(() -> new EntityNotFoundException("Permission not found: " + permissionType));
    //
    //     Set<NamespaceEntity> namespaces = namespaceNames.stream()
    //             .map(name -> namespaceRepository.findByName(name)
    //                     .orElseGet(() -> namespaceRepository.save(new NamespaceEntity(name))))
    //             .collect(Collectors.toSet());
    //
    //     perm.setPermission_namespaces(namespaces);
    //     return permissionRepository.save(perm);
    // }
}
