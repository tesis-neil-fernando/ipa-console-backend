package com.fernandoschilder.ipaconsolebackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernandoschilder.ipaconsolebackend.dto.ExecutionBriefDto;
import com.fernandoschilder.ipaconsolebackend.dto.ProcessCreateDto;
import com.fernandoschilder.ipaconsolebackend.dto.ProcessResponseDto;
import com.fernandoschilder.ipaconsolebackend.dto.ProcessUpdateDto;
import com.fernandoschilder.ipaconsolebackend.model.ParameterEntity;
import com.fernandoschilder.ipaconsolebackend.model.ProcessEntity;
import com.fernandoschilder.ipaconsolebackend.model.WorkflowEntity;
import com.fernandoschilder.ipaconsolebackend.repository.WorkflowRepository;
import com.fernandoschilder.ipaconsolebackend.repository.ProcessRepository;
import com.fernandoschilder.ipaconsolebackend.mapper.ProcessMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.core.context.SecurityContextHolder;
import com.fernandoschilder.ipaconsolebackend.repository.UserRepository;

@Service
public class ProcessService {

    private final ProcessRepository processRepository;
    private final WorkflowRepository workflowRepository;
    private final UserRepository userRepository;
    private final N8nApiService n8nApiService;
    private final N8nWebhookService n8nWebhookService;
    private final ProcessMapper processMapper;

    public ProcessService(ProcessRepository processRepository, WorkflowRepository workflowRepository, UserRepository userRepository, N8nApiService n8nApiService, N8nWebhookService n8nWebhookService, ObjectMapper objectMapper, ProcessMapper processMapper) {
        this.processRepository = processRepository;
        this.workflowRepository = workflowRepository;
        this.userRepository = userRepository;
        this.n8nApiService = n8nApiService;
        this.n8nWebhookService = n8nWebhookService;
        this.processMapper = processMapper;
    }

    @Transactional
    public ProcessEntity ensureProcessForWorkflow(WorkflowEntity workflow, String name, String description) {
        var existing = processRepository.findByWorkflow_Id(workflow.getId());
        if (existing.isPresent()) return existing.get();

        var p = new ProcessEntity();
        p.setName(name);
        p.setDescription(description);
        p.setWorkflow(workflow);
        workflow.setProcess(p);
        return processRepository.saveAndFlush(p);
    }

    @Transactional
    public ProcessResponseDto create(ProcessCreateDto dto) {
        var workflow = workflowRepository.findById(/* or findByIdForUpdate */ dto.workflowId())
                .orElseThrow(() -> new EntityNotFoundException("Workflow not found: " + dto.workflowId()));

        if (processRepository.existsByWorkflow_Id(workflow.getId())) {
            throw new jakarta.persistence.EntityExistsException("Workflow " + workflow.getId() + " already has a process");
        }

        var process = new ProcessEntity();
        process.setName(dto.name());
        process.setDescription(dto.description());
        // set both sides to keep the relation consistent
        process.setWorkflow(workflow);
        workflow.setProcess(process);

        if (dto.parameters() != null) {
            for (var pd : dto.parameters()) {
                var pe = new ParameterEntity();
                pe.setName(pd.name());
                pe.setValue(pd.value());
                pe.setType(pd.type());
                process.addParameter(pe);
            }
        }

        var saved = processRepository.saveAndFlush(process);
    var dtoBase = processMapper.toResponseDto(saved);
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

        // Use the low-level throwing API so failures are surfaced as exceptions
        // and handled by the existing GlobalExceptionHandler in the controller layer.
        String response = n8nWebhookService.postWebhookRaw(workflowId, body);
        return ResponseEntity.ok(new N8nApiService.ApiResponse<>(true, "Webhook ejecutado", response));
    }


    @Transactional(readOnly = true)
    public ProcessResponseDto get(Long id) {
        var p = processRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Process not found: " + id));
    var dtoBase = processMapper.toResponseDto(p);
        return enrichWithLastExecution(dtoBase);
    }

    @Transactional(readOnly = true)
    public List<ProcessResponseDto> list() {
        return processRepository.findAll().stream().map(processMapper::toResponseDto).map(this::enrichWithLastExecution).toList();
    }

    @Transactional(readOnly = true)
    public List<ProcessResponseDto> list(String tags) {
        return list(tags, null, null);
    }

