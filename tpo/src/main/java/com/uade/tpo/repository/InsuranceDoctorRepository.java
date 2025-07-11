package com.uade.tpo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uade.tpo.entity.Doctor;
import com.uade.tpo.entity.InsuranceDoctor;
import com.uade.tpo.entity.enumerations.Company;
import com.uade.tpo.entity.enumerations.Specialties;

@Repository
public interface InsuranceDoctorRepository extends JpaRepository<InsuranceDoctor, Long>{
    @Query("SELECT u FROM InsuranceDoctor u WHERE u.doctor.id = :doctor_id AND u.company = :company")
    Optional<InsuranceDoctor> findByDoctorAndCompany(@Param("doctor_id") Long doctor_id, @Param("company") Company company);

    @Query("SELECT d FROM InsuranceDoctor d WHERE d.doctor.id = :doctor_id")
    List<InsuranceDoctor> findByDoctorId(@Param("doctor_id") Long doctor_id);

    @Query("SELECT d.doctor FROM InsuranceDoctor d WHERE d.doctor IN :doctors AND d.company = :company")
    List<Doctor> findByDoctorsAndCompany(@Param("doctors") List<Doctor> doctors, @Param("company") Company company);

    @Query("""
    SELECT d.doctor
    FROM InsuranceDoctor d
    WHERE d.company IN :companies
    AND (:specialties IS NULL OR d.doctor.specialties = :specialties)
""")
List<Doctor> findByCompaniesAndOptionalSpecialties(
    @Param("companies") List<Company> companies,
    @Param("specialties") Specialties specialties
);
}
