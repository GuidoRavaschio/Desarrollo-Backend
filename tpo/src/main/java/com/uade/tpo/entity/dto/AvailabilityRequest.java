package com.uade.tpo.entity.dto;

import java.time.LocalTime;

import com.uade.tpo.entity.enumerations.Weekdays;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AvailabilityRequest {
    private Long id;
    private Long doctorId;
    private Weekdays weekday;
    private LocalTime startTime;
    private LocalTime endTime;
}
