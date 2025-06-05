package com.uade.tpo.entity.dto;

import com.uade.tpo.entity.enumerations.Company;

import lombok.Data;

@Data
public class UserRequest {
    private Long id;
    private int DNI;
    private String name;
    private String email;
    private String password;
    private String confirm_password;
    private String role;
    private Company company;
    private String affiliateNumber;
}
