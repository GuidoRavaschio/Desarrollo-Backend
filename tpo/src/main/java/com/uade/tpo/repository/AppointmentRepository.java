package com.uade.tpo.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uade.tpo.entity.Appointment;
import com.uade.tpo.entity.enumerations.Specialties;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    @Query("SELECT COUNT(a) >= 1 FROM Appointment a WHERE a.user.id = :user_id AND a.time = :time AND a.doctor.id = :doctor_id AND a.date = :date")
    boolean findAppointment(  @Param("user_id") Long user_id, 
                                            @Param("time") LocalTime time, 
                                            @Param("doctor_id") Long doctor_id, 
                                            @Param("date") LocalDate date);

    @Query("SELECT COUNT(a) >= 1 FROM Appointment a WHERE a.time = :time AND a.doctor.id = :doctor_id AND a.date = :date")
    boolean checkAppointment( @Param("time") LocalTime time, 
                                            @Param("doctor_id") Long doctor_id, 
                                            @Param("date") LocalDate date);

    List<Appointment> findByDate(LocalDate date);

    @Query("SELECT a FROM Appointment a WHERE a.user.id = :user_id AND a.date > :date")
    List<Appointment> findByUserId(@Param("user_id") Long user_id, @Param("date") LocalDate date);

    boolean existsByUserIdAndDoctorIdAndDateAndTime(Long userId, Long doctorId, LocalDate date, LocalTime time);
    boolean existsByDoctorIdAndDateAndTime(Long doctorId, LocalDate date, LocalTime time);
    boolean existsByUserIdAndDateAndTime(Long userId, LocalDate date, LocalTime time);

    @Query("SELECT a.time FROM Appointment a WHERE a.date = :date AND a.doctor.specialties = :specialties")
    List<LocalTime> timesNotAvailable(@Param("date") LocalDate date, @Param("specialties") Specialties specialties);

    @Query("SELECT a.time FROM Appointment a WHERE a.date = :date AND a.doctor.id = :doctor_id")
    List<LocalTime> timesNotAvailableDoctor(@Param("date") LocalDate date, @Param("doctor_id") Long doctor_id);

    @Query("SELECT a FROM Appointment a WHERE a.user.id = :user_id AND a.date < :date")
    List<Appointment> findPreviousAppointmentsByUserId(@Param("user_id") Long user_id, @Param("date") LocalDate date);
}
