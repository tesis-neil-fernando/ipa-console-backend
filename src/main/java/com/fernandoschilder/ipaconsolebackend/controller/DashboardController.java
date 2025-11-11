package com.fernandoschilder.ipaconsolebackend.controller;

import com.fernandoschilder.ipaconsolebackend.dto.ChartCountDto;
import com.fernandoschilder.ipaconsolebackend.dto.DayCountDto;
import com.fernandoschilder.ipaconsolebackend.dto.ProcessStatusBreakdownDto;
import com.fernandoschilder.ipaconsolebackend.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@Validated
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // 1. Count of processes by active/inactive state (pie chart)
    @GetMapping("/processes-by-active-state")
    public ResponseEntity<List<ChartCountDto>> processesByActiveState() {
        var res = dashboardService.countProcessesByActiveState();
        return ResponseEntity.ok(res);
    }

    // 2. Errors per day in the week (line chart). Query param `days` optional (default 7)
    @GetMapping("/errors-by-day")
    public ResponseEntity<List<DayCountDto>> errorsByDay(@RequestParam(required = false, defaultValue = "7") Integer days) {
        var res = dashboardService.countErrorsPerDayLastNDays(days != null ? days : 7);
        return ResponseEntity.ok(res);
    }

    // 3. Executions per day (column chart). Query param `days` optional (default 7)
    @GetMapping("/executions-by-day")
    public ResponseEntity<List<DayCountDto>> executionsByDay(@RequestParam(required = false, defaultValue = "7") Integer days) {
        var res = dashboardService.countExecutionsPerDayLastNDays(days != null ? days : 7);
        return ResponseEntity.ok(res);
    }

    // 4. Error percentage per process (stacked bar). Query param `days` optional (default 30)
    @GetMapping("/error-per-process")
    public ResponseEntity<List<ProcessStatusBreakdownDto>> errorPerProcess(@RequestParam(required = false, defaultValue = "30") Integer days) {
        var res = dashboardService.errorPercentagePerProcessLastNDays(days != null ? days : 30);
        return ResponseEntity.ok(res);
    }
}
