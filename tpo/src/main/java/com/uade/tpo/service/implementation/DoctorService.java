package com.uade.tpo.service.implementation;

import java.security.SecureRandom;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.entity.Doctor;
import com.uade.tpo.entity.dto.DoctorRequest;
import com.uade.tpo.entity.dto.FilterDoctorRequest;
import com.uade.tpo.entity.enumerations.Company;
import com.uade.tpo.entity.enumerations.Specialties;
import com.uade.tpo.repository.DoctorRepository;
import com.uade.tpo.service.interfaces.DoctorServiceInterface;

@Service
public class DoctorService implements DoctorServiceInterface{

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private InsuranceDoctorService insuranceDoctorService;

    @Override
    public void createDoctors() {
        SecureRandom random = new SecureRandom();
        List<String> names = List.of("Dr. Emilio", "Dr. Carlos", "Dra. Sofía", "Dra. Lucía", "Dr. Pedro");
        List<String> surnames = List.of("Pérez", "García", "López", "Martínez", "Fernández");
        List<Specialties> specialties = List.of(Specialties.values());
        List<Company> companies = List.of(Company.values());
        for (int i = 0; i < 10; i++) {
            String name = randomChoice(names, random) + " " + randomChoice(surnames, random);
            Specialties specialty = randomChoice(specialties, random);
            Doctor doctor = Doctor.builder().name(name).specialties(specialty).build();
            doctorRepository.save(doctor);
            int iterations = random.nextInt(companies.size());
            for (int j = 0; j < iterations; j++){
                insuranceDoctorService.setInsuranceDoctor(doctor.getId(), randomChoice(companies, random));
            }
        }
    }

    private <T> T randomChoice(List<T> list, SecureRandom rnd) {
        return list.get(rnd.nextInt(list.size()));
    }

    @Override
    public List<DoctorRequest> searchDoctor(String name) {
        return mapToRequest(doctorRepository.findByNameContaining(name));
    }


    @Override
    public Doctor getDoctor(Long doctor_id) {
        return doctorRepository.findById(doctor_id).orElseThrow(() -> new RuntimeException("El doctor no existe"));
    }

    @Override
    public List<DoctorRequest> filterDoctors(FilterDoctorRequest filterDoctorRequest) {
        List<Company> companies = filterDoctorRequest.getCompanies();
        List<Doctor> doc;
        if (companies.isEmpty()){
            doc = doctorRepository.findBySpecialties(filterDoctorRequest.getSpecialty());
        }else{
            doc = insuranceDoctorService.filterDoctors(filterDoctorRequest.getCompanies(), filterDoctorRequest.getSpecialty());
        }
        return mapToRequest(doc);
    }
    
    private List<DoctorRequest> mapToRequest(List<Doctor> doctors){
    return doctors.stream().map(doctor ->
        DoctorRequest.builder()
            .id(doctor.getId())
            .name(doctor.getName())
            .specialties(doctor.getSpecialties().name()) // ajusta según tu modelo
            .build()
    ).toList();
}

    @Override
    public void deleteDoctors() {
        doctorRepository.deleteAll();
        insuranceDoctorService.deleteAllInsuranceDoctor();
    }

    public List<DoctorRequest> getAllDoctors(){
        return mapToRequest(doctorRepository.findAll());
    }
}
