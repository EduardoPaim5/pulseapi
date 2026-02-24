package com.fluentia.pulseapi.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.validator.constraints.URL;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MonitorDtos {
  public record CreateMonitorRequest(
      @NotBlank @Size(max = 120) String name,
      @NotBlank @URL @Size(max = 2048) String url,
      @NotNull @Min(10) @Max(86400) Integer intervalSec,
      @NotNull @Min(100) @Max(600000) Integer timeoutMs,
      Boolean enabled
  ) {}

  public record UpdateMonitorRequest(
      @Size(max = 120) String name,
      @URL @Size(max = 2048) String url,
      @Min(10) @Max(86400) Integer intervalSec,
      @Min(100) @Max(600000) Integer timeoutMs,
      Boolean enabled
  ) {}

  public record EnableMonitorRequest(
      @NotNull Boolean enabled
  ) {}

  public record MonitorResponse(
      UUID id,
      String name,
      String url,
      Integer intervalSec,
      Integer timeoutMs,
      Boolean enabled,
      OffsetDateTime createdAt,
      OffsetDateTime updatedAt
  ) {}

  public record RecheckResponse(
      boolean success,
      Integer statusCode,
      Integer latencyMs,
      String errorMessage,
      OffsetDateTime checkedAt
  ) {}
}
