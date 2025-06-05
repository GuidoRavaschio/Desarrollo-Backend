package com.uade.tpo.service.implementation;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uade.tpo.entity.Availability;
import com.uade.tpo.entity.Doctor;
import com.uade.tpo.entity.dto.AvailabilityRequest;
import com.uade.tpo.entity.enumerations.Weekdays;
import com.uade.tpo.repository.AvailabilityRepository;
import com.uade.tpo.service.interfaces.AvailabilityServiceInterface;

@Service
public class AvailabilityService implements AvailabilityServiceInterface{

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Override
    public void createAvailability(Doctor doctor, Weekdays weekday, int shift) {
        availabilityRepository.findByDoctorAndWeekday(doctor.getId(), weekday)
        .orElse(create(doctor,weekday,shift));
    }

    @Override
    public List<AvailabilityRequest> getAvailabilityByDoctor(Long doctor_id) {
        List<Availability> a = availabilityRepository.findAvailabilityByDoctor(doctor_id);
        List<AvailabilityRequest> ars = new ArrayList<>();
        for (Availability availability : a){
            ars.add(mapToRequest(availability));
        }
        return ars;
    }

    @Override
public List<AvailabilityRequest> getAvailability(List<Long> doctor_id, List<Weekdays> weekdays, LocalTime time) {
    return availabilityRepository
        .findByOptionalWeekdaysAndTime(doctor_id, weekdays, time)
        .stream()
        .map(this::mapToRequest)
        .collect(Collectors.toList());
}


    @Override
public void deleteAvailability(AvailabilityRequest availabilityRequest) {
    availabilityRepository.deleteById(availabilityRequest.getId());
}

@Override
public void editAvailability(AvailabilityRequest availabilityRequest) {
    Availability a = availabilityRepository.findById(availabilityRequest.getId())
        .orElseThrow(() -> new RuntimeException("No existe la disponibilidad"));
    a.setWeekday(availabilityRequest.getWeekday());
    a.setStartTime(availabilityRequest.getStartTime());
    a.setEndTime(availabilityRequest.getEndTime());
    availabilityRepository.save(a);
}

    private Availability create(Doctor doctor, Weekdays weekday, int shift){
        LocalTime startTime;
        LocalTime endTime;
        if (shift == 1){
            startTime = LocalTime.of(8, 0);
            endTime = LocalTime.of(12, 0);
        }else{
            startTime = LocalTime.of(14, 0);
            endTime = LocalTime.of(20, 0);
        }
        Availability a = Availability.builder().doctor(doctor).weekday(weekday).startTime(startTime).endTime(endTime).build();
        availabilityRepository.save(a);
        return a;
    }

    private AvailabilityRequest mapToRequest(Availability availability) {
    return AvailabilityRequest.builder()
            .id(availability.getId())
            .weekday(availability.getWeekday())
            .startTime(availability.getStartTime())
            .endTime(availability.getEndTime())
            .doctorId(availability.getDoctor().getId())
            .build();
        }

    @Override
    public void deleteAllAvailability() {
        availabilityRepository.deleteAll();
    }
}
