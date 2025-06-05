package com.uade.tpo.service.interfaces;

import java.time.LocalTime;
import java.util.List;

import com.uade.tpo.entity.Doctor;
import com.uade.tpo.entity.dto.AvailabilityRequest;
import com.uade.tpo.entity.enumerations.Weekdays;

public interface AvailabilityServiceInterface {
    public void createAvailability(Doctor doctor, Weekdays weekday, int shift);
    public List<AvailabilityRequest> getAvailabilityByDoctor(Long doctor_id);
    public List<AvailabilityRequest> getAvailability(List<Long> doctor_id, List<Weekdays> weekday, LocalTime time);
    public void deleteAvailability(AvailabilityRequest availabilityRequest);
    public void editAvailability(AvailabilityRequest availabilityRequest);
    public void deleteAllAvailability();
}
