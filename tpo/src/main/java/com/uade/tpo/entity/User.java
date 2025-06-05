package com.uade.tpo.entity;

import java.util.List;

import com.uade.tpo.entity.enumerations.Company;
import com.uade.tpo.entity.enumerations.Role;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private int DNI;

    @Column
    private String name;

    @Column
    private String email;

    @Column
    private String password;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Company company = Company.PARTICULAR;

    @Column
    private String affiliateNumber;
    
    @OneToMany(mappedBy = "user")
    private List<Notification> notifications;

    @OneToMany (mappedBy =  "user")
    private List<Appointment> appointments;

    @OneToOne (mappedBy =  "user", cascade = CascadeType.PERSIST)
    private TemporaryData temporaryData;
}