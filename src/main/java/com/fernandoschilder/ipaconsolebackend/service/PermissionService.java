package com.fernandoschilder.ipaconsolebackend.service;

import com.fernandoschilder.ipaconsolebackend.model.PermissionEntity;
import com.fernandoschilder.ipaconsolebackend.repository.PermissionRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

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
}
