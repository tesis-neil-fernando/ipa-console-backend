package com.fernandoschilder.ipaconsolebackend.service;

import com.fernandoschilder.ipaconsolebackend.dto.ExecutionResponseDto;
import com.fernandoschilder.ipaconsolebackend.dto.PageResponse;
import com.fernandoschilder.ipaconsolebackend.repository.ExecutionRepository;
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

    public ExecutionsService(ExecutionRepository executionRepository) {
        this.executionRepository = executionRepository;
    }

    public PageResponse<ExecutionResponseDto> listExecutions(
            Boolean includeData, String status, String workflowId, String projectId,
            Integer limit, String cursor) {

        int pageSize = (limit == null) ? 20 : Math.max(1, limit);
        if (pageSize > 50) {
            pageSize = 50; // enforce maximum page size
        }

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

        return PageResponse.of(mapped, nextCursor);
    }
}

