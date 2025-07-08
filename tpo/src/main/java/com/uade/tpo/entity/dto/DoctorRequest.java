package com.uade.tpo.entity.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DoctorRequest {
    private Long id;
    private String name;
    private String specialties;
    private String image;
}
