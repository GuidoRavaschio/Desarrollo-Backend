package com.uade.tpo.entity.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.uade.tpo.entity.enumerations.Specialties;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppointmentData {
    private Long id;
    private String doctor; 
    private LocalDate date;
    private LocalTime time;
    private String image;
    private Specialties specialty;
}
