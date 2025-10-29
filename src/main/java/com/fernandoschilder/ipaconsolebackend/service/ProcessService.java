package com.fernandoschilder.ipaconsolebackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernandoschilder.ipaconsolebackend.dto.ExecutionBriefDto;
import com.fernandoschilder.ipaconsolebackend.dto.ProcessCreateDto;
import com.fernandoschilder.ipaconsolebackend.dto.ProcessResponseDto;
import com.fernandoschilder.ipaconsolebackend.dto.ProcessUpdateDto;
import com.fernandoschilder.ipaconsolebackend.model.ParameterEntity;
import com.fernandoschilder.ipaconsolebackend.model.ProcessEntity;
import com.fernandoschilder.ipaconsolebackend.repository.WorkflowRepository;
import com.fernandoschilder.ipaconsolebackend.repository.ProcessRepository;
import com.fernandoschilder.ipaconsolebackend.utils.ProcessMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcessService {

    private final ProcessRepository processRepository;
    private final WorkflowRepository workflowRepository;
    private final N8nApiService n8nApiService;
    private final N8nWebhookService n8nWebhookService;
    private final ObjectMapper objectMapper;

    @Transactional
    public ProcessResponseDto create(ProcessCreateDto dto) {
        var workflow = workflowRepository.findById(dto.workflowId())
                .orElseThrow(() -> new EntityNotFoundException("Workflow not found (aún no existe en DB o ID incorrecto): " + dto.workflowId()));

        var process = new ProcessEntity();
        process.setName(dto.name());
        process.setDescription(dto.description());
        process.setWorkflow(workflow);

        if (dto.parameters() != null) {
            for (var pd : dto.parameters()) {
                var pe = new ParameterEntity();
                pe.setName(pd.name());
                pe.setValue(pd.value());
                pe.setType(pd.type());
                process.addParameter(pe);           // sets both sides
            }
        }

        var saved = processRepository.save(process); // cascades parameters

        var dtoBase = ProcessMapper.toResponseDto(saved);
        return enrichWithLastExecution(dtoBase);
    }

    @Transactional
    public ResponseEntity<?> start(Long id) {

        ProcessEntity process = processRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Process not found: " + id));

        String workflowId = process.getWorkflow() != null ? process.getWorkflow().getId() : null;
        if (workflowId == null || workflowId.isBlank()) {
            throw new EntityNotFoundException("Process " + id + " has no workflowId");
        }

        // Optional: pass parameters, or null if you want a clean forward
        Map<String, Object> body = Map.of(
                "processId", id,
                "parameters", process.getParameters() == null
                        ? Map.of()
                        : process.getParameters().stream()
                        .collect(Collectors.toMap(
                                ParameterEntity::getName,
                                ParameterEntity::getValue,
                                (a,b) -> b
                        ))
        );

        return n8nWebhookService.postWebhook(workflowId, body);
    }


    @Transactional(readOnly = true)
    public ProcessResponseDto get(Long id) {
        var p = processRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Process not found: " + id));
        var dtoBase = ProcessMapper.toResponseDto(p);
        return enrichWithLastExecution(dtoBase);
    }

    @Transactional(readOnly = true)
    public List<ProcessResponseDto> list() {
        return processRepository.findAll().stream().map(ProcessMapper::toResponseDto).map(this::enrichWithLastExecution).toList();
    }

    @Transactional
    public ProcessResponseDto update(Long processId, ProcessUpdateDto dto) {
        var process = processRepository.findById(processId).orElseThrow(() -> new EntityNotFoundException("Process not found: " + processId));

        if (dto.name() != null && !dto.name().isBlank()) {
            process.setName(dto.name());
        }
        if (dto.description() != null && !dto.description().isBlank()) {
            process.setDescription(dto.description());
        }

        var currentParams = process.getParameters().stream().collect(java.util.stream.Collectors.toMap(ParameterEntity::getId, p -> p));

        for (var pEdit : dto.parameters()) {
            var existing = currentParams.get(pEdit.id());
            if (existing == null) {
                throw new EntityNotFoundException("Parameter " + pEdit.id() + " not found on process " + processId);
            }

            if (pEdit.name() != null && !pEdit.name().isBlank()) {
                existing.setName(pEdit.name());
            }
            if (pEdit.value() != null) {
                existing.setValue(pEdit.value());
            }
            if (pEdit.type() != null && !pEdit.type().isBlank()) {
                existing.setType(pEdit.type());
            }
        }

        var saved = processRepository.save(process);

        var reloaded = processRepository.findById(saved.getId()).orElseThrow(() -> new EntityNotFoundException("Process not found after update: " + saved.getId()));

        var base = ProcessMapper.toResponseDto(reloaded);
        return enrichWithLastExecution(base);
    }

    private ProcessResponseDto enrichWithLastExecution(ProcessResponseDto dto) {
        if (dto == null || dto.workflow() == null || dto.workflow().id() == null) return dto;
        var last = fetchLastExecution(dto.workflow().id());
        return new ProcessResponseDto(dto.id(), dto.name(), dto.description(), dto.workflow(), dto.parameters(), last);
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
            return new ExecutionBriefDto(e.path("id").asLong(), e.path("startedAt").asText(null), e.path("finished").asBoolean(), e.path("status").asText(null));
        } catch (Exception ex) {
            // log.warn("No se pudo parsear executionBrief", ex);
            return null;
        }
    }
}