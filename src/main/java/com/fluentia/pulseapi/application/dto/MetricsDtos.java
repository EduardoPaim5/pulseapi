package com.fluentia.pulseapi.application.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class MetricsDtos {
  public record MonitorSummaryResponse(
      double uptimePercent,
      long totalChecks,
      long failures,
      Double avgLatencyMs,
      Integer p95LatencyMs
  ) {}

  public record DashboardOverviewResponse(
      Totals totals,
      List<AlertItem> latestAlerts,
      List<WorstMonitorItem> worstMonitors
  ) {}

  public record Totals(
      long monitorsTotal,
      long monitorsUp,
      long monitorsDown,
      long incidentsOpen
  ) {}

  public record AlertItem(
      UUID id,
      UUID incidentId,
      UUID monitorId,
      String event,
      String status,
      String channel,
      OffsetDateTime createdAt,
      OffsetDateTime ackedAt
  ) {}

  public record WorstMonitorItem(
      UUID monitorId,
      String name,
      double uptimePercent,
      long totalChecks
  ) {}
}
