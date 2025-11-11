package com.fernandoschilder.ipaconsolebackend.dto;

public record ProcessErrorPercentageDto(Long processId, String processName, long totalExecutions, long errorExecutions, double errorPercentage) {
}
