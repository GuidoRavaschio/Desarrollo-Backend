package com.uade.tpo.controllers;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.uade.tpo.entity.Doctor;
import com.uade.tpo.entity.User;
import com.uade.tpo.entity.dto.AppointmentRequest;
import com.uade.tpo.entity.enumerations.Role;
import com.uade.tpo.entity.enumerations.Specialties;
import com.uade.tpo.service.implementation.AppointmentService;
import com.uade.tpo.service.implementation.DoctorService;
import com.uade.tpo.service.implementation.UserService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("api/appointment")
@RequiredArgsConstructor
public class AppointmentController {
    
    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserService userService;

    @Autowired
    private DoctorService doctorService;

    @PostMapping("/create")
    public ResponseEntity<?> createAppointment(@RequestBody AppointmentRequest appointmentRequest, 
            @RequestHeader("Authorization") String code) {
        User u = userService.getUser(code);
        userService.userAuthority(u, Role.USER);
        Doctor d = doctorService.getDoctor(appointmentRequest.getDoctorId());
        appointmentService.createAppointment(appointmentRequest, u, d);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/from-user")
    public ResponseEntity<List<AppointmentRequest>> getAppointments(@RequestHeader("Authorization") String code) {
        User u = userService.getUser(code);
        userService.userAuthority(u, Role.USER);
        return ResponseEntity.ok(appointmentService.getAppointments(u));
    }
    
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteAppointment(@RequestBody AppointmentRequest appointmentRequest, 
            @RequestHeader("Authorization") String code){
        User u = userService.getUser(code);
        userService.userAuthority(u, Role.USER);
        appointmentService.deleteAppointment(appointmentRequest, u);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/edit")
    public ResponseEntity<?> editAppointment(@RequestBody AppointmentRequest appointmentRequest, 
            @RequestHeader("Authorization") String code) {
        User u = userService.getUser(code);
        userService.userAuthority(u, Role.USER);
        Doctor d = doctorService.getDoctor(appointmentRequest.getDoctorId());
        appointmentService.editAppointment(appointmentRequest, u, d);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/result/load/{appointmentId}",
                consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> setImage(@PathVariable Long appointmentId,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String code) throws SQLException, IOException {
        User u = userService.getUser(code);
        userService.userAuthority(u, Role.ADMIN);
        appointmentService.setImage(appointmentId, file);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/result/{appointmentId}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long appointmentId, 
            @RequestHeader("Authorization") String code) throws SQLException {
        User u = userService.getUser(code);
        userService.userAuthority(u, Role.USER);
        byte[] image = appointmentService.getImage(appointmentId, u);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image);
    }

    @GetMapping("/date")
    public ResponseEntity<List<LocalDate>> datesAvailable(@RequestHeader("Authorization") String code) {
        User u = userService.getUser(code);
        userService.userAuthority(u, Role.USER);
        return ResponseEntity.ok(appointmentService.datesAvailable());
    }
    
    @GetMapping("/time")
    public ResponseEntity<List<LocalTime>> timesAvailableByDateAndSpecialties(
        @RequestHeader("Authorization") String code) {
    User u = userService.getUser(code);
    userService.userAuthority(u, Role.USER);
    try {
        return ResponseEntity.ok(appointmentService.timesAvailable());
    } catch (IllegalArgumentException | DateTimeParseException e) {
        throw new RuntimeException("Especialidad inválida: ");
    }
}
    @GetMapping("/specialties")
    public ResponseEntity<List<Specialties>> getSpecialties() {
        return ResponseEntity.ok(List.of(Specialties.values()));
    }
    
    @GetMapping("/{doctorId}/available-dates")
    public ResponseEntity<List<LocalDate>> getAvailableDatesByDoctor(
            @RequestHeader("Authorization") String token,
            @PathVariable Long doctorId) {
        try {
            User user = userService.getUser(token);
            userService.userAuthority(user, Role.USER);
            List<LocalDate> dates = appointmentService.datesAvailableByDoctor(doctorId);
            return ResponseEntity.ok(dates);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{doctorId}/available-times")
    public ResponseEntity<List<LocalTime>> getAvailableTimesByDoctorAndDate(
            @RequestHeader("Authorization") String token,
            @PathVariable Long doctorId,
            @RequestParam String date) {
        try {
            User user = userService.getUser(token);
            userService.userAuthority(user, Role.USER);
            LocalDate parsedDate = LocalDate.parse(date); // formato ISO: yyyy-MM-dd
            List<LocalTime> times = appointmentService.timesAvailableByDoctorAndDate(doctorId, parsedDate);
            return ResponseEntity.ok(times);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
}
