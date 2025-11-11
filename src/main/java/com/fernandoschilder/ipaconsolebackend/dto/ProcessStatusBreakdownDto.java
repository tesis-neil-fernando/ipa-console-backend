package com.fernandoschilder.ipaconsolebackend.dto;

import java.util.Map;

public record ProcessStatusBreakdownDto(Long processId, String processName, long totalExecutions, Map<String, Long> statusCounts, double errorPercentage) {
}
