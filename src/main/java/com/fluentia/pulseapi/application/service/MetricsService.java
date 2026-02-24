package com.fluentia.pulseapi.application.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import com.fluentia.pulseapi.application.dto.MetricsDtos.AlertItem;
import com.fluentia.pulseapi.application.dto.MetricsDtos.DashboardOverviewResponse;
import com.fluentia.pulseapi.application.dto.MetricsDtos.MonitorSummaryResponse;
import com.fluentia.pulseapi.application.dto.MetricsDtos.Totals;
import com.fluentia.pulseapi.application.dto.MetricsDtos.WorstMonitorItem;
import com.fluentia.pulseapi.domain.entity.Monitor;
import com.fluentia.pulseapi.domain.entity.User;
import com.fluentia.pulseapi.domain.repository.AlertRepository;
import com.fluentia.pulseapi.domain.repository.CheckRunRepository;
import com.fluentia.pulseapi.domain.repository.IncidentRepository;
import com.fluentia.pulseapi.domain.repository.MonitorRepository;
import com.fluentia.pulseapi.infrastructure.exception.ApiException;

@Service
public class MetricsService {
  private final MonitorRepository monitorRepository;
  private final CheckRunRepository checkRunRepository;
  private final IncidentRepository incidentRepository;
  private final AlertRepository alertRepository;

  public MetricsService(MonitorRepository monitorRepository,
      CheckRunRepository checkRunRepository,
      IncidentRepository incidentRepository,
      AlertRepository alertRepository) {
    this.monitorRepository = monitorRepository;
    this.checkRunRepository = checkRunRepository;
    this.incidentRepository = incidentRepository;
    this.alertRepository = alertRepository;
  }

  public MonitorSummaryResponse getMonitorSummary(User owner, UUID monitorId, String window) {
    Monitor monitor = monitorRepository.findByIdAndOwnerId(monitorId, owner.getId())
        .orElseThrow(() -> ApiException.notFound("Monitor não encontrado"));

    OffsetDateTime since = windowStart(window);

    long total = checkRunRepository.countByMonitorIdAndStartedAtGreaterThanEqual(monitor.getId(), since);
    long failures = checkRunRepository.countByMonitorIdAndStartedAtGreaterThanEqualAndSuccessFalse(monitor.getId(), since);
    long successes = total - failures;

    double uptimePercent = total == 0 ? 0.0 : (successes * 100.0) / total;
    Double avgLatency = checkRunRepository.avgLatency(monitor.getId(), since);

    Integer p95 = null;
    long latencyCount = checkRunRepository.countLatencies(monitor.getId(), since);
    if (latencyCount > 0) {
      int offset = (int) Math.floor(0.95 * (latencyCount - 1));
      p95 = checkRunRepository.findLatencyAtOffset(monitor.getId(), since, offset);
    }

    return new MonitorSummaryResponse(uptimePercent, total, failures, avgLatency, p95);
  }

  public DashboardOverviewResponse getOverview(User owner, String window) {
    OffsetDateTime since = windowStart(window);

    long monitorsTotal = monitorRepository.countByOwnerId(owner.getId());
    long monitorsUp = monitorRepository.countByOwnerIdAndLastStatus(owner.getId(), "UP");
    long monitorsDown = monitorRepository.countByOwnerIdAndLastStatus(owner.getId(), "DOWN");
    long incidentsOpen = incidentRepository.countByOwnerIdAndStatus(owner.getId(), "OPEN");

    Totals totals = new Totals(monitorsTotal, monitorsUp, monitorsDown, incidentsOpen);

    List<AlertItem> latestAlerts = alertRepository.findLatestByOwner(owner.getId(), PageRequest.of(0, 10))
        .stream()
        .map(alert -> new AlertItem(
            alert.getId(),
            alert.getIncident().getId(),
            alert.getIncident().getMonitor().getId(),
            alert.getEvent(),
            alert.getStatus(),
            alert.getChannel(),
            alert.getCreatedAt(),
            alert.getAckedAt()
        ))
        .collect(Collectors.toList());

    List<WorstMonitorItem> worstMonitors = monitorRepository.findWorstMonitors(owner.getId(), since)
        .stream()
        .map(row -> {
          double uptime = row.getTotalChecks() == 0 ? 0.0 : (row.getSuccessChecks() * 100.0) / row.getTotalChecks();
          return new WorstMonitorItem(row.getMonitorId(), row.getName(), uptime, row.getTotalChecks());
        })
        .collect(Collectors.toList());

    return new DashboardOverviewResponse(totals, latestAlerts, worstMonitors);
  }

  private OffsetDateTime windowStart(String window) {
    if (window == null || window.isBlank()) {
      throw ApiException.badRequest("Parâmetro window é obrigatório");
    }
    return switch (window) {
      case "24h" -> OffsetDateTime.now().minusHours(24);
      case "7d" -> OffsetDateTime.now().minusDays(7);
      case "30d" -> OffsetDateTime.now().minusDays(30);
      default -> throw ApiException.badRequest("Window inválido. Use 24h, 7d ou 30d");
    };
  }
}
