package com.fernandoschilder.ipaconsolebackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fernandoschilder.ipaconsolebackend.dto.ExecutionResponseDto;
import com.fernandoschilder.ipaconsolebackend.dto.ExecutionsListResponseDto;
import com.fernandoschilder.ipaconsolebackend.model.ExecutionEntity;
import com.fernandoschilder.ipaconsolebackend.model.ProcessEntity;
import com.fernandoschilder.ipaconsolebackend.repository.ExecutionRepository;
import com.fernandoschilder.ipaconsolebackend.repository.ProcessRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;


@Service
public class ExecutionsService {

    private static final Logger log = LoggerFactory.getLogger(ExecutionsService.class);

    private final N8nApiService n8nApiService;
    private final ProcessRepository processRepository;
    private final ExecutionRepository executionRepository;

    public ExecutionsService(N8nApiService n8nApiService, ProcessRepository processRepository, ExecutionRepository executionRepository, ObjectMapper objectMapper) {
        this.n8nApiService = n8nApiService;
        this.processRepository = processRepository;
        this.executionRepository = executionRepository;
    }

    public ExecutionsListResponseDto listExecutions(
            Boolean includeData, String status, String workflowId, String projectId,
            Integer limit, String cursor) {

        // If we have local executions stored, serve from DB with simple cursor semantics.
        long localCount = executionRepository.count();
        int pageSize = (limit == null) ? 100 : Math.max(1, limit);

        if (localCount > 0) {
            Pageable pageable = PageRequest.of(0, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
            java.util.List<com.fernandoschilder.ipaconsolebackend.repository.ExecutionSummary> summaries;

            if (cursor != null) {
                var cursorCreatedAtOpt = executionRepository.findCreatedAtByExecutionId(cursor);
                if (cursorCreatedAtOpt.isPresent()) {
                    summaries = executionRepository.findSummariesByCreatedAtBefore(cursorCreatedAtOpt.get(), pageable);
                } else {
                    // Strict mode: if the cursor is not present in our table, reject the request
                    throw new IllegalArgumentException("Cursor not found: " + cursor);
                }
            } else {
                summaries = executionRepository.findAllSummaries(pageable);
            }

            var mapped = summaries.stream()
                    .map(e -> new ExecutionResponseDto(
                            e.getExecutionId(),
                            e.getStartedAt(),
                            e.getStoppedAt(),
                            e.getProcessName(),
                            e.getStatus(),
                            e.getFinished()
                    ))
                    .toList();

            String nextCursor = null;
            if (summaries.size() == pageSize) {
                nextCursor = summaries.get(summaries.size() - 1).getExecutionId();
            }

            return new ExecutionsListResponseDto(mapped, nextCursor);
        }

    var envelope = n8nApiService.fetchExecutions(includeData, status, workflowId, projectId, limit, null);

    var mapped = envelope.data().stream()
                .map(e -> {
                    String name = processRepository.findByWorkflow_Id(e.workflowId())
                            .map(ProcessEntity::getName)
                            .orElse(null); // one-to-one => unique or null if not registered yet
                    return new ExecutionResponseDto(
                            e.id(),
                            parseTs(e.startedAt()),
                            parseTs(e.stoppedAt()),
                            name,
                            e.status(),
                            e.finished()
                    );
                })
                .toList();

    String nextCursor = null;
    if (!mapped.isEmpty()) nextCursor = mapped.get(mapped.size() - 1).id();

    return new ExecutionsListResponseDto(mapped, nextCursor);
    }

    private static OffsetDateTime parseTs(String iso) {
        if (iso == null) return null;
        try {
            return OffsetDateTime.parse(iso);
        } catch (java.time.format.DateTimeParseException ex) {
            log.debug("Failed to parse timestamp '{}'", iso, ex);
            return null;
        }
    }

    // parsing is delegated to N8nApiService.getExecutionsParsed
}

