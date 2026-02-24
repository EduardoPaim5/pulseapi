package com.fluentia.pulseapi.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.fluentia.pulseapi.domain.entity.Incident;

public interface IncidentRepository extends JpaRepository<Incident, UUID> {
  Optional<Incident> findFirstByMonitorIdAndStatus(UUID monitorId, String status);
  List<Incident> findByMonitorId(UUID monitorId);
  List<Incident> findByMonitorIdAndStatus(UUID monitorId, String status);

  @Query("SELECT COUNT(i) FROM Incident i JOIN i.monitor m WHERE m.owner.id = :ownerId AND i.status = :status")
  long countByOwnerIdAndStatus(@Param("ownerId") UUID ownerId, @Param("status") String status);
}