    @Transactional(readOnly = true)
    public List<ProcessResponseDto> list(String tags, Boolean active, Boolean archived) {
        List<ProcessResponseDto> base;

        if (tags == null || tags.isBlank()) {
            base = list();
        } else {
            var tagNames = Arrays.stream(tags.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            if (tagNames.isEmpty()) {
                base = list();
            } else {
                base = processRepository.findDistinctByWorkflow_Tags_NameIn(tagNames)
                        .stream()
                        .map(processMapper::toResponseDto)
                        .map(this::enrichWithLastExecution)
                        .toList();
            }
        }

        // apply active/archived filters on the DTO's workflow fields
        return base.stream()
                .filter(p -> {
                    var wf = p.workflow();
                    if (wf == null) return true;
                    if (active != null && wf.active() != active) return false;
                    if (archived != null && wf.archived() != archived) return false;
                    return true;
                })
                .toList();
    }

    /**
     * List processes for the current authenticated user, scoping by namespaces where the user
     * has the given permission type (e.g., "view"). This uses `UserRepository.findNamespaceIdsByUsernameAndPermissionType`.
     */
    @Transactional(readOnly = true)
    public List<ProcessResponseDto> listForCurrentUser(String tags, Boolean active, Boolean archived, String permissionType) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return List.of();

        String username = auth.getName();
        List<Long> allowedNamespaceIds = userRepository.findNamespaceIdsByUsernameAndPermissionType(username, permissionType);
        if (allowedNamespaceIds == null || allowedNamespaceIds.isEmpty()) return List.of();

        List<ProcessEntity> procEntities;
        if (tags == null || tags.isBlank()) {
            procEntities = processRepository.findAllByNamespace_IdIn(allowedNamespaceIds);
        } else {
            var tagNames = Arrays.stream(tags.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

            if (tagNames.isEmpty()) {
                procEntities = processRepository.findAllByNamespace_IdIn(allowedNamespaceIds);
            } else {
                // fetch by tags then filter by namespace id
                var byTags = processRepository.findDistinctByWorkflow_Tags_NameIn(tagNames);
                procEntities = byTags.stream()
                        .filter(p -> p.getNamespace() != null && allowedNamespaceIds.contains(p.getNamespace().getId()))
                        .toList();
            }
        }

        var base = procEntities.stream().map(processMapper::toResponseDto).map(this::enrichWithLastExecution).toList();

        return base.stream()
                .filter(p -> {
                    var wf = p.workflow();
                    if (wf == null) return true;
                    if (active != null && wf.active() != active) return false;
                    if (archived != null && wf.archived() != archived) return false;
                    return true;
                })
                .toList();
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

        if (dto.parameters() != null) {
            var currentParams = process.getParameters().stream().collect(java.util.stream.Collectors.toMap(ParameterEntity::getId, p -> p));

            for (var pEdit : dto.parameters()) {
                // If id is null -> create a new parameter on the process
                if (pEdit.id() == null) {
                    var pe = new ParameterEntity();
                    if (pEdit.name() != null && !pEdit.name().isBlank()) pe.setName(pEdit.name());
                    if (pEdit.value() != null) pe.setValue(pEdit.value());
                    if (pEdit.type() != null && !pEdit.type().isBlank()) pe.setType(pEdit.type());
                    process.addParameter(pe);
                    continue;
                }

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
        }

        var saved = processRepository.save(process);

        var reloaded = processRepository.findById(saved.getId()).orElseThrow(() -> new EntityNotFoundException("Process not found after update: " + saved.getId()));

    var base = processMapper.toResponseDto(reloaded);
        return enrichWithLastExecution(base);
    }

    private ProcessResponseDto enrichWithLastExecution(ProcessResponseDto dto) {
        if (dto == null || dto.workflow() == null || dto.workflow().id() == null) return dto;
        var last = fetchLastExecution(dto.workflow().id());
        return new ProcessResponseDto(dto.id(), dto.name(), dto.description(), dto.workflow(), dto.parameters(), last);
    }

    private ExecutionBriefDto fetchLastExecution(String workflowId) {
        try {
            var envelope = n8nApiService.fetchExecutions(null, null, workflowId, null, 1, null);
            if (envelope == null || envelope.data() == null || envelope.data().isEmpty()) return null;

            var e = envelope.data().get(0); // asumimos más reciente primero
            return new ExecutionBriefDto(e.id(), e.startedAt(), e.finished() == null ? false : e.finished(), e.status());
        } catch (N8nClientException ex) {
            // log.warn("No se pudo obtener executionBrief desde n8n", ex);
            return null;
        }
    }
}