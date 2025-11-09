package com.fernandoschilder.ipaconsolebackend.service;

import com.fernandoschilder.ipaconsolebackend.model.ParameterEntity;
import com.fernandoschilder.ipaconsolebackend.model.ProcessEntity;
import com.fernandoschilder.ipaconsolebackend.repository.ProcessRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Dynamically schedules jobs for Processes whose Workflow has the tag "scheduled".
 * Each job triggers the Process via ProcessService.start at the cron specified in the
 * "Programación" parameter (stored as String with type "cron").
 */
@Service
public class WorkflowSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowSchedulerService.class);

    private final ProcessRepository processRepository;
    private final ProcessService processService;

    // In-memory scheduler and bookkeeping
    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final Map<Long, String> cronByProcess = new ConcurrentHashMap<>();

    public WorkflowSchedulerService(ProcessRepository processRepository, ProcessService processService) {
        this.processRepository = processRepository;
        this.processService = processService;
    }

    @PostConstruct
    public void init() {
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("wf-scheduler-");
        scheduler.initialize();
        // Initialize schedules on startup
        refreshSchedules();
    }

    @PreDestroy
    public void shutdown() {
        // Cancel all tasks and shutdown scheduler
        for (var f : scheduledTasks.values()) {
            try { if (f != null) f.cancel(false); } catch (Exception ignored) {}
        }
        try { scheduler.shutdown(); } catch (Exception ignored) {}
        scheduledTasks.clear();
        cronByProcess.clear();
    }

    /**
     * Refresh all dynamic schedules based on current DB state.
     * - Schedules processes whose workflow has tag "scheduled" and have a valid "Programación" cron parameter.
     * - Cancels schedules for processes that no longer match or whose cron was removed.
     */
    @Transactional
    public synchronized void refreshSchedules() {
        List<ProcessEntity> candidates = processRepository.findDistinctByWorkflow_Tags_NameIn(List.of("scheduled"));
        Set<Long> toKeep = new HashSet<>();

        for (ProcessEntity p : candidates) {
            if (p == null || p.getId() == null) continue;
            // Ensure default parameters exist on startup/update: Programación (cron) and Programado (enabled flag)
            boolean hasCron = false;
            boolean hasEnabled = false;
            if (p.getParameters() != null) {
                for (ParameterEntity pe : p.getParameters()) {
                    if (pe == null || pe.getName() == null) continue;
                    if ("Programación".equalsIgnoreCase(pe.getName())) hasCron = true;
                    if ("Programado".equalsIgnoreCase(pe.getName())) hasEnabled = true;
                }
            }
            boolean needSave = false;
            if (!hasCron) {
                var pCron = new ParameterEntity();
                pCron.setName("Programación");
                pCron.setValue("0 * * * * *");
                pCron.setType("cron");
                p.addParameter(pCron);
                needSave = true;
            }
            if (!hasEnabled) {
                var pEnabled = new ParameterEntity();
                pEnabled.setName("Programado");
                pEnabled.setValue("false");
                pEnabled.setType("boolean");
                p.addParameter(pEnabled);
                needSave = true;
            }
            if (needSave) {
                try {
                    processRepository.save(p);
                } catch (Exception ex) {
                    log.warn("Failed to persist default scheduling parameters for process {}: {}", p.getId(), ex.getMessage());
                }
            }
            // Only schedule if the explicit enabled flag is present and true
            boolean enabled = isSchedulingEnabled(p);
            if (!enabled) {
                cancelIfScheduled(p.getId());
                continue;
            }

            String cron = extractCron(p);
            if (cron == null || cron.isBlank()) {
                // No cron -> ensure it's not scheduled anymore
                cancelIfScheduled(p.getId());
                continue;
            }

            String current = cronByProcess.get(p.getId());
            if (current == null || !current.equals(cron) || !scheduledTasks.containsKey(p.getId())) {
                // (Re)Schedule with new cron
                cancelIfScheduled(p.getId());
                try {
                    ScheduledFuture<?> future = scheduler.schedule(
                            () -> safeStart(p.getId()),
                            new CronTrigger(cron)
                    );
                    if (future != null) {
                        scheduledTasks.put(p.getId(), future);
                        cronByProcess.put(p.getId(), cron);
                        log.info("Scheduled process {} with cron '{}'", p.getId(), cron);
                    }
                } catch (IllegalArgumentException iae) {
                    log.warn("Invalid cron '{}' for process {}. Skipping schedule.", cron, p.getId());
                } catch (Exception ex) {
                    log.warn("Failed to schedule process {}: {}", p.getId(), ex.getMessage());
                }
            }
            toKeep.add(p.getId());
        }

        // Cancel schedules that no longer apply
        for (Long pid : List.copyOf(scheduledTasks.keySet())) {
            if (!toKeep.contains(pid)) {
                cancelIfScheduled(pid);
            }
        }
    }

    private String extractCron(ProcessEntity p) {
        if (p.getParameters() == null) return null;
        for (ParameterEntity param : p.getParameters()) {
            if (param != null && param.getName() != null && "Programación".equalsIgnoreCase(param.getName())) {
                String v = param.getValue();
                if (v == null) return null;
                String trimmed = v.trim();
                if (trimmed.isEmpty()) return null;
                String low = trimmed.toLowerCase();
                // treat some sentinel values as explicit disabled
                if (low.equals("off") || low.equals("disabled") || low.equals("none") || low.equals("false")) return null;
                return trimmed;
            }
        }
        return null;
    }

    private boolean isSchedulingEnabled(ProcessEntity p) {
        if (p.getParameters() == null) return false;
        for (ParameterEntity param : p.getParameters()) {
            if (param != null && param.getName() != null && "Programado".equalsIgnoreCase(param.getName())) {
                String v = param.getValue();
                if (v == null) return false;
                return v.trim().equalsIgnoreCase("true");
            }
        }
        return false;
    }

    private void cancelIfScheduled(Long processId) {
        var f = scheduledTasks.remove(processId);
        cronByProcess.remove(processId);
        if (f != null) {
            try { f.cancel(false); } catch (Exception ignored) {}
            log.info("Cancelled schedule for process {}", processId);
        }
    }

    private void safeStart(Long processId) {
        try {
            processService.start(processId);
        } catch (Exception ex) {
            log.warn("Scheduled execution failed for process {}: {}", processId, ex.getMessage());
        }
    }
}
