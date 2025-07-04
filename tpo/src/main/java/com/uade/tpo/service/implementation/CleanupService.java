package com.uade.tpo.service.implementation;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.uade.tpo.repository.TemporaryDataRepository;

@Service
public class CleanupService {

    @Autowired
    private TemporaryDataRepository repository;

    private static final Logger logger = LoggerFactory.getLogger(CleanupService.class);

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void removeExpiredData() {
        logger.info("[SCHEDULED] Ejecutando cleanup a las {}", LocalDateTime.now());
        repository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}

