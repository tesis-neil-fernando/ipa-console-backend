package com.fernandoschilder.ipaconsolebackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernandoschilder.ipaconsolebackend.dto.ExecutionBriefDto;
import com.fernandoschilder.ipaconsolebackend.dto.ProcessCreateDto;
import com.fernandoschilder.ipaconsolebackend.dto.ProcessResponseDto;
import com.fernandoschilder.ipaconsolebackend.model.ParameterEntity;
import com.fernandoschilder.ipaconsolebackend.model.ProcessEntity;
import com.fernandoschilder.ipaconsolebackend.repository.N8nWorkflowRepository;
import com.fernandoschilder.ipaconsolebackend.repository.ProcessRepository;
import com.fernandoschilder.ipaconsolebackend.utils.ProcessMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcessService {

    private final ProcessRepository processRepository;
    private final N8nWorkflowRepository workflowRepository;
    private final N8nApiService n8nApiService;
    private final ObjectMapper objectMapper; // inyectado por Spring

    @Transactional
    public ProcessResponseDto create(ProcessCreateDto dto) {
        var workflow = workflowRepository.findById(dto.workflowId())
                .orElseThrow(() -> new EntityNotFoundException("Workflow not found: " + dto.workflowId()));

        var process = new ProcessEntity();
        process.setName(dto.name());
        process.setDescription(dto.description());
        process.setWorkflow(workflow);

        if (dto.parameters() != null && !dto.parameters().isEmpty()) {
            dto.parameters().forEach(pd -> {
                var pe = new ParameterEntity();
                pe.setName(pd.name());
                pe.setValue(pd.value());
                pe.setType(pd.type());
                process.addParameter(pe); // mantiene ambos lados
            });
        }

        var saved = processRepository.save(process);

        var reloaded = processRepository.findById(saved.getId())
                .orElseThrow(() -> new EntityNotFoundException("Process not found after create: " + saved.getId()));

        var dtoBase = ProcessMapper.toResponseDto(reloaded);
        return enrichWithLastExecution(dtoBase);
    }

    @Transactional(readOnly = true)
    public ProcessResponseDto get(Long id) {
        var p = processRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Process not found: " + id));
        var dtoBase = ProcessMapper.toResponseDto(p);
        return enrichWithLastExecution(dtoBase);
    }

    @Transactional(readOnly = true)
    public List<ProcessResponseDto> list() {
        return processRepository.findAll().stream()
                .map(ProcessMapper::toResponseDto)
                .map(this::enrichWithLastExecution)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        if (!processRepository.existsById(id)) {
            throw new EntityNotFoundException("Process not found: " + id);
        }
        processRepository.deleteById(id); // orphanRemoval elimina parámetros asociados
    }

    // ---- helpers ----

    private ProcessResponseDto enrichWithLastExecution(ProcessResponseDto dto) {
        if (dto == null || dto.workflow() == null || dto.workflow().id() == null) return dto;
        var last = fetchLastExecution(dto.workflow().id());
        return new ProcessResponseDto(
                dto.id(),
                dto.name(),
                dto.description(),
                dto.workflow(),
                dto.parameters(),
                last
        );
    }

    private ExecutionBriefDto fetchLastExecution(String workflowId) {
        var resp = n8nApiService.getExecutions(null, null, workflowId, null, 1, null);

        var body = resp.getBody();
        if (body == null || body.getData() == null) return null;

        try {
            var root = objectMapper.readTree(body.getData()); // body.getData() es JSON String
            var arr = root.path("data");
            if (!arr.isArray() || arr.isEmpty()) return null;

            var e = arr.get(0); // asumimos más reciente primero
            return new ExecutionBriefDto(
                    e.path("id").asLong(),
                    e.path("startedAt").asText(null),
                    e.path("finished").asBoolean(),
                    e.path("status").asText(null)
            );
        } catch (Exception ex) {
            // log.warn("No se pudo parsear executionBrief", ex);
            return null;
        }
    }
}