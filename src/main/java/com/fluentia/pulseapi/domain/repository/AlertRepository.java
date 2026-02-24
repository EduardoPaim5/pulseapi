package com.fluentia.pulseapi.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.fluentia.pulseapi.domain.entity.Alert;

public interface AlertRepository extends JpaRepository<Alert, UUID> {
  boolean existsByIncidentIdAndEvent(UUID incidentId, String event);

  @Query("SELECT a FROM Alert a JOIN a.incident i JOIN i.monitor m WHERE m.owner.id = :ownerId")
  List<Alert> findAllByOwner(@Param("ownerId") UUID ownerId);

  @Query("SELECT a FROM Alert a JOIN a.incident i JOIN i.monitor m WHERE m.owner.id = :ownerId AND a.status = :status")
  List<Alert> findAllByOwnerAndStatus(@Param("ownerId") UUID ownerId, @Param("status") String status);

  @Query("SELECT a FROM Alert a JOIN a.incident i JOIN i.monitor m WHERE a.id = :alertId AND m.owner.id = :ownerId")
  Optional<Alert> findByIdAndOwner(@Param("alertId") UUID alertId, @Param("ownerId") UUID ownerId);

  @Query("SELECT a FROM Alert a JOIN a.incident i JOIN i.monitor m WHERE m.owner.id = :ownerId " +
      "ORDER BY CASE WHEN a.status = 'UNACKED' THEN 0 ELSE 1 END, a.createdAt DESC")
  List<Alert> findLatestByOwner(@Param("ownerId") UUID ownerId, Pageable pageable);
}
