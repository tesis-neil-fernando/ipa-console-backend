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
    private final com.fernandoschilder.ipaconsolebackend.mapper.ProcessMapper processMapper;

    public NamespaceService(NamespaceRepository namespaceRepository, com.fernandoschilder.ipaconsolebackend.mapper.ProcessMapper processMapper) {
        this.namespaceRepository = namespaceRepository;
        this.processMapper = processMapper;
    }

    public List<NamespaceDTO> listAll() {
    return namespaceRepository.findAll().stream()
        .map(this::toDTO)
        .toList();
    }

    public List<com.fernandoschilder.ipaconsolebackend.dto.NamespaceWithProcessesDTO> listAllWithProcesses() {
    return namespaceRepository.findAll().stream()
        .map(e -> new com.fernandoschilder.ipaconsolebackend.dto.NamespaceWithProcessesDTO(
            e.getId(), e.getName(),
            e.getProcesses().stream().map(processMapper::toResponseDto).toList()
        ))
        .toList();
    }

    public NamespaceDTO create(NamespaceDTO dto) {
        if (namespaceRepository.existsByName(dto.name())) {
            throw new EntityExistsException("Namespace '" + dto.name() + "' ya existe");
        }
        NamespaceEntity e = new NamespaceEntity(dto.name());
        return toDTO(namespaceRepository.save(e));
    }

    public NamespaceEntity getByNameOrThrow(String name) {
        return namespaceRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Namespace '" + name + "' no encontrado"));
    }

    private NamespaceDTO toDTO(NamespaceEntity e) {
        return new NamespaceDTO(e.getId(), e.getName());
    }

    /**
     * Replace/rename a namespace completely. The incoming dto carries the new name.
     */
    @jakarta.transaction.Transactional
    public NamespaceDTO replaceNamespace(String currentName, NamespaceDTO dto) {
        NamespaceEntity e = namespaceRepository.findByName(currentName)
                .orElseThrow(() -> new EntityNotFoundException("Namespace '" + currentName + "' no encontrado"));

        String newName = dto.name();
        if (!currentName.equals(newName) && namespaceRepository.existsByName(newName)) {
            throw new EntityExistsException("Namespace '" + newName + "' ya existe");
        }
        e.setName(newName);
        return toDTO(namespaceRepository.save(e));
    }
}
