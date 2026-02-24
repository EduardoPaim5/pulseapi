package com.fluentia.pulseapi.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public class IncidentDtos {
  public record IncidentResponse(
      UUID id,
      UUID monitorId,
      String status,
      OffsetDateTime openedAt,
      OffsetDateTime resolvedAt
  ) {}
}
