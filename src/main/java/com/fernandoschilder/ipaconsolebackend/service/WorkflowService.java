package com.fernandoschilder.ipaconsolebackend.service;

import com.fernandoschilder.ipaconsolebackend.dto.WorkflowResponseDto;
import com.fernandoschilder.ipaconsolebackend.repository.WorkflowRepository;
import com.fernandoschilder.ipaconsolebackend.mapper.WorkflowMapper;
import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkflowService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowMapper workflowMapper;

    public WorkflowService(WorkflowRepository workflowRepository, WorkflowMapper workflowMapper) {
        this.workflowRepository = workflowRepository;
        this.workflowMapper = workflowMapper;
    }

    @Transactional(readOnly = true)
    public List<WorkflowResponseDto> findAll(boolean includeRaw) {
    return workflowRepository.findAll().stream()
        .map(w -> includeRaw ? workflowMapper.toDtoWithRaw(w) : workflowMapper.toDto(w))
        .toList();
    }

    @Transactional(readOnly = true)
    public WorkflowResponseDto findById(String id, boolean includeRaw) {
        var w = workflowRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Workflow not found: " + id));
    return includeRaw ? workflowMapper.toDtoWithRaw(w) : workflowMapper.toDto(w);
    }
}