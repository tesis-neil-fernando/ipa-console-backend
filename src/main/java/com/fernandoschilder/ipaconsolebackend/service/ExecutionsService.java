package com.fernandoschilder.ipaconsolebackend.service;

import com.fernandoschilder.ipaconsolebackend.dto.ExecutionResponseDto;
import com.fernandoschilder.ipaconsolebackend.dto.PageResponse;
import com.fernandoschilder.ipaconsolebackend.repository.ExecutionRepository;
import com.fernandoschilder.ipaconsolebackend.repository.ProcessRepository;
import com.fernandoschilder.ipaconsolebackend.repository.UserRepository;
import com.fernandoschilder.ipaconsolebackend.model.PermissionAction;
import com.fernandoschilder.ipaconsolebackend.security.RbacSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class ExecutionsService {

    private static final Logger log = LoggerFactory.getLogger(ExecutionsService.class);

    private final ExecutionRepository executionRepository;
    private final UserRepository userRepository;
    private final ProcessRepository processRepository;
    private final RbacSecurity rbacSecurity;

    public ExecutionsService(ExecutionRepository executionRepository, UserRepository userRepository, ProcessRepository processRepository, RbacSecurity rbacSecurity) {
        this.executionRepository = executionRepository;
        this.userRepository = userRepository;
        this.processRepository = processRepository;
        this.rbacSecurity = rbacSecurity;
    }

    @Transactional(readOnly = true)
    public PageResponse<ExecutionResponseDto> listExecutions(
            Boolean includeData, String status, String workflowId, String projectId,
            Integer limit, String cursor) {

        int pageSize = (limit == null) ? 20 : Math.max(1, limit);
        if (pageSize > 50) {
            pageSize = 50; // enforce maximum page size
        }
        // Order by startedAt (execution time) so newest executions appear first
        Pageable pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "startedAt"));
        java.util.List<com.fernandoschilder.ipaconsolebackend.repository.ExecutionSummary> summaries = java.util.List.of();

        // Determine current user and permissions
        var auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth == null ? null : auth.getName();

        boolean isAdmin = username != null && rbacSecurity.isAdmin(username);

        // If an explicit workflowId is requested, handle it specially (still enforce RBAC)
        if (workflowId != null && !workflowId.isBlank()) {
            if (!isAdmin) {
                // ensure user can view the process related to this workflow
                var procOpt = processRepository.findByWorkflow_Id(workflowId);
                if (procOpt.isEmpty() || !rbacSecurity.canViewProcess(username, procOpt.get().getId())) {
                    // not allowed -> empty result
                    return PageResponse.of(java.util.List.of(), null);
                }
            }

            if (cursor != null) {
                var cursorStartedAtOpt = executionRepository.findStartedAtByExecutionId(cursor);
                if (cursorStartedAtOpt.isPresent()) {
                    summaries = executionRepository.findSummariesByWorkflowIdAndStartedAtBefore(workflowId, cursorStartedAtOpt.get(), pageable);
                } else {
                    throw new IllegalArgumentException("Cursor not found: " + cursor);
                }
            } else {
                summaries = executionRepository.findAllSummariesByWorkflowId(workflowId, pageable);
            }

        } else {
            // No specific workflow requested -> apply namespace RBAC if not admin
            if (isAdmin) {
                if (cursor != null) {
                    var cursorStartedAtOpt = executionRepository.findStartedAtByExecutionId(cursor);
                    if (cursorStartedAtOpt.isPresent()) {
                        summaries = executionRepository.findSummariesByStartedAtBefore(cursorStartedAtOpt.get(), pageable);
                    } else {
                        throw new IllegalArgumentException("Cursor not found: " + cursor);
                    }
                } else {
                    summaries = executionRepository.findAllSummaries(pageable);
                }
            } else {
                if (username == null) return PageResponse.of(java.util.List.of(), null);
                var allowedNs = userRepository.findNamespaceIdsByUsernameAndAction(username, PermissionAction.VIEW);
                boolean hasGlobal = userRepository.userHasGlobalPermission(username, PermissionAction.VIEW);

                if (hasGlobal) {
                    // user has global view permission -> same as admin
                    if (cursor != null) {
                        var cursorStartedAtOpt = executionRepository.findStartedAtByExecutionId(cursor);
                        if (cursorStartedAtOpt.isPresent()) {
                            summaries = executionRepository.findSummariesByStartedAtBefore(cursorStartedAtOpt.get(), pageable);
                        } else {
                            throw new IllegalArgumentException("Cursor not found: " + cursor);
                        }
                    } else {
                        summaries = executionRepository.findAllSummaries(pageable);
                    }
                } else {
                    if (allowedNs == null || allowedNs.isEmpty()) {
                        return PageResponse.of(java.util.List.of(), null);
                    }

                    // Load processes in allowed namespaces and use their workflowIds to filter executions
                    var procs = processRepository.findAllByNamespace_IdIn(allowedNs);
                    var wfIds = procs.stream()
                            .map(p -> p.getWorkflow() == null ? null : p.getWorkflow().getId())
                            .filter(java.util.Objects::nonNull)
                            .collect(java.util.stream.Collectors.toSet());

                    if (wfIds.isEmpty()) {
                        return PageResponse.of(java.util.List.of(), null);
                    }

                    if (cursor != null) {
                        var cursorStartedAtOpt = executionRepository.findStartedAtByExecutionId(cursor);
                        if (cursorStartedAtOpt.isPresent()) {
                            summaries = executionRepository.findSummariesByStartedAtBeforeAndWorkflowIds(cursorStartedAtOpt.get(), wfIds, pageable);
                        } else {
                            throw new IllegalArgumentException("Cursor not found: " + cursor);
                        }
                    } else {
                        summaries = executionRepository.findAllSummariesByWorkflowIds(wfIds, pageable);
                    }
                }
            }
        }

        // Build a map workflowId -> ProcessEntity for resolving current process name/id efficiently
        var workflowIds = summaries.stream()
                .map(com.fernandoschilder.ipaconsolebackend.repository.ExecutionSummary::getWorkflowId)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());

        java.util.Map<String, com.fernandoschilder.ipaconsolebackend.model.ProcessEntity> wfToProcess = new java.util.HashMap<>();
        if (!workflowIds.isEmpty()) {
            var procList = processRepository.findAllByWorkflow_IdIn(workflowIds);
            for (var p : procList) {
                if (p.getWorkflow() != null && p.getWorkflow().getId() != null) {
                    wfToProcess.put(p.getWorkflow().getId(), p);
                }
            }
        }

        var mapped = summaries.stream()
                .map(e -> {
                    var proc = e.getWorkflowId() == null ? null : wfToProcess.get(e.getWorkflowId());
                    Long procId = proc == null ? null : proc.getId();
                    String procName = proc == null ? null : proc.getName();
                    return new ExecutionResponseDto(
                            e.getExecutionId(),
                            e.getStartedAt(),
                            e.getStoppedAt(),
                            procId,
                            procName,
                            e.getWorkflowId(),
                            e.getStatus(),
                            e.getFinished()
                    );
                })
                .toList();

        String nextCursor = null;
        if (summaries.size() == pageSize) {
            nextCursor = summaries.get(summaries.size() - 1).getExecutionId();
        }

        return PageResponse.of(mapped, nextCursor);
    }
}

