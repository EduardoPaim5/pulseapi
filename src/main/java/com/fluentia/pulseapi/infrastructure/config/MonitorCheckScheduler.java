package com.fluentia.pulseapi.infrastructure.config;

import java.time.OffsetDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.fluentia.pulseapi.application.service.MonitorCheckService;
import com.fluentia.pulseapi.domain.entity.Monitor;
import com.fluentia.pulseapi.domain.repository.MonitorRepository;

@Component
@ConditionalOnProperty(name = "app.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class MonitorCheckScheduler {
  private static final Logger logger = LoggerFactory.getLogger(MonitorCheckScheduler.class);

  private final MonitorRepository monitorRepository;
  private final MonitorCheckService monitorCheckService;

  public MonitorCheckScheduler(MonitorRepository monitorRepository, MonitorCheckService monitorCheckService) {
    this.monitorRepository = monitorRepository;
    this.monitorCheckService = monitorCheckService;
  }

  @Scheduled(fixedDelay = 30_000)
  public void runScheduler() {
    try {
      List<Monitor> eligible = monitorRepository.findEligible(OffsetDateTime.now());
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
