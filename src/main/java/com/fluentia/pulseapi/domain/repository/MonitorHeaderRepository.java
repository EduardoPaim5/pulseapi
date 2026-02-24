package com.fluentia.pulseapi.domain.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.fluentia.pulseapi.domain.entity.MonitorHeader;

public interface MonitorHeaderRepository extends JpaRepository<MonitorHeader, UUID> {}
