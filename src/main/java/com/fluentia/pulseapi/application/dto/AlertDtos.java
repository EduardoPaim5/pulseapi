package com.fluentia.pulseapi.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class AlertDtos {
  public record AlertResponse(
      UUID id,
      UUID incidentId,
      UUID monitorId,
      String event,
      String status,
      String channel,
      OffsetDateTime createdAt,
      OffsetDateTime ackedAt
  ) {}
}
