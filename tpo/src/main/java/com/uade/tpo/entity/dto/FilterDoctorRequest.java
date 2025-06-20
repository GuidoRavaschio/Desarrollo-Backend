package com.uade.tpo.entity.dto;

import java.time.LocalTime;
import java.util.List;

import com.uade.tpo.entity.enumerations.Company;
import com.uade.tpo.entity.enumerations.Specialties;

import lombok.Data;

@Data
public class FilterDoctorRequest {
    private Specialties specialty;
    private List<Company> companies;
    private LocalTime time;
}
