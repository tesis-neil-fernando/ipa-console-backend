package com.fernandoschilder.ipaconsolebackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernandoschilder.ipaconsolebackend.dto.WorkflowDto;
import com.fernandoschilder.ipaconsolebackend.model.WorkflowEntity;
import com.fernandoschilder.ipaconsolebackend.repository.WorkflowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkflowPersistenceService {

    private final WorkflowRepository repo;
    private final ObjectMapper objectMapper;

    public WorkflowPersistenceService(WorkflowRepository repo, ObjectMapper objectMapper) {
        this.repo = repo;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void upsertAll(List<WorkflowDto> dtos) {
        for (WorkflowDto dto : dtos) {
            WorkflowEntity e = repo.findById(dto.id())
                    .orElseGet(WorkflowEntity::new);
            e.setId(dto.id());
            e.setName(dto.name());
            e.setActive(dto.active());
            e.setArchived(dto.isArchived());
            e.setCreatedAt(dto.createdAt());
            e.setUpdatedAt(dto.updatedAt());
            e.setVersionId(dto.versionId());
            e.setTriggerCount(dto.triggerCount());

            try {
                // guarda todo el documento
                e.setRawJson(objectMapper.writeValueAsString(dto));
            } catch (Exception ex) {
                throw new RuntimeException("Error serializando workflow " + dto.id(), ex);
            }
            repo.save(e);
        }
    }
}

