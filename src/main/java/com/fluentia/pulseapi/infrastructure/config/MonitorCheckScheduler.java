package com.fluentia.pulseapi.infrastructure.config;

import java.time.OffsetDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.fluentia.pulseapi.application.service.MonitorCheckService;
import com.fluentia.pulseapi.domain.entity.Monitor;
import com.fluentia.pulseapi.domain.repository.MonitorRepository;

@Component
@ConditionalOnProperty(name = "app.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class MonitorCheckScheduler {
  private static final Logger logger = LoggerFactory.getLogger(MonitorCheckScheduler.class);

  private final MonitorRepository monitorRepository;
  private final MonitorCheckService monitorCheckService;
  private final int claimBatchSize;
  private final int claimLeaseSeconds;

  public MonitorCheckScheduler(MonitorRepository monitorRepository,
      MonitorCheckService monitorCheckService,
      @Value("${app.scheduler.claim-batch-size:50}") int claimBatchSize,
      @Value("${app.scheduler.claim-lease-seconds:120}") int claimLeaseSeconds) {
    this.monitorRepository = monitorRepository;
    this.monitorCheckService = monitorCheckService;
    this.claimBatchSize = claimBatchSize;
    this.claimLeaseSeconds = claimLeaseSeconds;
  }

  @Transactional
  @Scheduled(fixedDelay = 30_000)
  public void runScheduler() {
    try {
      OffsetDateTime now = OffsetDateTime.now();
      OffsetDateTime leaseUntil = now.plusSeconds(claimLeaseSeconds);
      List<Monitor> eligible = monitorRepository.claimEligibleForCheck(now, leaseUntil, claimBatchSize);
      if (!eligible.isEmpty()) {
        logger.info("Scheduler claim {} monitores elegiveis para check", eligible.size());
      }
      for (Monitor monitor : eligible) {
        try {
          monitorCheckService.runCheck(monitor);
        } catch (Exception ex) {
          logger.error("Erro ao executar check do monitor {}", monitor.getId(), ex);
        }
      }
    } catch (Exception ex) {
      logger.error("Erro no scheduler de checks", ex);
    }
  }
}
