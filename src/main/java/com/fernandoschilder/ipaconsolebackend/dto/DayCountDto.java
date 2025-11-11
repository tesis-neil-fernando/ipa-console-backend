package com.fernandoschilder.ipaconsolebackend.dto;

import java.time.LocalDate;

public record DayCountDto(LocalDate day, long count) {
}
