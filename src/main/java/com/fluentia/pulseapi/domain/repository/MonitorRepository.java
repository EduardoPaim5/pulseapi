package com.fluentia.pulseapi.domain.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.fluentia.pulseapi.domain.entity.Monitor;

public interface MonitorRepository extends JpaRepository<Monitor, UUID> {
  List<Monitor> findAllByOwnerId(UUID ownerId);
  Optional<Monitor> findByIdAndOwnerId(UUID id, UUID ownerId);

  @Query("SELECT m FROM Monitor m WHERE m.enabled = true AND (m.nextCheckAt IS NULL OR m.nextCheckAt <= :now)")
  List<Monitor> findEligible(@Param("now") OffsetDateTime now);

  @Query(value = """
      WITH picked AS (
        SELECT id
        FROM monitors
        WHERE enabled = true
          AND (next_check_at IS NULL OR next_check_at <= :now)
        ORDER BY next_check_at NULLS FIRST
        FOR UPDATE SKIP LOCKED
        LIMIT :batchSize
      )
      UPDATE monitors m
      SET next_check_at = :leaseUntil
      FROM picked
      WHERE m.id = picked.id
      RETURNING m.*
      """, nativeQuery = true)
  List<Monitor> claimEligibleForCheck(@Param("now") OffsetDateTime now,
      @Param("leaseUntil") OffsetDateTime leaseUntil,
      @Param("batchSize") int batchSize);

  long countByOwnerId(UUID ownerId);

  long countByOwnerIdAndLastStatus(UUID ownerId, String lastStatus);

  @Query(value = "SELECT m.id AS monitorId, m.name AS name, COUNT(cr.id) AS totalChecks, " +
      "COALESCE(SUM(CASE WHEN cr.success THEN 1 ELSE 0 END), 0) AS successChecks " +
      "FROM monitors m LEFT JOIN check_runs cr ON cr.monitor_id = m.id AND cr.started_at >= :since " +
      "WHERE m.owner_id = :ownerId GROUP BY m.id, m.name HAVING COUNT(cr.id) > 0 " +
      "ORDER BY (COALESCE(SUM(CASE WHEN cr.success THEN 1 ELSE 0 END), 0) * 1.0 / COUNT(cr.id)) ASC LIMIT 5", nativeQuery = true)
  List<MonitorUptimeProjection> findWorstMonitors(@Param("ownerId") UUID ownerId, @Param("since") OffsetDateTime since);

  interface MonitorUptimeProjection {
    UUID getMonitorId();
    String getName();
    long getTotalChecks();
    long getSuccessChecks();
  }
}
