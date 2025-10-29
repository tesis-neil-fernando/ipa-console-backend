package com.fernandoschilder.ipaconsolebackend.service;

import com.fernandoschilder.ipaconsolebackend.dto.WorkflowResponseDto;
import com.fernandoschilder.ipaconsolebackend.repository.N8nWorkflowRepository;
import com.fernandoschilder.ipaconsolebackend.utils.WorkflowMapper;
import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkflowService {

    private final N8nWorkflowRepository workflowRepository;

    @Transactional(readOnly = true)
    public List<WorkflowResponseDto> findAll(boolean includeRaw) {
        return workflowRepository.findAll().stream()
                .map(w -> WorkflowMapper.toResponseDto(w, includeRaw))
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkflowResponseDto findById(String id, boolean includeRaw) {
        var w = workflowRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Workflow not found: " + id));
        return WorkflowMapper.toResponseDto(w, includeRaw);
    }
}