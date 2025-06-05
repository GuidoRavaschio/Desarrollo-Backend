package com.uade.tpo.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uade.tpo.entity.TemporaryData;
import com.uade.tpo.entity.User;

public interface TemporaryDataRepository extends JpaRepository<TemporaryData, Long>{
    void deleteByUser(User user);
    void deleteByExpiresAtBefore(LocalDateTime now);

    @Query("SELECT td FROM TemporaryData td WHERE td.user.email = :email")
    Optional<TemporaryData> findByEmail(@Param("email") String email);
}
