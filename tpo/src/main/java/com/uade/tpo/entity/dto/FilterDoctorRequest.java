package com.uade.tpo.entity.dto;

import java.time.LocalTime;
import java.util.List;

import com.uade.tpo.entity.enumerations.Specialties;
import com.uade.tpo.entity.enumerations.Weekdays;

import lombok.Data;

@Data
public class FilterDoctorRequest {
    private Specialties specialty;
    private List<Weekdays> weekdays;
    private LocalTime time;
}
