package com.fluentia.pulseapi.domain.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "monitors")
public class Monitor {
  @Id
  @Column(nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id", nullable = false)
  private User owner;

  @Column(nullable = false, length = 120)
  private String name;

  @Column(nullable = false, length = 2048)
  private String url;

  @Column(name = "interval_sec", nullable = false)
  private Integer intervalSec;

  @Column(name = "timeout_ms", nullable = false)
  private Integer timeoutMs;

  @Column(nullable = false)
  private Boolean enabled;

  @Column(name = "last_status", length = 10)
  private String lastStatus;

  @Column(name = "last_latency_ms")
  private Integer lastLatencyMs;

  @Column(name = "last_checked_at")
  private OffsetDateTime lastCheckedAt;

  @Column(name = "next_check_at")
  private OffsetDateTime nextCheckAt;

  @Column(name = "consecutive_failures", nullable = false)
  private Integer consecutiveFailures = 0;

  @Column(name = "consecutive_successes", nullable = false)
  private Integer consecutiveSuccesses = 0;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  public Monitor() {}

  public Monitor(UUID id, User owner, String name, String url, Integer intervalSec, Integer timeoutMs, Boolean enabled) {
    this.id = id;
    this.owner = owner;
    this.name = name;
    this.url = url;
    this.intervalSec = intervalSec;
    this.timeoutMs = timeoutMs;
    this.enabled = enabled;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public User getOwner() {
    return owner;
  }

  public void setOwner(User owner) {
    this.owner = owner;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Integer getIntervalSec() {
    return intervalSec;
  }

  public void setIntervalSec(Integer intervalSec) {
    this.intervalSec = intervalSec;
  }

  public Integer getTimeoutMs() {
    return timeoutMs;
  }

  public void setTimeoutMs(Integer timeoutMs) {
    this.timeoutMs = timeoutMs;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public String getLastStatus() {
    return lastStatus;
  }

  public void setLastStatus(String lastStatus) {
    this.lastStatus = lastStatus;
  }

  public Integer getLastLatencyMs() {
    return lastLatencyMs;
  }

  public void setLastLatencyMs(Integer lastLatencyMs) {
    this.lastLatencyMs = lastLatencyMs;
  }

  public OffsetDateTime getLastCheckedAt() {
    return lastCheckedAt;
  }

  public void setLastCheckedAt(OffsetDateTime lastCheckedAt) {
    this.lastCheckedAt = lastCheckedAt;
  }

  public OffsetDateTime getNextCheckAt() {
    return nextCheckAt;
  }

  public void setNextCheckAt(OffsetDateTime nextCheckAt) {
    this.nextCheckAt = nextCheckAt;
  }

  public Integer getConsecutiveFailures() {
    return consecutiveFailures;
  }

  public void setConsecutiveFailures(Integer consecutiveFailures) {
    this.consecutiveFailures = consecutiveFailures;
  }

  public Integer getConsecutiveSuccesses() {
    return consecutiveSuccesses;
  }

  public void setConsecutiveSuccesses(Integer consecutiveSuccesses) {
    this.consecutiveSuccesses = consecutiveSuccesses;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }
}
