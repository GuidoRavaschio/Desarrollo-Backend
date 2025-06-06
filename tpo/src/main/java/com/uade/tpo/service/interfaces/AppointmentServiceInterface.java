package com.uade.tpo.service.interfaces;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.uade.tpo.entity.Doctor;
import com.uade.tpo.entity.User;
import com.uade.tpo.entity.dto.AppointmentRequest;

public interface AppointmentServiceInterface {
    public void createAppointment(AppointmentRequest appointmentRequest, User user, Doctor doctor);
    public List<AppointmentRequest> getAppointments(User user);
    public void deleteAppointment(AppointmentRequest appointmentRequest, User user);
    public void editAppointment(AppointmentRequest appointmentRequest, User user, Doctor doctor);
    public byte[] getImage(Long appointment_id, User user) throws SQLException;
    public void setImage(Long appointment_id, MultipartFile image) throws SQLException, IOException;
}
