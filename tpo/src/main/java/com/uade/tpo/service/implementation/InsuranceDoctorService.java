package com.uade.tpo.service.implementation;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.entity.Doctor;
import com.uade.tpo.entity.InsuranceDoctor;
import com.uade.tpo.entity.dto.DoctorRequest;
import com.uade.tpo.entity.dto.InsuranceDoctorRequest;
import com.uade.tpo.entity.enumerations.Company;
import com.uade.tpo.repository.DoctorRepository;
import com.uade.tpo.repository.InsuranceDoctorRepository;
import com.uade.tpo.service.interfaces.InsuranceDoctorServiceInterface;

@Service
public class InsuranceDoctorService implements InsuranceDoctorServiceInterface{

    @Autowired
    private InsuranceDoctorRepository insuranceDoctorRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Override
    public void setInsuranceDoctor(Long doctor_id, Company company) {
        Doctor doctor = doctorRepository.findById(doctor_id)
        .orElseThrow(() -> new RuntimeException("Doctor no existe"));
        insuranceDoctorRepository.findByDoctorAndCompany(doctor_id, company)
        .orElse(create(doctor, company));
    }

    @Override
    public List<InsuranceDoctorRequest> getInsuranceDoctor(DoctorRequest doctorRequest) {
        return insuranceDoctorRepository.findByDoctorId(doctorRequest.getId())
            .stream()
            .map(ins -> InsuranceDoctorRequest.builder()
                    .id(ins.getId())
                    .doctorId(ins.getDoctor().getId())
                    .company(ins.getCompany().name())
                    .build())
            .collect(Collectors.toList());
}


    @Override
public void editInsuranceDoctor(InsuranceDoctorRequest insuranceDoctorRequest) {
    InsuranceDoctor ins = insuranceDoctorRepository
        .findById(insuranceDoctorRequest.getId())
        .orElseThrow(() -> new RuntimeException("InsuranceDoctor no encontrado"));
    // Solo cambia la compañía
    ins.setCompany(Company.valueOf(insuranceDoctorRequest.getCompany().toUpperCase()));
    insuranceDoctorRepository.save(ins);
}

@Override
public void deleteInsuranceDoctor(InsuranceDoctorRequest insuranceDoctorRequest) {
    insuranceDoctorRepository.deleteById(insuranceDoctorRequest.getId());
}

    
    private InsuranceDoctor create(Doctor doctor, Company company){
        InsuranceDoctor insurance = new InsuranceDoctor();
        insurance.setCompany(company);
        insurance.setDoctor(doctor);
        insuranceDoctorRepository.save(insurance);
        return insurance;
    }

    @Override
    public void deleteAllInsuranceDoctor() {
        insuranceDoctorRepository.deleteAll();
    }
}
