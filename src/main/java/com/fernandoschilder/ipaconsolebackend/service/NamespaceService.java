package com.fernandoschilder.ipaconsolebackend.service;

import com.fernandoschilder.ipaconsolebackend.dto.NamespaceDTO;
import com.fernandoschilder.ipaconsolebackend.model.NamespaceEntity;
import com.fernandoschilder.ipaconsolebackend.repository.NamespaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NamespaceService {

    private final NamespaceRepository namespaceRepository;

    public List<NamespaceDTO> listAll() {
        return namespaceRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public NamespaceDTO create(NamespaceDTO dto) {
        if (namespaceRepository.existsByName(dto.getName())) {
            throw new EntityExistsException("Namespace '" + dto.getName() + "' ya existe");
        }
        NamespaceEntity e = new NamespaceEntity(dto.getName());
        e.setDescription(dto.getDescription());
        return toDTO(namespaceRepository.save(e));
    }

    public NamespaceEntity getByNameOrThrow(String name) {
        return namespaceRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Namespace '" + name + "' no encontrado"));
    }

    private NamespaceDTO toDTO(NamespaceEntity e) {
        return NamespaceDTO.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .build();
    }
}
