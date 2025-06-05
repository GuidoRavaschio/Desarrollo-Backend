package com.uade.tpo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uade.tpo.entity.InsuranceDoctor;
import com.uade.tpo.entity.enumerations.Company;

@Repository
public interface InsuranceDoctorRepository extends JpaRepository<InsuranceDoctor, Long>{
    @Query("SELECT u FROM InsuranceDoctor u WHERE u.doctor.id = :doctor_id AND u.company = :company")
    Optional<InsuranceDoctor> findByDoctorAndCompany(@Param("doctor_id") Long doctor_id, @Param("company") Company company);

    @Query("SELECT d FROM InsuranceDoctor d WHERE d.doctor.id = :doctor_id")
    List<InsuranceDoctor> findByDoctorId(@Param("doctor_id") Long doctor_id);
}
