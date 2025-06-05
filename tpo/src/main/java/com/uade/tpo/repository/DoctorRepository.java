package com.uade.tpo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uade.tpo.entity.Doctor;
import com.uade.tpo.entity.enumerations.Specialties;


@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long>{
    @Query("SELECT d FROM Doctor d WHERE :specialties IS NULL OR d.specialties = :specialties")
    List<Doctor> filterBySpecialties(@Param("specialties") Specialties specialties);

    @Query("SELECT d FROM Doctor d WHERE d.id IN :doctors")
    List<Doctor> findDoctors(@Param("doctors") List<Long> doctors);

    List<Doctor> findByNameContaining(String name);
}
