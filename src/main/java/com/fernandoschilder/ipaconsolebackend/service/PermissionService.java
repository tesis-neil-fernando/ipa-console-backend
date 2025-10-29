package com.fernandoschilder.ipaconsolebackend.service;

import com.fernandoschilder.ipaconsolebackend.model.NamespaceEntity;
import com.fernandoschilder.ipaconsolebackend.model.PermissionEntity;
import com.fernandoschilder.ipaconsolebackend.repository.NamespaceRepository;
import com.fernandoschilder.ipaconsolebackend.repository.PermissionRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final NamespaceRepository namespaceRepository; // ← nuevo

    /* ======================== CRUD PERMISO ======================== */

    public PermissionEntity createPermission(String type) {
        if (permissionRepository.existsByType(type)) {
            throw new EntityExistsException("Permission already exists: " + type);
        }
        PermissionEntity p = new PermissionEntity(type);
        return permissionRepository.save(p);
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

        perm.setPermission_namespaces(namespaces);
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
