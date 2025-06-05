package com.uade.tpo.entity.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppointmentRequest {
    private Long id;
    private Long doctorId; 
    private Long userId;
    private LocalDate date;
    private LocalTime time;
    private String image;
}
