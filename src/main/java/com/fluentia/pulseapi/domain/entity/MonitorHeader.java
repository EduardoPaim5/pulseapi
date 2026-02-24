package com.fluentia.pulseapi.domain.entity;

import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "monitor_headers")
public class MonitorHeader {
  @Id
  @Column(nullable = false)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "monitor_id", nullable = false)
  private Monitor monitor;

  @Column(nullable = false, length = 120)
  private String name;

  @Column(nullable = false, length = 2048)
  private String value;

  public MonitorHeader() {}

  public MonitorHeader(UUID id, Monitor monitor, String name, String value) {
    this.id = id;
    this.monitor = monitor;
    this.name = name;
    this.value = value;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Monitor getMonitor() {
    return monitor;
  }

  public void setMonitor(Monitor monitor) {
    this.monitor = monitor;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
