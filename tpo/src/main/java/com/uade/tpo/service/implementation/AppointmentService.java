package com.uade.tpo.service.implementation;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.sql.rowset.serial.SerialBlob;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.uade.tpo.entity.Appointment;
import com.uade.tpo.entity.Availability;
import com.uade.tpo.entity.Doctor;
import com.uade.tpo.entity.User;
import com.uade.tpo.entity.dto.AppointmentRequest;
import com.uade.tpo.entity.enumerations.Specialties;
import com.uade.tpo.entity.enumerations.Weekdays;
import com.uade.tpo.repository.AppointmentRepository;
import com.uade.tpo.repository.DoctorRepository;
import com.uade.tpo.service.interfaces.AppointmentServiceInterface;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentService implements AppointmentServiceInterface{

    private final AvailabilityService availabilityService;

    private final AppointmentRepository appointmentRepository;

    private final DoctorRepository doctorRepository;

    @Override
    public void createAppointment(AppointmentRequest appointmentRequest, User user, Doctor doctor) {
        LocalDate date = appointmentRequest.getDate();
        LocalTime time = appointmentRequest.getTime();
        Long doctor_id = doctor.getId();
        validateAppointment(date, time, doctor_id, user.getId());
        Appointment a = new Appointment();
        a.setUser(user);
        a.setDate(date);
        a.setTime(time);
        a.setDoctor(doctor);
        appointmentRepository.save(a);
    }

    @Override
    public List<AppointmentRequest> getAppointments(User user) {
        return appointmentRepository
            .findByUserId(user.getId())
            .stream()
            .map(this::mapToRequest)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteAppointment(AppointmentRequest appointmentRequest, User user) {
        Appointment a = appointmentRepository.findById(appointmentRequest.getId()).orElseThrow(() -> new RuntimeException("No existe ese turno"));
        if (Objects.equals(a.getUser().getId(), user.getId())){
            appointmentRepository.delete(a);
        }else{
            throw new RuntimeException("No tienes acceso a este turno ya que el turno no te pertenece");
        }
    }

    @Override
    public void editAppointment(AppointmentRequest appointmentRequest, User user, Doctor doctor) {
        LocalDate date = appointmentRequest.getDate();
        LocalTime time = appointmentRequest.getTime();
        Long doctor_id = doctor.getId();
        validateAppointment(date, time, doctor_id, user.getId());
        Appointment a = appointmentRepository.findById(appointmentRequest.getId()).orElseThrow(() -> new RuntimeException("No existe ese turno"));
        if (Objects.equals(a.getUser().getId(), user.getId())){
            a.setUser(user);
            a.setDate(date);
            a.setTime(time);
            a.setDoctor(doctor);
            appointmentRepository.save(a);
        }else{
            throw new RuntimeException("No tienes acceso a este turno ya que el turno no te pertenece");
        }
    }

    private void validateAppointment(LocalDate date, LocalTime time, Long doctorId, Long userId) {
        if (!date.isAfter(LocalDate.now())) {
            throw new RuntimeException("La fecha debe ser futura");
        }
        if (appointmentRepository.existsByUserIdAndDoctorIdAndDateAndTime(userId, doctorId, date, time)) {
            throw new RuntimeException("El usuario ya registró este turno");
        }
        if (appointmentRepository.existsByDoctorIdAndDateAndTime(doctorId, date, time)) {
            throw new RuntimeException("El turno no está disponible");
        }
        Weekdays wd = getWeekdayFromLocalDate(date);
        if (wd == null){throw new RuntimeException("Dia no valido");}
        boolean available = availabilityService
            .getAvailability(List.of(doctorId), List.of(wd), time)
            .size() == 1;
        if (!available) {
            throw new RuntimeException("El doctor no está disponible ese día/hora");
        }
    }


    private Weekdays getWeekdayFromLocalDate(LocalDate date) {
    DayOfWeek day = date.getDayOfWeek(); // Ej: MONDAY, TUESDAY, etc.
    
    return switch (day) {
        case MONDAY -> Weekdays.Lunes;
        case TUESDAY -> Weekdays.Martes;
        case WEDNESDAY -> Weekdays.Miercoles;
        case THURSDAY -> Weekdays.Jueves;
        case FRIDAY -> Weekdays.Viernes;
        default -> null;
    };
    }
    private AppointmentRequest mapToRequest(Appointment appointment){
        return AppointmentRequest.builder()
                                .date(appointment.getDate())
                                .doctorId(appointment.getDoctor().getId())
                                .time(appointment.getTime())
                                .userId(appointment.getUser().getId())
                                .id(appointment.getId())
                                .build();
    }

    @Override
    public byte[] getImage(Long appointment_id, User user) throws SQLException {
        Appointment a = fetchOwnedAppointment(appointment_id, user);
        Blob blob = a.getImage();
        if (blob == null) {
            throw new RuntimeException("No hay imagen para este turno");
        }
        return blob.getBytes(1, (int) blob.length());
    }

    private Appointment fetchOwnedAppointment(Long id, User user) {
        Appointment a = appointmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Turno no encontrado"));
        if (!a.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("No autorizado");
        }
        return a;
    }

    @Override
    public void setImage(Long appointment_id, MultipartFile image) throws SQLException, IOException {
        if (image != null && !image.isEmpty()) {
            byte[] imageBytes = image.getBytes();
            Blob blob =  new SerialBlob(imageBytes);
            Appointment a = appointmentRepository.findById(appointment_id)
                            .orElseThrow(() -> new RuntimeException("El turno no existe"));
            a.setImage(blob);
            appointmentRepository.save(a);
        }else{
            throw new RuntimeException("No hay imagen adjunta");
        }
    }

    @Override
    public List<LocalDate> datesAvailableForSpecialities(Specialties specialties) {
    List<Doctor> doctors = doctorRepository.filterBySpecialties(specialties);
    List<LocalDate> availableDates = new ArrayList<>();
    if (doctors.isEmpty()) return availableDates;
    LocalDate date = LocalDate.now();
    final int MAX_DATES = 6;
    int foundDates = 0;
    while (foundDates < MAX_DATES) {
        boolean dateHasAvailableSlot = false;
        for (Doctor doctor : doctors) {
            Weekdays weekday = getWeekdayFromLocalDate(date);
            Availability availability = availabilityService.getAvailabilityByDoctorAndWeekday(doctor.getId(), weekday);
            if (availability != null && weekday != null) {
                int startHour = availability.getStartTime().getHour();
                int endHour = availability.getEndTime().getHour();
                for (int hour = startHour; hour <= endHour; hour++) {
                    if (!appointmentRepository.checkAppointment(LocalTime.of(hour, 0), doctor.getId(), date)) {
                        dateHasAvailableSlot = true;
                        break;
                    }
                }
            }
            if (dateHasAvailableSlot) break;
        }
        if (dateHasAvailableSlot) {
            availableDates.add(date);
            foundDates++;
        }
        date = date.plusDays(1);
    }
    return availableDates;
}


public List<LocalTime> timesAvailableByDateAndSpecialties(LocalDate date, Specialties specialties){
    List<Doctor> doctors = doctorRepository.filterBySpecialties(specialties);
    List<LocalTime> availableTimes = new ArrayList<>();
        for (Doctor doctor:doctors) {
            Weekdays weekday = getWeekdayFromLocalDate(date);
            Availability availability = availabilityService.getAvailabilityByDoctorAndWeekday(doctor.getId(), weekday);
            if (availability != null) {
                int startHour = availability.getStartTime().getHour();
                int endHour = availability.getEndTime().getHour();
                for(int i = startHour; i <= endHour; i++) {
                    LocalTime currentTime = LocalTime.of(i, 0);
                    boolean isOccupied = appointmentRepository.checkAppointment(currentTime, doctor.getId(), date);
                    if (!isOccupied && !availableTimes.contains(currentTime)) {
                        availableTimes.add(currentTime);
                    }
                }
            }
        }
    return availableTimes;
    }

    public List<LocalDate> datesAvailableByDoctor(Long doctor_id){
    List<LocalDate> availableDates = new ArrayList<>();
    LocalDate date = LocalDate.now();
    int foundDates = 0;
    while (foundDates < 6) {
        boolean dateHasAvailableSlot = false;
        Weekdays weekday = getWeekdayFromLocalDate(date);
        Availability availability = availabilityService.getAvailabilityByDoctorAndWeekday(doctor_id, weekday);
        if (availability != null) {
            int startHour = availability.getStartTime().getHour();
            int endHour = availability.getEndTime().getHour();
            int currentHour = startHour;
            while (!dateHasAvailableSlot && currentHour <= endHour) {
                boolean isOccupied = appointmentRepository.checkAppointment(LocalTime.of(currentHour, 0), doctor_id, date);
                if (!isOccupied) {
                    dateHasAvailableSlot = true;
                } else {
                    currentHour++;
                }
            }
        }
        if (dateHasAvailableSlot) {
            availableDates.add(date);
            foundDates++;
        }
        date = date.plusDays(1);
    }
    return availableDates;
    }

    public List<LocalTime> timesAvailableByDoctorAndDate(Long doctor_id, LocalDate date){
        List<LocalTime> availableTimes = new ArrayList<>();
        Weekdays weekday = getWeekdayFromLocalDate(date);
        Availability availability = availabilityService.getAvailabilityByDoctorAndWeekday(doctor_id, weekday);
            if (availability != null) {
                int startHour = availability.getStartTime().getHour();
                int endHour = availability.getEndTime().getHour();
                for(int i = startHour; i <= endHour; i++) {
                    LocalTime currentTime = LocalTime.of(i, 0);
                    boolean isOccupied = appointmentRepository.checkAppointment(currentTime, doctor_id, date);
                    if (!isOccupied && !availableTimes.contains(currentTime)) {
                        availableTimes.add(currentTime);
                    }
                }
            }
        return availableTimes;
    }

    public void createAppointmentBySpecialties(Specialties specialties, LocalDate date, LocalTime time, User user){
        List<Doctor> doctors = doctorRepository.filterBySpecialties(specialties);
        int i = 0;
        while (i < doctors.size()){
            if (!appointmentRepository.checkAppointment(time, doctors.get(i).getId(), date)){
                Appointment a = new Appointment();
                a.setUser(user);
                a.setDate(date);
                a.setTime(time);
                a.setDoctor(doctors.get(i));
                appointmentRepository.save(a);
                i = doctors.size();
            }
        }
    }
}
