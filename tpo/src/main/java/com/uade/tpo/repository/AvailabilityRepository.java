package com.uade.tpo.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.uade.tpo.entity.Availability;
import com.uade.tpo.entity.enumerations.Weekdays;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long>{
    @Query("SELECT a FROM Availability a WHERE a.doctor.id = :doctor_id AND a.weekday = :weekday")
    Optional<Availability> findByDoctorAndWeekday(@Param("doctor_id") Long doctor_id, @Param("weekday") Weekdays weekday);

    @Query("SELECT a FROM Availability a WHERE a.doctor.id = :doctor_id")
    List<Availability> findAvailabilityByDoctor(@Param("doctor_id") Long doctor_id);

    @Query("""
    SELECT a FROM Availability a 
    WHERE (:doctorIds IS NULL OR a.doctor.id IN :doctorIds)
    AND (:weekdays IS NULL OR a.weekday IN :weekdays)
    AND (:time IS NULL OR :time BETWEEN a.startTime AND a.endTime)
""")
List<Availability> findByOptionalWeekdaysAndTime(
    @Param("doctorIds") List<Long> doctor_id,
    @Param("weekdays") List<Weekdays> weekdays,
    @Param("time") LocalTime time
);


}
