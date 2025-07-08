package com.uade.tpo.service.implementation;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.sql.rowset.serial.SerialBlob;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.uade.tpo.entity.Appointment;
import com.uade.tpo.entity.Doctor;
import com.uade.tpo.entity.User;
import com.uade.tpo.entity.dto.AppointmentData;
import com.uade.tpo.entity.dto.AppointmentRequest;
import com.uade.tpo.entity.enumerations.Specialties;
import com.uade.tpo.repository.AppointmentRepository;
import com.uade.tpo.repository.DoctorRepository;
import com.uade.tpo.service.interfaces.AppointmentServiceInterface;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AppointmentService implements AppointmentServiceInterface{

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
    public List<AppointmentData> getAppointments(User user) {
        return appointmentRepository
            .findByUserId(user.getId(), LocalDate.now())
            .stream()
            .map(this::mapToData)
            .collect(Collectors.toList());
    }

    public List<AppointmentData> getAppointmentHistory(User user){
        return appointmentRepository
            .findPreviousAppointmentsByUserId(user.getId(), LocalDate.now())
            .stream()
            .map(this::mapToData)
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
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(20, 0);
        if (time.isBefore(start) && time.isAfter(end) && time.getMinute() != 0){
            throw new RuntimeException("La hora no es valida");
        }
    }

    private String transform(Blob blob) {
    if (blob == null) return null;
    try {
        byte[] bytes = blob.getBytes(1, (int) blob.length());
        return Base64.getEncoder().encodeToString(bytes);
    } catch (SQLException e) {
        return null; // Fallo al procesar imagen, devolvés null
    }
}


    private AppointmentData mapToData(Appointment appointment){
        return AppointmentData.builder()
                                .date(appointment.getDate())
                                .doctor(appointment.getDoctor().getName())
                                .time(appointment.getTime())
                                .specialty(appointment.getDoctor().getSpecialties())
                                .id(appointment.getId())
                                .image(transform(appointment.getDoctor().getImage()))
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

    public void createAppointmentBySpecialties(Specialties specialties, LocalDate date, LocalTime time, User user){
        List<Doctor> doctors = doctorRepository.findBySpecialties(specialties);
        if (appointmentRepository.existsByUserIdAndDateAndTime(user.getId(), date, time)){
            throw new RuntimeException("El usuario no esta disponible para este turno");
        }
        int i = 0;
        boolean notCreated = true;
        while (i < doctors.size()){
            if (!appointmentRepository.checkAppointment(time, doctors.get(i).getId(), date)){
                Appointment a = new Appointment();
                a.setUser(user);
                a.setDate(date);
                a.setTime(time);
                a.setDoctor(doctors.get(i));
                appointmentRepository.save(a);
                notCreated = false;
                i = doctors.size();
            }
            i++;
        }
        if (notCreated){
            throw new RuntimeException("No hay doctores disponibles para este turno");
        }
    }
}
