package com.uade.tpo.service.implementation;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.entity.Doctor;
import com.uade.tpo.entity.dto.AvailabilityRequest;
import com.uade.tpo.entity.dto.DoctorRequest;
import com.uade.tpo.entity.dto.FilterDoctorRequest;
import com.uade.tpo.entity.enumerations.Company;
import com.uade.tpo.entity.enumerations.Specialties;
import com.uade.tpo.entity.enumerations.Weekdays;
import com.uade.tpo.repository.DoctorRepository;
import com.uade.tpo.service.interfaces.DoctorServiceInterface;

@Service
public class DoctorService implements DoctorServiceInterface{

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private InsuranceDoctorService insuranceDoctorService;

    @Autowired
    private AvailabilityService availabilityService;

    @Override
    public void createDoctors() {
        SecureRandom random = new SecureRandom();
        List<String> names = List.of("Dr. Emilio", "Dr. Carlos", "Dra. Sofía", "Dra. Lucía", "Dr. Pedro");
        List<String> surnames = List.of("Pérez", "García", "López", "Martínez", "Fernández");
        List<Specialties> specialties = List.of(Specialties.values());
        List<Company> companies = List.of(Company.values());
        List<Weekdays> weekdays = List.of(Weekdays.values());
        for (int i = 0; i < 10; i++) {
            String name = randomChoice(names, random) + " " + randomChoice(surnames, random);
            Specialties specialty = randomChoice(specialties, random);
            Doctor doctor = Doctor.builder().name(name).specialties(specialty).build();
            doctorRepository.save(doctor);
            int iterations = random.nextInt(companies.size());
            for (int j = 0; j < iterations; j++){
                insuranceDoctorService.setInsuranceDoctor(doctor.getId(), randomChoice(companies, random));
            }
            iterations = random.nextInt(weekdays.size());
            for (int j = 0; j < iterations; j++){
                int shift = random.nextInt(2); // 1:Morning, 0:Afternoon
                availabilityService.createAvailability(doctor, randomChoice(weekdays, random), shift);
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
        List<Doctor> doctors = doctorRepository.filterBySpecialties(filterDoctorRequest.getSpecialty());
        List<Long> doctor_ids = new ArrayList<>();
        for (Doctor doctor : doctors){
            doctor_ids.add(doctor.getId());
        }
        List<AvailabilityRequest> ars = availabilityService.getAvailability(doctor_ids, filterDoctorRequest.getWeekdays(), filterDoctorRequest.getTime());
        List<Long> ids_doctors = new ArrayList<>();
        for (AvailabilityRequest ar : ars){
            ids_doctors.add(ar.getDoctorId());
        }
        List<Doctor> doc = doctorRepository.findDoctors(ids_doctors);
        return mapToRequest(doc);
    }
    
    private List<DoctorRequest> mapToRequest(List<Doctor> doctors){
        List<DoctorRequest> results = new ArrayList<>();
        for (Doctor doctor : doctors) {
                DoctorRequest d = DoctorRequest.builder().id(doctor.getId()).name(doctor.getName()).specialties(doctor.getSpecialties().toString()).build();
                results.add(d);
            }
        return results;
    }

    @Override
    public void deleteDoctors() {
        doctorRepository.deleteAll();
        availabilityService.deleteAllAvailability();
        insuranceDoctorService.deleteAllInsuranceDoctor();
    }

    public List<DoctorRequest> getAllDoctors(){
        return mapToRequest(doctorRepository.findAll());
    }
}
