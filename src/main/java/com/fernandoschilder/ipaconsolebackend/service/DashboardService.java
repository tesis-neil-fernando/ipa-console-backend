package com.fernandoschilder.ipaconsolebackend.service;

import com.fernandoschilder.ipaconsolebackend.dto.ChartCountDto;
import com.fernandoschilder.ipaconsolebackend.dto.DayCountDto;
import com.fernandoschilder.ipaconsolebackend.dto.ProcessErrorPercentageDto;
import com.fernandoschilder.ipaconsolebackend.dto.ProcessStatusBreakdownDto;
import com.fernandoschilder.ipaconsolebackend.repository.ExecutionSummary;
import com.fernandoschilder.ipaconsolebackend.model.ProcessEntity;
import com.fernandoschilder.ipaconsolebackend.repository.ExecutionRepository;
import com.fernandoschilder.ipaconsolebackend.repository.ProcessRepository;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final ProcessRepository processRepository;
    private final ExecutionRepository executionRepository;

    public DashboardService(ProcessRepository processRepository, ExecutionRepository executionRepository) {
        this.processRepository = processRepository;
        this.executionRepository = executionRepository;
    }

    /**
     * Returns counts of processes grouped by workflow active state ("active"/"inactive").
     */
    public List<ChartCountDto> countProcessesByActiveState() {
        var procs = processRepository.findAll();
        Map<String, Long> m = procs.stream()
                .collect(Collectors.groupingBy(p -> {
                    var wf = p.getWorkflow();
                    boolean active = wf != null && wf.isActive();
                    return active ? "active" : "inactive";
                }, Collectors.counting()));

        return m.entrySet().stream().map(e -> new ChartCountDto(e.getKey(), e.getValue())).toList();
    }

    /**
     * Count errors per day for the last `days` days (including today). "error" is matched case-insensitively
     * against the execution.status field.
     */
    public List<DayCountDto> countErrorsPerDayLastNDays(int days) {
        if (days <= 0) days = 7;
        Instant now = Instant.now();
        Instant start = now.minus(Duration.ofDays(days - 1)).truncatedTo(ChronoUnit.DAYS);

    var executions = executionRepository.findSummariesByStartedAtBetween(start, now.plusSeconds(1));

        ZoneId zone = ZoneOffset.UTC; // use UTC for consistent day buckets

    Map<LocalDate, Long> counts = executions.stream()
        .filter(e -> e.getStatus() != null && "error".equalsIgnoreCase(e.getStatus()))
        .map(e -> LocalDate.ofInstant(e.getStartedAt(), zone))
        .collect(Collectors.groupingBy(d -> d, Collectors.counting()));

        // build list for each day in range in chronological order
        List<DayCountDto> out = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate day = LocalDate.now(zone).minusDays(days - 1 - i);
            long c = counts.getOrDefault(day, 0L);
            out.add(new DayCountDto(day, c));
        }
        return out;
    }

    /**
     * Count executions per day for the last `days` days (including today).
     */
    public List<DayCountDto> countExecutionsPerDayLastNDays(int days) {
        if (days <= 0) days = 7;
        Instant now = Instant.now();
        Instant start = now.minus(Duration.ofDays(days - 1)).truncatedTo(ChronoUnit.DAYS);

    var executions = executionRepository.findSummariesByStartedAtBetween(start, now.plusSeconds(1));

        ZoneId zone = ZoneOffset.UTC;

    Map<LocalDate, Long> counts = executions.stream()
        .filter(e -> e.getStartedAt() != null)
        .map(e -> LocalDate.ofInstant(e.getStartedAt(), zone))
        .collect(Collectors.groupingBy(d -> d, Collectors.counting()));

        List<DayCountDto> out = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate day = LocalDate.now(zone).minusDays(days - 1 - i);
            long c = counts.getOrDefault(day, 0L);
            out.add(new DayCountDto(day, c));
        }
        return out;
    }

    /**
     * For the last `days` days returns for each process: total executions and counts per status (suitable for stacked bar charts).
     */
    public List<ProcessStatusBreakdownDto> errorPercentagePerProcessLastNDays(int days) {
        if (days <= 0) days = 30;
        Instant now = Instant.now();
        Instant start = now.minus(Duration.ofDays(days));

        var executions = executionRepository.findSummariesByStartedAtBetween(start, now.plusSeconds(1));

        // group by workflowId
        Map<String, List<ExecutionSummary>> byWf = executions.stream()
                .filter(e -> e.getWorkflowId() != null)
                .collect(Collectors.groupingBy(ExecutionSummary::getWorkflowId));

        var wfIds = byWf.keySet();

        Map<String, ProcessEntity> wfToProcess = Collections.emptyMap();
        if (!wfIds.isEmpty()) {
            var processes = processRepository.findAllByWorkflow_IdIn(wfIds);
            wfToProcess = processes.stream().filter(Objects::nonNull).collect(Collectors.toMap(p -> p.getWorkflow().getId(), p -> p));
        }

        List<ProcessStatusBreakdownDto> result = new ArrayList<>();

        // Known statuses to normalize keys in reports
        List<String> knownStatuses = List.of("canceled", "error", "running", "success", "waiting");

        for (var entry : byWf.entrySet()) {
            String wfId = entry.getKey();
            var list = entry.getValue();
            long total = list.size();

            Map<String, Long> statusCounts = list.stream()
                    .map(e -> e.getStatus() == null ? "unknown" : e.getStatus().toLowerCase())
                    .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

            // ensure all known statuses are present with zero defaults
            for (var ks : knownStatuses) {
                statusCounts.putIfAbsent(ks, 0L);
            }

            long errors = statusCounts.getOrDefault("error", 0L);

            ProcessEntity p = wfToProcess.get(wfId);
            Long pid = p == null ? null : p.getId();
            String pname = p == null ? wfId : p.getName();

            double perc = total == 0 ? 0.0 : (100.0 * errors) / (double) total;
            result.add(new ProcessStatusBreakdownDto(pid, pname, total, statusCounts, perc));
        }

        // Include processes with zero executions in the period
        var allProcs = processRepository.findAll();
        for (var p : allProcs) {
            String wfId = p.getWorkflow() == null ? null : p.getWorkflow().getId();
            if (wfId == null) continue;
            if (!byWf.containsKey(wfId)) {
                Map<String, Long> zeros = new HashMap<>();
                for (var ks : List.of("canceled", "error", "running", "success", "waiting")) zeros.put(ks, 0L);
                result.add(new ProcessStatusBreakdownDto(p.getId(), p.getName(), 0L, zeros, 0.0));
            }
        }

        // sort by error percentage descending
        result.sort(Comparator.comparingDouble(ProcessStatusBreakdownDto::errorPercentage).reversed());

        return result;
    }
}
