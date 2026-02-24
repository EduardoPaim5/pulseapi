package com.fluentia.pulseapi.domain.repository;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.fluentia.pulseapi.domain.entity.CheckRun;

public interface CheckRunRepository extends JpaRepository<CheckRun, UUID> {
  long countByMonitorIdAndStartedAtGreaterThanEqual(UUID monitorId, OffsetDateTime since);

  long countByMonitorIdAndStartedAtGreaterThanEqualAndSuccessTrue(UUID monitorId, OffsetDateTime since);

  long countByMonitorIdAndStartedAtGreaterThanEqualAndSuccessFalse(UUID monitorId, OffsetDateTime since);

  @Query("SELECT AVG(cr.latencyMs) FROM CheckRun cr WHERE cr.monitor.id = :monitorId AND cr.startedAt >= :since AND cr.latencyMs IS NOT NULL")
  Double avgLatency(@Param("monitorId") UUID monitorId, @Param("since") OffsetDateTime since);

  @Query("SELECT COUNT(cr.id) FROM CheckRun cr WHERE cr.monitor.id = :monitorId AND cr.startedAt >= :since AND cr.latencyMs IS NOT NULL")
  long countLatencies(@Param("monitorId") UUID monitorId, @Param("since") OffsetDateTime since);

  @Query(value = "SELECT latency_ms FROM check_runs WHERE monitor_id = :monitorId AND started_at >= :since AND latency_ms IS NOT NULL ORDER BY latency_ms OFFSET :offset LIMIT 1", nativeQuery = true)
  Integer findLatencyAtOffset(@Param("monitorId") UUID monitorId, @Param("since") OffsetDateTime since, @Param("offset") int offset);
}
