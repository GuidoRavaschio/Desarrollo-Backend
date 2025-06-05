package com.uade.tpo.entity.dto;


import com.uade.tpo.entity.enumerations.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private int DNI;
    private String name;
    private String email;
    private String password;
    private String confirm_password;
    private Role role;
}
