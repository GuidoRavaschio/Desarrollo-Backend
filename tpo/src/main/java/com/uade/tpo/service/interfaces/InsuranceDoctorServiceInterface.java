package com.uade.tpo.service.interfaces;

import java.util.List;

import com.uade.tpo.entity.dto.DoctorRequest;
import com.uade.tpo.entity.dto.InsuranceDoctorRequest;
import com.uade.tpo.entity.enumerations.Company;

public interface InsuranceDoctorServiceInterface {
    public void setInsuranceDoctor(Long doctor_id, Company company);
    public List<InsuranceDoctorRequest> getInsuranceDoctor(DoctorRequest doctorRequest);
    public void editInsuranceDoctor(InsuranceDoctorRequest insuranceDoctorRequest);
    public void deleteInsuranceDoctor(InsuranceDoctorRequest insuranceDoctorRequest);
    public void deleteAllInsuranceDoctor();
}
