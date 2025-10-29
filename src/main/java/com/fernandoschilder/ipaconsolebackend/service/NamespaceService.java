package com.fernandoschilder.ipaconsolebackend.service;

import com.fernandoschilder.ipaconsolebackend.dto.NamespaceDTO;
import com.fernandoschilder.ipaconsolebackend.model.NamespaceEntity;
import com.fernandoschilder.ipaconsolebackend.repository.NamespaceRepository;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class NamespaceService {

    private final NamespaceRepository namespaceRepository;

    public NamespaceService(NamespaceRepository namespaceRepository) {
        this.namespaceRepository = namespaceRepository;
    }

    public List<NamespaceDTO> listAll() {
        return namespaceRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public NamespaceDTO create(NamespaceDTO dto) {
        if (namespaceRepository.existsByName(dto.name())) {
            throw new EntityExistsException("Namespace '" + dto.name() + "' ya existe");
        }
        NamespaceEntity e = new NamespaceEntity(dto.name());
        e.setDescription(dto.description());
        return toDTO(namespaceRepository.save(e));
    }

    public NamespaceEntity getByNameOrThrow(String name) {
        return namespaceRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Namespace '" + name + "' no encontrado"));
    }

    private NamespaceDTO toDTO(NamespaceEntity e) {
    return new NamespaceDTO(e.getId(), e.getName(), e.getDescription());
    }
}
